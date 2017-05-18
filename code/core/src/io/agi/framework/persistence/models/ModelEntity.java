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

package io.agi.framework.persistence.models;

import io.agi.framework.Entity;

/**
 * Created by dave on 17/02/16.
 */
public class ModelEntity {

    public String name;
    public String type;
    public String node;
    public String parent;
    public String config;

    public ModelEntity( String name, String type, String node, String parent, String config ) {
        this.name = name;
        this.type = type;
        this.node = node;
        this.parent = parent;
        this.config = config;
    }

    public ModelEntity( Entity e ) {
        name = e.getName();
        type = e.getType();
        node = e.getNode().getName();
        parent = e.getParent();
    }

}
