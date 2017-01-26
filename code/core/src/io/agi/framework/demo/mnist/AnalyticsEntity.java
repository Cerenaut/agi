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

package io.agi.framework.demo.mnist;

import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.ml.supervised.SupervisedUtil;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * A generic setup for an analytics layer in the hierarchy.
 * The analytics layer evaluates the performance of the core algorithm.
 * e.g. Supervised Learning algorithm to evaluate the performance of an unsupervised algorithm
 * on a dataset for which we have labels.
 *
 * We have one feature series and one label series which is segmented into a
 * - training dataset, and a
 * - testing dataset
 *
 * Default is 'batch mode' where presence of the full dataset is required, and the subscribed 'testing entities'
 * must process in batch mode.
 * In non-'batch mode', iteratively pass over the dataset emitting one feature vector and class label at a time
 *
 * This entity terminates an experiment when the test dataset are processed.
 *
 * Created by gideon on 08/01/2017.
 */
public class AnalyticsEntity extends Entity {

    public static final String ENTITY_TYPE = "analytics";
    public static final String INPUT_FEATURES = "input-features";
    public static final String INPUT_LABELS = "input-labels";
    public static final String OUTPUT_FEATURES = "output-features";
    public static final String OUTPUT_LABELS = "output-labels";

    public AnalyticsEntity( ObjectMap om, Node n, ModelEntity model ) {
        super(om, n, model);
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_FEATURES );
        attributes.add( INPUT_LABELS );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_FEATURES );
        attributes.add( OUTPUT_LABELS );
    }

    // DO NOT USE THIS FUNCTIONALITY FOR NOW (may or may not use it)
//    @Override
//    public void getInputRefs( HashMap< String, AbstractPair< String, String> > input2refs, DataFlags flags ) {
//        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;
//
//        String dataEntity = Framework.GetEntityNameWithPrefix( config.datasetExpPrefix, config.datasetEntity );
//        input2refs.put( INPUT_FEATURES, new AbstractPair<>( dataEntity, config.datasetFeaturesAttribute) );
//        input2refs.put( INPUT_LABELS, new AbstractPair<>( dataEntity, config.datasetLabelsAttribute) );
//    }

    @Override
    public Class getConfigClass() {
        return AnalyticsEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;

        if( config.reset ) {
            config.terminate = false;
            config.phase = AnalyticsEntityConfig.PHASE_TRAINING;
            config.count = 0;
            config.reset = false;
        }

        Data features = getData( INPUT_FEATURES );
        Data labels = getData( INPUT_LABELS );

        if ( features == null || labels == null )
        {
            String message = "Features or Labels are empty";
            _logger.error( message );
            return;
        }

        int numDataPoints = SupervisedUtil.calcMFromFeatureMatrix( features );
        if ( config.batchMode )
        {
            if (numDataPoints < (config.trainSetSize + config.testSetSize) ) {
                String message = "batch mode, but there are not enough features to train and test";
                _logger.error( message );
                throw new java.lang.UnsupportedOperationException( message );
            }
        }

        _logger.warn( "=======> Phase: " + config.phase + ", index: " + config.count );
        calcPhase();

        // inform subscribed testing entities, whether they are in 'learn' or 'test' mode
        try {
            Collection< String > entityNames = getEntityNames( config.testingEntities );
            for( String entityName : entityNames ) {
                Framework.SetConfig( entityName, "learn", String.valueOf( isTraining() ) );
            }
        }
        catch( Exception e ) {
        } // this is ok, the experiment is just not configured to have a learning flag

        incrementCount();

        Data featuresOut, labelsOut;
        if ( config.batchMode ) {
            // copy relevant section of features and labels to output
            if ( isTraining() )
            {
                featuresOut = Data2d.subset( features, 0, config.trainSetSize - 1 );
                labelsOut = Data2d.subset( labels, 0, config.trainSetSize - 1 );
            }
            else
            {
                featuresOut = Data2d.subset( features, config.trainSetSize, config.testSetSize - 1 );
                labelsOut = Data2d.subset( labels, config.trainSetSize, config.testSetSize - 1 );
            }
        }
        else {
            // go through the features and labels matrices one data point at a time
            featuresOut = Data2d.subset( features, 0, config.testSetSize );
            labelsOut = Data2d.subset( labels, 0, config.testSetSize );
        }

        setData( OUTPUT_FEATURES, featuresOut );
        setData( OUTPUT_LABELS, labelsOut );
    }

    /**
     * Use phase and count to determine current phase (perform transition if necessary).
     * This is meant to be run at the start of the run loop (phase value from previous iteration)
     */
    private void calcPhase( ) {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;

        // set current phase (transition if necessary)
        if( isTraining() ) {
            if( config.count >= config.trainSetSize ) {
                config.phase = AnalyticsEntityConfig.PHASE_TESTING; // transition to testing
                _logger.warn( "=======> Transition to test phase. (2)" );
            }
        }
        else {
            if( config.count >= config.testSetSize ) {
                config.terminate = true;                            // terminate
                _logger.warn( "=======> Terminating on end of test set (3)" );
            }
        }
    }

    /**
     * Increment count based on current phase.
     */
    private void incrementCount( ) {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;

        if ( config.batchMode ) {
            if( isTraining() ) {
                config.count += config.trainSetSize;
            }
            else {
                config.count += config.testSetSize;
            }
        }
        else {
            ++config.count;
        }
    }

    public boolean isTraining() {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;
        return config.phase.equals( AnalyticsEntityConfig.PHASE_TRAINING );
    }
}
