package io.swagger.api.factories;

import io.swagger.api.DataApiService;
import io.swagger.api.impl.DataApiServiceImpl;

public class DataApiServiceFactory {

   private final static DataApiService service = new DataApiServiceImpl();

   public static DataApiService getDataApi()
   {
      return service;
   }
}
