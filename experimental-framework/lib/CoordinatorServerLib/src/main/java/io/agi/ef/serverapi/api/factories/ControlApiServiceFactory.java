package io.agi.ef.serverapi.api.factories;

import io.agi.ef.serverapi.api.ControlApiService;

public class ControlApiServiceFactory {

   private static ControlApiService service = null;

   public static void setService(ControlApiService pService) {
      service = pService;
   } 

   public static ControlApiService getControlApi()
   {
      return service;
   }
}
