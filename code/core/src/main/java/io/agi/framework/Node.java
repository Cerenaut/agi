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
import io.agi.framework.coordination.Coordination;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.references.DataRefMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * A container in which the program can execute.
 * The system can be distributed by having multiple nodes, potentially on different virtual or physical machines.
 * This is a Singleton class.
 * Created by dave on 14/02/16.
 */
public class Node {

    private static final Logger logger = LogManager.getLogger();

    public static final String KEY_NODE = "node";

    protected ObjectMap _om;
    protected String _name;
    protected String _host;
    protected int _port;
    protected EntityFactory _ef;
    protected Coordination _c;
    protected Persistence _p;
    protected DataRefMap _dataRefMap = new DataRefMap();

    protected HashMap< String, ArrayList< EntityListener > > _entityListeners = new HashMap< String, ArrayList< EntityListener > >();

    static protected Node _node = null;

    private Node() {
    }

    /**
     * Stops the Node. TODO: Make it check nothing is happening (e.g. no pending updates to complete?)
     *
     */
    public void stop() {
        logger.info( "Stopping process on request." );
        System.exit( 0 );
    }

    /**
     * Sets up the interfaces, which cannot be modified afterwards.
     */
    public void setup(
            ObjectMap om,
            String name,
            String host,
            int port,
            EntityFactory ef,
            Coordination c,
            Persistence p ) {

        _om = om;
        _om.put( KEY_NODE, this );

        _name = name;
        _host = host;
        _port = port;

        _ef = ef;
        _c = c;
        _p = p;

        _c.setNode( this );
        _dataRefMap.setNode( this );
//        ModelNode jn = new ModelNode( _name, _host, _port );
//        _p.persistNode(jn);
    }

    public static Node NodeInstance() {
        if( _node == null ) {
            _node = new Node();
        }
        return _node;
    }

