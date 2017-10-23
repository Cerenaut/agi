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
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by gideon on 26/03/2016.
 */
public class Reward2LearningRateEntity extends Entity {

    public static final String ENTITY_TYPE = "reward-2-learning-rate";

    // data
    public static final String INPUT_REWARD = "reward";
    public static final String OUTPUT_LEARNING_RATE = "output-learning-rate";

    public Reward2LearningRateEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_REWARD );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_LEARNING_RATE );
    }

    @Override
    public Class getConfigClass() {
        return Reward2LearningRateEntityConfig.class;
    }

    public void doUpdateSelf() {

        Reward2LearningRateEntityConfig config = ( Reward2LearningRateEntityConfig ) _config;

        Data input = getData( INPUT_REWARD );
        Data output = new Data( input._dataSize );

        int size = input._values.length;
        for( int i = 0; i < size; ++ i ) {
            float reward = input._values[ i ];
            float learningRate = 1f - reward;
            // i.e. learningRate = 1 if reward = 0.
            // Try to over-represent confusing inputs.
            // Now to moderate the effect of the bias.
            // let's say we only want a weight of 0.1
            // then 90% of the learningRate is 1, and 10%
            learningRate =      config.weight  * learningRate
                         + ( 1f-config.weight) * 1f;

            output._values[ i ] = learningRate;
        }

        setData( OUTPUT_LEARNING_RATE, output );
    }

}