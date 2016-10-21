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

package io.agi.framework.persistence.jdbc;

import io.agi.core.util.PropertiesUtil;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * Created by dave on 16/02/16.
 */
public class JdbcPersistence implements Persistence {

    //    Entity --< Properties
    //           --< Data
    //    Entity --- name, type, children, node
    //               Could be JSON string for entities: { name: xxx, type: yyy } etc
    public static final String PROPERTY_DATABASE_USER = "database-user";
    public static final String PROPERTY_DATABASE_PASSWORD = "database-password";
    public static final String PROPERTY_DATABASE_URL = "database-url";
    public static final String PROPERTY_DATABASE_DRIVER_CLASS = "database-driver-class";

    public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    public static final String DRIVER_POSTGRESQL = "org.postgresql.Driver";

    protected String _user;
    protected String _password;
    protected String _url; // e.g. jdbc:postgresql://localhost:5432/agidb"; // https://jdbc.postgresql.org/documentation/80/connect.html

    private static final Logger logger = LogManager.getLogger();

    public JdbcPersistence() {
    }

    public static JdbcPersistence Create( Properties properties ) {
        String databaseUser = PropertiesUtil.get( properties, JdbcPersistence.PROPERTY_DATABASE_USER, "agiu" );
        String databasePassword = PropertiesUtil.get( properties, JdbcPersistence.PROPERTY_DATABASE_PASSWORD, "password" );
        String databaseUrl = PropertiesUtil.get( properties, JdbcPersistence.PROPERTY_DATABASE_URL, "jdbc:postgresql://localhost:5432/agidb" );
        String databaseDriverClass = PropertiesUtil.get( properties, JdbcPersistence.PROPERTY_DATABASE_DRIVER_CLASS, JdbcPersistence.DRIVER_POSTGRESQL );

        JdbcPersistence p = new JdbcPersistence();

        try {
            p.setup( databaseDriverClass, databaseUser, databasePassword, databaseUrl );
        }
        catch( ClassNotFoundException e ) {
            e.printStackTrace();
            return null;
        }

        return p;
    }

    public void setup( String driver, String user, String password, String url ) throws ClassNotFoundException {
        Class.forName( driver );
        _user = user;
        _password = password;
        _url = url;
    }

