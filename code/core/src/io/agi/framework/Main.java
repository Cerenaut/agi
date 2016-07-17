/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.framework;

import io.agi.core.orm.ObjectMap;
import io.agi.core.util.PropertiesUtil;
import io.agi.framework.coordination.Coordination;
import io.agi.framework.coordination.http.HttpCoordination;
import io.agi.framework.coordination.monolithic.SingleProcessCoordination;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.NodeMemoryPersistence;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.couchbase.CouchbasePersistence;
import io.agi.framework.persistence.jdbc.JdbcPersistence;
import io.agi.framework.persistence.models.ModelNode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * NOTE: Run with the following virtual machine (VM) arguments:
 * <p/>
 * -Xms2000m  -Dlog4j.configurationFile=file:./log4j2.xml
 * <p/>
 * Created by dave on 6/03/16.
 */
public class Main {

    private static final Logger logger = LogManager.getLogger();

    public static final String PROPERTY_NODE_NAME = "node-name";
    public static final String PROPERTY_NODE_HOST = "node-host";
    public static final String PROPERTY_NODE_PORT = "node-port";

    public static final String PROPERTY_PERSISTENCE_TYPE = "persistence-type";
    public static final String PROPERTY_COORDINATION_TYPE = "coordination-type";

    public ModelNode _modelNode;


    public EntityFactory _ef;
    public ObjectMap _om;
    public Coordination _c;
    public Persistence _p;
    public Node _n;

    public Main() {
        String version = Main.getPackageVersion();
        System.out.println( "---------- AGIEF Package version = " + version + "------------" );
    }

    public void setup( String propertiesFile, ObjectMap om, EntityFactory ef ) {
        _ef = ef;

        if( om == null ) {
            om = ObjectMap.GetInstance();
        }

        _om = om;

        // Create persistence & Node now so you can Create entities in code that are hosted and persisted on the Node.
        _p = createPersistence( propertiesFile );
        _c = createCoordination( propertiesFile );

        String nodeName = PropertiesUtil.get( propertiesFile, PROPERTY_NODE_NAME, "node-1" );
        String nodeHost = PropertiesUtil.get( propertiesFile, PROPERTY_NODE_HOST, "localhost" );
        int nodePort = Integer.valueOf( PropertiesUtil.get( propertiesFile, PROPERTY_NODE_PORT, "8491" ) );
        _modelNode = new ModelNode( nodeName, nodeHost, nodePort );

        // The persistent description of this Node
        Node node = Node.NodeInstance();
        node.setup( _om, _modelNode._name, _modelNode._host, _modelNode._port, ef, _c, _p );
        _n = node;

        ef.setNode( node );
    }

    public Coordination createCoordination( String propertiesFile ) {
        String type = PropertiesUtil.get( propertiesFile, PROPERTY_COORDINATION_TYPE, "http" );
        Coordination c = null;
        if( type.equals( "http" ) ) {
            logger.info( "Distributed coordination." );
            c = new HttpCoordination();
        } else {
            logger.info( "Monolithic coordination." );
            c = new SingleProcessCoordination();
        }
        return c;
    }

    public Persistence createPersistence( String propertiesFile ) {
        String type = PropertiesUtil.get( propertiesFile, PROPERTY_PERSISTENCE_TYPE, "couchbase" );
        Persistence p = null;
        if( type.equals( "couchbase" ) ) {
            logger.info( "Using Couchbase for persistence." );
            p = CouchbasePersistence.Create( propertiesFile );
        }
        else if( type.equals( "jdbc" ) ) {
            logger.info( "Using JDBC (SQL) for persistence." );
            p = JdbcPersistence.Create( propertiesFile );
        }
        else if( type.equals( "node" ) ) {
            logger.info( "Using Node memory for persistence (note: You mustn't have more than one node in this configuration)." );
            p = new NodeMemoryPersistence();
        }
        return p;
    }

    public void run() {
        try {
            if( _c instanceof HttpCoordination ) {
                HttpCoordination c = ( HttpCoordination ) _c;
                c.setNode( _n );
                c.start();
            }
        }
        catch( Exception e ) {
            logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    public static String getPackageVersion() {

        String version = Main.class.getPackage().getImplementationVersion();

        if( version == null ) {
            logger.error( "Could not load properties file to query version." );
            version = "No version found.";
        }

        return version;
    }

    /**
     * A default main method to create an empty Node.
     *
     * @param args
     */
    public static void main( String[] args ) {

        // Create a Node
        if( args.length == 0 ) {
            System.err.println( "Must specify node.properties file as first argument." );
            help();
            System.exit( -1 );
        }

        if( args[ 0 ].indexOf( "help" ) >= 0 ) {
            help();
            System.exit( -1 );
        }

        String nodePropertiesFile = args[ 0 ];

        Main m = new Main();
        m.setup( nodePropertiesFile, null, new CommonEntityFactory() );

        // Create custom entities and references
        if( args.length > 1 ) {
            Framework.LoadEntities( args[ 1 ] );
        }
        if( args.length > 2 ) {
            Framework.LoadData( args[ 2 ] );
        }
        if( args.length > 3 ) {
            Framework.LoadConfigs( args[ 3 ] ); // apply a delta config patch to loaded entities
        }

        // Start the system
        m.run();
    }

    public static void help() {
        System.out.println( "Arguments: node.properties [entities.json] [data.json] [configs.json]" );
        System.out.println( "The first argument is mandatory. The other arguments are optional." );
        System.out.println( "Entities and data JSON files may be empty JSON arrays." );
    }
}
