package io.agi.ef.helloworld;

import io.agi.ef.coordinator.Coordinator;
import io.agi.ef.core.CommsMode;
import io.agi.ef.world.World;

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
            Coordinator c = new Coordinator( CommsMode.NON_NETWORK );

            HelloWorld w = new HelloWorld( CommsMode.NON_NETWORK );

            // create and run a 'hello world' Agent
            HelloAgent a = new HelloAgent( CommsMode.NON_NETWORK );

            c.addAgent( a );

            c.start();
            w.start();
            a.start();
        }
        else if ( mode == 1 ) {
            // create and run a Coordinator
            Coordinator c = new Coordinator( CommsMode.NETWORK );
            c.start();
        }
        else if ( mode == 2 ) {
            // Create and run a 'hello world' Agent
            HelloAgent a = new HelloAgent( CommsMode.NETWORK, "agent" );
            a.start();
        }

    }

}