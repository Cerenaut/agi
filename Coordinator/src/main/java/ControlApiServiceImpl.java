

import io.swagger.api.*;
import io.swagger.model.*;

import com.sun.jersey.multipart.FormDataParam;

import io.swagger.model.TStamp;
import io.swagger.model.Error;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.swagger.api.NotFoundException;

import java.io.InputStream;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import javax.ws.rs.core.Response;

public class ControlApiServiceImpl extends ControlApiService {

    @Override
    public Response controlRunGet()
            throws NotFoundException {

        TStamp tstamp = new TStamp();
        tstamp.setTimeId( new BigDecimal( "2" ) );

        ArrayList<TStamp> tsl = new ArrayList<TStamp>();
        tsl.add(tstamp);
        tstamp.setTimeId( new BigDecimal( "3" ) );
        tsl.add( tstamp );

        return Response.ok().entity( tsl ).build();
    }

    @Override
    public Response controlStepGet()
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "c magic!" ) ).build();
    }

    @Override
    public Response controlStopGet()
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity( new ApiResponseMessage( ApiResponseMessage.OK, "c magic!" ) ).build();
    }

}
