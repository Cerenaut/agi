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

import io.agi.core.alg.QuiltedCompetitiveLearning;
import io.agi.core.alg.QuiltedCompetitiveLearningConfig;
import io.agi.core.ann.reinforcement.QLearning;
import io.agi.core.ann.reinforcement.QLearningConfig;
import io.agi.core.ann.unsupervised.BinaryTreeQuiltConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Created by dave on 23/10/16.
 */
public class QLearningEntity extends Entity {

    public static final String ENTITY_TYPE = "q-learning";

    public static final String INPUT_REWARD = "input-reward";
    public static final String INPUT_STATES  = "input-states";
    public static final String INPUT_ACTIONS = "input-actions";

    public static final String OUTPUT_ACTION_QUALITY = "output-action-quality";

    public static final String STATES_OLD   = "states-old";
    public static final String ACTIONS_OLD  = "actions-old";
    public static final String STATE_ACTION_QUALITY = "state-action-quality";

    public QLearningEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_REWARD );
        attributes.add( INPUT_STATES );
        attributes.add( INPUT_ACTIONS );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( STATES_OLD );
        flags.putFlag( STATES_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( STATES_OLD, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( ACTIONS_OLD );
        flags.putFlag( ACTIONS_OLD, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( ACTIONS_OLD, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( STATE_ACTION_QUALITY );
        flags.putFlag( STATE_ACTION_QUALITY, DataFlags.FLAG_NODE_CACHE );

        attributes.add( OUTPUT_ACTION_QUALITY );
        flags.putFlag( OUTPUT_ACTION_QUALITY, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return QLearningEntityConfig.class;
    }

    protected void doUpdateSelf() {

        QLearningEntityConfig config = (QLearningEntityConfig) _config;

        // Do nothing unless the input is defined
        Data inputR = getData( INPUT_REWARD );
        Data inputS = getData( INPUT_STATES );
        Data inputA = getData( INPUT_ACTIONS );

        if( ( inputR == null ) || ( inputS == null ) || ( inputA == null ) ) {
            Data actionQuality = new Data( config.actions );

            setData( OUTPUT_ACTION_QUALITY, actionQuality );

            if( config.reset ) {
                config.resetDelayed = true;
            }

            return; // can't update yet.
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
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

        QLearning ql = new QLearning( entityName, om );
        ql.setup( qLearningConfig );

        // Load data, overwriting the default setup.
        ql._stateNew = inputS;
        ql._actionOld = inputA;
        ql._rewardNew = inputR;

        copyDataFromPersistence( ql );

        // Update the classification (forward) output, including optional reset and learning on/off switch
        if( config.reset || config.resetDelayed ) {
            ql.reset();
            config.resetDelayed = false;
        }

        ql._c.setLearn( config.learn );
        ql.update();

        // Save data
        copyDataToPersistence( ql );

        // Save config changes - eg stats about the entity
    }

    protected void copyDataFromPersistence( QLearning ql ) {

        int S = ql._c.getNbrStates();
        int A = ql._c.getNbrActions();

        DataSize dataSizeSA = DataSize.create( S, A );
        DataSize dataSizeS = DataSize.create( S );
        DataSize dataSizeA = DataSize.create( A );

        ql._expectedRewards = getDataLazyResize( STATE_ACTION_QUALITY, dataSizeSA );
        ql._actionQuality = getDataLazyResize( OUTPUT_ACTION_QUALITY, dataSizeA );
        ql._stateOld = getDataLazyResize( OUTPUT_ACTION_QUALITY, dataSizeS );
    }

    protected void copyDataToPersistence( QLearning ql ) {
        setData( STATE_ACTION_QUALITY, ql._expectedRewards );
        setData( OUTPUT_ACTION_QUALITY, ql._actionQuality );
        setData( OUTPUT_ACTION_QUALITY, ql._stateOld );
    }
}
