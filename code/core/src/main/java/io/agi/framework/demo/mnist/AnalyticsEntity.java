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
import io.agi.core.data.DataSize;
import io.agi.core.ml.supervised.SupervisedUtil;
import io.agi.core.orm.ObjectMap;
import io.agi.core.util.MemoryUtil;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
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
 * This entity terminates an experiment after testing phase (learn=false).
 * In 'Test phase', run the test on both Training and Testing data sets.
 *
 * Created by gideon on 08/01/2017.
 */
public class AnalyticsEntity extends Entity {

    protected static final Logger _logger = LogManager.getLogger();

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

        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;
//        flags.putFlag( OUTPUT_FEATURES, DataFlags.FLAG_SPARSE_BINARY );
        if( config.featuresEncoding.equals( DataJsonSerializer.ENCODING_DENSE ) ) {
            flags._dataFlags.clear();
        }
        else if( config.featuresEncoding.equals( DataJsonSerializer.ENCODING_SPARSE_BINARY ) ) {
            flags.putFlag( OUTPUT_FEATURES, DataFlags.FLAG_SPARSE_BINARY );
        }
        else if( config.featuresEncoding.equals( DataJsonSerializer.ENCODING_SPARSE_REAL ) ) {
            flags.putFlag( OUTPUT_FEATURES, DataFlags.FLAG_SPARSE_REAL );
        }
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

        Data features = null;
        Data labels = null;

        if( config.useInputFiles ) {
            features = Data2d.readCsvFile( config.fileNameFeatures );
            labels = Data2d.readCsvFile( config.fileNameLabels );
        }
        else {
            features = getData( INPUT_FEATURES );
            labels = getData( INPUT_LABELS );
        }

        if( features == null || labels == null ) {
            String message = "Features or Labels are empty";
            _logger.error( message );
            return;
        }

        // if labels is 1 dimensional, then convert to column matrix (form expected by supervised learning)
        if( labels._dataSize.getDimensions() == 1 )
        {
            int length = labels._dataSize.getSize( DataSize.DIMENSION_X );
            labels.setSize( DataSize.create( 1, length ) );
        }

        int numDataPoints = SupervisedUtil.calcMFromFeatureMatrix( features );
        if ( config.batchMode )
        {
            if (    numDataPoints < config.trainSetSize
                    || numDataPoints < config.testSetSize - config.testSetOffset ) {
                String message = "Batch mode, but there are not enough features to train and test.";
                message += "\nSizes: [Dataset, Train, Test, Test Offset] = [" + numDataPoints + ", " + config.trainSetSize + ", " + config.testSetSize + ", " + config.testSetOffset + "]";
                _logger.error( message );
                throw new java.lang.UnsupportedOperationException( message );
            }
        }

        MemoryUtil.logMemory( _logger );

        updatePhase();
        boolean isTerminate = isTerminating();

        _logger.info( "=====> Age = " + _config.age + ", Phase: " + config.phase + ", " + idxMessage( numDataPoints ) + ", " + sizeMessage() );
        _logger.info( "=====> Update to: " + idxMessage( numDataPoints ) );

        // inform subscribed testing entities, whether they should TRAIN or PREDICT
        try {
            Collection< String > entityNames = getEntityNames( config.testingEntities );
            for( String entityName : entityNames ) {

                boolean doReset = config.reset;
                boolean doLearn = isTraining();
                boolean doPredict = true;

                if( doLearn ) {
                    if( !config.predictDuringTraining ) {
                        doPredict = false;
                    }
                }

                if( isTerminate ) {
                    doReset = false;
                    doLearn = false;
                    doPredict = false;
                }

                // NB: Need to intersperse these, or the log will be inaccurate in the event of an exception. (not needed, framework logs anyway)
                //_logger.info( "Set entity flag: 'reset', Entity = " + entityName + ", " + doReset );
                PersistenceUtil.SetConfig( entityName, "predict", String.valueOf( doReset ) );
                //_logger.info( "Set entity flag: 'learn', Entity = " + entityName + ", " + doLearn );
                PersistenceUtil.SetConfig( entityName, "learn", String.valueOf( doLearn ) );
                //_logger.info( "Set entity flag: 'predict', Entity = " + entityName + ", " + doPredict );
                PersistenceUtil.SetConfig( entityName, "predict", String.valueOf( doPredict ) );
            }
        }
        catch( Exception e ) {
        } // this is ok, the experiment is just not configured to have a learning flag

