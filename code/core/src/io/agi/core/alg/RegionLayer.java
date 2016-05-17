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

import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.SparseHebbianLearning;
import io.agi.core.data.*;
import io.agi.core.math.Geometry;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.*;

/**
 * A computational analogy to a layer of Pyramidal cells as found in Cortex layers 2,3 or 5, and intermingled interneurons
 * whose role is to generate sparse activity in the Pyramidal cells.
 *
 * The RegionLayer uses self-organization at two scales and performs both spatial pooling (dimensionality reduction and
 * pattern recognition) and temporal pooling, sequence learning. It uses Predictive Coding for the latter. Activation is
 * sparse, so the output encoding is Sparse Coding as well. All learning is unsupervised and fully online.
 *
 * The output of a RegionLayer is designed to be a suitable input for another RegionLayer. The parameterization of the
 * RegionLayer is designed so that you don't need to know anything about the statistics of the learning problem presented
 * to an arbitrarily deep RegionLayer. Instead, you just make sure the learning rate is slow enough and the parameters
 * are suitable for the size of region and the size of its input (ie. a fixed branching factor).
 *
 * RegionLayers can be stacked to generate deep representations. Therefore, a hierarchy of RegionLayers produces an
 * Online, Deep Unsupervised Representation.
 *
 * Created by dave on 14/05/16.
 */
public class RegionLayer extends NamedObject {

    // Data structures
    public Data _ffInput;
    public Data _ffInputOld;
    public Data _fbInput;
    public Data _fbInputOld;

    public Data _outputUnfoldedActivityRaw;
    public Data _outputUnfoldedActivity;
    public Data _outputUnfoldedPredictionRaw;
    public Data _outputUnfoldedPrediction;

    public Data _regionActivityOld;
    public Data _regionActivityNew;
    public Data _regionActivity;

    public Data _regionPredictionOld;
    public Data _regionPredictionNew;
    public Data _regionPredictionRaw;

    public Data _regionPredictionFP;
    public Data _regionPredictionFN;

    public Data _regionPredictorContext;
    public Data _regionPredictorWeights;

    // Sparse structures (temporary)
    protected RegionLayerTransient _transient;

    // Member objects
    public RegionLayerFactory _rf;
    public RegionLayerConfig _rc;

    public GrowingNeuralGas _organizer;
    public HashMap< Integer, GrowingNeuralGas > _classifiers = new HashMap< Integer, GrowingNeuralGas >();
    public SparseHebbianLearning _predictor; // actually this one object stands in for one predictor per column, because the columns may update asynchronously

