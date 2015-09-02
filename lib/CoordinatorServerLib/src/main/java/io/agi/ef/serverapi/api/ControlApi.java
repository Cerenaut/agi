package io.agi.ef.serverapi.api;

import io.agi.ef.serverapi.model.*;
import io.agi.ef.serverapi.api.ControlApiService;
import io.agi.ef.serverapi.api.factories.ControlApiServiceFactory;

import io.swagger.annotations.ApiParam;

import com.sun.jersey.multipart.FormDataParam;

import io.agi.ef.serverapi.model.TStamp;
import io.agi.ef.serverapi.model.Error;

import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

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
    @Path("/command/{command}")
    
    
    @io.swagger.annotations.ApiOperation(value = "Send command to the server.", notes = "Send a control command signal to the server. It can consist of Step, Stop, Start, Pause and Resume.\n", response = TStamp.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Timestamps"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response controlCommandCommandGet(@ApiParam(value = "The command to send.",required=true ) @PathParam("command") String command)
    throws NotFoundException {
    return delegate.controlCommandCommandGet(command);
    }
    @GET
    @Path("/status/{state}")
    
    
    @io.swagger.annotations.ApiOperation(value = "Get the status.", notes = "Get the status of a particular state, Paused, Running and Stopping.\n", response = Boolean.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "status"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response controlStatusStateGet(@ApiParam(value = "The status returns refers to this state.",required=true ) @PathParam("state") String state)
    throws NotFoundException {
    return delegate.controlStatusStateGet(state);
    }
}

