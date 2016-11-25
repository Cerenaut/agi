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
import io.agi.core.data.*;
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
    public Data _input3;

//    public Data _outputInput1;
//    public Data _outputInput2;

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
    public Data _spikeAge;

    // Member objects
    public AutoRegionLayerConfig _rc;
    public AutoRegionLayerTransient _transient = null;

//    public KSparseAutoencoder _classifier; tried to reduce to one classifier
    public KSparseAutoencoder _contextFreeClassifier;
//    public KSparseAutoencoder _contextualClassifier;
    public HebbianLearning _predictor; // actually this one object stands in for one predictor per column, because the columns may update asynchronously

    public boolean _classificationChanged = false;
    public float _predictionOldErrorInput1 = 0f;
    public float _predictionOldErrorInput2 = 0f;
    public float _predictionOldErrorInput3 = 0f;

//    int _minSpikeAge = 3;
//    int _maxSlowAge = 3;

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
        Point input3Size = _rc.getInput3Size();

        DataSize dataSizeInput1 = DataSize.create( input1Size.x, input1Size.y );
        DataSize dataSizeInput2 = DataSize.create( input2Size.x, input2Size.y );
        DataSize dataSizeInput3 = DataSize.create( input3Size.x, input3Size.y );

        DataSize dataSizeContextFree = DataSize.create( _rc._contextFreeConfig.getWidthCells(), _rc._contextFreeConfig.getHeightCells() );
//        DataSize dataSizeContextual  = DataSize.create( _rc._contextualConfig .getWidthCells(), _rc._contextualConfig .getHeightCells() );

        _input1 = new Data( dataSizeInput1 );
        _input2 = new Data( dataSizeInput2 );
        _input3 = new Data( dataSizeInput3 );

//        _outputInput1 = new Data( dataSizeInput1 );
//        _outputInput2 = new Data( dataSizeInput2 );
//        _outputInput3 = new Data( dataSizeInput3 );

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
        _spikeAge = new Data( dataSizeContextFree );
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

        _spikeAge.set( 0f );
        // classifier cell mask will be reset to all 1 also.
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
//        updateCellMask(); this generates more unique sequences, but if the thing is predicted correctly, what's the point? It won't be output anyways.
        updateContextFreeClassification();

        // 2. If context-free classification unchanged, do nothing else
        if( !contextFreeClassificationChanged() ) {
            _classificationChanged = false;
            updateInputPredictionError();
            return;
        }

        _classificationChanged = true;

        onClassificationChanged();
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

        // Binary
        /*
        _contextFreeClassifier._inputValues.set( 0.f );

        for( Integer i : _transient._input1Active ) {
            _contextFreeClassifier._inputValues._values[ i ] = 1.f;
        }

        int offset = _input1.getSize();

        // Original
        for( Integer i : _transient._input2Active ) {
            _contextFreeClassifier._inputValues._values[ offset + i ] = 1.f;
        }*/

        // real:
        int size1 = _input1.getSize();
        int size2 = _input2.getSize();
        int size3 = _input3.getSize();
        int offset = 0;
        _contextFreeClassifier._inputValues.copyRange( _input1, offset, 0, size1 );
        offset += size1;
        _contextFreeClassifier._inputValues.copyRange( _input2, offset, 0, size2 );
        offset += size2;
        _contextFreeClassifier._inputValues.copyRange( _input3, offset, 0, size3 );