    public String getName() {
        return _name;
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    public ObjectMap getObjectMap() {
        return _om;
    }

    public EntityFactory getEntityFactory() {
        return _ef;
    }

    /**
     * Returns the persistence layer
     *
     * @return
     */
    public Persistence getPersistence() {
        return _p;
    }

    /**
     * Returns the coordination layer
     *
     * @return
     */
    public Coordination getCoordination() {
        return _c;
    }

    public DataRefMap getDataRefMap() {
        return _dataRefMap;
    }

//    public void notifySetData( String dataName ) {
//        _c.onSetData( dataName );
//    }


    /**
     * A callback that is called when an Entity has been updated, including all its children.
     *
     * @param entityName
     */
    public void notifyUpdated( String entityName ) {
//        int count = _p.getEntityAge(entityName);
//        count += 1;
//        _p.setEntityAge(entityName, count);
        logger.debug( " %%Unlock%% " + entityName );
        unlock( entityName );

        // broadcast to any distributed listeners:
        _c.onUpdated( entityName );
    }

    /**
     * Called by the distributed system when an entity has been updated.
     *
     * @param entityName
     */
    public void onUpdated( String entityName ) {
        callEntityListeners( entityName );
    }

    /**
     * This method requests the distributed system to update the specified entity.
     * We don't know which Node hosts the Entity - it could be this, it could be another.
     * So, broadcast (or directly send) the update requet to another Node[s].
     *
     * @param entityName
     */
    public void requestUpdate( String entityName ) {
        _c.doUpdate( entityName );
    }

    /**
     * This method is called when the distributed system has received a request for an update of an Entity.
     *
     * @param entityName
     */
    public void doUpdate( String entityName ) {

        ModelEntity modelEntity = _p.getEntity( entityName );

        if( modelEntity == null ) {
            return; // bad entity
        }

        if( !modelEntity.node.equals( getName() ) ) {
            return;
        }

        forkUpdate( entityName ); // returns immediately
    }

    /**
     * Creates a thread to actually do the work of updating the entity
     *
     * @param entityName
     */
    protected void forkUpdate( String entityName ) {
        Thread t = new Thread( new Runnable() {
            @Override
            public void run() {
                // block forked thread forking until entity can be updated.
                if( !lock( entityName ) ) {
                    return;
                }

                logger.debug( " %%Lock%% " + entityName );

                ModelEntity modelEntity = _p.getEntity( entityName ); // NOTE: Can't get the model entity UNTIL I have the lock, or the model might be out of date.

                Entity e = _ef.create( _om, modelEntity );

                e.update();
            }
        } );
        t.start();
    }

    /**
     * Notify the system that this Data has changed.
     * @param key
     */
    public void notifySetData( String key ) {
        _c.onSetData( key, _name );
    }

    /**
     * Update cache that this Data most recent copy is now located at the specified Node.
     * @param key
     * @param nodeName
     */
    public void onSetData( String key, String nodeName ) {
        _dataRefMap.onSetData( key, nodeName );
    }

    /**
     * Called by Coordination when a Data has been fetched from a remote Node.
     * @param key
     * @param node
     * @param modelData
     */
    public void onGetData( String key, String node, ModelData modelData ) {
        _dataRefMap.onGetData( key, node, modelData );
    }

    /**
     * Synchronously fetch the specified data from a remote Node, because we need it and we know our local copy is out
     * of date.
     * @param key
     * @param node
     * @return
     */
    public ModelData doGetData( String key, String node ) {
        ModelData modelData = _c.getData( key, node );
        return modelData;
    }

    public boolean lock( String entityName ) {
        Semaphore s = getLock( entityName );

        logger.debug( "Thread " + Thread.currentThread().hashCode() + " waiting for " + entityName );
        try {
            s.acquire();
        }
        catch( InterruptedException ie ) {
            logger.debug( "Thread " + Thread.currentThread().hashCode() + " cant get lock for " + entityName );
            return false;
        }

        logger.debug( "Thread " + Thread.currentThread().hashCode() + " has lock for " + entityName );

        return true;
    }

    public Semaphore getLock( String entityName ) {
        synchronized( _entityNameSemaphores ) {
            Semaphore l = _entityNameSemaphores.get( entityName );
            if( l == null ) {
                l = new Semaphore( 1 ); // binary
                _entityNameSemaphores.put( entityName, l );
            }

            return l;
        }
    }

    public void unlock( String entityName ) {
        Semaphore l = getLock( entityName );

        //System.err.println("Thread " + Thread.currentThread().hashCode() + " about to unlock " + entityName);

        l.release();
    }

    public HashMap< String, Semaphore > _entityNameSemaphores = new HashMap< String, Semaphore >();

    /**
     * Adds a listener to the specified Entity.
     * It will persist for only one call.
     *
     * @param entity
     * @param listener
     */
    public void addEntityListener( String entity, EntityListener listener ) {
        synchronized( _entityListeners ) {
            ArrayList< EntityListener > al = _entityListeners.get( entity );
            if( al == null ) {
                al = new ArrayList();
                _entityListeners.put( entity, al );
            }
            al.add( listener );
        }
    }

    public void removeEntityListener( String entity, EntityListener el ) {
        synchronized( _entityListeners ) {
            ArrayList< EntityListener > al = _entityListeners.get( entity );
            if( al != null ) {
                for( EntityListener el2 : al ) {
                    if( el2.equals( el ) ) {
                        al.remove( el );
                    }
                }
            }
        }
    }

    /**
     * Call any listeners associated with this Entity, and then remove them.
     *
     * @param entity
     */
    public void callEntityListeners( String entity ) {
        synchronized( _entityListeners ) {
            ArrayList< EntityListener > al = _entityListeners.get( entity );
            if( al == null ) {
                al = new ArrayList< EntityListener >();
                _entityListeners.put( entity, al );
            }

            for( EntityListener listener : al ) {
                listener.onEntityUpdated( entity );
            }

            al.clear(); // remove references, it doesn't need calling twice.
        }
    }

}
