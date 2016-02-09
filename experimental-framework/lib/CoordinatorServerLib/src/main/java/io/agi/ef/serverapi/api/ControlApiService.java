package io.agi.ef.serverapi.api;

import io.agi.ef.serverapi.api.*;
import io.agi.ef.serverapi.model.*;

import com.sun.jersey.multipart.FormDataParam;

import io.agi.ef.serverapi.model.TStamp;
import io.agi.ef.serverapi.model.Error;

import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-01T23:41:44.379+11:00")
public abstract class ControlApiService {
  
      public abstract Response controlEntityEntityNameCommandCommandGet(String entityName,String command,SecurityContext securityContext)
      throws NotFoundException;
  
      public abstract Response controlEntityEntityNameStatusStateGet(String entityName,String state,SecurityContext securityContext)
      throws NotFoundException;
  
}
