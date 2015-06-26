package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.ControlApiService;
import io.swagger.api.factories.ControlApiServiceFactory;

import io.swagger.annotations.ApiParam;

import com.sun.jersey.multipart.FormDataParam;

import io.swagger.model.TStamp;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/control")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/control", description = "the control API")
public class ControlApi  {

   private final ControlApiService delegate = ControlApiServiceFactory.getControlApi();

    @GET
    @Path("/run")
    
    
    @io.swagger.annotations.ApiOperation(value = "Run the Coordinator", notes = "The Run endpoint starts the World and Agents to run freely.\n", response = TStamp.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Timestamps"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response controlRunGet()
    throws NotFoundException {
    return delegate.controlRunGet();
    }
    @GET
    @Path("/step")
    
    
    @io.swagger.annotations.ApiOperation(value = "Step the Coordinator", notes = "The Step endpoint steps the World and Agents.\n", response = TStamp.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Timestamps"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response controlStepGet()
    throws NotFoundException {
    return delegate.controlStepGet();
    }
    @GET
    @Path("/stop")
    
    
    @io.swagger.annotations.ApiOperation(value = "Stop the Coordinator", notes = "The Stop endpoint stops the World and Agents.\n", response = TStamp.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Timestamps"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response controlStopGet()
    throws NotFoundException {
    return delegate.controlStopGet();
    }
}

