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

package io.agi.framework.references;

import io.agi.core.orm.Keys;
import io.agi.framework.Node;
import io.agi.framework.coordination.Coordination;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.references.DataRef;

import java.util.*;

/**
 * An in-memory cache of data structures, using their unique keys.
 * Thread safe.
 * <p/>
 * Created by dave on 28/03/16.
 */
public class DataRefMap {

    protected Node _n;
    protected HashMap< String, DataRef > _cache = new HashMap< String, DataRef >();
//    protected HashMap< String, HashSet< String > > _cachedReferences = new HashMap< String, HashSet< String > >();
    protected HashMap< String, String > _dirtyKeyNodes = new HashMap< String, String >();

    public DataRefMap() {

    }

    public void setNode( Node n ) {
        _n = n;
    }

    /**
     * This notifies the Node that a Data is out of date.
     * @param key
     */
    protected void notifySetData( String key ) {
        Coordination c = _n.getCoordination();
        _n.notifySetData( key );
        //c.onSetData( key );
    }

    /**
     * Overload for convenience.
     *
     * @param keys
     */
    protected void notifySetData( Collection< String > keys ) {
        for( String key : keys ) {
            notifySetData( key );
        }
    }

    /**
     * Notification that a remote Node has updated a Data.
     * @param key
     * @param node
     */
    public void onSetData( String key, String node ) {
        // invalidate local copy, store who has most recent copy
        synchronized( _cache ) {
            if( node.equals( _n.getName() ) ) {
                _dirtyKeyNodes.remove( key ); // not dirty if this node
            }
            else {
                _dirtyKeyNodes.put( key, node );
            }
        }
    }

    /**
     * Receive a Data from remote Node.
     *
     * @param key
     * @param node
     * @param modelData
     */
    public void onGetData( String key, String node, ModelData modelData ) {
        DataRef dataRef = modelData.deserialize();
        synchronized( _cache ) {
            onGetDataUpdateUnsafe( key, node, dataRef );
        }
    }


    public void onGetDataUpdateUnsafe( String key, String node, DataRef dataRef ) {

        if( dataRef != null ) {
            _cache.put( key, dataRef );
        }
        else{
            _cache.remove( key );
        }

        String cleanNode = _dirtyKeyNodes.get( key );
        if( cleanNode != null ) {
            if( cleanNode.equals( node ) ) {
                _dirtyKeyNodes.remove( key ); // no longer dirty
            }
        }

        // No notification, we got this from another Node so there was already a notification.
    }

//    /**
//     * Uses the entity-name prefix of a data to determine which Node has the data in its cache.
//     *
//     * @param key
//     * @return
//     */
//    public String getDataNode( String key ) {
//        Persistence p = _n.getPersistence();
//        Collection< ModelEntity > entities = p.getEntities(); // list all
//        for( ModelEntity entity : entities ) {
//            String prefix = Keys.concatenate( entity.name, "" );
//            if( key.indexOf( prefix ) == 0 ) {
//                return entity.node;
//            }
//        }
//        return null; // no node found
//    }
//
//    public boolean isDataLocal( String key ) {
//        String dataNodeName = getDataNode( key );
//        if( dataNodeName == null ) {
//            return false;
//        }
//
//        String localNodeName = _n.getName();
//
//        if( dataNodeName.equals( localNodeName ) ) {
//            return true;
//        }
//
//        return false;
//    }
//
//    public boolean hasRefs( String key ) {
//        synchronized( _cache ) {
//            HashSet< String > refs = _cachedReferences.get( key );
//            if( refs == null ) {
//                return false;
//            }
//
//            if( refs.isEmpty() ) {
//                return false;
//            }
//            return true;
//        }
//    }

//    /**
//     * Load the data map from the persistence layer.
//     * Note, this loads EVERYTHING in the shared Persistence into the local Node data cache.
//     * This includes Data from other Nodes. The most recently persisted copy is loaded.
//     */
//    public void fetchData() {
//        Persistence p = _n.getPersistence();
//        Collection< String > keys = p.getDataKeys();
//        for( String key : keys ) {
//            fetchData( key );
//        }
//    }
//
//    /**
//     * This fetches a specific Data from the Persistence layer.
//     * @param key
//     * @return
//     */
//    public DataRef fetchData( String key ) {
//        Persistence p = _n.getPersistence();
//        ModelData md = p.getData( key );
//        if( md == null ) return null;
//        return setData( md ); // when you set this, it will serialize, so we want a copy.
//    }
//
//    /**
//     * Persist the data map into the persistence layer
//     */
//    public void persistData() {
//        // TODO here I could track for unchanged data..
//        Collection< String > keys = getDataKeys();
//        for( String key : keys ) {
//            if( isDataLocal( key ) ) {
//                persistData( key );
//            }
//            else {
//                // don't persist, other node will
//            }
//        }
//    }

//    /**
//     * Persist item from the cache
//     * @param key
//     */
//    public void persistData( String key ) {
//        DataRef dataRef = getData( key );
//        ModelData md = new ModelData();
//        md.serialize( dataRef );
//        persistData( md );
//    }
//
//    /**
//     * Persist a serialized Data
//     * @param md
//     */
//    public void persistData( ModelData md ) {
//        Persistence p = _n.getPersistence();
//        p.persistData( md );
//    }

