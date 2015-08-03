package io.agi.ef.demos.helloworld;

import io.agi.ef.core.network.coordinator.Coordinator;
import io.agi.ef.core.CommsMode;

/**
 * Created by gideon on 31/07/15.
 */
public class Main {

    public static void main( String[] args ) throws Exception {

//        int mode = 0;           // Create and run a Coordinator and a Client
//        int mode = 1;         // Create and run a Coordinator only
        int mode = 2;         // Create and run a HelloWorld Agent only

        if ( mode == 0 ) {
            // create and run a Coordinator
            Coordinator c = new Coordinator();

            HelloWorld w = new HelloWorld();

            // create and run a 'hello world' Agent
            HelloAgent a = new HelloAgent();

            c.addWorld( w );
            c.addAgent( a );
        }
        else if ( mode == 1 ) {
            // create and run a Coordinator
            Coordinator c = new Coordinator();
        }
        else if ( mode == 2 ) {
            // Create and run a 'hello world' Agent
            HelloAgent a = new HelloAgent( "agent" );
        }
        else if ( mode == 3 ) {
            // Create and run a 'hello world' Agent
            HelloWorld w = new HelloWorld( "world" );
        }

    }

}