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
import io.agi.framework.coordination.CoordinationFactory;
import io.agi.framework.coordination.http.HttpCoordination;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.PersistenceFactory;
import io.agi.framework.persistence.models.ModelNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
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

    public void setup( Properties properties, ObjectMap om, EntityFactory ef ) {
        _ef = ef;

        if( om == null ) {
            om = ObjectMap.GetInstance();
        }

        _om = om;

        // Create persistence & Node now so you can Create entities in code that are hosted and persisted on the Node.
        _p =  PersistenceFactory.createPersistence ( properties );
        _c = CoordinationFactory.createCoordination( properties );

        // Create Node object
        String nodeName = PropertiesUtil.get( properties, PROPERTY_NODE_NAME, "node-1" );
        String nodeHost = PropertiesUtil.get( properties, PROPERTY_NODE_HOST, "localhost" );
        int nodePort = Integer.valueOf( PropertiesUtil.get( properties, PROPERTY_NODE_PORT, "8491" ) );
        _modelNode = new ModelNode( nodeName, nodeHost, nodePort );

        // The persistent description of this Node
        Node node = Node.NodeInstance();
        node.setup( _om, _modelNode._name, _modelNode._host, _modelNode._port, ef, _c, _p );
        _n = node;

        ef.setNode( node );
    }

    public static Properties createProperties( String propertiesFile, Properties overrides ) {
        // Load all properties from file
        Properties properties = new Properties();

        try {
            properties.load( new FileInputStream( propertiesFile ) );
        }
        catch( Exception e ) {
            logger.error( "Error reading properties for Persistence from: " + propertiesFile );
            logger.error( e.toString(), e );
            System.exit( -1 );
        }

        // apply overrides
        for( Object o : overrides.keySet() ) {
            String key = (String)o;
            String value = overrides.getProperty( key );
            properties.setProperty( key, value );
        }

        return properties;
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
            logger.error( "Could not setup HttpCoordination in Main.run()" );
            logger.error( e.toString(), e );
            System.exit( -1 );
        }
    }

    public static String getPackageVersion() {

        String version = Main.class.getPackage().getImplementationVersion();

        if( version == null ) {
            logger.error( "Could not load package properties file to query code version." );
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
        String entityJson = getArg( args, 1 );
        String dataJson   = getArg( args, 2 );
        String configJson = getArg( args, 3 );

        Properties overrides = getPropertyArgs( args, 4 );

        Main m = new Main();

        Properties properties = createProperties( nodePropertiesFile, overrides );
        EntityFactory ef = new CommonEntityFactory();
        ObjectMap om = null;

        m.setup( properties, om, ef );

        // Create custom entities and references
        if( entityJson != null && !entityJson.toLowerCase().equals( "null" ) ) {
            Framework.LoadEntities( entityJson );
        }
        if( dataJson != null  && !entityJson.toLowerCase().equals( "null" ) ) {
            Framework.LoadData( dataJson );
        }
        if( configJson != null && !entityJson.toLowerCase().equals( "null" ) ) {
            Framework.LoadConfigs( configJson ); // apply a delta config patch to loaded entities
        }

        // Start the system
        m.run();
    }

    public static String getArg( String[] args, int index ) {
        if( index < args.length ) {
            return args[ index ];
        }
        return null;
    }

    public static Properties getPropertyArgs( String[] args, int firstIndex ) {
        Properties properties = new Properties();

        int index = firstIndex;

        while( index < args.length ) {

            String arg1 = args[ index    ]; // key
            String arg2 = args[ index +1 ]; // value
            properties.setProperty( arg1, arg2 );

            index += 2;
        }

        return properties;
    }

    public static void help() {
        System.out.println( "Arguments: node.properties [entities.json] [data.json] [configs.json] [property-key] [property-value] ... " );
        System.out.println( "The first argument is mandatory. The other arguments are optional." );
        System.out.println( "Entities and data JSON files may be empty JSON arrays." );
    }
}
