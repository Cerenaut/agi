package io.swagger.api.factories;

import io.swagger.api.DataApiService;

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
