package io.agi.ef.helloworld;

import io.agi.ef.coordinator.Coordinator;
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
            Coordinator c = new Coordinator( CommsMode.NON_NETWORK );

            // Create and run a 'hello world' Agent
            AgentHello a = new AgentHello( "agent", CommsMode.NON_NETWORK );

            c.addAgent( a );

            c.start();
            a.start();
        }
        else if ( mode == 1 ) {
            // create and run a Coordinator
            Coordinator c = new Coordinator( CommsMode.NETWORK );
            c.start();
        }
        else if ( mode == 2 ) {
            // Create and run a 'hello world' Agent
            AgentHello a = new AgentHello( "agent", CommsMode.NETWORK );
            a.start();
        }

    }

}