//        _contextFreeClassifier.updatePositionInhibition( _contextFreeActivityNew );
        _contextFreeClassifier.update(); // produces a new classification
        _contextFreeActivity.copy( _contextFreeClassifier._cellActivity );

        ///////////////
        /*
        HashSet< Integer > activeOld = _contextFreeClassifier._cellActivity.indicesMoreThan( 0.5f );

        for( int i = 0; i < 10; ++i ) {
            _contextFreeClassifier._c.setLearn( false );
            _contextFreeClassifier.update(); // produces a new classification
            HashSet< Integer > activeNew = _contextFreeClassifier._cellActivity.indicesMoreThan( 0.5f );

            if( !activeNew.equals( activeOld ) ) {
                int g = 0;
                g++;
            }

            activeOld = activeNew;
            _contextFreeClassifier._inputValues.mul( 0.5f );
        }*/
        ///////////////

        _transient._contextFreeActive    = _contextFreeActivity   .indicesMoreThan( 0.5f );
        _transient._contextFreeActiveNew = _contextFreeActivityNew.indicesMoreThan( 0.5f );
        _transient._contextFreeActiveOld = _contextFreeActivityOld.indicesMoreThan( 0.5f );

        // Hack - present all labels as inputs, pick the highest response. This being kept only to show it was tried.
        /*
        boolean doHack = false;
        int input2Size = _input2.getSize();
        if( _rc.getLearn() == false ) {
            if( input2Size > 1 ) {
                doHack = true;
            }
        }

        if( doHack ) {
            // test each label
            float maxSum = 0f;
            int bestLabel = 0;

            // reset the input2

            // set one col to represent a label
            int labels = 10;
            int w = labels;
            int h = 8;
            for( int label = 0; label < labels; ++label ) {

                for( int i = 0; i < input2Size; ++i ) {
                    int inputOffset = offset + i;
                    _contextFreeClassifier._inputValues._values[ inputOffset ] = 0.f;
                }

                // set all input bits - one col
                for( int y = 0; y < h; ++y ) {
                    int input2Offset = y * labels + label;
                    int inputOffset = offset + input2Offset;

                    _contextFreeClassifier._inputValues._values[ inputOffset ] = 1.f;
                }

                _contextFreeClassifier.update(); // produces a new classification

                float sum = _contextFreeClassifier._sumResponse;
                if( sum >= maxSum ) {
                    maxSum = sum;
                    bestLabel = label;
                }
            }

            // now repeat the best label:
            for( int i = 0; i < input2Size; ++i ) {
                int inputOffset = offset + i;
                _contextFreeClassifier._inputValues._values[ inputOffset ] = 0.f;
            }

            // set all input bits - one col
            for( int y = 0; y < h; ++y ) {
                int input2Offset = y * labels + bestLabel;
                int inputOffset = offset + input2Offset;

                _contextFreeClassifier._inputValues._values[ inputOffset ] = 1.f;
            }

            _contextFreeClassifier.update(); // produces a new classification
            _contextFreeActivity.copy( _contextFreeClassifier._cellActivity );

            _transient._contextFreeActive    = _contextFreeActivity   .indicesMoreThan( 0.5f );
            _transient._contextFreeActiveNew = _contextFreeActivityNew.indicesMoreThan( 0.5f );
            _transient._contextFreeActiveOld = _contextFreeActivityOld.indicesMoreThan( 0.5f );
        }
        else {
            // Original
            for( Integer i : _transient._input2Active ) {
                _contextFreeClassifier._inputValues._values[ offset + i ] = 1.f;
            }

            _contextFreeClassifier.update(); // produces a new classification
            _contextFreeActivity.copy( _contextFreeClassifier._cellActivity );

            _transient._contextFreeActive    = _contextFreeActivity   .indicesMoreThan( 0.5f );
            _transient._contextFreeActiveNew = _contextFreeActivityNew.indicesMoreThan( 0.5f );
            _transient._contextFreeActiveOld = _contextFreeActivityOld.indicesMoreThan( 0.5f );
            // Original
        }

        // Hack */
    }

