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

import io.agi.core.ann.reinforcement.VectorProblem;
import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 4/09/17.
 */
public class VectorProblemEntity extends Entity {

    public static final String ENTITY_TYPE = "vector-problem";

 //   public static final String INPUT_STATES = "input-states";
    public static final String INPUT_ACTIONS = "input-actions";
    public static final String INPUT_ACTIONS_IDEAL = "input-actions-ideal";
    public static final String OUTPUT_REWARD = "output-reward";

    public VectorProblemEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
//        attributes.add( INPUT_STATES );
        attributes.add( INPUT_ACTIONS );
        attributes.add( INPUT_ACTIONS_IDEAL );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( OUTPUT_REWARD );
        flags.putFlag( OUTPUT_REWARD, DataFlags.FLAG_NODE_CACHE );
    }

    @Override
    public Class getConfigClass() {
        return VectorProblemEntityConfig.class;
    }

    public void doUpdateSelf() {

        VectorProblemEntityConfig config = ( VectorProblemEntityConfig ) _config;

        // default input
//        Data inputS = getData( INPUT_STATES );
        Data inputA = getData( INPUT_ACTIONS );
        Data inputIA = getData( INPUT_ACTIONS_IDEAL );

        VectorProblem p = new VectorProblem();

        if( ( inputA == null ) || ( inputA == null ) || ( inputA == null ) ) {
            // save state
            copyDataToPersistence( p, config );
            return;
        }

        p.setup( _r, config.actions );

//        p.setState( inputS );
        p.setActions( inputA );
        p.setIdealActions( inputIA );

        // load state
        copyDataFromPersistence( p );

        if( config.reset ) {
            // no reset
        }

        // update
        p.update();

        // save state
        copyDataToPersistence( p, config );
    }

    protected void copyDataFromPersistence( VectorProblem p ) {

    }

    protected void copyDataToPersistence( VectorProblem p, VectorProblemEntityConfig config ) {

        Data rewardData = new Data( 1 );
        rewardData._values[ 0 ] = p._reward;

        setData( OUTPUT_REWARD, rewardData );
        config.reward = p._reward;
    }
}