package io.swagger.api.impl;

import io.swagger.api.*;
import io.swagger.model.*;

import com.sun.jersey.multipart.FormDataParam;

import io.swagger.model.State;
import io.swagger.model.Error;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;

public class DataApiServiceImpl extends DataApiService {
  
      @Override
      public Response dataStateGet() throws NotFoundException {


          State st = new State();
          st.setStateId( new BigDecimal( "3" ) );

          Response resp = Response.ok().entity(
                                                    new ApiResponseMessage(ApiResponseMessage.OK, "message")
                                              ).build();

      return resp;
  }
  
}
