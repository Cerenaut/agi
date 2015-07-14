package io.agi.ef.agent.services;


import io.agi.ef.serverapi.api.*;
import io.agi.ef.serverapi.api.ApiException;
import io.agi.ef.serverapi.model.*;

import com.sun.jersey.multipart.FormDataParam;

import io.agi.ef.serverapi.model.TStamp;
import io.agi.ef.serverapi.model.Error;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import io.agi.ef.serverapi.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;


public class ControlApiServiceImpl extends ControlApiService {

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        System.out.println( "agent received run!" );
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "agent received run!" ) ).build();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {

        System.out.println( "agent received step!" );
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "agent received step!" ) ).build();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {

        System.out.println( "agent received stop!" );
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "agent received stop!" ) ).build();
    }

}
