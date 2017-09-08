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

import java.util.Collection;

/**
 * Created by dave on 14/02/16.
 */
public interface Persistence {

//    Entity --< Data
//    Entity --- name, type, children, node, config (multiple properties)
//               Could be JSON string for entities: { name: xxx, type: yyy } etc

    // Nodes
    Collection< ModelNode > fetchNodes(); // list all

    void persistNode( ModelNode m ); /// creates if nonexistent (upsert)

    ModelNode fetchNode( String nodeName ); /// retrieves if exists, or null

    void removeNode( String nodeName ); /// removes if exists

    // Entities
    Collection< ModelEntity > getEntities(); // list all

    Collection< String > getChildEntities( String parent );

    void persistEntity( ModelEntity m );

    ModelEntity fetchEntity( String name );

    void removeEntity( String name );

    // Data
    Collection< String > getData(); // list all, note, only obtains keys, as the volume of data would be too large to fetch all.

    Collection< ModelData > getDataMeta( String filter ); // return matching data, but only the meta properties.

    void persistData( ModelData modelData );

    ModelData fetchData( String name );

    void removeData( String name );

}