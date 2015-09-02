package io.agi.ef.serverapi.api;

import io.agi.ef.serverapi.model.*;
import io.agi.ef.serverapi.api.TestApiService;
import io.agi.ef.serverapi.api.factories.TestApiServiceFactory;

import io.swagger.annotations.ApiParam;

import com.sun.jersey.multipart.FormDataParam;


import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/test")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/test", description = "the test API")
public class TestApi  {

   private final TestApiService delegate = TestApiServiceFactory.getTestApi();

    @GET
    
    
    
    @io.swagger.annotations.ApiOperation(value = "test call", notes = "this is a test call to test server is running\n", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })

    public Response testGet()
    throws NotFoundException {
    return delegate.testGet();
    }
}

