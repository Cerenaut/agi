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

package io.agi.framework.entities.reinforcement_learning;

import io.agi.core.ann.reinforcement.EpsilonGreedyQLearningPolicy;
import io.agi.core.ann.reinforcement.GatedRecurrentMemory;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 5/06/17.
 */
public class GatedRecurrentMemoryEntity extends Entity {

    public static final String ENTITY_TYPE = "gated-recurrent-memory";

    public static final String INPUT_CONTENT  = "input-content";
    public static final String INPUT_GATES_WRITE  = "input-gates-write";
    public static final String INPUT_GATES_CLEAR  = "input-gates-clear";

    public static final String OUTPUT_STORED = "output-stored";
    public static final String OUTPUT_CONTENT = "output-content";

    public GatedRecurrentMemoryEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_CONTENT );
        attributes.add( INPUT_GATES_WRITE );
        attributes.add( INPUT_GATES_CLEAR );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_STORED );
        flags.putFlag( OUTPUT_STORED, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_STORED, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_CONTENT );
        flags.putFlag( OUTPUT_CONTENT, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_CONTENT, DataFlags.FLAG_SPARSE_BINARY );
    }

    @Override
    public Class getConfigClass() {
        return GatedRecurrentMemoryEntityConfig.class;
    }

    protected void doUpdateSelf() {

        GatedRecurrentMemoryEntityConfig config = ( GatedRecurrentMemoryEntityConfig ) _config;

        // Do nothing unless the input is defined
        Data inputContent = getData( INPUT_CONTENT );
        Data inputGatesWrite = getData( INPUT_GATES_WRITE );
        Data inputGatesClear = getData( INPUT_GATES_CLEAR );

        int inputContentSize = inputContent.getSize();
        int inputGatesWriteSize = inputGatesWrite.getSize();
        int inputGatesClearSize = inputGatesClear.getSize();

        int maxSize = Math.max( inputContentSize, inputGatesWriteSize );
        maxSize = Math.max( maxSize, inputGatesClearSize );

        // check whether they have all become consistent, which is necessary for the thing to function
        DataSize memorySize = DataSize.create( maxSize );

        float sizeTest = (float)( inputContentSize + inputGatesWriteSize + inputGatesClearSize ) / 3.0f;
        if( sizeTest != (float)maxSize ) {
            Data stored = new Data( memorySize );
            Data output = new Data( memorySize );
            setData( OUTPUT_STORED, stored );
            setData( OUTPUT_CONTENT, output );
            return;
        }

        // OK so to be here, we have to have valid input dimensions
        GatedRecurrentMemory grm = new GatedRecurrentMemory();
        grm.setup( maxSize );

        Data inputStored = getDataLazyResize( OUTPUT_STORED, memorySize );

        grm._input.copy( inputContent );

        if( inputStored != null ) {
            grm._stored.copy( inputStored );
        }

        grm._gateWrite.copy( inputGatesWrite );
        grm._gateClear.copy( inputGatesClear );

        grm.update();

        setData( OUTPUT_STORED, grm._stored );
        setData( OUTPUT_CONTENT, grm._output );
    }

}
