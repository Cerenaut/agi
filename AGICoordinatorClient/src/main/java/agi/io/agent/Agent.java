package agi.io.agent;

import io.swagger.client.api.ControlApi;
import io.swagger.client.model.TStamp;
import io.swagger.client.model.Timestamp;

import java.util.List;

/**
 * Created by gideon on 25/06/15.
 */
public class Agent {

    public static void main(String[] args) throws Exception {
        ControlApi capi = new ControlApi(  );

        List<TStamp> tsl = capi.controlRunGet();

        for ( TStamp ts : tsl ) {
            System.out.println( ts );
        }

        System.out.println( "finished" );
    }

}
