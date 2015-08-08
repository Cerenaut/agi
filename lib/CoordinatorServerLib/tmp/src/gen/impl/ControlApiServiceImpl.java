package io.agi.ef.serverapi.api.impl;

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

public class ControlApiServiceImpl extends ControlApiService {
  

  	public ControlApiInterface _serviceDelegate = null;

      @Override
      public Response controlRunGet()
      throws NotFoundException {
      
      return _serviceDelegate.controlRunGet()
  }
  

  	public ControlApiInterface _serviceDelegate = null;

      @Override
      public Response controlStepGet()
      throws NotFoundException {
      
      return _serviceDelegate.controlStepGet()
  }
  

  	public ControlApiInterface _serviceDelegate = null;

      @Override
      public Response controlStopGet()
      throws NotFoundException {
      
      return _serviceDelegate.controlStopGet()
  }
  
}
