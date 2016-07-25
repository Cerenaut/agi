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
import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray;
import io.agi.core.data.Ranking;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * A rough simulation of one layer of Pyramidal cells and associated interneurons. So, we call it a Region-Layer.
 *
 * This class takes some input and performs dimensionality reduction both over space and time. In other words, the input
 * is replaced by a simpler form representing frequently observed patterns, both instantaneous and sequential.
 *
 * The intent of this class is to create an unsupervised acyclic processor of input data that can be stacked into a
 * hierarchy. The avoidance of internal cycles is supposed to prevent runaway feedback loops. The function performed by
 * the class is supposed to ensure that adding more of these objects continues to yield higher-order modelling and greater
 * representational power, even while all instances are functionally equivalent.
 *
 * To achieve this, we have the following components.
 *
 * 1. A context-free classifier, that takes external input and replaces it with a sparse distributed binary representation.
 *    The representation is based only on the current input. If the context-free classification does not change, neither
 *    does anything else in the Region-Layer. This classification effectively finds the distinct transition boundaries
 *    in a continuously changing input.
 *
 * 2. A contextual classifier. This is identical to the first component but also takes the previous context-free class-
 *    -ification into account as part of its input. The purpose of this is to generate a 1st-order sequential model so
 *    that additional instances of the Region-Layer will have the benefit of short proto-sequences to build on, which
 *    means that given enough instances, higher order sequences can be discovered. If the contextual classification does
 *    not change, neither does the output.
 *
 * 3. A predictor. This component predicts the next contextual classification, given the current contextual classification.
 *    The purpose of prediction is to inhibit output where it can be predicted. The output of the Region-Layer is a
 *    False-Negative encoding of the contextual classification where predicted bits/cells are masked out. The output of
 *    the Region-Layer includes the most recent N unpredicted but contextually-classified-active cells, where N is a
 *    factor (e.g. 2) of the number of contextually-classified cells. The predictor is only update on a change in
 *    classification.
 *
 * The Region-Layer only loses information due to the limited number of cells available to the classifiers. Sequential
 * prediction problems are transformed into classification problems. At the onset of unpredictable events, there is a
 * large increase in the number of changed output bits; therefore, sequence boundaries become distinct. Conversely, where
 * data is predictable, the output of the Region-Layer becomes stable and quiet.
 *
 * Key parameters are:
 *
 * a) width and height of the context-free cell layer
 * b) width and height of the contextual cell layer
 * c) sparsity of each cell layer
 * d) sparsity of the output
 * e) The learning rate of the predictor and cell layers
 *
 * The sparsity of the output determines how data is compressed over time. The more bits that are output, the longer
 * the time period that can be represented by the output at any given time.
 *
 * Created by dave on 4/07/16.
 */
public class AutoRegionLayer extends NamedObject {

    // Data structures
    public Data _input1;
    public Data _input2;

    public Data _contextFreeActivity;
    public Data _contextFreeActivityOld;
    public Data _contextFreeActivityNew;
//    public Data _contextFreePredictionOld;
//    public Data _contextFreePredictionNew;

//    public Data _contextualActivity;
//    public Data _contextualActivityOld;
//    public Data _contextualActivityNew;

    public Data _predictionFP;
    public Data _predictionFN;
    public Data _predictionNew;
    public Data _predictionNewReal;
    public Data _predictionOld;
    public Data _predictionInhibition;

    public Data _output;
    public Data _outputAge;

    // Member objects
    public AutoRegionLayerConfig _rc;
    public AutoRegionLayerTransient _transient = null;

//    public KSparseAutoencoder _classifier; tried to reduce to one classifier
    public KSparseAutoencoder _contextFreeClassifier;
//    public KSparseAutoencoder _contextualClassifier;
    public HebbianLearning _predictor; // actually this one object stands in for one predictor per column, because the columns may update asynchronously

