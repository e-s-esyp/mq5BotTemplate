#property copyright ""
#property link      ""
#property version   "1.0"
#import "link64.dll"
   void init_dll();
   void func_dll(long &d[]);
   void close_dll();
#import

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
//+------------------------------------------------------------------+ 
//| Покупка через асинхронную функцию OrderSendAsync()               | 
//+------------------------------------------------------------------+ 
void BuyAsync(double volume) 
  { 
   double addvolume = 0;
   if(PositionSelect(_Symbol)){
      if(PositionGetInteger(POSITION_TYPE)==1){
         //BUY = 0
         //SELL = 1  
         addvolume = PositionGetDouble(POSITION_VOLUME); 
         }
       else {
         return;
         }  
      }
   double d = SymbolInfoDouble(_Symbol,SYMBOL_ASK)-SymbolInfoDouble(_Symbol,SYMBOL_BID);
   Print(__FUNCTION__," at ",SymbolInfoDouble(_Symbol,SYMBOL_ASK)," spread = ",d);
//--- подготовим запрос 
   MqlTradeRequest req={0}; 
   req.action      =TRADE_ACTION_DEAL; 
   req.symbol      =_Symbol; 
   req.magic       =0; 
   req.volume      =volume+addvolume; 
   req.type        =ORDER_TYPE_BUY; 
   req.price       =SymbolInfoDouble(req.symbol,SYMBOL_ASK); 
   req.deviation   =10; 
   req.comment     ="Buy using OrderSendAsync()"; 
//   req.tp          =req.price+d*3;
//   req.sl          =req.price-d*3; 
   MqlTradeResult  res={0}; 
   if(!OrderSendAsync(req,res)) 
     { 
      Print(__FUNCTION__,": ошибка ",GetLastError(),", retcode = ",res.retcode); 
     } 
//--- 
   Print("total = ",PositionGetDouble(POSITION_VOLUME));
  } 
//+------------------------------------------------------------------+ 
//| Продажа через асинхронную функцию OrderSendAsync()               | 
//+------------------------------------------------------------------+ 
void SellAsync(double volume) 
  { 
   double addvolume = 0;
   if(PositionSelect(_Symbol)){
      if(PositionGetInteger(POSITION_TYPE)==0){
         //BUY = 0
         //SELL = 1  
         addvolume = PositionGetDouble(POSITION_VOLUME); 
         }
       else {
         return;
         }  
      }
   double d = SymbolInfoDouble(_Symbol,SYMBOL_ASK)-SymbolInfoDouble(_Symbol,SYMBOL_BID);
   Print(__FUNCTION__," at ",SymbolInfoDouble(_Symbol,SYMBOL_BID)," spread = ",d);
//--- подготовим запрос 
   MqlTradeRequest req={0}; 
   req.action      =TRADE_ACTION_DEAL; 
   req.symbol      =_Symbol; 
   req.magic       =0; 
   req.volume      =volume+addvolume; 
   req.type        =ORDER_TYPE_SELL; 
   req.price       =SymbolInfoDouble(req.symbol,SYMBOL_BID); 
   req.deviation   =10; 
   req.comment     ="Sell using OrderSendAsync()"; 
//   req.tp          =req.price-d*3;
//   req.sl          =req.price+d*3; 
  MqlTradeResult  res={0}; 
   if(!OrderSendAsync(req,res)) 
     { 
      Print(__FUNCTION__,": ошибка ",GetLastError(),", retcode = ",res.retcode); 
     } 
//--- 
    Print("total = ",PositionGetDouble(POSITION_VOLUME));
 } 
//+------------------------------------------------------------------+
 
int Log;

void toLog(string s){
   FileWrite(Log,TimeCurrent()," ",s);
   FileFlush(Log);
   }

void logOnInit(){
   Log = FileOpen("test "+getDate_(TimeLocal())+".log",FILE_WRITE|FILE_SHARE_READ|FILE_TXT|FILE_ANSI);
   printf("LogFile %d",Log);
   FileWrite(Log,TimeCurrent()," ",Symbol()," ",EnumToString(_Period));
   long result;
   bool succ;
   toLog(StringFormat("Symbol() = %s", Symbol()));
   toLog(StringFormat("Period() = %d", Period()));
   toLog(StringFormat("Bars() = %d", Bars(Symbol(),Period())));
   toLog(StringFormat("Point() = %f", Point()));
   succ = SeriesInfoInteger(Symbol(),0,SERIES_BARS_COUNT,result);  
   toLog(StringFormat("SERIES_BARS_COUNT =         %d", result));
   succ = SeriesInfoInteger(Symbol(),0,SERIES_FIRSTDATE,result);  
   toLog(StringFormat("SERIES_FIRSTDATE  =         %s", getDate(result)));
   succ = SeriesInfoInteger(Symbol(),0,SERIES_LASTBAR_DATE,result);  
   toLog(StringFormat("SERIES_LASTBAR_DATE =       %s", getDate(result)));
   succ = SeriesInfoInteger(Symbol(),0,SERIES_SERVER_FIRSTDATE,result);  
   toLog(StringFormat("SERIES_SERVER_FIRSTDATE =   %s", getDate(result)));
   succ = SeriesInfoInteger(Symbol(),0,SERIES_TERMINAL_FIRSTDATE,result);  
   toLog(StringFormat("SERIES_TERMINAL_FIRSTDATE = %s", getDate(result)));
   succ = SeriesInfoInteger(Symbol(),0,SERIES_SYNCHRONIZED,result);  
   toLog(StringFormat("SERIES_SYNCHRONIZED =       %d", result));
   int ratesLog = FileOpen(Symbol()+
                  StringFormat("%d",Period())+
                  getDate_(TimeLocal())+
                  ".log",
                  FILE_WRITE|FILE_SHARE_READ|FILE_TXT|FILE_ANSI);
   MqlRates rates[];
   ArraySetAsSeries(rates,true);
   int copied=CopyRates(Symbol(),Period(),0,1000000,rates);
   toLog(StringFormat("copied = %d",copied));
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
   toLog(StringFormat("average spread = %f",
         (double)sum/copied
         ));
   }   