    /**
     * Get all the Data keys in the cache.
     * @return
     */
    public Collection< String > getDataKeys() {
        HashSet< String > keys = new HashSet< String >();
        synchronized( _cache ) {
            Set< String > keySet = _cache.keySet();
            keys.addAll( keySet );
        }
        return keys;
    }

    /**
     * Return meta-data of all matching data in the cache.
     *
     * @param filter
     * @return
     */
    public Collection< ModelData > getDataMeta( String filter ) {
        ArrayList< ModelData > meta = new ArrayList< ModelData >();
        synchronized( _cache ) {
            Set< String > keySet = _cache.keySet();
            for( String key : keySet ) {
                if( filter.isEmpty() || ( key.indexOf( filter ) < 0 ) ) {
                    continue; // not to include
                }

                DataRef dataRef = _cache.get( key );
                ModelData modelData = new ModelData();
                modelData.serializeMeta( dataRef );
                meta.add( modelData );
            }
        }

        return meta;
    }

    public boolean hasData( String name ) {
        synchronized( _cache ) {
            return _cache.keySet().contains( name );
        }
    }

    public DataRef getData( String name ) {
        synchronized( _cache ) {
            String cleanNode = _dirtyKeyNodes.get( name );
            if( cleanNode != null ) {
                // perform a synchronous fetch
                ModelData modelData = _n.doGetData( name, cleanNode );
                DataRef d = modelData.deserialize();
                onGetDataUpdateUnsafe( name, cleanNode, d );
                return d;
            }
            else {
                DataRef d = _cache.get( name );
                return d;
            }
        }
    }

    public DataRef setData( ModelData md ) {
        DataRef dataRef = md.deserialize();
        if( dataRef != null ) {
            setData( md.name, dataRef );
        }
        return dataRef;
    }

    public void setData( String name, DataRef d ) {
        synchronized( _cache ) {
            _cache.put( name, d );
        }
        notifySetData( name );
    }

    /**
     * Clear the local data map entirely
     */
    public void removeData() {
        Collection< String > keys = getDataKeys();
        synchronized( _cache ) {
            _cache.clear();
        }
        notifySetData( keys );
    }

    public void removeData( String name ) {
        synchronized( _cache ) {
            _cache.remove( name );
        }
        notifySetData( name );
    }



/*    public void setData( String key, DataRef d ) {
        if( isDataLocal( key ) ) {
            _dataCache.setData( key, d );
        }
        else {
            // TODO: This should set in the map on the correct node?
            _dataCache.setData( key, d );
        }
    }

    public void clearData( String key ) {
        _dataCache.removeData( key );
        if( isDataLocal( key ) ) {
        }
        else {
            // TODO: This should set in the map on the correct node?
            _dataCache.removeData( key );
        }
    }

    public DataRef getData( String key ) {
        if( isDataLocal( key ) ) {
            return _dataCache.getData( key );
        }
        else {
            // TODO: This should search other nodes, if not present locally due to key.
            return null;
        }
    }*/

}
