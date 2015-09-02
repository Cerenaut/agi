package io.agi.ef.serverapi.api.factories;

import io.agi.ef.serverapi.api.TestApiService;

public class TestApiServiceFactory {

   private static TestApiService service = null;

   public static void setService(TestApiService pService) {
      service = pService;
   } 

   public static TestApiService getTestApi()
   {
      return service;
   }
}