    public RegionLayer( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( RegionLayerFactory rf, RegionLayerConfig rc ) {
        _rf = rf;
        _rc = rc;

        setupObjects();
        setupData();

        reset();
    }

    protected void setupObjects() {
        _organizer = _rf.createOrganizer( this );

        Point organizerSizeCells = _rc.getOrganizerSizeCells();

        for( int y = 0; y < organizerSizeCells.y; ++y ) {
            for( int x = 0; x < organizerSizeCells.x; ++x ) {
                GrowingNeuralGas classifier = _rf.createClassifier( this );
                int regionOffset = _rc.getOrganizerOffset( x, y );
                _classifiers.put( regionOffset, classifier );
            }
        }

        // Hebbian Predictor:
        Point fbInputSize = _rc.getFbInputSize();
        int fbInputArea = fbInputSize.x * fbInputSize.y;
        float predictorLearningRate = _rc.getPredictorLearningRate();// 0.1f;
        int hebbianStates = _rc.getHebbianPredictorStates();
        int hebbianContext = _rc.getHebbianPredictorContext( fbInputArea );
        _predictor = new SparseHebbianLearning();
        _predictor.setup( hebbianStates, hebbianContext, predictorLearningRate );
    }

    protected void setupData() {
        Point ffInputSize = _rc.getFfInputSize();
        Point fbInputSize = _rc.getFbInputSize();
        Point regionSize = _rc.getRegionSizeCells();

        int predictorContextSize = _predictor._context.getSize();
        int predictorWeightsSize = _predictor._weights.getSize();
        int hebbianPredictorInputs  = _rc.getHebbianPredictorContextSizeRegion( predictorContextSize );
        int hebbianPredictorWeights = _rc.getHebbianPredictorWeightsSizeRegion( predictorWeightsSize );

        DataSize dataSizeInputFF = DataSize.create( ffInputSize.x, ffInputSize.y );
        DataSize dataSizeInputFB = DataSize.create( fbInputSize.x, fbInputSize.y );
        DataSize dataSizeRegion = DataSize.create( regionSize.x, regionSize.y );

        _ffInput = new Data( dataSizeInputFF );
        _ffInputOld = new Data( dataSizeInputFF );
        _fbInput = new Data( dataSizeInputFB );
        _fbInputOld = new Data( dataSizeInputFB );

        _outputUnfoldedActivityRaw   = new Data( dataSizeInputFF );
        _outputUnfoldedActivity      = new Data( dataSizeInputFF );
        _outputUnfoldedPredictionRaw = new Data( dataSizeInputFF );
        _outputUnfoldedPrediction    = new Data( dataSizeInputFF );

        _regionActivityOld = new Data( dataSizeRegion );
        _regionActivityNew = new Data( dataSizeRegion );
        _regionActivity = new Data( dataSizeRegion );

        _regionPredictionOld = new Data( dataSizeRegion );
        _regionPredictionNew = new Data( dataSizeRegion );
        _regionPredictionRaw = new Data( dataSizeRegion );

        _regionPredictionFP = new Data( dataSizeRegion );
        _regionPredictionFN = new Data( dataSizeRegion );

        _regionPredictorContext = new Data( DataSize.create( hebbianPredictorInputs ) );
        _regionPredictorWeights = new Data( DataSize.create( hebbianPredictorWeights ) );
    }

    public Data getFfInput() {
        return _ffInput;
    }

    public Data getFbInput() {
        return _fbInput;
    }

    public void reset() {
        _transient = null;

        _organizer.reset();
        _predictor.reset();

        Point p = _rc.getOrganizerSizeCells();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                int regionOffset = _rc.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = _classifiers.get( regionOffset );
                classifier.reset();
            }
        }
    }

    public void update() {
        _transient = new RegionLayerTransient(); // this replaces all the transient data structures, ensuring they are truly transient

        updateSparseInput(); // region wide sparse input
        updateOrganizer(); // does nothing if input unchanged

        // update all the classifiers and thus the set of active cells in the region
        _regionActivity.set( 0.f ); // clear
//        _transient._regionActiveCells = new ArrayList< Integer >(); // clear
//        _transient._classifierActiveCells.clear();

        updateClassifiers(); // adds to _transient._regionActiveCells, _transient._classifierActiveCells, and _regionActivity

        _ffInputOld.copy( _ffInput );

        boolean classificationChanged = hasClassificationChanged(); // based on current value of _regionActivityNew
        if( classificationChanged ) {
            _regionActivityOld.copy( _regionActivityNew );
            _regionActivityNew.copy( _regionActivity );

            // Note: Must train predictor based on old activation and prediction output, before generating a new prediction
            trainPredictor(); // based on previous activation state and weights.
            updateRegionOutput(); // based on previous prediction and current classification
        }

        boolean feedbackChanged = hasFbInputChanged();
        if( classificationChanged || feedbackChanged ) {
            updatePrediction(); // make a new prediction
            updateUnfoldedOutput();
        }
    }

    protected void updateUnfoldedOutput() {
//        unfold( _regionActivityNew, _outputUnfoldedActivity );

        // Remove unchanged cols from current state reconstruction.
        HashSet< Integer > regionBits = _regionActivityNew.indicesMoreThan( 0.f ); // find all the active bits.

        for( Integer c : _transient._unchangedCells ) {
            regionBits.remove( c );
        }

        unfold( regionBits, _outputUnfoldedActivityRaw );
        unfold( _regionPredictionNew, _outputUnfoldedPredictionRaw );

        // now threshold:
        _outputUnfoldedActivityRaw  .scaleRange( 0.f, 1.f );
        _outputUnfoldedPredictionRaw.scaleRange( 0.f, 1.f );
        _outputUnfoldedActivity  .copy( _outputUnfoldedActivityRaw );
        _outputUnfoldedPrediction.copy( _outputUnfoldedPredictionRaw );
        _outputUnfoldedActivity  .thresholdMoreThan( 0.5f, 1.f, 0.f );
        _outputUnfoldedPrediction.thresholdMoreThan( 0.5f, 1.f, 0.f );
//        Otsu.apply( _outputUnfoldedActivityRaw  , _outputUnfoldedActivity  , 20, 0.f, 1.f );
//        Otsu.apply( _outputUnfoldedPredictionRaw, _outputUnfoldedPrediction, 20, 0.f, 1.f );
    }

    /**
     * Invert the transformation from the original input into a distributed set of bits. Since the transformation is
     * lossy, the inverse is also nonexact.
     *
     * Since each classifier only has a few input bits in its receptive field, and the receptive field varies in size,
     * we don't know how to consider zero bits. They don't necessarily have any opinion on some bits.
     *
     * @param region
     * @param ffInput
     */
    public void unfold( Data region, Data ffInput ) {

//        float threshold = 0.5f; // this is as meaningful as anything else..

        HashSet< Integer > regionBits = region.indicesMoreThan( 0.f ); // find all the active bits.

        unfold( regionBits, ffInput );
    }

    public void unfold( HashSet< Integer > regionBits, Data ffInput ) {//, Float threshold ) {

        ffInput.set( 0.f );

        if( regionBits.isEmpty() ) {
            return;
        }

        int weights = ffInput.getSize();

//        float bitWeight = 1.f / (float)regionBits.size();

        for( Integer i : regionBits ) {

            Point xyRegion = _rc.getRegionGivenOffset( i );
            Point xyOrganizer = _rc.getOrganizerCoordinateGivenRegionCoordinate( xyRegion.x, xyRegion.y );
            Point xyClassifier = _rc.getClassifierCoordinateGivenRegionCoordinate( xyRegion.x, xyRegion.y );
            // TODO dont unfold unchanged columns (in activity, with prediction, cant tell)?
            int organizerOffset = _rc.getOrganizerOffset( xyOrganizer.x, xyOrganizer.y );
            GrowingNeuralGas classifier = _classifiers.get( organizerOffset );

            int cell = classifier._c.getCell( xyClassifier.x, xyClassifier.y );

            for( int w = 0; w < weights; ++w ) {

                int weightsOrigin = cell * weights;
                int weightsOffset = weightsOrigin +w;
                float weight = classifier._cellWeights._values[ weightsOffset ];

//                weight *= bitWeight;
//                if( threshold != null ) {
//                    if( weight > threshold ) {
                        ffInput._values[ w ] += weight; // either was zero, or was 1. Either way the update is correct.
//                    }
//                }
            }
        }
    }

    protected void updateSparseInput() {
        // Find the sparse input bits:
        _transient._ffInputActive = _ffInput.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._fbInputActive = _fbInput.indicesMoreThan( 0.f ); // find all the active bits.
    }

    /**
     * Trains the receptive fields of the classifiers via a specified number of samples.
     */
    protected void updateOrganizer() {

        boolean learn = _rc.getLearn();
        if( !learn ) {
            return;
        }

        _organizer._c.setLearn( true );

        boolean inputChanged = hasFfInputChanged();
        if( !inputChanged ) {
            return; // don't do anything.
        }

        int nbrActiveInput = _transient._ffInputActive.size();
        if( nbrActiveInput == 0 ) {
            return; // can't train, ignore blank patterns.
        }

        Object[] activeInput = _transient._ffInputActive.toArray();

        Data inputValues = _organizer.getInput();

        Point inputSize = Data2d.getSize( _ffInput );

        // randomly sample a fixed number of input bits.
        float samplesFraction = _rc.getReceptiveFieldsTrainingSamples();
        float sampleArea = (float)_ffInput.getSize();
        int samples = (int)( samplesFraction * sampleArea ); // e.g. 0.1 (10%) of the input area

        // TODO: Consider limiting samples to the number of active input bits, to reduce overtraining on these.
        for( int s = 0; s < samples; ++s ) {

            int sample = _rc._r.nextInt( nbrActiveInput );

            Integer offset = ( Integer ) activeInput[ sample ];

            Point p = Data2d.getXY( _ffInput._dataSize, offset );

            float x_i = ( float ) p.x / ( float ) inputSize.x;
            float y_i = ( float ) p.y / ( float ) inputSize.y;

            inputValues._values[ 0 ] = x_i;
            inputValues._values[ 1 ] = y_i;

            _organizer.update(); // train the organizer to look for this value.
        }
    }

    /**
     * Returns the receptive field centroid, in pixels, of the specified classifier.
     *
     * @param xClassifier
     * @param yClassifier
     * @return
     */
    public float[] getClassifierReceptiveField( int xClassifier, int yClassifier ) {
        float[] rf = new float[ RegionLayerConfig.RECEPTIVE_FIELD_DIMENSIONS ];

        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );
        int organizerOffset = classifierOffset * RegionLayerConfig.RECEPTIVE_FIELD_DIMENSIONS;

        Point inputSize = Data2d.getSize( _ffInput );

        float rf_x = _organizer._cellWeights._values[ organizerOffset + 0 ];
        float rf_y = _organizer._cellWeights._values[ organizerOffset + 1 ];

        rf_x *= inputSize.x;
        rf_y *= inputSize.y;

        rf[ 0 ] = rf_x; // now in pixel coordinates, whereas it is trained as unit coordinates
        rf[ 1 ] = rf_y;

        return rf;
    }

    protected void updateClassifiers() {
        Point p = _rc.getOrganizerSizeCells();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                // only update active
                int organizerOffset = _rc.getOrganizerOffset( x, y );
                float mask = _organizer._cellMask._values[ organizerOffset ];
                if( mask != 1.f ) {
                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
                }

                rankClassifierReceptiveField( x, y );
//                updateClassifierReceptiveFields( x, y );
//                updateClassifier( x, y ); // adds to _transient._regionActiveCells and _regionActivity
            }
        }

        updateClassifierInput();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                // only update active
                int organizerOffset = _rc.getOrganizerOffset( x, y );
                float mask = _organizer._cellMask._values[ organizerOffset ];
                if( mask != 1.f ) {
                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
                }

                updateClassifier( x, y ); // adds to _transient._regionActiveCells and _regionActivity
            }
        }
    }

