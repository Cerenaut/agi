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
import io.agi.core.ann.reinforcement.QLearning;
import io.agi.core.ann.reinforcement.QLearningConfig;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dave on 2/06/17.
 */
public class EpsilonGreedyEntity extends Entity {

    public static final String ENTITY_TYPE = "epsilon-greedy";

    public static final String INPUT_STATES_NEW  = "input-states-new";
    public static final String INPUT_ACTIONS_QUALITY = "input-actions-quality";
    public static final String OUTPUT_ACTIONS_  = "output-actions-";
    public static final String OUTPUT_ACTIONS  = "output-actions";

    public EpsilonGreedyEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_STATES_NEW );
        attributes.add( INPUT_ACTIONS_QUALITY );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

//        EpsilonGreedyEntityConfig config = ( EpsilonGreedyEntityConfig ) _config;
//
//        int sets = config.getNbrSelectionSets();
//
//        for( int i = 0; i < sets; ++i ) {
//            String suffix = GetSelectionSetSuffix( i );
//            attributes.add( suffix );
//            flags.putFlag( suffix, DataFlags.FLAG_NODE_CACHE );
//            flags.putFlag( suffix, DataFlags.FLAG_SPARSE_BINARY );
//        }

        attributes.add( OUTPUT_ACTIONS );
        flags.putFlag( OUTPUT_ACTIONS, DataFlags.FLAG_NODE_CACHE );
        flags.putFlag( OUTPUT_ACTIONS, DataFlags.FLAG_SPARSE_BINARY );
    }

    public static String GetSelectionSetSuffix( int i ) {
        return OUTPUT_ACTIONS_ + i;
    }

    @Override
    public Class getConfigClass() {
        return EpsilonGreedyEntityConfig.class;
    }

    protected void doUpdateSelf() {

        EpsilonGreedyEntityConfig config = ( EpsilonGreedyEntityConfig ) _config;

        // Do nothing unless the input is defined
        Data inputS = getData( INPUT_STATES_NEW );
        Data inputAQ = getData( INPUT_ACTIONS_QUALITY );
        Data outputA = new Data( inputAQ._dataSize );

        ArrayList< Integer > selectionSetSizes = config.getSelectionSetSizes();

        int sets = config.getNbrSelectionSets();
        int actionOffset = 0;

        for( int i = 0; i < sets; ++i ) {

            String suffix = GetSelectionSetSuffix( i );
            int setSize = selectionSetSizes.get( i );

            int selectionSize = setSize;
//            if( config.selectNone ) {
//                ++selectionSize;
//            }

            Data actionQuality = new Data( selectionSize );
            Data actionSelection = new Data( selectionSize );

            actionQuality.copyRange( inputAQ, 0, actionOffset, selectionSize );

            EpsilonGreedyQLearningPolicy egp = new EpsilonGreedyQLearningPolicy();
            egp.setup( _r, config.epsilon );
            egp._learn = config.learn;
            egp.selectActions( inputS, actionQuality, actionSelection ); // select one action

            // create a copy without the "select none" representation
            Data actionSelectionExcludingNone = new Data( setSize );
            actionSelectionExcludingNone.copyRange( actionSelection, 0, 0, setSize ); // the last action is the "none"
            setData( suffix, actionSelectionExcludingNone );

            outputA.copyRange( actionSelection, actionOffset, 0, selectionSize );

            actionOffset += selectionSize;
        }

        setData( OUTPUT_ACTIONS, outputA );
    }

}