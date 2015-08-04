package io.agi.ef.demos;

import io.agi.ef.core.network.entities.Agent;
import io.agi.ef.core.network.coordinator.Coordinator;
import io.agi.ef.core.CommsMode;

import java.util.ArrayList;


public abstract class MainDemo {

    public static final String ARG_MODE_COORDINATOR = "coordinator";
    public static final String ARG_MODE_AGENT = "agent";
    public static final String ARG_MODE_WORLD = "world";
    public static final String ARG_MODE_ALL = "all";
    public static final String ARG_MODE_HELP = "help";

    public void run( String[] args ) throws Exception {

        String mode = ARG_MODE_COORDINATOR;
        ArrayList<String> files = new ArrayList<String>();

        for ( String arg : args ) {
            if (        arg.equalsIgnoreCase( ARG_MODE_AGENT )
                    ||  arg.equalsIgnoreCase( ARG_MODE_COORDINATOR )
                    ||  arg.equalsIgnoreCase( ARG_MODE_WORLD )
                    ||  arg.equalsIgnoreCase( ARG_MODE_ALL )
                )
            {
                mode = arg;
            }
            else if ( arg.equalsIgnoreCase( ARG_MODE_HELP ) ) {
                help();
            }
            else {
                files.add( arg );
            }
        }

        if( mode.equalsIgnoreCase( ARG_MODE_COORDINATOR ) ) {
            runCoordinatorOnly();
        }
        else if( mode.equalsIgnoreCase( ARG_MODE_AGENT ) ) {
            runAgentOnly();
        }
        else if( mode.equalsIgnoreCase( ARG_MODE_WORLD ) ) {
            runWorldOnly();
        }
        else if( mode.equalsIgnoreCase( ARG_MODE_ALL ) ) {
            runAllLocally();
        }
    }

    public static void help() {
        System.out.println( "Usage: Zero or more arguments in any order." );
        System.out.println( "Arguments 'coordinator' and 'agent' will run the base version of the respective module." );
        System.out.println( "Argument 'help' will display this message." );
        System.out.println( "Any other arguments are interpreted as the name[s] of .properties file[s] that configures the program." );
        System.out.println( "----> To run custom versions of Agent, Coordinator, World or combinations, see the HelloWorld Main() ." );
        System.exit( 0 );
    }

    public abstract void runAgentOnly() throws Exception;
    public abstract void runWorldOnly() throws Exception;
    public abstract void runCoordinatorOnly() throws Exception;
    public abstract void runAllLocally() throws Exception;
}


