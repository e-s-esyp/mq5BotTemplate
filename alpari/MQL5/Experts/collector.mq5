#property copyright ""
#property link      ""
#property version   "1.0"

string getDate(long x){
   MqlDateTime date;
   TimeToStruct(x, date);
   return StringFormat("%04d-%02d-%02d %02d:%02d:%02d", 
      date.year, 
      date.mon, 
      date.day,
      date.hour,
      date.min,
      date.sec);
   }

string getDateC(long x){
   MqlDateTime date;
   TimeToStruct(x, date);
   return StringFormat("%04d-%02d-%02d %02d:%02d", 
      date.year, 
      date.mon, 
      date.day,
      date.hour,
      date.min);
   }

string getDate_(long x){
   MqlDateTime date;
   TimeToStruct(x, date);
   return StringFormat(" %04d-%02d-%02d %02d-%02d-%02d", 
      date.year, 
      date.mon, 
      date.day,
      date.hour,
      date.min,
      date.sec);   
   }   
 
int Log;

void toLog(string s){
   FileWrite(Log,TimeCurrent()," ",s);
   FileFlush(Log);
   }

void saveArchive(){
   int ratesLog = FileOpen(
      Symbol()+StringFormat("%d",Period())+getDate_(TimeLocal())+".log",
      FILE_WRITE|FILE_SHARE_READ|FILE_TXT|FILE_ANSI);
   MqlRates rates[];
   ArraySetAsSeries(rates,true);
   int copied=CopyRates(Symbol(),Period(),0,1000000,rates);
   long sum = 0;
   for(int i=copied-1; i>=0; --i){
      FileWrite(ratesLog,(StringFormat("%s,%0.5f,%0.5f,%0.5f,%0.5f,%d",
         getDateC(rates[i].time),
         rates[i].open,
         rates[i].high,
         rates[i].low,
         rates[i].close,
         rates[i].spread
         )));
         sum += rates[i].spread;
   }
   FileClose(ratesLog);
   }

long position = 1;
long value = 0;

//---------------------------------------------------------------------------

int OnInit(){
   printf("OnInit");
   return(INIT_SUCCEEDED);
   }

void OnDeinit(const int reason){
   printf("OnDeinit");
   FileClose(Log);  
   }

void OnTick(){ 
   printf("OnTick");
   
   
   toLog("OnTick()");
   }

void OnTimer(){
   printf("OnTimer");
   toLog("OnTimer()");
   }

