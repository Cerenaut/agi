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

package io.agi.core.alg;

import io.agi.core.ann.unsupervised.*;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.data.Ranking;
import io.agi.core.math.Geometry;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.*;


/**
 * TOOD add use of feedback to adjust the inference of winning cells.
 *
 * Created by dave on 22/10/16.
 */
public class HierarchicalQuiltedCompetitiveLearning {} /*extends NamedObject {

    // Data structures
    public Data _ffInput1;
    public Data _ffInput1Old;
    public Data _ffInput2;
    public Data _ffInput2Old;
    public Data _fbInput1;
    public Data _fbInput1Old;

    public Data _regionActivity;
    public Data _regionActivityInferred;
    public Data _regionPredictionWeights;
    public Data _regionPrediction;
    public Data _regionLikelihood;
    public Data _regionErrorHistory;
    public Data _regionErrorHistoryIndex;

    public HierarchicalQuiltedCompetitiveLearningConfig _config;
    public HierarchicalQuiltedCompetitiveLearningTransient _transient;

    public BinaryTreeQuilt _organizer;
    public HashMap< Integer, GrowingNeuralGas > _classifiers = new HashMap< Integer, GrowingNeuralGas >();

    public HierarchicalQuiltedCompetitiveLearning( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( HierarchicalQuiltedCompetitiveLearningConfig config ) {
        _config = config;

        setupObjects();
        setupData();
        reset();
    }

    protected void setupObjects() {

        String organizerName = getKey( _config.ORGANIZER );
        BinaryTreeQuiltConfig hqc = new BinaryTreeQuiltConfig();
        hqc.copyFrom( _config._organizerConfig, organizerName );

//        BinaryTreeQuilt hq = new BinaryTreeQuilt( hqc._name, hqc._om );
//        hq.setup( hqc );
//        _organizer = hq;

        Point organizerSizeCells = _config.getOrganizerSizeCells();

        for( int y = 0; y < organizerSizeCells.y; ++y ) {
            for( int x = 0; x < organizerSizeCells.x; ++x ) {

                String name = getKey( _config.CLASSIFIER );
                GrowingNeuralGasConfig gngc = new GrowingNeuralGasConfig();
                gngc.copyFrom( _config._classifierConfig, name );

                GrowingNeuralGas gng = new GrowingNeuralGas( gngc._name, gngc._om );
                gng.setup( gngc );

                int regionOffset = _config.getOrganizerOffset( x, y );
                _classifiers.put( regionOffset, gng );
            }
        }
    }

    protected void setupData() {
        Point ffInput1Size = _config.getFfInput1Size();
        Point ffInput2Size = _config.getFfInput2Size();
        Point fbInputSize = _config.getFbInputSize();
        Point regionSize = _config.getRegionSizeCells();

        DataSize dataSizeFfInput1 = DataSize.create( ffInput1Size.x, ffInput1Size.y );
        DataSize dataSizeFfInput2 = DataSize.create( ffInput2Size.x, ffInput2Size.y );
        DataSize dataSizeFbInput = DataSize.create( fbInputSize.x, fbInputSize.y );
        DataSize dataSizeRegion  = DataSize.create( regionSize.x, regionSize.y );

        // external inputs
        _ffInput1    = new Data( dataSizeFfInput1 );
        _ffInput1Old = new Data( dataSizeFfInput1 );
        _ffInput2    = new Data( dataSizeFfInput2 );
        _ffInput2Old = new Data( dataSizeFfInput2 );
        _fbInput1 = new Data( dataSizeFbInput );
        _fbInput1Old = new Data( dataSizeFbInput );

        _regionActivity = new Data( dataSizeRegion );
        _regionPrediction = new Data( dataSizeRegion );
        _regionLikelihood = new Data( dataSizeRegion );
//        _regionErrorMean = new Data( dataSizeRegion );
        _regionActivityInferred = new Data( dataSizeRegion );

        int cells = _config.getRegionAreaCells();
        _regionPredictionWeights = new Data( cells, cells );

        int history = _config.getErrorHistoryLength();
        _regionErrorHistory = new Data( cells, history );
        _regionErrorHistoryIndex = new Data( dataSizeRegion );
    }

    public void reset() {
        organizerReset();

        Point p = _config.getOrganizerSizeCells();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                int regionOffset = _config.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = _classifiers.get( regionOffset );
                classifier.reset();
            }
        }

        _regionPredictionWeights.set( 0.01f ); // no association

        _regionErrorHistory.set( 0 );
        _regionErrorHistoryIndex.set( 0 );
    }

    public void update() {

        _transient = new HierarchicalQuiltedCompetitiveLearningTransient(); // this replaces all the transient data structures, ensuring they are truly transient
        _transient._ffInput1Active = _ffInput1.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._ffInput2Active = _ffInput2.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._ffInput1ActiveOld = _ffInput1Old.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._ffInput2ActiveOld = _ffInput2Old.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._fbInputActive = _fbInput1.indicesMoreThan( 0.f ); // find all the active bits.

        boolean inputChanged = hasFfInputChanged();
        if( !inputChanged ) {
            return;
        }

        organizerUpdate();
        classifierUpdate(); // adds to _transient._regionActiveCells, _transient._columnActiveCells, and _regionActivity
        likelihoodUpdate(); // convert to likelihood fn
//        predictionUpdate( _transient._regionActiveCells ); // moved to inside inference
        predictionLearn();
        inferenceSearch();

        _ffInput1Old.copy( _ffInput1 );
        _ffInput2Old.copy( _ffInput2 );
    }

// in layers except the lowest layers, we can expect relevant content everywhere. So adaptive receptive fields aren't worthwhile
// We want a topology preserving organizer on the principle that closer things are more similar
// Separate the classification for column training from the prediction and estimation functions.
// Do an initial classification based on bottom-up (ff) then do a second classification based on neighbours?
// ie predict self classification based on other cols in region.
// Can use Oja's rule for this? Given all cells FF class., predict my class. ie cells * cells weights size.
// Now I have two pieces of information about my class - from other cols, and from my col.
// The prediction is continuous.
// How to combine the FF and the prediction?
// Bayes? https://en.wikipedia.org/wiki/Bayesian_inference
// P( H | E ) = P( E | H ) * P( H )
//              -------------------
//                    P( E )
// H = best cell is c
//
// P( H ) = prior = (prediction / sum( predictions ))
// P(E|H) is the probability of observing E given H. As a function of E H fixed, this is the likelihood –
//        it indicates the compatibility of the evidence with the given hypothesis.
//        The likelihood function is a function of the evidence, E, while the posterior probability is a function
//        of the hypothesis, H.
//        So the likelihood should be higher if the error is less. So it's the inverse of the error.
//        What is the likelihood the evidence is as shown, when the cell is picked?
// I could do the likelihood empirically - when the cell is the winner, update mean and variance of error.
// Then assume some distribution, (eg Gaussian) and return the P(E|H) that way, given an error.
// This way I get to model some part of the input with the awareness of the whole of the input.
//
// P( E ) = irrelevant.
// For different values of H, only the factors P(H) and P(E|H), both in the numerator, affect the value of P(H|E)

    protected void inferenceSearch() {

        updateClassifierRegionCells();

//        int history = _config.getErrorHistoryLength();
//        int regionAreaCells = _config.getRegionAreaCells();
        int nbrSelections = 1;
        float fracExclusions = 0;//.15f;
        int nbrExclusions = (int)( (float)_transient._regionActiveCells.size() * fracExclusions );

        ArrayList< Integer > allActiveCells = new ArrayList< Integer >();
        allActiveCells.addAll( _transient._regionActiveCells );

        float maxSum = 0f;
        HashSet< Integer > bestExclusions = null;

        for( int s = 0; s < nbrSelections; ++s ) {

            // TODO maximize likelihood or posterior?
            HashSet< Integer > exclusions = new HashSet< Integer >();

            // build the exclusion set
            for( int e = 0; e < nbrExclusions; ++e ) {
                int n = _config._r.nextInt( allActiveCells.size() );
                int c = allActiveCells.get( n );
                exclusions.add( c ); // can have duplicates
            }

            HashSet< Integer > activeCells = new HashSet<>();
            activeCells.addAll( _transient._regionActiveCells );
            for( Integer c : exclusions ) {
                activeCells.remove( c );
            }

            predictionUpdate( activeCells );

            float sum = inferenceUpdate();

            if( ( bestExclusions == null ) || ( sum >= maxSum ) ) {
                bestExclusions = exclusions;
                maxSum = sum;
//                System.err.println( "New best: " + s + " = " + sum );
            }
            else {
//                System.err.println( "  - reject " + sum );
            }
        }

        // Now apply the best exclusion set.
        HashSet< Integer > activeCells = new HashSet<>();
        activeCells.addAll( _transient._regionActiveCells );
        if( bestExclusions != null ) {
            for( Integer c : bestExclusions ) {
                activeCells.remove( c );
            }
        }

        predictionUpdate( activeCells );
        //predictionUpdate( _transient._regionActiveCells );

        inferenceUpdate();
    }

    protected void likelihoodUpdate() {
        Point organizerSizeCells = _config.getOrganizerSizeCells();
        Point classifierSizeCells = _config.getClassifierSizeCells();

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

                Point classifierOrigin = _config.getRegionClassifierOrigin( xClassifier, yClassifier );
                int classifierOffset = _config.getOrganizerOffset( xClassifier, yClassifier );
                GrowingNeuralGas classifier = _classifiers.get( classifierOffset );

                float sumCellError = 0f;

                for( int yCell = 0; yCell < classifierSizeCells.y; ++yCell ) {
                    for( int xCell = 0; xCell < classifierSizeCells.x; ++xCell ) {

                        int cell = classifier._c.getCell( xCell, yCell );

                        float mask = classifier._cellMask._values[ cell ];
                        float sumSqError = classifier._cellErrors._values[ cell ];
//                        float error = ( float ) Math.sqrt( sumSqError );
                        float error = sumSqError;

                        if( mask > 0f ) {
                            sumCellError += error;
                        }
                    }
                }

                for( int yCell = 0; yCell < classifierSizeCells.y; ++yCell ) {
                    for( int xCell = 0; xCell < classifierSizeCells.x; ++xCell ) {

                        int regionX = classifierOrigin.x + xCell;
                        int regionY = classifierOrigin.y + yCell;
                        int regionOffset = _config.getRegionOffset( regionX, regionY );

                        int cell = classifier._c.getCell( xCell, yCell );

                        float mask = classifier._cellMask._values[ cell ];
                        float sumSqError = classifier._cellErrors._values[ cell ];
//                        float error = ( float ) Math.sqrt( sumSqError );
                        float error = sumSqError;

                        // now compute statistics from the history window
                        float likelihood = 0;
                        if( mask > 0f ) {
                            if( sumCellError <= 0f ) {
                                error = 0f;
                                likelihood = 1f;
                            }
                            else {
                                error = error / sumCellError; // so now it is some fraction of all error ie unit. 0 is better.
                                likelihood = 1f - error;
                            }
                        }

                        // e.g. error = 0.01,   0.05,  0.25, 0.8
                        // sum = 1.11
                        //      unit  = 0.009,  0.045, 0.225, 0.72
                        //     likeli = 0.991,  0.95,  0.775, 0.27

                        // likelihood = probability of observed error, given the cell is the best matching cell
                        // prior = probability that this cell is the best cell, given all the other active cells
                        // posterior = combination of the prior evidence from all cells and the likelihood based on error.
                        _regionLikelihood._values[ regionOffset ] = likelihood;
                    }
                }
            }
        }
    }

    protected float inferenceUpdate() {
        Point organizerSizeCells = _config.getOrganizerSizeCells();
        Point classifierSizeCells = _config.getClassifierSizeCells();

        _regionActivityInferred.set( 0f );

        float sum = 0f;

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

                Point classifierOrigin = _config.getRegionClassifierOrigin( xClassifier, yClassifier );

                float bestPosterior = 0f;
                int xBestCell = 0;
                int yBestCell = 0;

                for( int yCell = 0; yCell < classifierSizeCells.y; ++yCell ) {
                    for( int xCell = 0; xCell < classifierSizeCells.x; ++xCell ) {

                        int regionX = classifierOrigin.x + xCell;
                        int regionY = classifierOrigin.y + yCell;
                        int regionOffset = _config.getRegionOffset( regionX, regionY );

                        // e.g. error = 0.01,   0.05,  0.25, 0.8
                        // sum = 1.11
                        //      unit  = 0.009,  0.045, 0.225, 0.72
                        //     likeli = 0.991,  0.95,  0.775, 0.27

                        // likelihood = probability of observed error, given the cell is the best matching cell
                        // prior = probability that this cell is the best cell, given all the other active cells
                        // posterior = combination of the prior evidence from all cells and the likelihood based on error.
                        float likelihood = _regionLikelihood._values[ regionOffset ];
                        float prior      = _regionPrediction._values[ regionOffset ];
                        float posterior = likelihood * prior;

                        if( posterior >= bestPosterior ) {
                            bestPosterior = posterior;
                            xBestCell = xCell;
                            yBestCell = yCell;
                        }

                    }
                }

                sum += bestPosterior;

                int regionX = classifierOrigin.x + xBestCell;
                int regionY = classifierOrigin.y + yBestCell;
                int regionOffset = _config.getRegionOffset( regionX, regionY );

                // pick the winner in each classifier by maximizing the posterior
                _regionActivityInferred._values[ regionOffset ] = 1f;
            }
        }

        return sum;
    }

    protected void predictionUpdateOld( Collection< Integer > activeCells ) {
        // calculate inhibition for each cell
        int cells = _config.getRegionAreaCells();

        float sumAllCells = 0f; // normalize for number active cells

        for( int j = 0; j < cells; ++j ) {

            float sumWeight = 0;

            for( Integer i : activeCells ) {//_transient._regionActiveCells ) {

                if( i.equals( j ) ) {
                    continue; // no self support
                }

                // how much does c1 support c2?
                int weightsOffset = j * cells + i;
                float w_ji = _regionPredictionWeights._values[ weightsOffset ];
                sumWeight += w_ji;
            }

            _regionPrediction._values[ j ] = sumWeight;

            sumAllCells += sumWeight;
        }

        if( sumAllCells <= 0f ) {
            return;
        }

        for( int j = 0; j < cells; ++j ) {

            float sumWeight = _regionPrediction._values[ j ];

            float unitWeight = sumWeight / sumAllCells;

            _regionPrediction._values[ j ] = unitWeight;
        }
    }

    protected void updateClassifierRegionCells() {
        _transient._classifierRegionCells.clear();

        Point organizerSizeCells = _config.getOrganizerSizeCells();
        Point classifierSizeCells = _config.getClassifierSizeCells();

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

                Point classifierOrigin = _config.getRegionClassifierOrigin( xClassifier, yClassifier );
                int classifierOffset = _config.getOrganizerOffset( xClassifier, yClassifier );
                GrowingNeuralGas classifier = _classifiers.get( classifierOffset );

                HashSet< Integer > classifierRegionCells = new HashSet< Integer >();

                for( int yCell = 0; yCell < classifierSizeCells.y; ++yCell ) {
                    for( int xCell = 0; xCell < classifierSizeCells.x; ++xCell ) {

                        int cell = classifier._c.getCell( xCell, yCell );

                        int regionX = classifierOrigin.x + xCell;
                        int regionY = classifierOrigin.y + yCell;
                        int j = _config.getRegionOffset( regionX, regionY );

                        float mask = classifier._cellMask._values[ cell ];
                        if( mask == 0f ) {
                            continue;
                        }

                        classifierRegionCells.add( j );
                    }
                }

                _transient._classifierRegionCells.put( classifierOffset, classifierRegionCells );
            }
        }
    }

    protected void predictionUpdate( Collection< Integer > activeCells ) {

        _regionPrediction.set( 0f );

        int cells = _config.getRegionAreaCells();

        Point organizerSizeCells = _config.getOrganizerSizeCells();
        Point classifierSizeCells = _config.getClassifierSizeCells();

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

//                Point classifierOrigin = _config.getRegionClassifierOrigin( xClassifier, yClassifier );
                int classifierOffset = _config.getOrganizerOffset( xClassifier, yClassifier );

                HashSet< Integer > classifierRegionCells = _transient._classifierRegionCells.get( classifierOffset );

                float sumColWeight = 0f;

                for( Integer j : classifierRegionCells ) {

                    float sumWeight = 0f;

                    for( Integer i : activeCells ) {//_transient._regionActiveCells ) {
                        if( classifierRegionCells.contains( i ) ) {
                            continue; // no support from within the column
                        }

                        // how much does c1 support c2?
                        int weightsOffset = j * cells + i;
                        float w_ji = _regionPredictionWeights._values[ weightsOffset ];
                        sumWeight += w_ji;
                    }

                    sumColWeight += sumWeight;

                    _regionPrediction._values[ j ] = sumWeight;
                }

                for( Integer j : classifierRegionCells ) {

                    float weight = _regionPrediction._values[ j ];
                    float unitWeight = 1f;
                    if( sumColWeight > 0f ) {
                        unitWeight = weight / sumColWeight; // so if I am the most predicted cell, I get a high score.
                    }

                    _regionPrediction._values[ j ] = unitWeight;
                }
            }
        }
    }

    protected void predictionUpdateOld2( Collection< Integer > activeCells ) {

        int cells = _config.getRegionAreaCells();

        Point organizerSizeCells = _config.getOrganizerSizeCells();
        Point classifierSizeCells = _config.getClassifierSizeCells();

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

                Point classifierOrigin = _config.getRegionClassifierOrigin( xClassifier, yClassifier );
                int classifierOffset = _config.getOrganizerOffset( xClassifier, yClassifier );
                GrowingNeuralGas classifier = _classifiers.get( classifierOffset );

                float sumColWeight = 0f;
                float sumColCells = 0;

                for( int yCell = 0; yCell < classifierSizeCells.y; ++yCell ) {
                    for( int xCell = 0; xCell < classifierSizeCells.x; ++xCell ) {

                        int cell = classifier._c.getCell( xCell, yCell );

                        int regionX = classifierOrigin.x + xCell;
                        int regionY = classifierOrigin.y + yCell;
                        int j = _config.getRegionOffset( regionX, regionY );

                        float mask = classifier._cellMask._values[ cell ];
                        if( mask == 0f ) {
                            _regionPrediction._values[ j ] = 0f;
                            continue;
                        }

                        float sumWeight = 0f;

                        for( Integer i : activeCells ) {//_transient._regionActiveCells ) {

                            // don't count any cell in same column
                            Point p_r = _config.getRegionGivenOffset( i );
                            Point p_i = _config.getOrganizerCoordinateGivenRegionCoordinate( p_r.x, p_r.y );
                            int classifierOffsetActiveCell = _config.getOrganizerOffset( p_i.x, p_i.y );
                            if( classifierOffsetActiveCell == classifierOffset ) {
                                continue; // no self support
                            }

                            // how much does c1 support c2?
                            int weightsOffset = j * cells + i;
                            float w_ji = _regionPredictionWeights._values[ weightsOffset ];
                            sumWeight += w_ji;
                        }

                        ++sumColCells;
                        sumColWeight += sumWeight;

                        _regionPrediction._values[ j ] = sumWeight;
                    }
                }

                for( int yCell = 0; yCell < classifierSizeCells.y; ++yCell ) {
                    for( int xCell = 0; xCell < classifierSizeCells.x; ++xCell ) {

                        int cell = classifier._c.getCell( xCell, yCell );

                        int regionX = classifierOrigin.x + xCell;
                        int regionY = classifierOrigin.y + yCell;
                        int j = _config.getRegionOffset( regionX, regionY );

                        float mask = classifier._cellMask._values[ cell ];
                        if( mask == 0f ) {
                            continue;
                        }

                        float weight = _regionPrediction._values[ j ];
                        float unitWeight = 1f;
                        if( sumColWeight > 0 ) {
                            unitWeight = weight / sumColWeight;
                        }

                        _regionPrediction._values[ j ] = unitWeight;
                    }
                }
            }
        }
    }

    /**
     * Use Oja's rule to associate the winning cells in each classifier with each other.
     * Then, in future we can use these associations to help to improve an ambiguous input to a particular
     * /
    protected void predictionLearn() {

        int cells = _config.getRegionAreaCells();

        // new parameters:
        float predictionLearningRate = _config.getPredictionLearningRate();// 0.01f;
        float predictionDecayRate = _config.getPredictionDecayRate();// 0.99f; // the system is nonstationary so we want it to be able to reduce weights not just grow more slowly

        HashSet< Integer > activeCellSet = new HashSet< Integer >();
        activeCellSet.addAll( _transient._regionActiveCells );

//        for( int j = 0; j < cells; ++j ) { // all cells (probably doesnt matter as y will be small when cell is not active
        for( Integer j : activeCellSet ) { // only active cells

//            float y = 0;
//
//            for( int i = 0; i < cells; ++i ) {
//                int weightsOffset = j * cells +i;
//                float w_ji = _regionPredictionWeights._values[ weightsOffset ];
//                float x_i = 0f;
//                if( activeCellSet.contains( i ) ) {
//                    x_i = 1f;
//                }
//                float y_i = x_i * w_ji;
//                y += y_i;
//            }

//replace with normal learning because it should be the likelihood that informs it
//which cant be ojas rule
//            y /= (float)cells; // normalize for number of cells, so if all cells are priors then y = 1
//
            float y = 1; // because active
            float ySq = y * y; // redundant

            for( int i = 0; i < cells; ++i ) {

                // use ojas rule
                // https://en.wikipedia.org/wiki/Oja%27s_rule
                // http://www.scholarpedia.org/article/Oja_learning_rule
                int weightsOffset = j * cells +i;
                float w_ji = _regionPredictionWeights._values[ weightsOffset ];
                float x_i = 0f;
                if( activeCellSet.contains( i ) ) {
                    x_i = 1f;
                }
                // e.g. x = 1, y = 0.01 (wrong input)
                // 0.01 * 0.01 = 0.001
                //
                float delta = predictionLearningRate * ( x_i * y - ySq * w_ji );
                // 1 = 1 - 1 = 0
                // 0 = 0 - 1 = -1
                // 1 = 1 - 0 = 1      learns fast when far away from observation
                // 1 = 1 - 0.9 = 0.1
                // 1 = 1 - 0.5 = 0.5
                // 1 = 1 - 0.25 = 0.75
                float w_ji2 = w_ji * predictionDecayRate + delta;
                _regionPredictionWeights._values[ weightsOffset ] = w_ji2;
            }
        }
    }

    /**
     * Returns the receptive field centroid, in pixels, of the specified classifier.
     *
     * @param xClassifier
     * @param yClassifier
     * @return
     * /
    public float[] getClassifierReceptiveField( int xClassifier, int yClassifier ) {

        int dimensions = 2;
        int inputs = 2;
        int elements = dimensions * inputs;
        float[] rf = new float[ elements ];

//        int classifierOffset = _config.getOrganizerOffset( xClassifier, yClassifier );
//        int organizerOffset = classifierOffset * elements;//RegionLayerConfig.RECEPTIVE_FIELD_DIMENSIONS;
//
//        Point inputSize1 = Data2d.getSize( _ffInput1 );
//        Point inputSize2 = Data2d.getSize( _ffInput2 );
//
//        float rf1_x = _organizer._cellWeights._values[ organizerOffset + 0 ];
//        float rf1_y = _organizer._cellWeights._values[ organizerOffset + 1 ];
//        float rf2_x = _organizer._cellWeights._values[ organizerOffset + 2 ];
//        float rf2_y = _organizer._cellWeights._values[ organizerOffset + 3 ];
//
//        rf1_x *= inputSize1.x;
//        rf1_y *= inputSize1.y;
//
//        rf2_x *= inputSize2.x;
//        rf2_y *= inputSize2.y;
//
//        rf[ 0 ] = rf1_x; // now in pixel coordinates, whereas it is trained as unit coordinates
//        rf[ 1 ] = rf1_y;
//        rf[ 2 ] = rf2_x; // now in pixel coordinates, whereas it is trained as unit coordinates
//        rf[ 3 ] = rf2_y;

        return rf;
    }

    protected void classifierUpdate() {
        // update all the classifiers and thus the set of active cells in the region
        _regionActivity.set( 0.f ); // clear

        Point p = _config.getOrganizerSizeCells();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                // only update active
                int organizerOffset = _config.getOrganizerOffset( x, y );
//                float mask = _organizer._cellMask._values[ organizerOffset ];
//                if( mask != 1.f ) {
//                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
//                }

                rankClassifierReceptiveFields( x, y );
            }
        }

        updateClassifierInput();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                // only update active
                int organizerOffset = _config.getOrganizerOffset( x, y );
//                float mask = _organizer._cellMask._values[ organizerOffset ];
//                if( mask != 1.f ) {
//                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
//                }

                updateClassifier( x, y ); // adds to _transient._regionActiveCells and _regionActivity
            }
        }
    }

    protected void rankClassifierReceptiveFields( int xClassifier, int yClassifier ) {

        // find the closest N cols to each active input bit
        float[] rf = getClassifierReceptiveField( xClassifier, yClassifier ); // in pixels units
        float xField1 = rf[ 0 ];
        float yField1 = rf[ 1 ];
        float xField2 = rf[ 2 ];
        float yField2 = rf[ 3 ];

        int inputOffset1 = 0;
        int inputOffset2 = _ffInput1.getSize();

        rankClassifierReceptiveField( xClassifier, yClassifier, _ffInput1, _transient._ffInput1Active, _transient._activeInputClassifierRanking, xField1, yField1, inputOffset1 );
        rankClassifierReceptiveField( xClassifier, yClassifier, _ffInput2, _transient._ffInput2Active, _transient._activeInputClassifierRanking, xField2, yField2, inputOffset2 );

        rankClassifierReceptiveField( xClassifier, yClassifier, _ffInput1Old, _transient._ffInput1ActiveOld, _transient._activeInputClassifierRankingOld, xField1, yField1, inputOffset1 );
        rankClassifierReceptiveField( xClassifier, yClassifier, _ffInput2Old, _transient._ffInput2ActiveOld, _transient._activeInputClassifierRankingOld, xField2, yField2, inputOffset2 );
    }

    protected void rankClassifierReceptiveField(
            int xClassifier,
            int yClassifier,
            Data ffInput,
            HashSet< Integer > ffInputActive,
            HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > ranking,
            float xField,
            float yField,
            int inputOffset ) {
        int classifierOffset = _config.getOrganizerOffset( xClassifier, yClassifier );

        for( Integer i : ffInputActive ) {
            Point p = Data2d.getXY( ffInput._dataSize, i );

            float d = Geometry.distanceEuclidean2d( ( float ) p.getX(), ( float ) p.getY(), xField, yField );
            int inputBit = i + inputOffset;

            TreeMap< Float, ArrayList< Integer > > activeInputRanking = _transient.getRankingLazy( ranking, inputBit );

            // Rank by classifier:
            Ranking.add( activeInputRanking, d, classifierOffset ); // add classifier with quality d (distance) to i.
        }

    }

    protected void updateClassifierInput() {
        updateClassifierInput( _transient._activeInputClassifierRanking, _transient._classifierActiveInput );
        updateClassifierInput( _transient._activeInputClassifierRankingOld, _transient._classifierActiveInputOld );
    }

    protected void updateClassifierInput(
            HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > activeInputClassifierRanking,
            HashMap< Integer, ArrayList< Integer > > classifierActiveInput ) {

        int classifiersPerBit1 = _config.getClassifiersPerBit1();
        int classifiersPerBit2 = _config.getClassifiersPerBit2();
        boolean max = false; // ie min [distance]

        //int inputOffset1 = 0; conceptually
        int inputOffset2 = _ffInput1.getSize();

        Set< Integer > activeInputBits = activeInputClassifierRanking.keySet();
        for( Integer inputBit : activeInputBits ) {

            // pick the right spread of input through the region depending on which input it is from
            int maxRank = classifiersPerBit1;
            if( inputBit >= inputOffset2 ) {
                maxRank = classifiersPerBit2;
            }

            TreeMap< Float, ArrayList< Integer > > activeInputRanking = _transient.getRankingLazy( activeInputClassifierRanking, inputBit );

            ArrayList< Integer > activeInputClassifiers = Ranking.getBestValues( activeInputRanking, max, maxRank ); // ok now we got the current set of inputs for the column

            for( Integer classifierOffset : activeInputClassifiers ) {
                _transient.addClassifierActiveInput( classifierOffset, inputBit, classifierActiveInput );
            }
        }
    }

    protected void updateClassifier( int xClassifier, int yClassifier ) {

        Point classifierOrigin = _config.getRegionClassifierOrigin( xClassifier, yClassifier );
        int classifierOffset = _config.getOrganizerOffset( xClassifier, yClassifier );

        ArrayList< Integer > activeInput = _transient.getClassifierActiveInput( classifierOffset );//_classifierActiveInput.get( classifierOffset );
        ArrayList< Integer > activeInputOld = _transient.getClassifierActiveInputOld( classifierOffset );//_classifierActiveInput.get( classifierOffset );

        Collections.sort( activeInput );
        Collections.sort( activeInputOld );

        boolean activeInputChanged = !activeInput.equals( activeInputOld ); // not sure of correctness without sorting

        GrowingNeuralGas classifier = _classifiers.get( classifierOffset );

        boolean learn = _config.getLearn();

        // disable learning and aging when input hasnt changed
        if( activeInputChanged == false ) {
            learn = false;
        }
//        else {
//            int g= 0;
//            g++;
//        }

        classifier._c.setLearn( learn );
        classifier.setSparseUnitInput( activeInput );
        classifier.update(); // trains with this sparse input.

        // map the best cell into the region/quilt
        int bestColumnCell = classifier.getBestCell();
        int bestColumnCellX = classifier._c.getCellX( bestColumnCell );
        int bestColumnCellY = classifier._c.getCellY( bestColumnCell );
        int regionX = classifierOrigin.x + bestColumnCellX;
        int regionY = classifierOrigin.y + bestColumnCellY;
        int regionOffset = _config.getRegionOffset( regionX, regionY );

        _regionActivity._values[ regionOffset ] = 1.f;
        _transient._regionActiveCells.add( regionOffset );
    }

    protected boolean hasFfInputChanged( Data ffInput, Data ffInputOld, HashSet< Integer > ffInputActive ) {
        if( ffInputOld == null ) {
            return true;
        }

        int ffArea = ffInput.getSize();

        for( int i = 0; i < ffArea; ++i ) {
            float oldValue = ffInputOld._values[ i ];
            float newValue = 0.f;

            if( ffInputActive.contains( i ) ) {
                newValue = 1.f;
            }

            if( oldValue != newValue ) {
                return true;
            }
        }

        return false;
    }

    protected void organizerReset() {
        // uniform quilt
        _organizer.reset();
    }

    /**
     * Trains the receptive fields of the classifiers via a specified number of samples.
     * /
    protected void organizerUpdate() {
        // uniform quilt
        _organizer.update();
    }

    protected boolean hasFfInputChanged() {
        return ( hasFfInput1Changed() || hasFfInput2Changed() );
    }

    protected boolean hasFfInput1Changed() {
        return hasFfInputChanged( _ffInput1, _ffInput1Old, _transient._ffInput1Active );
    }

    protected boolean hasFfInput2Changed() {
        return hasFfInputChanged( _ffInput2, _ffInput2Old, _transient._ffInput2Active );
    }

    protected boolean hasFbInputChanged() {
        if( _fbInput1Old == null ) {
            return true;
        }

        int fbArea = _fbInput1.getSize();

        for( int i = 0; i < fbArea; ++i ) {
            float oldValue = _fbInput1Old._values[ i ];
            float newValue = _fbInput1._values[ i ];

            if( oldValue != newValue ) {
                return true;
            }
        }

        return false;
    }

}
*/

