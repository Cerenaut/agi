package io.agi.ef.coordinator.services;

import io.swagger.api.*;

import io.swagger.api.NotFoundException;

import javax.ws.rs.core.Response;

public class DataApiServiceImpl extends DataApiService {
  
      @Override
      public Response dataStateGet()
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "d magic!")).build();
  }
  
}
