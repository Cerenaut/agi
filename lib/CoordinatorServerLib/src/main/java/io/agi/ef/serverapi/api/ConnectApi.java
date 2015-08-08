package io.agi.ef.serverapi.api;

import io.agi.ef.serverapi.model.*;
import io.agi.ef.serverapi.api.ConnectApiService;
import io.agi.ef.serverapi.api.factories.ConnectApiServiceFactory;

import io.swagger.annotations.ApiParam;

import com.sun.jersey.multipart.FormDataParam;


import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/connect")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/connect", description = "the connect API")
public class ConnectApi  {

   private final ConnectApiService delegate = ConnectApiServiceFactory.getConnectApi();

    @GET
    @Path("/agent/{contextPath}")
    
    
    @io.swagger.annotations.ApiOperation(value = "Connect to Agent", notes = "Send request to connect this Agent server as specified url base path.\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })

    public Response connectAgentContextPathGet(@ApiParam(value = "ID of agent to connect to",required=true ) @PathParam("contextPath") String contextPath)
    throws NotFoundException {
    return delegate.connectAgentContextPathGet(contextPath);
    }
    @GET
    @Path("/world/{contextPath}")
    
    
    @io.swagger.annotations.ApiOperation(value = "Connect to World", notes = "Send request to connect this World server as specified url base path.\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })

    public Response connectWorldContextPathGet(@ApiParam(value = "ID of agent to connect to",required=true ) @PathParam("contextPath") String contextPath)
    throws NotFoundException {
    return delegate.connectWorldContextPathGet(contextPath);
    }
}