    // Nodes
    public Collection< ModelNode > fetchNodes() {
        String sql = "SELECT name, host, port FROM nodes";
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "name" );
        rsm._fields.add( "host" );
        rsm._fields.add( "port" );
        executeQuery( sql, rsm );
        ArrayList< ModelNode > nodes = new ArrayList< ModelNode >();
        for( int i = 0; i < rsm._rows.size(); ++i ) {
            String key = rsm.getRowValue( i, "name" );
            String host = rsm.getRowValue( i, "host" );
            String port = rsm.getRowValue( i, "port" );
            ModelNode jn = new ModelNode( key, host, Integer.valueOf( port ) );
            nodes.add( jn );
        }
        return nodes;
    }

    public void persistNode( ModelNode e ) {
        // https://www.sitepoint.com/community/t/how-to-use-on-duplicate-key-update-in-postgresql-with-php/200335/4
        String sql1 = "UPDATE nodes SET host = '" + e._host + "', port = '" + e._port + "' WHERE name = '" + e._name + "'";
        execute( sql1 );
        String sql2 = "INSERT INTO nodes (name, host,port) SELECT '" + e._name + "', '" + e._host + "', '" + e._port + "' WHERE NOT EXISTS (SELECT name from nodes WHERE name = '" + e._name + "')";
        execute( sql2 );
    }

    public ModelNode fetchNode( String nodeName ) {
        String sql = "SELECT name, host, port FROM nodes where name = '" + nodeName + "'";
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "host" );
        rsm._fields.add( "port" );
        executeQuery( sql, rsm );
        if( rsm._rows.isEmpty() ) {
            return null;
        }
        String host = rsm.getRowValue( 0, "host" );
        String port = rsm.getRowValue( 0, "port" );
        ModelNode jn = new ModelNode( nodeName, host, Integer.valueOf( port ) );
        return jn;
    }

    public void removeNode( String nodeName ) {
        String sql = "DELETE FROM nodes WHERE name = '" + nodeName + "'";
        execute( sql );
    }

    // Entities
    public Collection< ModelEntity > getEntities() {
        String sql = "SELECT name, type, node, parent, config FROM entities";
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "name" );
        rsm._fields.add( "type" );
        rsm._fields.add( "node" );
        rsm._fields.add( "parent" );
        rsm._fields.add( "config" );
        executeQuery( sql, rsm );
        ArrayList< ModelEntity > nodes = new ArrayList< ModelEntity >();
        for( int i = 0; i < rsm._rows.size(); ++i ) {
            String key = rsm.getRowValue( i, "name" );
            String type = rsm.getRowValue( i, "type" );
            String node = rsm.getRowValue( i, "node" );
            String parent = rsm.getRowValue( i, "parent" );
            String config = rsm.getRowValue( i, "config" );
            ModelEntity je = new ModelEntity( key, type, node, parent, config );
            nodes.add( je );
        }
        return nodes;
    }

    public Collection< String > getChildEntities( String parent ) {
        // SELECT
        String sql = "SELECT name, parent FROM entities where parent = '" + parent + "'";
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "name" );
        rsm._fields.add( "parent" );
        executeQuery( sql, rsm );

        ArrayList< String > children = new ArrayList< String >();

        for( int i = 0; i < rsm._rows.size(); ++i ) {
            String key = rsm.getRowValue( i, "name" );
            children.add( key );
        }

        return children;
    }

    public void persistEntity( ModelEntity e ) {
        // https://www.sitepoint.com/community/t/how-to-use-on-duplicate-key-update-in-postgresql-with-php/200335/4
        String sql1 = "UPDATE entities SET type = '" + e.type + "', node = '" + e.node + "', parent = '" + e.parent + "', config = '" + e.config + "' WHERE name = '" + e.name + "'";
        execute( sql1 );
        String sql2 = "INSERT INTO entities (name, type, node, parent, config) SELECT '" + e.name + "', '" + e.type + "', '" + e.node + "', '" + e.parent + "', '" + e.config + "' WHERE NOT EXISTS (SELECT name from entities WHERE name = '" + e.name + "')";
        execute( sql2 );
    }

    public ModelEntity fetchEntity( String name ) {
        String sql = "SELECT type, node, parent, config FROM entities where name = '" + name + "'";
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "type" );
        rsm._fields.add( "node" );
        rsm._fields.add( "parent" );
        rsm._fields.add( "config" );
        executeQuery( sql, rsm );
        if( rsm._rows.isEmpty() ) {
            return null;
        }
        String type = rsm.getRowValue( 0, "type" );
        String node = rsm.getRowValue( 0, "node" );
        String parent = rsm.getRowValue( 0, "parent" );
        String config = rsm.getRowValue( 0, "config" );
        ModelEntity modelEntity = new ModelEntity( name, type, node, parent, config );

        return modelEntity;
    }

    public void removeEntity( String key ) {
        String sql = "DELETE FROM entities WHERE name = '" + key + "'";
        execute( sql );
    }

    // Data
    public void persistData( ModelData modelData ) {
        String refKeyString = ( modelData.refKeys != null ) ? "'" + modelData.refKeys + "'" : "null";
        //_logger.info( "persistData T: {} @1 ", System.currentTimeMillis() );
        String sql1 = "UPDATE data SET ref_name = '" + modelData.refKeys + "', sizes = '" + modelData.sizes + "', elements = '" + modelData.elements + "' WHERE name = '" + modelData.name + "'";
        execute( sql1 );
        //_logger.info( "persistData T: {} @2 ", System.currentTimeMillis() );
        String sql2 = "INSERT INTO data (name, ref_name, sizes, elements) SELECT '" + modelData.name + "', " + refKeyString + ", '" + modelData.sizes + "', '" + modelData.elements + "' WHERE NOT EXISTS (SELECT name from data WHERE name = '" + modelData.name + "' )";
        execute( sql2 );
        //_logger.info( "persistData T: {} @3 ", System.currentTimeMillis() );
    }

    public Collection< ModelData > getDataMeta( String filter ) {
        String sql = "SELECT name, ref_name, sizes FROM data where name like '%" + filter + "%'";

        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "name" );
        rsm._fields.add( "ref_name" );
        rsm._fields.add( "sizes" );
        executeQuery( sql, rsm );

        ArrayList< ModelData > al = new ArrayList< ModelData >();

        for( int i = 0; i < rsm._rows.size(); ++i ) {
            String name    = rsm.getRowValue( i, "name" );
            String refKeys = rsm.getRowValue( i, "ref_name" );
            String sizes   = rsm.getRowValue( i, "sizes" );
            ModelData md2 = new ModelData( name, refKeys, sizes, null ); // sans actual data
            al.add( md2 );
        }

        return al;
    }

    public Collection< String > getData() {
        String sql = "SELECT name FROM data";
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "name" );
        executeQuery( sql, rsm );

        ArrayList< String > names = new ArrayList< String >();

        for( int i = 0; i < rsm._rows.size(); ++i ) {
            String name = rsm.getRowValue( i, "name" );
            names.add( name );
        }

        return names;
    }

    public ModelData fetchData( String key ) {
        String sql = "SELECT ref_name, sizes, elements FROM data where name = '" + key + "'";
        ResultSetMap rsm = new ResultSetMap();
        rsm._fields.add( "ref_name" );
        rsm._fields.add( "sizes" );
        rsm._fields.add( "elements" );
        executeQuery( sql, rsm );
        String refKey = null;
        if( rsm._rows.isEmpty() ) {
            return null;
        } else {
            refKey = rsm.getRowValue( 0, "ref_name" );
            if( refKey != null ) {
                if( refKey.equals( "null" ) ) {
                    refKey = null;
                }
            }
        }
        String sizes = rsm.getRowValue( 0, "sizes" );
        String elements = rsm.getRowValue( 0, "elements" );
        ModelData modelData = new ModelData( key, refKey, sizes, elements );
        return modelData;
    }

    public void removeData( String key ) {
        String sql = "DELETE FROM data WHERE name = '" + key + "'";
        execute( sql );
    }

    public void execute( String sql ) {
        JdbcUtil.Execute( _url, _user, _password, sql );
    }

    public void executeQuery( String sql, ResultSetCallback cb ) {
        JdbcUtil.ExecuteQuery( _url, _user, _password, sql, cb );
    }


}