//    I could make each classifier remember its average distance, and exclude anything beyond that? - but this has weird failure modes e.g. one bit brings average down to almost zero.
//    I could have a threshold which is a fraction of the input size? But this is a parameter and might not suit very spread out bits in higher regions.
//    I could limit the number of classifiers per bit. Originally I thought in combination with the max bits per classifier, but in fact this is better alone?
    protected void rankClassifierReceptiveField( int xClassifier, int yClassifier ) {

        // find the closest N active input to col.
        // Do this with ranking.
        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );

//        int columnInputs = _rc.getReceptiveFieldSize();

        float[] rf = getClassifierReceptiveField( xClassifier, yClassifier ); // in pixels units
        float rf_x = rf[ 0 ];
        float rf_y = rf[ 1 ];

//        Ranking r = new Ranking();
//        TreeMap< Float, ArrayList< Integer > > classifierRanking = _transient.getRankingLazy( _transient._classifierActiveInputRanking, classifierOffset );

        for( Integer i : _transient._ffInputActive ) {
            Point p = Data2d.getXY( _ffInput._dataSize, i );

            float d = Geometry.distanceEuclidean2d( ( float ) p.getX(), ( float ) p.getY(), rf_x, rf_y );

            TreeMap< Float, ArrayList< Integer > > activeInputRanking = _transient.getRankingLazy( _transient._activeInputClassifierRanking, i );

            //Ranking.add( r._ranking, d, i ); // add input i with quality d (distance) to r.
//            Ranking.add( classifierRanking, d, i ); // add input i with quality d (distance) to r.

            // also rank by classifier:
            Ranking.add( activeInputRanking, d, classifierOffset ); // add classifier with quality d (distance) to i.
        }
    }

    protected void updateClassifierInput() {

        int classifiersPerBit = _rc.getClassifiersPerBit();
        boolean max = false; // ie min [distance]
        int maxRank = classifiersPerBit;

        for( Integer i : _transient._ffInputActive ) {
            TreeMap< Float, ArrayList< Integer > > activeInputRanking = _transient.getRankingLazy( _transient._activeInputClassifierRanking, i );

            ArrayList< Integer > activeInputClassifiers = Ranking.getBestValues( activeInputRanking, max, maxRank ); // ok now we got the current set of inputs for the column

            for( Integer classifierOffset : activeInputClassifiers ) {
                _transient.addClassifierActiveInput( classifierOffset, i );
            }
        }
    }

    protected void updateClassifier( int xClassifier, int yClassifier ) {

        Point classifierOrigin = _rc.getRegionClassifierOrigin( xClassifier, yClassifier );
        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );

        ArrayList< Integer > activeInput = _transient.getClassifierActiveInput( classifierOffset );//_classifierActiveInput.get( classifierOffset );
        GrowingNeuralGas classifier = _classifiers.get( classifierOffset );

        int bestCell = 0;

        if( activeInput.isEmpty() ) {
            // don't update this classifier. Let it ignore the current input.
            // For stability, preserve the current active cell, even though the classifier had no input.
            Point classifierSizeCells = _rc.getClassifierSizeCells();

//            boolean bestCellFound = false;

            for( int yc = 0; yc < classifierSizeCells.y; ++yc ) {
                for( int xc = 0; xc < classifierSizeCells.x; ++xc ) {

                    int regionX = classifierOrigin.x + xc;
                    int regionY = classifierOrigin.y + yc;
                    int regionOffset = _rc.getRegionOffset( regionX, regionY );

                    float active = _regionActivityNew._values[ regionOffset ];

                    if( active > 0.f ) {
                        bestCell = classifier._c.getCell( xc, yc ); // find the original best cell in this column (classifier)
                        _transient._unchangedCells.add( regionOffset );
//                        bestCellFound = true;
                    }
                }
            }

            _transient._unchangedClassifiers.add( classifierOffset );
            // happens at the start only (verified)
//            if( !bestCellFound ) { // a bug?
//            }
        }
        else {
            boolean learn = _rc.getLearn();
            classifier._c.setLearn( learn );
            classifier.setSparseUnitInput( activeInput );
            classifier.update(); // trains with this sparse input.

            bestCell = classifier.getBestCell();
        }

        // Now deal with the best cell:
        int bestCellX = classifier._c.getCellX( bestCell );
        int bestCellY = classifier._c.getCellY( bestCell );

        int regionX = classifierOrigin.x + bestCellX;
        int regionY = classifierOrigin.y + bestCellY;
        int regionOffset = _rc.getRegionOffset( regionX, regionY );

        _regionActivity._values[ regionOffset ] = 1.f;

        _transient._regionActiveCells.add( regionOffset );
        _transient._classifierActiveCells.put( classifierOffset, bestCell );
    }

    protected boolean hasClassificationChanged() {
        // Since there can be only 1 bit per classifier, we can check if any bit has changed by only checking the
        // new '1' bits. They should all be the same in the old structure if there is no change.
        // In other words, look for any currently active bit that wasn't previously active.
        for( Integer i : _transient._regionActiveCells ) {
            float lastValue = _regionActivityNew._values[ i ];
            if( lastValue != 1.f ) {
                return true;
            }
        }

        return false;
    }

    protected boolean hasFbInputChanged() {
        if( _fbInputOld == null ) {
            return true;
        }

        int fbArea = _fbInput.getSize();

        for( int i = 0; i < fbArea; ++i ) {
            float oldValue = _fbInputOld._values[ i ];
            float newValue = _fbInput._values[ i ];

            if( oldValue != newValue ) {
                return true;
            }
        }

        return false;
    }

    protected boolean hasFfInputChanged() {
        if( _ffInputOld == null ) {
            return true;
        }

        int ffArea = _ffInput.getSize();

        for( int i = 0; i < ffArea; ++i ) {
            float oldValue = _ffInputOld._values[ i ];
            float newValue = 0.f;

            if( _transient._ffInputActive.contains( i ) ) {
                newValue = 1.f;
            }

            if( oldValue != newValue ) {
                return true;
            }
        }

        return false;
    }

    protected void updateRegionOutput() {

//        Point regionSizeCells = _rc.getRegionSizeCells();
        Point organizerSizeCells = _rc.getOrganizerSizeCells();
        Point classifierSizeCells = _rc.getClassifierSizeCells();

        int stride = organizerSizeCells.x * classifierSizeCells.x;

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

                // copy weights, context, state
                int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );

                boolean unchanged = _transient._unchangedClassifiers.contains( classifierOffset );

                if( unchanged ) {
                    continue; // leave FP/FN unchanged
                }

                for( int yc = 0; yc < classifierSizeCells.y; ++yc ) {
                    for( int xc = 0; xc < classifierSizeCells.x; ++xc ) {

//        for( int y = 0; y < regionSizeCells.y; ++y ) {
//            for( int x = 0; x < regionSizeCells.x; ++x ) {
                        int xr = ( xClassifier * classifierSizeCells.x ) + xc;
                        int yr = ( yClassifier * classifierSizeCells.y ) + yc;
                        int regionOffset = Data2d.getOffset( stride, xr, yr );

                        // Rules:
                        // Prediction must START and CONTINUE until cell becomes ACTIVE.
                        // If prediction ends too early - FP, then when cell active, FN.
                        // If prediction ends too late -
                        // If the cell becomes active after the prediction, OK
                        // If the cell doesnt become active during prediction, Bad.
//                int regionOffset = y * regionSizeCells.x + x;

                        // update regional output
                        float errorFP = 0.f;
                        float errorFN = 0.f;

                        // no error output for cells that didn't change.
                        // Hold earlier errors for these columns? It might be informative..
//                        if( unchanged ) {
//                            errorFP = _regionPredictionFP._values[ regionOffset ];
//                            errorFN = _regionPredictionFN._values[ regionOffset ];
//                        }
//                        else{
                            float     activeNew = _regionActivityNew  ._values[ regionOffset ];
                            float predictionOld = _regionPredictionNew._values[ regionOffset ]; // we didn't update the prediction yet, so use current prediction

                            // FN
                            if( ( activeNew == 1.f ) && ( predictionOld == 0.f ) ) {
                                errorFN = 1.f;
                            }

                            // FP
                            if( ( activeNew == 0.f ) && ( predictionOld == 1.f ) ) {
                                errorFP = 1.f;
                            }
//                        }

                        // store updated error state
                        _regionPredictionFP._values[ regionOffset ] = errorFP;
                        _regionPredictionFN._values[ regionOffset ] = errorFN;
                    }
                }

            }
        }

    }

    protected void updatePrediction() {

        // We get a stream of activeCells. This should be used for training as well as input.
        // The stream may not change each step.
        // The objective is to predict next cells up to the time they become active.
        // Should not predict too many (self enforced only)
        //
        // have a column-sized predictor and copy the weights and inputs to it.
        // Options: Hebbian learning, MLP
        //
        // Note that the prediction input doesn't affect the output, except that it may improve prediction. It doesnt
        // change the winning cell. So we can in fact use any cell to predict without consequences, except that prediction
        // improves or worsens and we have too many inputs.
        // So one way to do it would be to provide all cells at level +1 ... +N to predict. Or all cells with reciprocal relations.
        // That will scale well...

        // Create active context bits:
        HashSet< Integer > activeContext = new HashSet< Integer >();

        // put the local cells first:
        for( Integer i : _transient._regionActiveCells ) { // this is the latest set of active cells, used for a new prediction
            int j = i;
            activeContext.add( j );
        }

        // copy feedback:
        int regionSizeCells = _rc.getRegionAreaCells();
        for( Integer i : _transient._fbInputActive ) {
            int j = i + regionSizeCells;
            activeContext.add( j );
        }

        Point organizerSizeCells = _rc.getOrganizerSizeCells();
        Point classifierSizeCells = _rc.getClassifierSizeCells();

        int fbInputArea = _fbInput.getSize();
        int hebbianPredictorStates  = _rc.getHebbianPredictorStates();
        int hebbianPredictorContext = _rc.getHebbianPredictorContext( fbInputArea );
        int hebbianPredictorWeights = _predictor._weights.getSize();

        Data classifierState = new Data( DataSize.create( hebbianPredictorStates ) );

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

                // copy weights, context, state
                int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );
                int offsetContext = classifierOffset * hebbianPredictorContext;
                int offsetWeights = classifierOffset * hebbianPredictorWeights;

                // build the input state matrix for the hebbian module
                classifierState.set( 0.f );
                Integer bestCell = _transient._classifierActiveCells.get( classifierOffset );
                if( bestCell != null ) {
                    classifierState._values[ bestCell ] = 1.f;
                }

                // Copy other state for the hebbian module
                //                        .copyRange( that, offsetThis, offsetThat, range );
                _predictor._state  .copyRange( classifierState, 0, 0, hebbianPredictorStates );
                _predictor._context.copyRange( _regionPredictorContext, 0, offsetContext, hebbianPredictorContext );
                _predictor._weights.copyRange( _regionPredictorWeights, 0, offsetWeights, hebbianPredictorWeights );

                _predictor.updateContext( activeContext );
                _predictor.predict();

                // Copy changed state for hebbian module back to region permanent storage
                // copy? weights(n), context(y), state(n), predicted(n)
                //                      .copyRange( that, offsetThis, offsetThat, range );
                _regionPredictorContext.copyRange( _predictor._context, offsetContext, 0, hebbianPredictorContext );
