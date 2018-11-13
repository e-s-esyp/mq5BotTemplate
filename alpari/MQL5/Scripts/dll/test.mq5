//+------------------------------------------------------------------+
//|                                                         test.mq5 |
//|                        Copyright 2016, MetaQuotes Software Corp. |
//|                                             https://www.mql5.com |
//+------------------------------------------------------------------+
#property copyright "Copyright 2016, MetaQuotes Software Corp."
#property link      "https://www.mql5.com"
#property version   "1.00"
//+------------------------------------------------------------------+
//| Script program start function                                    |
//+------------------------------------------------------------------+
#import "link64.dll"
   void init_dll();
#import
void OnStart()
  {
//---
    init_dll();
  
  }
//+------------------------------------------------------------------+
