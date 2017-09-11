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
import io.agi.framework.persistence.models.ModelEntity;

/**
 * Created by dave on 14/02/16.
 */
public interface EntityFactory {

    /**
     * The node the Entities will run on. This is useful so you can pass the Node as the single point of access to all
     * other objects.
     *
     * @param n
     */
    void setNode( Node n );

    /**
     * Create an Entity on demand. Entities are created every time they are updated.
     * The factory does not create the config object in the entity (expressed as a string in the model).
     *
     * @param om
     * @param me
     * @return
     */
    Entity create( ObjectMap om, ModelEntity me ); //String entityName, String entityType );

}
