package io.agi.ef.demo;

import io.agi.core.orm.Keys;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.ef.*;
import io.agi.ef.http.HttpCoordination;
import io.agi.ef.serialization.JsonData;
import io.agi.ef.serialization.JsonEntity;
import io.agi.ef.sql.JdbcPersistence;

/**
 * Created by dave on 18/02/16.
 */
public class LightDemo {

    public static void main( String[] args ) {
        LightDemo ffnt = new LightDemo();
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
            int nodePort = 8491;

            ObjectMap om = ObjectMap.GetInstance();
            JdbcPersistence p = new JdbcPersistence();
            p.setup( JdbcPersistence.DRIVER_POSTGRESQL, user, password, url );

            // Define some entities
            String lightSourceName = "myLight";
            String lightControlName = "mySwitch";
            JsonEntity lightControl = new JsonEntity( lightControlName, LightControl.ENTITY_TYPE, nodeName, null );
            JsonEntity lightSource = new JsonEntity( lightSourceName, LightSource.ENTITY_TYPE, nodeName, lightControlName );

            p.setEntity( lightSource );
            p.setEntity( lightControl );

            // Connect the entities
            Entity.SetInputReference( p, lightSourceName, LightSource.CONTROL_INPUT, lightControlName, LightControl.CONTROL_OUTPUT );

            // Build the framework
//            MonolithicCoordination c = new MonolithicCoordination();
            HttpCoordination c = new HttpCoordination();

            // The persistent description of this Node
            Node n = new Node( nodeName, om );
            LightEntityFactory ef = new LightEntityFactory();
            n.setup( nodeHost, nodePort, ef, c, p );

            ef.setNode(n);
            c.setNode(n);
            c.start();
        }
        catch( Exception e ) {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

}
