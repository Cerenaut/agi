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

import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.coordination.Coordination;
import io.agi.framework.persistence.DataModelData;
import io.agi.framework.persistence.DataDeserializer;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelNode;
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

    protected HashMap< String, ArrayList< EntityListener > > _entityListeners = new HashMap< String, ArrayList< EntityListener > >();
    protected DataMap _dataCache = new DataMap();

    static protected Node _node = null;

    private Node() {
    }

    /**
     * Stops the Node. TODO: Make it check nothing is happening (e.g. no pending updates to complete?)
     *
     */
    public void stop() {
        logger.info( "Stopping process on request." );
        System.exit(0);
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

        ModelNode jn = new ModelNode( _name, _host, _port );
        _p.persistNode(jn);
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

//
//    public ModelData fetchData( String key ) {
//            Data d = _dataCache._cache.get( key );
//            if( d == null ) {//!_dataCache._cache.keySet().contains( key ) ) {
//                return _p.fetchData( key );
//            }
//
//            // ok it's in the cache. Persist it and return it from persistence
//            String encoding = ModelData.ENCODING_DENSE;
//            ModelData modelData = new ModelData( key, d, encoding ); // converts to json
//            return modelData;
//        }
//    }

//    public void setDataCache( DataModelData dmd ) {
//        _dataCache.setData( dmd._md.name, dmd );
//    }
//
    public void clearDataCache( String name ) {
        _dataCache.removeData( name );
    }

    public void flushDataCache( String name ) {
        // TODO
    }

    /**
     * Returns all names, whether in cache or persistence.
     *
     * @return
     */
    public Collection< String > getDataNames() {
        HashSet< String > keys = new HashSet< String >();

        // lock the cache
        synchronized ( _dataCache._cache ) {

            // get everything from persistence
            Collection< String > c = _p.getData();
            for( String key : c ) {
                keys.add(key);
            }

            // replace anything stale in persistence
            Set< String > keySet = _dataCache._cache.keySet();
            for( String key : keySet ) {
                if( !keys.contains( key ) ) {
                    keys.add( key );
                }
            }
        }

        ArrayList< String > al = new ArrayList< String >();
        al.addAll(keys);
        return al;
    }

    public Collection< ModelData > getDataMeta( String filter ) {

        HashMap< String, ModelData > keyModelData = new HashMap< String, ModelData >();

        // lock the cache
        synchronized ( _dataCache._cache ) {

            // get everything from persistence
            Collection< ModelData > c = _p.getDataMeta( filter );
            for( ModelData md : c ) {
                keyModelData.put( md.name, md );
            }

            // replace anything stale in persistence
            Set< String > keySet = _dataCache._cache.keySet();

            for( String key : keySet ) {

                if( key.indexOf( filter ) >= 0 ) {
                    DataModelData dmd = _dataCache._cache.get( key );

                    ModelData mdMeta = keyModelData.get( key );

                    if( mdMeta == null ) {
                        mdMeta = new ModelData();
                    }

//                    mdMeta.name = dmd.name; // replace ref keys
                    mdMeta.refKeys = dmd._refKeys; // replace ref keys

                    // now figure out the sizes..
                    if( dmd._md != null ) {
                        // compute size by coping from serialized size
                        mdMeta.sizes = dmd._md.sizes; // replace sizes
                    }
                    else if( dmd._d != null ) {
                        // compute size by serializing size
                        if( dmd._d._dataSize != null ) {
                            mdMeta.sizes = ModelData.DataSizeToString( dmd._d._dataSize );
                        }
                    }

                    keyModelData.put( mdMeta.name, mdMeta );

//                    String encoding = ModelData.ENCODING_DENSE;
//                    ModelData md = new ModelData( key, dmd., encoding ); // converts to json
//                    ModelData md2 = new ModelData( md.name, md.refKeys, md.sizes, null ); // sans actual data
//                    keyModelData.put(md2.name, md2);
                }
            }

        }

        ArrayList< ModelData > al = new ArrayList< ModelData >();
        al.addAll( keyModelData.values() );
        return al;
    }

    public ModelData getModelData( String name, DataDeserializer deserializer ) {

        // Look in cache first, then persistence
        synchronized ( _dataCache._cache ) {
            DataModelData dmd = _dataCache._cache.get( name );

            if( dmd == null ) {
                dmd = new DataModelData();
            }
            else { // existing one - it is cached
                if( dmd.hasReferences() ) {
                    // never cache
                }
                else if( dmd._md != null ) { // already serialized
                    return dmd._md;
                }
                else if( dmd._d != null ) { // in cache, but not serialized.
                    String encoding = deserializer.getEncoding( name );
                    dmd._md = new ModelData( name, dmd._d, encoding ); // serialize it
                    dmd._encoding = encoding;
                    dmd._md.refKeys = dmd._refKeys;
                    return dmd._md;
                }
            }

            // not found: Fetch it.
            ModelData md = _p.fetchData( name );

            dmd._md = md; // serialized form.
            dmd._encoding = null;
            if( md != null ) {
                dmd._refKeys = md.refKeys;
            }
            dmd._d = null; // don't deserialize unless necessary       //dmd._md.getData( this, deserializer );

            _dataCache._cache.put( name, dmd );

            return dmd._md;
        }
    }

    public Data getData( String name, DataDeserializer deserializer ) {

        // Look in cache first, then persistence
        synchronized ( _dataCache._cache ) {
            DataModelData dmd = _dataCache._cache.get( name );

            if( dmd == null ) {
                dmd = new DataModelData();
            }
            else { // existing one
                if( dmd.hasReferences() ) {
                    // never cache
                }
                else if( dmd._d != null ) {
                    return dmd._d;
                }
                else if( dmd._md != null ) {
                    dmd._d = dmd._md.getData( this, deserializer ); // serialize on demand
                    return dmd._d;
                }
            }

            // not found: Fetch it.
            ModelData md = _p.fetchData( name );

            dmd._md = md;
            dmd._encoding = null;
            if( md != null ) {
                dmd._refKeys = md.refKeys;
                dmd._d = dmd._md.getData( this, deserializer ); // serialize on demand
            }

            _dataCache._cache.put( name, dmd );

            return dmd._d; // may be null, if md was null and nothing in cache.
        }
    }

    public void setDataCache( String name, Data d, String encoding ) {
        synchronized ( _dataCache._cache ) {
            DataModelData dmd = _dataCache._cache.get( name );

            if( dmd == null ) {
                dmd = new DataModelData();
            }
            else { // existing one
            }

            // refkeys unchanged or unknown
            dmd._encoding = encoding;
            dmd._d = d;
            dmd._md = null; // invalidated

            _dataCache._cache.put( name, dmd );
        }

        // not serialized yet
    }

    public void setDataPersist( String name, Data d, String encoding ) {
        // need to remember the extra properties.
        // Look for a model data in the cache
        ModelData md = new ModelData( name, d, encoding ); // serialize this data

        synchronized ( _dataCache._cache ) {

            DataModelData dmd = _dataCache._cache.get( name );

            if( dmd == null ) {
                dmd = new DataModelData();
            }
            else { // existing one
                if( dmd._refKeys != null ) {
                    md.refKeys = dmd._refKeys;
                }
            }

            // refkeys unchanged or unknown
            dmd._encoding = encoding;
            dmd._d = d; // store updated data
            dmd._md = md; // replace previous

            _dataCache._cache.put( name, dmd );
        }

        _p.persistData( md ); // overwrite
    }

    /**
     * Since this is the superset of data, it overrides whatever else is there.
     * @param md
     */
    public void setModelDataPersist( ModelData md ) {
        clearDataCache( md.name ); // may be stale
        _p.persistData( md ); // overwrite
    }

    /**
     * A callback that is called when an Entity has been updated, including all its children.
     *
     * @param entityName
     */
    public void notifyUpdated( String entityName ) {
//        int count = _p.getEntityAge(entityName);
//        count += 1;
//        _p.setEntityAge(entityName, count);
        logger.info( " %%Unlock%% " + entityName );
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

        ModelEntity modelEntity = _p.fetchEntity( entityName );

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

                logger.info( " %%Lock%% " + entityName );

                ModelEntity modelEntity = _p.fetchEntity( entityName ); // NOTE: Can't get the model entity UNTIL I have the lock, or the model might be out of date.

                Entity e = _ef.create( _om, modelEntity );

                e.update();
            }
        } );
        t.start();
    }

    public boolean lock( String entityName ) {
        Semaphore s = getLock( entityName );

        logger.info( "Thread " + Thread.currentThread().hashCode() + " waiting for " + entityName );
        try {
            s.acquire();
        }
        catch( InterruptedException ie ) {
            logger.info( "Thread " + Thread.currentThread().hashCode() + " cant get lock for " + entityName );
            return false;
        }

        logger.info( "Thread " + Thread.currentThread().hashCode() + " has lock for " + entityName );

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
