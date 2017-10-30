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
import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 2/06/17.
 */
public class TrainingScheduleEntity extends Entity {

    public static final String ENTITY_TYPE = "training-schedule";

    public TrainingScheduleEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
    }

    @Override
    public Class getConfigClass() {
        return TrainingScheduleEntityConfig.class;
    }

    protected void doUpdateSelf() {

        TrainingScheduleEntityConfig config = ( TrainingScheduleEntityConfig ) _config;

        int epoch = getEpoch();
        if( epoch >= config.totalEpochs ) {
            config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
            _logger.warn( "=======> Terminating on end of test set. (2)" );
        }
        else {
            config.terminate = false;
        }

        // accumulate stats over a set of training and testing epochs.
        float reward = getReward();

        boolean isTraining = true;

        int period = config.trainingEpochs + config.testingEpochs;
        int relative = epoch % period;
System.err.println( "Epoch: " + epoch );
        if( relative >= config.trainingEpochs ) { // testing
            isTraining = false;
            config.rewardSumTesting += reward;
            config.rewardCountTesting += 1;
        }
        else { // training
            config.rewardSumTraining += reward;
            config.rewardCountTraining += 1;
        }

        if( relative == 0 ) { // start of training epoch
            // reset training stats
            config.rewardSumTraining = 0;
            config.rewardCountTraining = 0;

            // update testing stats
            if( config.rewardCountTesting > 0 ) {
                config.rewardTesting = config.rewardSumTesting / config.rewardCountTesting;
            }
            else {
                config.rewardTesting = 0;
            }
        }
        else if( relative == config.trainingEpochs ) { // start of testing epoch
            // reset testing stats
            config.rewardSumTesting = 0;
            config.rewardCountTesting = 0;

            // update training stats
            if( config.rewardCountTraining > 0 ) {
                config.rewardTraining = config.rewardSumTraining / config.rewardCountTraining;
            }
            else {
                config.rewardTraining = 0;
            }
        }

        setLearning( isTraining );
    }

    protected float getReward() {
        TrainingScheduleEntityConfig config = ( TrainingScheduleEntityConfig ) _config;

        String stringValue = PersistenceUtil.GetConfig( config.rewardEntityName, config.rewardConfigPath );
        Float rewardValue = Float.valueOf( stringValue );

        // default missing values to 0
        if( rewardValue == null ) {
            rewardValue = 0.f;
        }

        return rewardValue;
    }

    protected int getEpoch() {
        TrainingScheduleEntityConfig config = ( TrainingScheduleEntityConfig ) _config;

        // get the current epoch of the test problem
        String stringValue = PersistenceUtil.GetConfig( config.epochEntityName, config.epochConfigPath );
        Integer epoch = Integer.valueOf( stringValue );

        // default missing values to 0
        if( epoch == null ) {
            epoch = 0;
        }

        return epoch;
    }

    protected void setLearning( boolean isTraining ) {
        TrainingScheduleEntityConfig config = ( TrainingScheduleEntityConfig ) _config;

        // set config of entities that are being trained
        Collection< String > entityNamesTraining = getEntityNames( config.trainingEntities );
        Collection< String > entityNamesTesting  = getEntityNames( config.testingEntities );

        boolean doLearnTraining = false;
        boolean doLearnTesting = false;
        if( isTraining ) {
            doLearnTraining = true;
        }
        else {
            doLearnTesting = true;
        }

        for( String entityName : entityNamesTraining ) {
            PersistenceUtil.SetConfig( entityName, "learn", String.valueOf( doLearnTraining ) );
        }
        for( String entityName : entityNamesTesting ) {
            PersistenceUtil.SetConfig( entityName, "learn", String.valueOf( doLearnTesting ) );
        }
    }
}