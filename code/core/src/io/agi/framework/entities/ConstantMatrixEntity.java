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

package io.agi.framework.entities;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Produces a constant output.
 * <p/>
 * Useful for creating stub data for connecting to entities that require some form of input to be provided.
 * <p/>
 * Created by dave on 26/03/16.
 */
public class ConstantMatrixEntity extends Entity {

    public static final String ENTITY_TYPE = "constant-matrix";

    public static final String OUTPUT = "output";

    public ConstantMatrixEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return ConstantMatrixEntityConfig.class;
    }

    protected void doUpdateSelf() {

        ConstantMatrixEntityConfig config = ( ConstantMatrixEntityConfig ) _config;

        // Get all the parameters:
        Data output = getDataLazyResize( OUTPUT, DataSize.create( config.width, config.height ) );
        output.set( config.value );

        setData( OUTPUT, output );
    }
}
