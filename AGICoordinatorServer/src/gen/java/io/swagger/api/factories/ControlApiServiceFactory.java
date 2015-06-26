package io.swagger.api.factories;

import io.swagger.api.ControlApiService;
import io.swagger.api.impl.ControlApiServiceImpl;

public class ControlApiServiceFactory {

   private final static ControlApiService service = new ControlApiServiceImpl();

   public static ControlApiService getControlApi()
   {
      return service;
   }
}
