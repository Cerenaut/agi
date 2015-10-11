package io.agi.ef.serverapi.api;

import io.agi.ef.serverapi.api.*;
import io.agi.ef.serverapi.model.*;

import com.sun.jersey.multipart.FormDataParam;

import io.agi.ef.serverapi.model.Error;
import java.sql.Timestamp;

import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;

public abstract class ApiApiService {
  
      public abstract Response apiCreateGet(String name,String type,String parent,String config)
      throws NotFoundException;
  
      public abstract Response apiDataGet(String entity,String action)
      throws NotFoundException;
  
      public abstract Response apiEntityGet(String entity,String action)
      throws NotFoundException;
  
      public abstract Response apiStopGet()
      throws NotFoundException;
  
}
