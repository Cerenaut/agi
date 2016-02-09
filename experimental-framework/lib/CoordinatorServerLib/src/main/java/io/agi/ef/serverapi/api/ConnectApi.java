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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/connect")

@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the connect API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-02-01T23:41:44.379+11:00")
public class ConnectApi  {
   private final ConnectApiService delegate = ConnectApiServiceFactory.getConnectApi();

    @GET
    @Path("/host/{host}/port/{port}/contextPath/{contextPath}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Connect to server", notes = "Send request to connect to server at specified address.", response = Void.class, tags={ "Connect" })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = Void.class) })

    public Response connectHostHostPortPortContextPathContextPathGet(@ApiParam(value = "ip address of machine running the server",required=true) @PathParam("host") String host,@ApiParam(value = "port of server",required=true) @PathParam("port") String port,@ApiParam(value = "the context path of the server",required=true) @PathParam("contextPath") String contextPath,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.connectHostHostPortPortContextPathContextPathGet(host,port,contextPath,securityContext);
    }
}
