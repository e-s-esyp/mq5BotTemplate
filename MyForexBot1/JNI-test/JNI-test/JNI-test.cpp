// JNI-test.cpp : main project file.

#include "stdafx.h"
#include <conio.h>
#include "jni.h"
#include <windows.h>

using namespace System;


typedef jlong INOUTDATA[200];


int main(array<System::String ^> ^args)
	{
	HINSTANCE hModule=NULL;
	hModule=::LoadLibrary(L"C:/Program Files/Alpari Limited MT5/MQL5/Experts/link64.dll");
	if (hModule!=NULL)
		{
		printf("JNI-test.dll load succeeded\n");
		typedef  void (WINAPI VOID_)(void);
		VOID_* init_P =(VOID_*)::GetProcAddress((HMODULE)hModule,"init_dll");
		if (init_P!=NULL)
			{
			(*init_P)();
			_getch();
			}
		else 
			{
			printf("error load function init_dll() %d\n",GetLastError());
			_getch();
			return 0;
		}
		typedef  void (WINAPI FUNC)(INOUTDATA);
		FUNC* func_P=(FUNC*)::GetProcAddress((HMODULE)hModule,"func_dll");
		if (func_P!=NULL)
			{
			INOUTDATA data;
			data[0]=-5;
			data[1]=1;
			data[2]=3333;
			(*func_P)(data);
			printf("%d\n",data[0]);
			printf("%d\n",data[1]);
			printf("%d\n",data[2]);
			_getch();
			}
		else 
			{
			printf("error load function func_dll(INOUTDATA) %d\n",GetLastError());
			}
		VOID_* close_P =(VOID_*)::GetProcAddress((HMODULE)hModule,"close_dll");
		if (close_P!=NULL)
			{
			(*close_P)();
			_getch();
			}
		else 
			{
			printf("error load function close_dll() %d\n",GetLastError());
			_getch();
			return 0;
			}
		::FreeLibrary(hModule); 
		}
	else 
		{
		printf("error load Dll %d\n",GetLastError());
			_getch();
		}
	return 0;
}

JavaVM *jvm;                      // Pointer to the JVM (Java Virtual Machine)
JNIEnv *env;                      // Pointer to native interface
jclass cls;

extern "C" __declspec( dllexport ) void init_dll()
	{
	Console::WriteLine(L"init_dll()");
	//================== prepare loading of Java VM ============================
	JavaVMInitArgs vm_args;                        // Initialization arguments
	JavaVMOption* options = new JavaVMOption[2];   // JVM invocation options
	options[0].optionString = "-Djava.class.path=Z:/JAVA/out/production/JAVA";   // where to find java .class
	options[1].optionString = "-Xmx5000m";   
	vm_args.version = JNI_VERSION_1_6;             // minimum Java version
	vm_args.nOptions = 2;                          // number of options
	vm_args.options = options;
	vm_args.ignoreUnrecognized = false;     // invalid options make the JVM init fail
	//=============== load and initialize Java VM and JNI interface =============
	HINSTANCE hinstLib;  
	hinstLib = LoadLibrary(TEXT("C:\\Program Files\\Java\\jdk1.7.0_75\\jre\\bin\\server\\jvm.dll")); 
	if(hinstLib==0)
		{
		printf("Error %d\n",GetLastError());
		}
	else
		{
		printf("jvm.dll load succeeded\n");
		typedef jint (JNICALL *PtrCreateJavaVM)(JavaVM **, void **, void *);
		PtrCreateJavaVM ptrCreateJavaVM = (PtrCreateJavaVM)GetProcAddress(hinstLib,"JNI_CreateJavaVM");
		int res = ptrCreateJavaVM(&jvm, (void**)&env, &vm_args);
		delete options;    // we then no longer need the initialisation options. 
		if (res != JNI_OK) {
			// TO DO: error processing... 
			return;
			}
		//=============== Display JVM version =======================================
		Console::WriteLine(L"JVM load succeeded: ");
		jint ver = env->GetVersion();
		Console::WriteLine(L"Version "+((ver>>16)&0x0f)+"."+(ver&0x0f));
		cls = env->FindClass("com/gmware/lib/neuro/mynet/F/NeuroJNI");
		if(cls == nullptr) {
			printf("ERROR: class not found ! Error %d\n",GetLastError());
			}
		else{
			printf("Class NeuroJNI found\n");
			jmethodID mid = env->GetStaticMethodID(cls, "init", "()V");  // find method
			if(mid == nullptr){
				printf("ERROR: method void init() not found !\n");
				}
			else {
				env->CallStaticVoidMethod(cls, mid);                // call method
				}
			}
		}
	}

extern "C" __declspec( dllexport ) void close_dll()
	{
	Console::WriteLine(L"close_dll()");
	if(cls != nullptr) {
		jmethodID mid = env->GetStaticMethodID(cls, "close", "()V");  // find method
		if(mid == nullptr){
			printf("ERROR: method void close() not found !\n");
			}
		else {
			env->CallStaticVoidMethod(cls, mid);                     // call method
			}
		}
	jvm->DestroyJavaVM();
	}

extern "C" __declspec( dllexport ) void func_dll(jlong *data)
	{
//	Console::WriteLine(L"func_dll()");
	if(cls != nullptr) {
		jmethodID mid = env->GetStaticMethodID(cls, "getAction", "([J)I");  // find method
		if(mid == nullptr)
			printf("ERROR: method void getAction() not found !\n");
		else {
			if (data[0]<0) {
				long size = data[1] + 2;
				jlongArray  d = env->NewLongArray(size);
				env->SetLongArrayRegion(d, 0, size, (jlong*)data);    
				long ret = (long) env->CallStaticObjectMethod(cls, mid, d); // call method
//				printf("ret = %d\n",ret);
				if (env->ExceptionCheck()){
					printf("CallStaticObjectMethod failed.\n");
					}
				env->GetLongArrayRegion(d, 0, ret, (jlong*)data);
				}
			}
		}
	}
	