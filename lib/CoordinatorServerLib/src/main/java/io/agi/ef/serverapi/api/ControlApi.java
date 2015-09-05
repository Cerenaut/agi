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
    @Path("/entity/{entityName}/command/{command}")
    
    
    @io.swagger.annotations.ApiOperation(value = "Send command to a specific entity..", notes = "Send a control command signal to an entity. It can consist of Step, Stop, Start, Pause and Resume.\n", response = TStamp.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Timestamp"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response controlEntityEntityNameCommandCommandGet(@ApiParam(value = "The name of the entity to receive command.",required=true ) @PathParam("entityName") String entityName,
    @ApiParam(value = "The command to send.",required=true ) @PathParam("command") String command)
    throws NotFoundException {
    return delegate.controlEntityEntityNameCommandCommandGet(entityName,command);
    }
    @GET
    @Path("/entity/{entityName}/status/{state}")
    
    
    @io.swagger.annotations.ApiOperation(value = "Get the status for a specific entity.", notes = "Get the status of a particular state, Paused, Running and Stopping.\n", response = Boolean.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "status"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response controlEntityEntityNameStatusStateGet(@ApiParam(value = "The name of the entity to receive status request.",required=true ) @PathParam("entityName") String entityName,
    @ApiParam(value = "The status returns refers to this state.",required=true ) @PathParam("state") String state)
    throws NotFoundException {
    return delegate.controlEntityEntityNameStatusStateGet(entityName,state);
    }
}

