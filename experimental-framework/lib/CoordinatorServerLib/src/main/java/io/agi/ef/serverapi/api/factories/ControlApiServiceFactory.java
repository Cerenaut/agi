package io.agi.ef.serverapi.api.factories;

import io.agi.ef.serverapi.api.ControlApiService;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-01T23:41:44.379+11:00")
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
