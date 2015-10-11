package io.agi.ef.serverapi.api.factories;

import io.agi.ef.serverapi.api.ConnectApiService;

public class ConnectApiServiceFactory {

   private static ConnectApiService service = null;

   public static void setService(ConnectApiService pService) {
      service = pService;
   } 

   public static ConnectApiService getConnectApi()
   {
      return service;
   }
}
