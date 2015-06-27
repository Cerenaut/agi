package io.swagger.api.factories;

import io.swagger.api.ControlApiService;

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