//                _regionPredictorWeights.copyRange( _predictor._weights, offsetWeights, 0, hebbianPredictorWeights ); // unchanged

                // Extract the prediction values (raw)
                Point classifierOrigin = _rc.getRegionClassifierOrigin( xClassifier, yClassifier );

                for( int yc = 0; yc < classifierSizeCells.y; ++yc ) {
                    for( int xc = 0; xc < classifierSizeCells.x; ++xc ) {
                        int offsetPredictor = ( yc * classifierSizeCells.x ) + xc;

                        float pValue = _predictor._statePredictedRaw._values[ offsetPredictor ];

                        int regionX = classifierOrigin.x + xc;
                        int regionY = classifierOrigin.y + yc;
                        int regionOffset = _rc.getRegionOffset( regionX, regionY );
                        _regionPredictionRaw._values[ regionOffset ] = pValue;
                    }
                }
            }
        }

        _regionPredictionOld.copy( _regionPredictionNew );
//        _regionPredictionRaw.copy( output );

        findPredictionLocalMaxima();
    }

    /**
     * Since we always produce exactly one winner per column (classifier), we can safely take the prediction as the max
     * value over all the cells in the Column, for each Column. This doesn't affect the predictor, only the output of
     * the Region. Also doesn't affect classifiers or organizer.
     */
    protected void findPredictionLocalMaxima() {

        // This method doesn't need to ignore cols (classifiers) that are not in use due to the organizer not giving
        // them receptive fields, or cells that are not in use, because in both cases they will never be a FN case
        // (active but not predicted) so they will never be output. Secondly, the training of the predictor is unaffected
        // by this function. So the only effect these bad bits have, is in debugging (they will display as FP errors)
        Point regionSizeCols = _rc.getOrganizerSizeCells();
        Point classifierSizeCells = _rc.getClassifierSizeCells();

        _regionPredictionNew.set( 0.f ); // clear

        int stride = regionSizeCols.x * classifierSizeCells.x;

        for( int y = 0; y < regionSizeCols.y; ++y ) {
            for( int x = 0; x < regionSizeCols.x; ++x ) {

                float pMax = 0.f;
                int xMax = -1;
                int yMax = -1;

                for( int yc = 0; yc < classifierSizeCells.y; ++yc ) {
                    for( int xc = 0; xc < classifierSizeCells.x; ++xc ) {

                        int xr = ( x * classifierSizeCells.x ) + xc;
                        int yr = ( y * classifierSizeCells.y ) + yc;
                        int offset = Data2d.getOffset( stride, xr, yr );
                        float p = _regionPredictionRaw._values[ offset ];

                        if( p >= pMax ) {
                            pMax = p;
                            xMax = xc;
                            yMax = yc;
                        }
                    }
                }

                int xr = ( x * classifierSizeCells.x ) + xMax;
                int yr = ( y * classifierSizeCells.y ) + yMax;
                int offset = Data2d.getOffset( stride, xr, yr );
                _regionPredictionNew._values[ offset ] = 1.f;
            }
        }
    }

    protected void trainPredictor() {

        Point organizerSizeCells = _rc.getOrganizerSizeCells();
        Point classifierSizeCells = _rc.getClassifierSizeCells();

        int fbInputArea = _fbInput.getSize();
        int hebbianPredictorStates  = _rc.getHebbianPredictorStates();
        int hebbianPredictorContext = _rc.getHebbianPredictorContext( fbInputArea );
        int hebbianPredictorWeights = _predictor._weights.getSize();

        Data classifierStateOld = new Data( DataSize.create( hebbianPredictorStates ) );
        Data classifierStateNew = new Data( DataSize.create( hebbianPredictorStates ) );

        boolean learn = _rc.getLearn();

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

                int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );
                int offsetContext = classifierOffset * hebbianPredictorContext;
                int offsetWeights = classifierOffset * hebbianPredictorWeights;

                // build the state matrices for this predictor:
                classifierStateOld.set( 0.f );
                classifierStateNew.set( 0.f );

                Point classifierOrigin = _rc.getRegionClassifierOrigin( xClassifier, yClassifier );

