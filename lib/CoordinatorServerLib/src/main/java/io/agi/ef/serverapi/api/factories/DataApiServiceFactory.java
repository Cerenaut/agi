package io.agi.ef.serverapi.api.factories;

import io.agi.ef.serverapi.api.DataApiService;

public class DataApiServiceFactory {

   private static DataApiService service = null;

   public static void setService(DataApiService pService) {
      service = pService;
   } 

   public static DataApiService getDataApi()
   {
      return service;
   }
}