        if( !isTerminate ) {
            incrementCount();

            Data featuresOut, labelsOut;
            int startIdx;
            int endIdx;

            if( config.batchMode ) {

                // copy relevant section of features and labels to output
                if( isTraining() ) {
                    startIdx = config.trainSetOffset;
                    endIdx = startIdx + config.trainSetSize - 1;
                }
                else {
                    startIdx = config.testSetOffset;
                    endIdx = startIdx + config.testSetSize - 1;
                }
            }
            else {
                // go through the features and labels matrices one data point at a time
                startIdx = config.count;
                endIdx = config.count;
            }

            _logger.info( "Output datapoints in idx range (incl.): (" + startIdx + ", " + endIdx + ")" );

            featuresOut = Data2d.copyRows( features, startIdx, endIdx );
            labelsOut = Data2d.copyRows( labels, startIdx, endIdx );

            if( config.trainingDropoutProbability > 0f ) {
                if( isTraining() ) {
                    dropoutFeatures( featuresOut, config.trainingDropoutProbability );
                }
            }

            setData( OUTPUT_FEATURES, featuresOut );
            setData( OUTPUT_LABELS, labelsOut );
        }
    }

    /**
     * Implements dropout during training in an attempt to use the features more evenly rather than focusing on a few
     * stronger features. Intended to promote regularization of the supervised model.
     *
     * @param features
     * @param probability Probability that dropout will occur - i.e. 0.05 = 5% chance
     */
    protected void dropoutFeatures( Data features, float probability ) {
        Point p = Data2d.getSizeExplicit( features );

        int rows = p.y;
        int cols = p.x;

        for( int r = 0; r < rows; ++r ) {
            for( int c = 0; c < cols; ++c ) {

                float value = features._values[ r * cols + c ];
                if( value <= 0f ) {
                    continue;
                }

                // e.g. 5% = 0.05 if z >= 0.05, (95%) then no dropout
                float z = _r.nextFloat();
                if( z >= probability ) {
                    continue; // not dropout
                }

                features._values[ r * cols + c ] = 0; // replace with zero
            }
        }

    }

    private String idxMessage( int numDataPoints ) {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;
        return "idx: " + config.count + " of " + numDataPoints;
    }

    private String sizeMessage( ) {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;
        String message = "Sizes:[Train,Test,Offset] = [" + config.trainSetSize + ", " + config.testSetSize + ", " + config.testSetOffset + "]";
        return message;
    }

    /**
     * Use phase and count to determine current phase
     *  - perform transition if necessary
     *  - terminate when finished test phase
     * This is meant to be run at the start of the run loop (phase value from previous iteration)
     */
    private void updatePhase() {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;

        // set current phase (transition if necessary)
        if( isTraining() ) {
            if( config.count >= config.trainSetSize ) {
                config.phase = AnalyticsEntityConfig.PHASE_TESTING; // transition to testing
                config.count = config.testSetOffset;
                _logger.warn( "===> Transition to test phase. (2)" );
            }
        }
        else if( isTesting() ) {
            if( config.count >= config.testSetOffset + config.testSetSize ) {
                config.phase = AnalyticsEntityConfig.PHASE_TERMINATING;
                config.terminate = true;                            // terminate
                _logger.warn( "===> Transition to terminate phase. (3)" );
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

    public boolean isTesting() {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;
        return config.phase.equals( AnalyticsEntityConfig.PHASE_TESTING );
    }

    public boolean isTerminating() {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;
        return config.phase.equals( AnalyticsEntityConfig.PHASE_TERMINATING );
    }
}

