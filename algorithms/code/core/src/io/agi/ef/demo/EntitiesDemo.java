package io.agi.ef.demo;

import io.agi.ef.*;
import io.agi.ef.monolithic.MonolithicCoordination;
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
            String user = "agiu";//args[ 0 ];
            String password = "password";//args[ 1 ];
            String url = "jdbc:postgresql://localhost:5432/agidb"; // https://jdbc.postgresql.org/documentation/80/connect.html

            EntityFactory ef = this;
            MonolithicCoordination c = new MonolithicCoordination();
            JdbcPersistence p = new JdbcPersistence();

            p.setup( JdbcPersistence.DRIVER_POSTGRESQL, user, password, url );

p.setPropertyInt( "dave", 5 );

Integer z = p.getPropertyInt( "dave" );
System.err.println( "dave =" + z);
            String nodeName = "demoNode";
            String nodeHost = "localhost";
            int nodePort = 8080;

            Node n = new Node();
            n.setup(nodeName, nodeHost, nodePort, ef, c, p);
            c.setNode(n);

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
