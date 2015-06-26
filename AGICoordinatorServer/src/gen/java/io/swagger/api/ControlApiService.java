package io.swagger.api;

import io.swagger.api.*;
import io.swagger.model.*;

import com.sun.jersey.multipart.FormDataParam;

import io.swagger.model.TStamp;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;

public abstract class ControlApiService {
  
      public abstract Response controlRunGet()
      throws NotFoundException;
  
      public abstract Response controlStepGet()
      throws NotFoundException;
  
      public abstract Response controlStopGet()
      throws NotFoundException;
  
}