    public AutoRegionLayer( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( AutoRegionLayerConfig rc ) {
        _rc = rc;

        _contextFreeClassifier = new KSparseAutoencoder( getKey( AutoRegionLayerConfig.SUFFIX_CONTEXT_FREE ), _rc._om );
//         _contextualClassifier = new KSparseAutoencoder( getKey( AutoRegionLayerConfig.SUFFIX_CONTEXTUAL   ), _rc._om );

        _contextFreeClassifier.setup( _rc._contextFreeConfig );
//         _contextualClassifier.setup( _rc._contextualConfig );

        // Predictor
//        int predictorStates = _rc._contextualConfig.getNbrCells();
        int predictorStates = _rc._contextFreeConfig.getNbrCells();
        float predictorLearningRate = rc.getPredictorLearningRate();

        _predictor = new HebbianLearning();
        _predictor.setup( predictorStates, predictorLearningRate );

        // Create data
        Point input1Size = _rc.getInput1Size();
        Point input2Size = _rc.getInput2Size();

        DataSize dataSizeInput1 = DataSize.create( input1Size.x, input1Size.y );
        DataSize dataSizeInput2 = DataSize.create( input2Size.x, input2Size.y );

        DataSize dataSizeContextFree = DataSize.create( _rc._contextFreeConfig.getWidthCells(), _rc._contextFreeConfig.getHeightCells() );
//        DataSize dataSizeContextual  = DataSize.create( _rc._contextualConfig .getWidthCells(), _rc._contextualConfig .getHeightCells() );

        _input1 = new Data( dataSizeInput1 );
        _input2 = new Data( dataSizeInput2 );

        _contextFreeActivity    = new Data( dataSizeContextFree );
        _contextFreeActivityOld = new Data( dataSizeContextFree );
        _contextFreeActivityNew = new Data( dataSizeContextFree );
//        _contextFreePredictionOld = new Data( dataSizeContextFree );
//        _contextFreePredictionNew = new Data( dataSizeContextFree );

//        _contextualActivity = new Data( dataSizeContextual );
//        _contextualActivityOld = new Data( dataSizeContextual );
//        _contextualActivityNew = new Data( dataSizeContextual );
//        _contextualActivityFP = new Data( dataSizeContextual );
//        _contextualActivityFN = new Data( dataSizeContextual );
//        _contextualPredictionNew = new Data( dataSizeContextual );
//        _contextualPredictionOld = new Data( dataSizeContextual );
//        _contextualPredictionReal = new Data( dataSizeContextual );
//        _contextualPredictionInhibition = new Data( dataSizeContextual );
//        _contextualOutput = new Data( dataSizeContextual );
//        _contextualOutputAge = new Data( dataSizeContextual );
        _predictionFP = new Data( dataSizeContextFree );
        _predictionFN = new Data( dataSizeContextFree );
        _predictionNew = new Data( dataSizeContextFree );
        _predictionOld = new Data( dataSizeContextFree );
        _predictionNewReal = new Data( dataSizeContextFree );
        _predictionInhibition = new Data( dataSizeContextFree );
        _output = new Data( dataSizeContextFree );
        _outputAge = new Data( dataSizeContextFree );
    }

    public void reset() {
        _transient = null;
        _contextFreeClassifier.reset();
//        _contextualClassifier.reset();
        _predictor.reset();

        float defaultPredictionInhibition = _rc.getDefaultPredictionInhibition();
        _predictionInhibition.set( defaultPredictionInhibition );

        _predictionFP.set( 0.f );
        _predictionFN.set( 0.f );

        _outputAge.set( 0.f );
        _output.set( 0.f );
    }

    public void update() {

        // copy new values to the contextual classifier
        // update the active hidden cells (without learning)
        // if the active hidden cells have changed
        // update with learning
        // copy the new values to the "old" section.
        // etc.
        _contextFreeClassifier._c.setLearn( _rc.getLearn() );

        // 1. update context-free classification
        updateContextFreeClassification();

        // 2. If context-free classification unchanged, do nothing else
        if( !contextFreeClassificationChanged() ) {
            return;
        }

        onClassificationChanged();

        // 3. Form the input to the contextual classifier from the new and previous context-free classification
//        updateContextualClassification();
//
//        // 4. If contextual classification unchanged, do nothing else
//        if( !contextualClassificationChanged() ) {
//            return;
//        }

        // 5. Update the region output with incorrectly predicted bits. Remove old predicted bits until the desired
        //    output sparsity is achieved.
        updateOutput();

        // 6. Update & train the prediction of the next contextual classification, given old and new bits.
        updatePrediction();
    }

//    protected void updateClassification() {

        // if all the old bits were unpredicted and all the new bits, then if the output density is 2x
        // then we can represent the edge old->new as a continuous input.

        // old input  new     class:
        //   0001    0001      1000
        // input changes:
        //   0001    0010      ????
        // class changes:
        //   0001    0010      0100
        // copy old to new
        //   0010    ????      0100
        // maybe use overlap to transition to a new state?

    protected void updateContextFreeClassification() {
        _transient = new AutoRegionLayerTransient();
        _transient._input1Active = _input1.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._input2Active = _input2.indicesMoreThan( 0.f ); // find all the active bits.

        _contextFreeClassifier._inputValues.set( 0.f );

        for( Integer i : _transient._input1Active ) {
            _contextFreeClassifier._inputValues._values[ i ] = 1.f;
        }

        int offset = _input1.getSize();

        for( Integer i : _transient._input2Active ) {
            _contextFreeClassifier._inputValues._values[ offset + i ] = 1.f;
        }

        _contextFreeClassifier.update(); // produces a new classification
        _contextFreeActivity.copy( _contextFreeClassifier._cellActivity );

        _transient._contextFreeActive    = _contextFreeActivity   .indicesMoreThan( 0.5f );
        _transient._contextFreeActiveNew = _contextFreeActivityNew.indicesMoreThan( 0.5f );
        _transient._contextFreeActiveOld = _contextFreeActivityOld.indicesMoreThan( 0.5f );
    }

    protected boolean contextFreeClassificationChanged() {
        boolean changed = !_transient._contextFreeActive.equals( _transient._contextFreeActiveNew );
        return changed;
    }

    protected void onClassificationChanged() {
        _contextFreeActivityOld.copy( _contextFreeActivityNew );
        _contextFreeActivityNew.copy( _contextFreeActivity    );

        _transient._contextFreeActiveOld = _transient._contextFreeActiveNew;
        _transient._contextFreeActiveNew = _transient._contextFreeActive;
    }

//    protected void updateContextualClassification() {
//
//        _contextFreeActivityOld.copy( _contextFreeActivityNew );
//        _contextFreeActivityNew.copy( _contextFreeActivity    );
//
//        _transient._contextFreeActiveOld = _transient._contextFreeActiveNew;
//        _transient._contextFreeActiveNew = _transient._contextFreeActive;
//
//        _contextualClassifier._inputValues.set( 0.f );
//
//        for( Integer i : _transient._contextFreeActiveOld ) {
//            _contextualClassifier._inputValues._values[ i ] = 1.f;
//        }
//
//        int offset = _contextFreeActivityNew.getSize();
//
//        for( Integer i : _transient._contextFreeActiveNew ) {
//            _contextualClassifier._inputValues._values[ offset + i ] = 1.f;
//        }
//
//        _contextualClassifier.update();
//        _contextualActivity.copy( _contextualClassifier._cellActivity );
//
//        _transient._contextualActive    = _contextualActivity   .indicesMoreThan( 0.5f );
//        _transient._contextualActiveNew = _contextualActivityNew.indicesMoreThan( 0.5f );
//        _transient._contextualActiveOld = _contextualActivityOld.indicesMoreThan( 0.5f );
//    }
//
//    protected boolean contextualClassificationChanged() {
//        boolean changed = !_transient._contextualActive.equals( _transient._contextualActiveNew );
//        return changed;
//    }

    protected void updateOutput() {
        updateOutput( _transient._contextFreeActiveNew );
    }

    protected void updateOutput( HashSet< Integer > activeNew ) {

        // 1. Set FP / FN status of all currently active bits.
        _predictionFP.set( 0.f );
        _predictionFN.set( 0.f );

        _transient._predictionFP = new HashSet< Integer >();
        _transient._predictionFN = new HashSet< Integer >();

        // calculate FP and FN errors
        for( Integer i : activeNew ) {

            float prediction           = _predictionNew._values[ i ]; // most recent prediction, which hasn't been updated yet.
            float predictionInhibition = _predictionInhibition._values[ i ];

            boolean error = false;

            if( predictionInhibition > 0.f ) {
                error = true; // if you inhibit the prediction, then it can't be predicted
            }
            else if( prediction < 1.f ) {
                error = true;
            }

            if( error ) {
                _transient._predictionFN.add( i );
                _predictionFN._values[ i ] = 1.f;
            }
        }

        HashSet< Integer > predictions = _predictionNew.indicesMoreThan( 0.5f ); //_predictor._statePredicted.indicesMoreThan( 0.5f );
        for( Integer p : predictions ) {

            boolean error = false;

            if( !activeNew.contains( p ) ) {
                error = true; // predicted, but not active
            }

            if( error ) {
                _transient._predictionFP.add( p );
                _predictionFP._values[ p ] = 1.f;
            }
        }

        // 2. Increase age of current output:
        int outputBits = _output.getSize();

        _transient._output = new HashSet< Integer >();

        for( int i = 0; i < outputBits; ++i ) {
            float age = _outputAge._values[ i ];

            if( age == 0.f ) {
                continue;
            }

            age += 1.f;
            _outputAge._values[ i ] = age;
            _transient._output.add( i );
        }

        // 2. Set all new active FN bits in output to age = 1.
        for( Integer i : _transient._predictionFN ) {
            _output   ._values[ i ] = 1.f;
            _outputAge._values[ i ] = 1.f;
            _transient._output.add( i );
        }

        // 3. Rank all active bits in output (including historic) by age.
        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

        for( Integer i : _transient._output ) {
            float age = _outputAge._values[ i ];
            Ranking.add( ranking, age, i );
        }

        // 4. Prune output by age (removing oldest) until desired sparsity is reached.
        float outputSparsityFactor = _rc.getOutputSparsity();
        float currentSparsity = _contextFreeClassifier._c.getSparsity();
        int maxRank = (int)( outputSparsityFactor * currentSparsity );
        boolean findMaxima = false; // keep the youngest

        HashSet< Integer > youngOutput = new HashSet< Integer >();
        Ranking.getBestValues( ranking, findMaxima, maxRank, youngOutput );

        for( Integer i : _transient._output ) {

            if( youngOutput.contains( i ) ) {
                continue; // young enough to keep
            }

            // turn it off:
            _output   ._values[ i ] = 0.f;
            _outputAge._values[ i ] = 0.f;
        }
    }

    protected void updatePrediction() {
//        int density = _contextualClassifier._c.getSparsity();
//        updatePrediction( _contextualActivityOld, _contextualActivityNew, density );
        int density = _contextFreeClassifier._c.getSparsity();
        updatePrediction( _contextFreeActivityOld, _contextFreeActivityNew, density );
    }
    protected void updatePrediction( Data activityOld, Data activityNew, int density ) {
        _predictor.train( activityOld, activityNew );
        _predictor.predict( activityNew, density );
        _predictionOld .copy( _predictionNew ); // the old prediction
        _predictionNew .copy( _predictor._statePredicted ); // copy the new prediction
        _predictionNewReal.copy( _predictor._statePredictedReal );
    }
}
