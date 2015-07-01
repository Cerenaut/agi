package io.agi.ef.agent;

import io.swagger.client.ApiClient;
import io.swagger.client.Configuration;
import io.swagger.client.api.ControlApi;
import io.swagger.client.model.TStamp;

import java.util.List;

/**
 * Created by gideon on 25/06/15.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath( "http://localhost:9999" );
        apiClient.setUserAgent( "Agent" );

        // test a call
        ControlApi capi = new ControlApi(  );
        List<TStamp> tsl = capi.controlRunGet();
        for ( TStamp ts : tsl ) {
            System.out.println( ts );
        }
        System.out.println( "finished" );
    }

}