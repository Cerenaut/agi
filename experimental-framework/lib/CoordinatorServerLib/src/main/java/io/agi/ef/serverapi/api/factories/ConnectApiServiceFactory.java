package io.agi.ef.serverapi.api.factories;

import io.agi.ef.serverapi.api.ConnectApiService;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-01T23:41:44.379+11:00")
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
