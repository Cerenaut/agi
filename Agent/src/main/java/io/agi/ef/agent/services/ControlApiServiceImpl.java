package io.agi.ef.agent.services;

import io.swagger.api.*;

import io.swagger.api.ControlApi;
import io.swagger.client.*;
import io.swagger.client.ApiException;
import io.swagger.client.api.*;
import io.swagger.model.TStamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.swagger.api.NotFoundException;

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
