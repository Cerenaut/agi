package io.agi.ef.demo;

import com.sun.net.httpserver.HttpServer;
import io.agi.ef.*;
import io.agi.ef.http.HttpCoordination;
import io.agi.ef.http.HttpCoordinationHandler;
import io.agi.ef.http.HttpCoordinationServer;
import io.agi.ef.monolithic.MonolithicCoordination;
import io.agi.ef.serialization.JsonEntity;
import io.agi.ef.serialization.JsonNode;
import io.agi.ef.sql.JdbcPersistence;

/**
 * Created by dave on 18/02/16.
 */
public class EntitiesDemo implements EntityFactory {

    public static void main( String[] args ) {
        EntitiesDemo ffnt = new EntitiesDemo();
        ffnt.test( args );
    }

    public void test( String[] args ) {

        try {

            // Persistence
            String user = "agiu";//args[ 0 ];
            String password = "password";//args[ 1 ];
            String url = "jdbc:postgresql://localhost:5432/agidb"; // https://jdbc.postgresql.org/documentation/80/connect.html

            // Node
            String nodeName = "demoNode";
            String nodeHost = "localhost";
            int nodePort = 8080;

            EntityFactory ef = this;
            JdbcPersistence p = new JdbcPersistence();
            p.setup( JdbcPersistence.DRIVER_POSTGRESQL, user, password, url );

//p.setPropertyInt("dave", 5);
//Integer z = p.getPropertyInt( "dave" );
//System.err.println("dave =" + z);
//p.removeNode("x");
//            JsonEntity je = new JsonEntity("e-key", "e-type", "e-node", "e-parent" );
//            JsonNode jn = new JsonNode("n-key", "n-host", 1111 );
//            p.setEntity( je );
//            p.setNode( jn );

//            MonolithicCoordination c = new MonolithicCoordination();
            HttpCoordination c = new HttpCoordination();

            // The persistent description of this Node
            Node n = new Node();
            n.setup(nodeName, nodeHost, nodePort, ef, c, p);

            c.setNode(n);
            c.start();

            // The server
//            String handlerClassName = HttpCoordinationHandler.class.getName();
//            HttpServer s = HttpCoordinationServer.create( n, nodePort, HttpCoordinationHandler.CONTEXT, handlerClassName );
            // ok ready to start:

//            how to trigger the event remotely?
//            c.requestUpdate( "" );
        }
        catch( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public Entity create( String entityName, String entityType ) {
        return null;
    }
}
