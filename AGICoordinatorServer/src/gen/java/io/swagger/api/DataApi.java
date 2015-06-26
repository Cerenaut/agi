package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.DataApiService;
import io.swagger.api.factories.DataApiServiceFactory;

import io.swagger.annotations.ApiParam;

import com.sun.jersey.multipart.FormDataParam;

import io.swagger.model.State;
import io.swagger.model.Error;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/data")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/data", description = "the data API")
public class DataApi  {

   private final DataApiService delegate = DataApiServiceFactory.getDataApi();

    @GET
    @Path("/state")
    
    
    @io.swagger.annotations.ApiOperation(value = "Get the State of the system.", notes = "The State endpoint returns a model that describes the current state.\n", response = State.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "States"),
        
        @io.swagger.annotations.ApiResponse(code = 0, message = "Unexpected error") })

    public Response dataStateGet()
    throws NotFoundException {
    return delegate.dataStateGet();
    }
}

