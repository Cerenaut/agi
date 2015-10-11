package io.agi.ef.serverapi.api.factories;

import io.agi.ef.serverapi.api.ApiApiService;

public class ApiApiServiceFactory {

   private static ApiApiService service = null;

   public static void setService(ApiApiService pService) {
      service = pService;
   } 

   public static ApiApiService getApiApi()
   {
      return service;
   }
}