/*    protected void updateInputOutput() {

        // use the active cells in _cellActivity
        // reproject back into the input space.
        int offset = 0;
        int inputs1 = _input1.getSize();
        int inputs2 = _input2.getSize();
        int inputs3 = _input3.getSize();

        for( int i = 0; i < inputs1; ++i ) {
            _outputInput1._values[ i ] = _contextFreeClassifier._inputReconstructionK2._values[ offset +i ];
        }

        offset += inputs1;

        for( int i = 0; i < inputs2; ++i ) {
            _outputInput2._values[ i ] = _contextFreeClassifier._inputReconstructionK2._values[ offset +i ];
        }

        offset += inputs2;

        for( int i = 0; i < inputs2; ++i ) {
            _outputInput3._values[ i ] = _contextFreeClassifier._inputReconstructionK2._values[ offset +i ];
        }
    }*/

    protected void updateInputPredictionError() {
        // no error if classification didn't change
        _predictionOldErrorInput1 = 0f;
        _predictionOldErrorInput2 = 0f;

        if( !_classificationChanged ) {
            return;
        }

        // use the active cells in _cellActivity
        // reproject back into the input space.
        Data predictedInput = new Data( _contextFreeClassifier._inputValues._dataSize );

        _contextFreeClassifier.reconstruct( _predictionOld, null, predictedInput );

        int offset = 0;
        int inputs1 = _input1.getSize();
        int inputs2 = _input2.getSize();
        int inputs3 = _input3.getSize();

        float sumAbsDiff1 = 0f;
        float sumAbsDiff2 = 0f;
        float sumAbsDiff3 = 0f;

        for( int i = 0; i < inputs1; ++i ) {
            float i1 = predictedInput._values[ offset +i ];
            float i2 = _contextFreeClassifier._inputValues._values[ offset +i ];
            sumAbsDiff1 += Math.abs( i1 - i2 );
        }

        offset += inputs1;

        for( int i = 0; i < inputs2; ++i ) {
            float i1 = predictedInput._values[ offset +i ];
            float i2 = _contextFreeClassifier._inputValues._values[ offset +i ];
            sumAbsDiff2 += Math.abs( i1 - i2 );
        }

        offset += inputs2;

        for( int i = 0; i < inputs3; ++i ) {
            float i1 = predictedInput._values[ offset +i ];
            float i2 = _contextFreeClassifier._inputValues._values[ offset +i ];
            sumAbsDiff3 += Math.abs( i1 - i2 );
        }

        _predictionOldErrorInput1 = sumAbsDiff1;
        _predictionOldErrorInput2 = sumAbsDiff2;
        _predictionOldErrorInput3 = sumAbsDiff3;
    }

    protected void updateInputOutputPredicted() {
        // public void reconstruct( Data hiddenActivity, Data reconstructionWeightedSum, Data reconstructionTransfer ) {
//        _contextFreeClassifier.reconstruct( _predictionNew, null, )
    }

    protected boolean contextFreeClassificationChanged() {
        boolean changed = !_transient._contextFreeActive.equals( _transient._contextFreeActiveNew );
        return changed;
    }

    protected void onClassificationChanged() {
        _contextFreeActivityOld.copy( _contextFreeActivityNew );
        _contextFreeActivityNew.copy( _contextFreeActivity );

        _transient._contextFreeActiveOld = _transient._contextFreeActiveNew;
        _transient._contextFreeActiveNew = _transient._contextFreeActive;

        //updateInputOutput(); not that useful

        // 3. Form the input to the contextual classifier from the new and previous context-free classification
//        updateContextualClassification();
//
//        // 4. If contextual classification unchanged, do nothing else
//        if( !contextualClassificationChanged() ) {
//            return;
//        }

        // 6. Update & train the prediction of the next contextual classification, given old and new bits.
        updatePrediction();

        // 5. Update the region output with incorrectly predicted bits. Remove old predicted bits until the desired
        //    output sparsity is achieved.
        updateOutput();

//        // 6. Update & train the prediction of the next contextual classification, given old and new bits.
//        updatePrediction();

        updateInputOutputPredicted();
        updateInputPredictionError();
        //updateSpikeAges();
    }

    protected void updateCellMask() {
        // if the cell spikes, it can't be active again until spikeAge > minAge
        int cells = _output.getSize();

        for( int i = 0; i < cells; ++i ) {

//            float spikeAge = _spikeAge._values[ i ];
            float spikeOutput = _output._values[ i ]; // currently output is spiking
            float spikeOutputNew = _contextFreeActivityNew._values[ i ]; // most recent value
//            float spikeOutputOld = _contextFreeActivityNew._values[ i ]; // most recent value

            float mask = 1f; // allow

            // disallow if active output bit, but not currently active. Need to pick a different cell.
            if( spikeOutputNew == 0f ) { // not currently active
                if( spikeOutput == 1f ) { // .. but still in the active output
                    mask = 0f; // disallow it to be selected again.
                }
            }

            // spikeAge
            // so if minSpikeAge = 3
            // 1 = spiked last time
            // 2 = inhibited
            // 3 = inhibited
            // 4 = allowed
//            if( ( spikeAge > 1 ) && ( spikeAge <= _minSpikeAge ) ) {
//            if( ( spikeAge != 0 ) && ( spikeAge <= _minSpikeAge ) ) {
//                mask = 0f;     // don't allow cells to fire again too quickly.
//            }

            _contextFreeClassifier._cellMask._values[ i ] = mask;
        }
    }

    protected void updateSpikeAges() {
        // update age on classification only
        // update the spike age and classifier cell mask
        // spikeAge += 1
        // if( cell active ) spikeAge = 0
        // if the cell spikes, it can't be active again until spikeAge > minAge

        int cells = _output.getSize();

        for( int i = 0; i < cells; ++i ) {

            float spikeAge = _spikeAge._values[ i ];
            float spikeOutput = _output._values[ i ]; // currently output is spiking

            if( spikeOutput == 1f ) {
                spikeAge = 1;
            }
            else {
                if( spikeAge != 0 ) {
                    spikeAge += 1; // aka time since spike.
                }
            }

            // 0 = never spiked. 1 = spiked now
            //
            // so if minSpikeAge = 3
            // 1 = spiked
            // 2 = inhibited
            // 3 = inhibited
            // 4 = allowed

            _spikeAge._values[ i ] = spikeAge;
        }
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
        updateOutputExcludePredicted( _transient._contextFreeActiveNew );
//        updateOutputBinaryAgeLimit( _transient._contextFreeActiveNew );
//        updateOutputBinary( _transient._contextFreeActiveNew );
//        updateOutputUnit( _transient._contextFreeActiveNew );
    }

    protected void updateOutputExcludePredicted( HashSet< Integer > activeNew ) {

        // 1. Set FP / FN status of all currently active bits.
        _predictionFP.set( 0.f );
        _predictionFN.set( 0.f );

        _transient._predictionFP = new HashSet< Integer >();
        _transient._predictionFN = new HashSet< Integer >();

        // calculate FP and FN errors
        for( Integer i : activeNew ) {

            //dont include cells that are already active in fns?

            float prediction           = _predictionOld._values[ i ]; // most recent prediction, which hasn't been updated yet.
            float predictionInhibition = _predictionInhibition._values[ i ];

            boolean error = false;

            if( predictionInhibition > 0.f ) {
                error = true; // if you inhibit the prediction, then it can't be predicted
            }
            else if( prediction < 1.f ) {
                error = true;
            }

            // rule: don't re-add old bits. they only get added when they turn on.
            // Is this a good thing? Maybe this bit represents something in the new input too.
            // But, this is kinda irrelevant because there's now a refractory period in the classification.
            // So, there's not much point in this, because it means the classification has changed
            // at least N times before the bit can become active again anyway, so it'd be a genuine re-activation.
            if( _transient._contextFreeActiveOld.contains( i ) ) {
                error = false; // if already active before, don't re-add it.
            }

            if( error ) {
                _transient._predictionFN.add( i );
                _predictionFN._values[ i ] = 1.f;
            }
        }

        HashSet< Integer > predictions = _predictionOld.indicesMoreThan( 0.5f ); //_predictor._statePredicted.indicesMoreThan( 0.5f );

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
        HashSet< Integer > predictionsNew = _predictionNew.indicesMoreThan( 0.5f ); //_predictor._statePredicted.indicesMoreThan( 0.5f );

        int outputBits = _output.getSize();

        _transient._output = new HashSet< Integer >();

        for( int i = 0; i < outputBits; ++i ) {
            float age = _outputAge._values[ i ];

            if( age == 0.f ) {
                continue;
            }

            age += 1.f;

            // If we want a max pooling to disambiguate the sequences where they loop, then we limit the max age
            // But age can be affected by just 1 new bit.
            // The method of limiting output density is a more robust way because one new bit barely changes the output.
            // But, a lot of new bits will change the output a lot.

            boolean cancelOutput = false;

//            if( age > _maxSlowAge ) {
//                cancelOutput = false;
//            }

            if( predictionsNew.contains( i ) ) {
                cancelOutput = true;
            }

            if( cancelOutput ) {
                age = 0; // no longer an output
                _output._values[ i ] = 0.f;
            }
            else {
                _output._values[ i ] = 1.f;
                _transient._output.add( i );
            }

            _outputAge._values[ i ] = age; // store new age, either incremented or killed
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
        float slowSparsityFactor = _rc.getSlowSparsity();
        float currentSparsity = activeNew.size();//_contextFreeClassifier._c.getSparsity();
        int maxRank = (int)( slowSparsityFactor * currentSparsity );
        boolean findMaxima = false; // keep the youngest

        HashSet< Integer > youngOutput = new HashSet< Integer >();
        Ranking.getBestValuesRandomTieBreak( ranking, findMaxima, maxRank, youngOutput, _rc.getRandom() );

        for( Integer i : _transient._output ) {

            if( youngOutput.contains( i ) ) {
                continue; // young enough to keep
            }

            // turn it off:
            _output   ._values[ i ] = 0.f;
            _outputAge._values[ i ] = 0.f;
        }
    }

    protected void updateOutputBinaryAgeLimit( HashSet< Integer > activeNew ) {

        // 1. Set FP / FN status of all currently active bits.
        _predictionFP.set( 0.f );
        _predictionFN.set( 0.f );

        _transient._predictionFP = new HashSet< Integer >();
        _transient._predictionFN = new HashSet< Integer >();

        // calculate FP and FN errors
        for( Integer i : activeNew ) {

            //dont include cells that are already active in fns?

            float prediction           = _predictionNew._values[ i ]; // most recent prediction, which hasn't been updated yet.
            float predictionInhibition = _predictionInhibition._values[ i ];

            boolean error = false;

            if( predictionInhibition > 0.f ) {
                error = true; // if you inhibit the prediction, then it can't be predicted
            }
            else if( prediction < 1.f ) {
                error = true;
            }

            // rule: don't re-add old bits. they only get added when they turn on.
            // Is this a good thing? Maybe this bit represents something in the new input too.
            // But, this is kinda irrelevant because there's now a refractory period in the classification.
            // So, there's not much point in this, because it means the classification has changed
            // at least N times before the bit can become active again anyway, so it'd be a genuine re-activation.
            if( _transient._contextFreeActiveOld.contains( i ) ) {
                error = false; // if already active before, don't re-add it.
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

            // If we want a max pooling to disambiguate the sequences where they loop, then we limit the max age
            // But age can be affected by just 1 new bit.
            // The method of limiting output density is a more robust way because one new bit barely changes the output.
            // But, a lot of new bits will change the output a lot.
//            if( age > _maxSlowAge ) {
//                age = 0; // no longer an output
//                _output._values[ i ] = 0.f;
//            }
//            else {
                _transient._output.add( i );
//            }

            _outputAge._values[ i ] = age; // store new age, either incremented or killed
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
        float slowSparsityFactor = _rc.getSlowSparsity();
        float currentSparsity = activeNew.size();//_contextFreeClassifier._c.getSparsity();
        int maxRank = (int)( slowSparsityFactor * currentSparsity );
        boolean findMaxima = false; // keep the youngest

        HashSet< Integer > youngOutput = new HashSet< Integer >();
        Ranking.getBestValuesRandomTieBreak( ranking, findMaxima, maxRank, youngOutput, _rc.getRandom() );

        for( Integer i : _transient._output ) {

            if( youngOutput.contains( i ) ) {
                continue; // young enough to keep
            }

            // turn it off:
            _output   ._values[ i ] = 0.f;
            _outputAge._values[ i ] = 0.f;
        }
    }

    protected void updateOutputBinary( HashSet< Integer > activeNew ) {

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
        float slowSparsityFactor = _rc.getSlowSparsity();
        float currentSparsity = activeNew.size();//_contextFreeClassifier._c.getSparsity();
        int maxRank = (int)( slowSparsityFactor * currentSparsity );
        boolean findMaxima = false; // keep the youngest

        HashSet< Integer > youngOutput = new HashSet< Integer >();
        Ranking.getBestValuesRandomTieBreak( ranking, findMaxima, maxRank, youngOutput, _rc.getRandom() );

        for( Integer i : _transient._output ) {

            if( youngOutput.contains( i ) ) {
                continue; // young enough to keep
            }

            // turn it off:
            _output   ._values[ i ] = 0.f;
            _outputAge._values[ i ] = 0.f;
        }
    }

    protected void updateOutputUnit( HashSet< Integer > activeNew ) {

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

        _output.mul( 0.8f );

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
        float slowSparsityFactor = _rc.getSlowSparsity();
        float currentSparsity = activeNew.size();//_contextFreeClassifier._c.getSparsity();
        int maxRank = (int)( slowSparsityFactor * currentSparsity );
        boolean findMaxima = false; // keep the youngest

        HashSet< Integer > youngOutput = new HashSet< Integer >();
        Ranking.getBestValuesRandomTieBreak( ranking, findMaxima, maxRank, youngOutput, _rc.getRandom() );

        for( Integer i : _transient._output ) {

            if( youngOutput.contains( i ) ) {
                continue; // young enough to keep
            }

            // turn it off:
            _output   ._values[ i ] = 0.f;
            _outputAge._values[ i ] = 0.f;
        }

        // age the traces of all remaining bits
    }

    protected void updatePrediction() {
//        int density = _contextualClassifier._c.getSparsity();
//        updatePrediction( _contextualActivityOld, _contextualActivityNew, density );
//        int density = _contextFreeClassifier._c.getSparsity();
        int density = _contextFreeActivityNew.indicesMoreThan( 0f ).size();
        updatePrediction( _contextFreeActivityOld, _contextFreeActivityNew, density );
    }

    protected void updatePrediction( Data activityOld, Data activityNew, int density ) {
//        _predictor.trainLinear( activityOld, activityNew );
        _predictor.trainSpikeTiming( activityOld, activityNew );
        _predictor.predict( activityNew, density );
        _predictionOld .copy( _predictionNew ); // the old prediction
        _predictionNew .copy( _predictor._statePredicted ); // copy the new prediction
        _predictionNewReal.copy( _predictor._statePredictedReal );
    }
}
