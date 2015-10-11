package io.agi.ef.serverapi.api;

import io.agi.ef.serverapi.model.*;
import io.agi.ef.serverapi.api.ApiApiService;
import io.agi.ef.serverapi.api.factories.ApiApiServiceFactory;

import io.swagger.annotations.ApiParam;

import com.sun.jersey.multipart.FormDataParam;

import io.agi.ef.serverapi.model.Error;
import java.sql.Timestamp;

import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/api")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/api", description = "the api API")
public class ApiApi  {

   private final ApiApiService delegate = ApiApiServiceFactory.getApiApi();

    @GET
    @Path("/create")
    
    
    @io.swagger.annotations.ApiOperation(value = "Notify the system of a data event.", notes = "Dynamically creates an entity of the specified type, using the factories registered within the Java code.\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })

    public Response apiCreateGet(@ApiParam(value = "Name of the entity to be created.",required=true) @QueryParam("name") String name,
    @ApiParam(value = "Type of the entity to be created.",required=true) @QueryParam("type") String type,
    @ApiParam(value = "Name of the parent entity of the entity to be created.",required=true) @QueryParam("parent") String parent,
    @ApiParam(value = "A JSON object as a string that describes the configuration of the entity to be created, such as parameter overrides.",required=true) @QueryParam("config") String config)
    throws NotFoundException {
    return delegate.apiCreateGet(name,type,parent,config);
    }
    @GET
    @Path("/data")
    
    
    @io.swagger.annotations.ApiOperation(value = "Notify the system of a data event.", notes = "These events allow Entities to wait on modifications to input data. These events are automatically fired when data is modified by the Java API, but may need to be called when data is changed from other APIs.\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })

    public Response apiDataGet(@ApiParam(value = "Name of the data affected.",required=true) @QueryParam("entity") String entity,
    @ApiParam(value = "Action affecting the data.",required=true) @QueryParam("action") String action)
    throws NotFoundException {
    return delegate.apiDataGet(entity,action);
    }
    @GET
    @Path("/entity")
    
    
    @io.swagger.annotations.ApiOperation(value = "Notify the system of an entity/action event.", notes = "This can be used to trigger an action by an entity, or to notify the system that an entity has completed and action, which in turn may cause further events. For example, event/experiment1/step will cause the experiment to step and emit event/experiment1/stepped. This in turn will be noticed by other entities (such as Agents), who will step themselves.\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })

    public Response apiEntityGet(@ApiParam(value = "Name of the entity affected.",required=true) @QueryParam("entity") String entity,
    @ApiParam(value = "Action affecting the entity.",required=true) @QueryParam("action") String action)
    throws NotFoundException {
    return delegate.apiEntityGet(entity,action);
    }
    @GET
    @Path("/stop")
    
    
    @io.swagger.annotations.ApiOperation(value = "Stop the Node", notes = "The Stop endpoint (gracefully) stops the Node that receives the request. Note this can also be sent to non-coordinator Nodes.\n", response = Timestamp.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Timestamp"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response apiStopGet()
    throws NotFoundException {
    return delegate.apiStopGet();
    }
}

