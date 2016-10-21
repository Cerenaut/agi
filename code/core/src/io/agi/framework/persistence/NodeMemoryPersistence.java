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

package io.agi.framework.persistence;

import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Persistence in-memory in a single Node.
 *
 * A super fast in-memory persistence implementation, that isn't actually persisted - it assumes you're running all the
 * entities on a single node as they're not shared with other nodes. However, in that situation it is very fast. You
 * also need to export the data to make a permanent copy of it.
 *
 * Created by dave on 1/05/16.
 */
public class NodeMemoryPersistence implements Persistence {

    protected HashMap< String, ModelNode > _nodeMap = new HashMap< String, ModelNode >();
    protected HashMap< String, ModelEntity > _entityMap = new HashMap< String, ModelEntity >();
    protected HashMap< String, ModelData > _dataMap = new HashMap< String, ModelData >();

    private static final Logger logger = LogManager.getLogger();

    public NodeMemoryPersistence() {
    }

    // Nodes
    public Collection< ModelNode > fetchNodes() {
        return _nodeMap.values();
    }

    public void persistNode( ModelNode e ) {
        _nodeMap.put( e._name, e );
    }

    public ModelNode fetchNode( String nodeName ) {
        return _nodeMap.get( nodeName );
    }

    public void removeNode( String nodeName ) {
        _nodeMap.remove( nodeName );
    }

    // Entities
    public Collection< ModelEntity > getEntities() {
        return _entityMap.values();
    }

    public Collection< String > getChildEntities( String parent ) {
        ArrayList< String > children = new ArrayList< String >();

        for( String name : _entityMap.keySet() ) {
            ModelEntity modelEntity = _entityMap.get( name );
            String parentName = modelEntity.parent;
            if( parentName != null ) {
                if( parentName.equals( parent ) ) {
                    children.add( modelEntity.name );
                }
            }
        }

        return children;
    }

    public void persistEntity( ModelEntity e ) {
        _entityMap.put( e.name, e );
    }

    public ModelEntity fetchEntity( String name ) {
        return _entityMap.get( name );
    }

    public void removeEntity( String key ) {
        _entityMap.remove( key );
    }

    // Data
    public void persistData( ModelData modelData ) {
        _dataMap.put( modelData.name, modelData );
    }

    public Collection< ModelData > getDataMeta( String filter ) {
        ArrayList< ModelData > al = new ArrayList< ModelData >();

        for( String key : _dataMap.keySet() ) {
            if( key.indexOf( filter ) >= 0 ) {
                ModelData md = _dataMap.get( key );
                ModelData md2 = new ModelData( md.name, md.refKeys, md.sizes, null ); // sans actual data
                al.add( md2 );
            }
        }

        return al;
    }

    public Collection< String > getData() {
        ArrayList< String > names = new ArrayList< String >();
        names.addAll( _dataMap.keySet() );
        return names;
    }

    public ModelData fetchData( String key ) {
        return _dataMap.get( key );
    }

    public void removeData( String key ) {
        _dataMap.remove( key );
    }

}
