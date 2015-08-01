package io.agi.ef;

import io.agi.ef.agent.Agent;
import io.agi.ef.coordinator.Coordinator;
import io.agi.ef.core.CommsMode;

import java.util.ArrayList;


public class Main {

    public static final String ARG_MODE_COORDINATOR = "coordinator";
    public static final String ARG_MODE_AGENT = "agent";
    public static final String ARG_MODE_HELP = "help";

    public static void main( String[] args ) throws Exception {

        String mode = ARG_MODE_COORDINATOR;
        ArrayList<String> files = new ArrayList<String>();

        for ( String arg : args ) {
            if ( ( arg.equalsIgnoreCase( ARG_MODE_AGENT ) )
                    || ( arg.equalsIgnoreCase( ARG_MODE_COORDINATOR ) ) ) {
                mode = arg;
            }
            else if ( arg.equalsIgnoreCase( ARG_MODE_HELP ) ) {
                help();
            }
            else {
                files.add( arg );
            }
        }

        run( mode, files );
    }

    public static void run( String mode, ArrayList< String > files ) throws Exception {
        if( mode.equalsIgnoreCase( ARG_MODE_COORDINATOR ) ) {
            Coordinator coordinator = new Coordinator( CommsMode.NETWORK );
            coordinator.setupProperties( files );
            coordinator.start();
        }
        else if( mode.equalsIgnoreCase( ARG_MODE_AGENT ) ) {
            // create an agent at contextPath /agent
            // we could make this string anything we wanted, and in theory create multiple agents
            // ---> except that at this point, the port is hardcoded and it will conflict
            Agent agent = new Agent( "agent", CommsMode.NETWORK );
            agent.setupProperties( files );
            agent.start();
        }
    }

    public static void help() {
        System.out.println( "Usage: Zero or more arguments in any order." );
        System.out.println( "Arguments 'coordinator' and 'agent' will trigger the corresponding modes." );
        System.out.println( "Argument 'help' will display this message." );
        System.out.println( "Any other arguments are interpreted as the name[s] of .properties file[s] that configures the program." );
        System.exit( 0 );
    }

}


