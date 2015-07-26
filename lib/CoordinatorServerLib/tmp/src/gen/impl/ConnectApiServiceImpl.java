package io.agi.ef.serverapi.api.impl;

import io.agi.ef.serverapi.api.*;
import io.agi.ef.serverapi.model.*;

import com.sun.jersey.multipart.FormDataParam;


import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;

public class ConnectApiServiceImpl extends ConnectApiService {
  
      @Override
      public Response connectAgentBaseurlGet(String baseurl)
      throws NotFoundException {
      // do some magic!
      return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
  }
  
}
