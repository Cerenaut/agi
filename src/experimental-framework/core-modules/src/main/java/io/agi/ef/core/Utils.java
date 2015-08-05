package io.agi.ef.core;

import io.agi.ef.clientapi.model.TStamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gideon on 30/07/15.
 */
public class Utils {
    public static List< io.agi.ef.clientapi.model.TStamp > currentClientTimeStamp( int time ) {

        TStamp tstamp = new TStamp();
        tstamp.setTimeId( new BigDecimal( time ) );
        ArrayList< io.agi.ef.clientapi.model.TStamp > tsl = new ArrayList< >();
        tsl.add( tstamp );

        return tsl;
    }

    public static List< io.agi.ef.serverapi.model.TStamp > currentServerTimeStamp( int time ) {

        io.agi.ef.serverapi.model.TStamp tstamp = new io.agi.ef.serverapi.model.TStamp();
        tstamp.setTimeId( new BigDecimal( time ) );
        ArrayList< io.agi.ef.serverapi.model.TStamp > tsl = new ArrayList< >();
        tsl.add( tstamp );

        return tsl;
    }
}
