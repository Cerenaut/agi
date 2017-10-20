/*
 * Copyright (c) 2017.
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
import io.agi.core.orm.ObjectMap;
import io.agi.core.sdr.SparseDistributedEncoder;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.EntityConfig;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by gideon on 26/03/2016.
 */
public class DelayEntity extends Entity {

    public static final String ENTITY_TYPE = "delay";

    // data
    public static final String DATA_INPUT = "input";
    public static final String DATA_INPUT_OLD = "input-old";
    public static final String DATA_OUTPUT = "output";

    public DelayEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( DATA_INPUT_OLD );
        attributes.add( DATA_OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return EntityConfig.class;
    }

    public void doUpdateSelf() {

        Data input = getData( DATA_INPUT );
        if( input == null ) {
            return;
        }

        Data inputOld = getData( DATA_INPUT_OLD ); // previous input

        if( inputOld != null ) {
            Data output = new Data( inputOld );
            setData( DATA_OUTPUT, output ); // emit previous input
            inputOld.copy( input );
        }
        else {
            inputOld = new Data( input );
        }

        setData( DATA_INPUT_OLD, inputOld ); // store new input for next time
    }

}