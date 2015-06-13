
import io.agi.coordinator.CoordinatorServer;
import io.agi.http.ClientServer;
import io.agi.http.StaticServer;
import java.util.ArrayList;



/**
 * This program must be run in either client or server mode.
 * 
 * The first argument is interpreted as the mode.
 * 
 * Server mode creates a persistent web service for running and configuring
 * one or more client instances.
 * 
 * Client mode is a process that will be created and controlled by a server.
 * 
 * If client and server are not specified, the program will default to server mode.
 * 
 * Any other argument is interpreted as a .properties file containing server configuation.
 * 
 * @author dave
 */
public class Main {

    public static final String ARG_MODE_SERVER = "server";
    public static final String ARG_MODE_CLIENT = "client";
    public static final String ARG_MODE_HELP = "help";
    
    public static void main( String[] args ) {// throws Exception {

        String mode = ARG_MODE_SERVER;
        ArrayList< String > files = new ArrayList< String >();
        
        for( String arg : args ) {
            if(    ( arg.equalsIgnoreCase( ARG_MODE_CLIENT ) )
                || ( arg.equalsIgnoreCase( ARG_MODE_SERVER ) ) ) {
                mode = arg;
            }
            else if( arg.equalsIgnoreCase( ARG_MODE_HELP ) ) {
                help();
            }
            else {
                files.add( arg );
            }
        }

        run( mode, files );
    }
    
    public static void run( String mode, ArrayList< String > files ) {
        if( mode.equalsIgnoreCase( ARG_MODE_SERVER ) ) {

            CoordinatorServer.run( files );

        }
        else if( mode.equalsIgnoreCase( ARG_MODE_CLIENT ) ) {
            ClientServer.run( files );
        }
    }
    
    public static void help() {
        System.out.println( "Usage: Zero or more arguments in any order." );
        System.out.println( "Arguments 'client' and 'server' will trigger the corresponding modes." );
        System.out.println( "Argument 'help' will display this message." );
        System.out.println( "Any other arguments are interpreted as the name[s] of .properties file[s] that configures the program." );
        System.exit( 0 );
    }
    
}
