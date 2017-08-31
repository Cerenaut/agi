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

import io.agi.core.ann.reinforcement.*;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 23/10/16.
 */
public class QLearningEntity extends Entity {

    public static final String ENTITY_TYPE = "q-learning";

    public static final String INPUT_REWARD = "input-reward";
    public static final String INPUT_STATES_NEW  = "input-states-new";
    public static final String INPUT_ACTIONS_NEW = "input-actions-new";

    public static final String OUTPUT_STATES_OLD   = "output-states-old";
    public static final String OUTPUT_ACTIONS_OLD  = "output-actions-old";
    public static final String OUTPUT_STATES_NEW   = "output-states-new";
    public static final String OUTPUT_ACTIONS_NEW  = "output-actions-new";
    public static final String OUTPUT_ACTIONS_QUALITY = "output-actions-quality";
    public static final String OUTPUT_STATES_ACTIONS_QUALITY = "output-states-actions-quality";

    public QLearningEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_REWARD );
        attributes.add( INPUT_STATES_NEW );
        attributes.add( INPUT_ACTIONS_NEW );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( OUTPUT_STATES_OLD );
        flags.putFlag( OUTPUT_STATES_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_STATES_OLD, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_ACTIONS_OLD );
        flags.putFlag( OUTPUT_ACTIONS_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_ACTIONS_OLD, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_STATES_NEW );
        flags.putFlag( OUTPUT_STATES_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_STATES_NEW, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_ACTIONS_NEW );
        flags.putFlag( OUTPUT_ACTIONS_NEW, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_ACTIONS_NEW, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_ACTIONS_QUALITY );
        flags.putFlag( OUTPUT_ACTIONS_QUALITY, DataFlags.FLAG_NODE_CACHE );

        attributes.add( OUTPUT_STATES_ACTIONS_QUALITY );
        flags.putFlag( OUTPUT_STATES_ACTIONS_QUALITY, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return QLearningEntityConfig.class;
    }

    protected void doUpdateSelf() {

        QLearningEntityConfig config = ( QLearningEntityConfig ) _config;

        // Do nothing unless the input is defined
        Data inputR = getData( INPUT_REWARD );
        Data inputS = getData( INPUT_STATES_NEW );
        Data inputA = getData( INPUT_ACTIONS_NEW );

        if( ( inputR == null ) || ( inputS == null ) || ( inputA == null ) ) {
            Data actionQuality = new Data( config.actions );
            setData( OUTPUT_ACTIONS_QUALITY, actionQuality );

            if( config.reset ) {
                config.resetDelayed = true;
            }

            return; // can't update yet.
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Instantiate Algorithm
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        String entityName = getName();
        ObjectMap om = ObjectMap.GetInstance();
        QLearningConfig qLearningConfig = new QLearningConfig();

        qLearningConfig.setup(
                om, entityName, _r,
                config.states,
                config.actions,
                config.learningRate,
                config.discountRate );

        QLearning ql = new QLearning();
        ql.setup( qLearningConfig, null, null, null );

        copyDataFromPersistence( ql );

        if( config.reset || config.resetDelayed ) {
            ql.reset();
            config.resetDelayed = false;
        }

        ql._c.setLearn( config.learn );
        float rewardValue = inputR._values[ 0 ];
        ql.update( inputS, rewardValue, inputA );

        copyDataToPersistence( ql );

        // TODO: Save config changes - eg stats about the entity
    }

    protected void copyDataFromPersistence( QLearning ql ) {

        int S = ql._c.getNbrStates();
        int A = ql._c.getNbrActions();

        DataSize dataSizeSA = DataSize.create( A, S );
        DataSize dataSizeS = DataSize.create( S );
        DataSize dataSizeA = DataSize.create( A );

        ql._stateNew = getDataLazyResize( OUTPUT_STATES_NEW, dataSizeS );
        ql._stateOld = getDataLazyResize( OUTPUT_STATES_OLD, dataSizeS );
        ql._actionNew = getDataLazyResize( OUTPUT_ACTIONS_NEW, dataSizeA );
        ql._actionOld = getDataLazyResize( OUTPUT_ACTIONS_OLD, dataSizeA );
        ql._actionQuality = getDataLazyResize( OUTPUT_ACTIONS_QUALITY, dataSizeA );
        ql._quality = getDataLazyResize( OUTPUT_STATES_ACTIONS_QUALITY, dataSizeSA );
    }

    protected void copyDataToPersistence( QLearning ql ) {
        setData( OUTPUT_STATES_NEW, ql._stateNew );
        setData( OUTPUT_STATES_OLD, ql._stateOld );
        setData( OUTPUT_ACTIONS_NEW, ql._actionNew );
        setData( OUTPUT_ACTIONS_OLD, ql._actionOld );
        setData( OUTPUT_ACTIONS_QUALITY, ql._actionQuality );
        setData( OUTPUT_STATES_ACTIONS_QUALITY, ql._quality );
    }

}