//                int bestOld = 0;
//                int bestNew = 0;

                for( int yc = 0; yc < classifierSizeCells.y; ++yc ) {
                    for( int xc = 0; xc < classifierSizeCells.x; ++xc ) {
                        int offsetPredictor = ( yc * classifierSizeCells.x ) + xc;

                        int regionX = classifierOrigin.x + xc;
                        int regionY = classifierOrigin.y + yc;
                        int regionOffset = _rc.getRegionOffset( regionX, regionY );

                        float activityOld = _regionActivityOld._values[ regionOffset ];
                        if( activityOld > 0.f ) {
                            classifierStateOld._values[ offsetPredictor ] = 1.f;
//                            bestOld = offsetPredictor;
                        }

                        float activityNew = _regionActivityNew._values[ regionOffset ];
                        if( activityNew > 0.f ) {
                            classifierStateNew._values[ offsetPredictor ] = 1.f;
//                            bestNew = offsetPredictor;
                        }
                    }
                }

                // Copy other state for the hebbian module
                //                        .copyRange( that, offsetThis, offsetThat, range );
                _predictor._state  .copyRange( classifierStateOld, 0, 0, hebbianPredictorStates );
                _predictor._context.copyRange( _regionPredictorContext, 0, offsetContext, hebbianPredictorContext );
                _predictor._weights.copyRange( _regionPredictorWeights, 0, offsetWeights, hebbianPredictorWeights );

                if( learn ) {
                    _predictor.train( classifierStateNew );
                }
                _predictor.update( classifierStateNew );

                // Copy changed state for hebbian module back to region permanent storage
                // copy? weights(n), context(y), state(n), predicted(n)
                //                      .copyRange( that, offsetThis, offsetThat, range );
                _regionPredictorContext.copyRange( _predictor._context, offsetContext, 0, hebbianPredictorContext ); // changed (cleared)
                _regionPredictorWeights.copyRange( _predictor._weights, offsetWeights, 0, hebbianPredictorWeights ); // changed (trained)
            }
        }
    }
}
