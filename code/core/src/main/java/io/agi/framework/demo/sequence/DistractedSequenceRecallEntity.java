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

package io.agi.framework.demo.sequence;

import io.agi.core.ann.reinforcement.DistractedSequenceRecallProblem;
import io.agi.core.ann.reinforcement.QLearning;
import io.agi.core.ann.reinforcement.QLearningConfig;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.ShuffledIndex;
import io.agi.core.orm.ObjectMap;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceImageFile;
import io.agi.core.util.images.ImageScreenScraper;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.demo.mnist.ImageLabelEntityConfig;
import io.agi.framework.entities.ImageSensorEntity;
import io.agi.framework.entities.reinforcement_learning.QLearningEntityConfig;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 19/05/17.
 */
public class DistractedSequenceRecallEntity extends Entity {

    public static final String ENTITY_TYPE = "distracted-sequence-recall";

    public static final String INPUT_ACTIONS = "input-actions";
    public static final String OUTPUT_SEQUENCE_STATES = "output-sequence-states";
    public static final String OUTPUT_SEQUENCE_ACTIONS = "output-sequence-actions";
    public static final String OUTPUT_STATES = "output-states";
    public static final String OUTPUT_ACTIONS_IDEAL = "output-actions-ideal";
    public static final String OUTPUT_REWARD = "output-reward";

    public DistractedSequenceRecallEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_ACTIONS );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( OUTPUT_SEQUENCE_STATES );
        flags.putFlag( OUTPUT_SEQUENCE_STATES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_SEQUENCE_STATES, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_SEQUENCE_ACTIONS );
        flags.putFlag( OUTPUT_SEQUENCE_ACTIONS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_SEQUENCE_ACTIONS, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_STATES );
        flags.putFlag( OUTPUT_STATES, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_STATES, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_ACTIONS_IDEAL );
        flags.putFlag( OUTPUT_ACTIONS_IDEAL, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_ACTIONS_IDEAL, DataFlags.FLAG_SPARSE_BINARY );

        attributes.add( OUTPUT_REWARD );
        flags.putFlag( OUTPUT_REWARD, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return DistractedSequenceRecallEntityConfig.class;
    }

    public void doUpdateSelf() {

        DistractedSequenceRecallEntityConfig config = ( DistractedSequenceRecallEntityConfig ) _config;

        DistractedSequenceRecallProblem p = new DistractedSequenceRecallProblem();
        p.setup( _r, config.sequenceLength, config.targets, config.prompts, config.distractors );
        p._sequence = config.sequence;
        p._epoch = config.epoch;

        // default input
        Data inputA = getData( INPUT_ACTIONS );
        if( inputA == null ) {
            inputA = new Data( p.getNbrActions() );
            inputA._values[ 0 ] = 1f;
        }
        p._actions = inputA;

        // load state
        copyDataFromPersistence( p );

        if( config.reset ) {
            p.reset();
            p._epoch = 0;
        }

        // update
        p.update();

        // save state
        copyDataToPersistence( p );

        config.reward = p._reward;
        config.sequence = p._sequence;
        config.sequenceLength = p._sequenceLength;
        config.epoch = p._epoch;
    }

    protected void copyDataFromPersistence( DistractedSequenceRecallProblem p ) {

        int S = p.getNbrObservations();
        int A = p.getNbrActions();
        int L = p.getSequenceLength();

        DataSize dataSizeSL = DataSize.create( S, L );
        DataSize dataSizeAL = DataSize.create( A, L );
        DataSize dataSizeS = DataSize.create( S );
        DataSize dataSizeA = DataSize.create( A );

        p._sequenceState = getDataLazyResize( OUTPUT_SEQUENCE_STATES, dataSizeSL );
        p._sequenceActions = getDataLazyResize( OUTPUT_SEQUENCE_ACTIONS, dataSizeAL );
        p._state = getDataLazyResize( OUTPUT_STATES, dataSizeS );
        //p._actions = getDataLazyResize( OUTPUT_STATES_OLD, dataSize );
        p._idealActions = getDataLazyResize( OUTPUT_ACTIONS_IDEAL, dataSizeA );
    }

    protected void copyDataToPersistence( DistractedSequenceRecallProblem p ) {

        Data rewardData = new Data( 1 );
        rewardData._values[ 0 ] = p._reward;

        setData( OUTPUT_SEQUENCE_STATES, p._sequenceState );
        setData( OUTPUT_SEQUENCE_ACTIONS, p._sequenceActions );
        setData( OUTPUT_STATES, p._state );
        //setData( OUTPUT_STATES_OLD, p._actions );
        setData( OUTPUT_ACTIONS_IDEAL, p._idealActions );
        setData( OUTPUT_REWARD, rewardData );
    }


}