long data1[600002];
void send_data(){
   MqlRates rates[];
   ArraySetAsSeries(rates,true);
   int copied=CopyRates(Symbol(),Period(),0,100000,rates);
   data1[0] = -2;
   data1[1] = copied * 6; 
   for(int i=0; i<copied; ++i){
      data1[i*6+2] = rates[copied-i-1].time / 60;
      data1[i*6+3] = (long)(1000000*rates[copied-i-1].open);
      data1[i*6+4] = (long)(1000000*rates[copied-i-1].high);
      data1[i*6+5] = (long)(1000000*rates[copied-i-1].low);
      data1[i*6+6] = (long)(1000000*rates[copied-i-1].close);
      data1[i*6+7] = (long)(rates[copied-i-1].spread);
      }
   func_dll(data1);
   }

long position = 1;
long value = 0;
void setPosition(){
//   toLog("setPosition()");
   int total = PositionsTotal();
   position = 1;
   value = 0;
   if(total == 1){
      PositionSelect(_Symbol); 
      long symbol = PositionGetInteger(POSITION_TYPE);
      if(symbol == POSITION_TYPE_BUY){
         position = 0;
         }
      if(symbol == POSITION_TYPE_SELL){
         position = 2;
         }
      double volume = 0;
      while(volume == 0){
         volume = PositionGetDouble(POSITION_VOLUME);
         }
      value = (long) volume * 100;
      }
   toLog(StringFormat("total=%d position=%d value=%d",total,position,value)); 
   }

long  data[22];
long lastTime = 0;
void checkStock(){
   MqlRates rates[];
   ArraySetAsSeries(rates,true);
   int copied=CopyRates(Symbol(),Period(),0,3,rates);
   if(lastTime==0){
      lastTime = rates[0].time;
      return;   
      }
   if(lastTime==rates[0].time){
      return;   
      }
//   toLog("checkStock()");
   setPosition();
   lastTime = rates[0].time;
   data[0] = -1;
   data[1] = 3*6+2;
   data[2] = position;
   data[3] = value;
   for(int i = 0; i < 3; ++i){
      data[4 + i*6] = rates[i].time / 60;
      data[5 + i*6] = (long)(1000000*rates[i].open);
      data[6 + i*6] = (long)(1000000*rates[i].high);
      data[7 + i*6] = (long)(1000000*rates[i].low);
      data[8 + i*6] = (long)(1000000*rates[i].close);
      data[9 + i*6] = (long)(rates[i].spread);
      }
   toLog(StringFormat("in  %d, %d, %d, %d",data[0],data[1],data[2],data[3]));
   func_dll(data);
   toLog(StringFormat("out %d, %d, %d, %d",data[0],data[1],data[2],data[3]));
   printf("%s %d", eventName, copied);  
   if(data[0]==position) 
      return; 
   if (data[0]==0) {
      double v=1;
      toLog(StringFormat("Try BuyAsync(%0.4f)", v));
      printf("Try BuyAsync(%0.4f)", v);
      BuyAsync(v);
      } 
   if (data[0]==2) {
      double v=1;
      toLog(StringFormat("Try SellAsync(%0.4f)", v));
      printf("Try SellAsync(%0.4f)", v);
      SellAsync(v);
      }
   }

bool isTimed = false;
string eventName = "OnTick";
//---------------------------------------------------------------------------

int OnInit(){
   isTimed = false;
   if(isTimed){
      EventSetTimer(10);
      eventName = "OnTimer";
      }
   printf("OnInit");
   logOnInit();
   printf("logOnInit");
   init_dll();
   printf("init_dll");
   send_data();
   printf("send_data");
   return(INIT_SUCCEEDED);
   }

void OnDeinit(const int reason){
   EventKillTimer();   
   printf("OnDeinit");
   close_dll();
   FileClose(Log);  
   }

void OnTick(){ 
   if(!isTimed){
//      toLog("OnTick()");
      checkStock();
      }
   }

void OnTimer(){
   if(isTimed){
//      toLog("OnTimer()");
      checkStock();   
      }
   }

void OnTrade(){
//   printf("OnTrade");   
   }

void OnTradeTransaction(const MqlTradeTransaction& trans,
                        const MqlTradeRequest& request,
                        const MqlTradeResult& result){
 //  printf("OnTradeTransaction");   
   }

void OnChartEvent(const int id,
                  const long &lparam,
                  const double &dparam,
                  const string &sparam){
  // printf("OnChartEvent");   
   }

void OnBookEvent(const string &symbol){
   printf("OnBookEvent");   
   }
