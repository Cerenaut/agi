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
import io.agi.core.math.Geometry;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.awt.geom.Point2D;
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
    public Data _ffInput1;
    public Data _ffInput1Old;
    public Data _ffInput2;
    public Data _ffInput2Old;
    public Data _fbInput;
    public Data _fbInputOld;

    public Data _output1UnfoldedActivityRaw;
    public Data _output1UnfoldedActivity;
    public Data _output1UnfoldedPredictionRaw;
    public Data _output1UnfoldedPrediction;

    public Data _output2UnfoldedActivityRaw;
    public Data _output2UnfoldedActivity;
    public Data _output2UnfoldedPredictionRaw;
    public Data _output2UnfoldedPrediction;

    public Data _regionActivityOld;
    public Data _regionActivityNew;
    public Data _regionActivity;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    public Data _regionActivity1;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Data _regionPredictionOld;
    public Data _regionPredictionNew;
    public Data _regionPredictionRaw;

    public Data _regionPredictionFP;
    public Data _regionPredictionFN;
    public Data _regionPredictionInhibition;

    public Data _regionPredictorContext;
    public Data _regionPredictorWeights;

    // Sparse structures (temporary)
    protected RegionLayerTransient _transient;

    // Member objects
    public RegionLayerFactory _rf;
    public RegionLayerConfig _rc;

//    public GrowingNeuralGas _organizer;
//    public DynamicSelfOrganizingMap _organizer;
    public ParameterLessSelfOrganizingMap _organizer;
    public HashMap< Integer, GrowingNeuralGas > _classifiers = new HashMap< Integer, GrowingNeuralGas >();
//    public HashMap< Integer, PlasticNeuralGas > _classifiers = new HashMap< Integer, PlasticNeuralGas >();
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
//                PlasticNeuralGas classifier = _rf.createClassifier( this );
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
        Point ffInput1Size = _rc.getFfInput1Size();
        Point ffInput2Size = _rc.getFfInput2Size();
        Point fbInputSize = _rc.getFbInputSize();
        Point regionSize = _rc.getRegionSizeCells();

        int predictorContextSize = _predictor._context.getSize();
        int predictorWeightsSize = _predictor._weights.getSize();
        int hebbianPredictorInputs  = _rc.getHebbianPredictorContextSizeRegion( predictorContextSize );
        int hebbianPredictorWeights = _rc.getHebbianPredictorWeightsSizeRegion( predictorWeightsSize );

        DataSize dataSizeFfInput1 = DataSize.create( ffInput1Size.x, ffInput1Size.y );
        DataSize dataSizeFfInput2 = DataSize.create( ffInput2Size.x, ffInput2Size.y );
        DataSize dataSizeFbInput = DataSize.create( fbInputSize.x, fbInputSize.y );
        DataSize dataSizeRegion  = DataSize.create( regionSize.x, regionSize.y );

        // external inputs
        _ffInput1    = new Data( dataSizeFfInput1 );
        _ffInput1Old = new Data( dataSizeFfInput1 );
        _ffInput2    = new Data( dataSizeFfInput2 );
        _ffInput2Old = new Data( dataSizeFfInput2 );
        _fbInput    = new Data( dataSizeFbInput );
        _fbInputOld = new Data( dataSizeFbInput );

        // unfolded structures: input size.
        _output1UnfoldedActivityRaw   = new Data( dataSizeFfInput1 );
        _output1UnfoldedActivity      = new Data( dataSizeFfInput1 );
        _output1UnfoldedPredictionRaw = new Data( dataSizeFfInput1 );
        _output1UnfoldedPrediction    = new Data( dataSizeFfInput1 );

        _output2UnfoldedActivityRaw   = new Data( dataSizeFfInput2 );
        _output2UnfoldedActivity      = new Data( dataSizeFfInput2 );
        _output2UnfoldedPredictionRaw = new Data( dataSizeFfInput2 );
        _output2UnfoldedPrediction    = new Data( dataSizeFfInput2 );

        // region sized structures
        _regionActivityOld = new Data( dataSizeRegion );
        _regionActivityNew = new Data( dataSizeRegion );
        _regionActivity = new Data( dataSizeRegion );
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        _regionActivity1 = new Data( dataSizeRegion );
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        _regionPredictionOld = new Data( dataSizeRegion );
        _regionPredictionNew = new Data( dataSizeRegion );
        _regionPredictionRaw = new Data( dataSizeRegion );

        _regionPredictionFP         = new Data( dataSizeRegion );
        _regionPredictionFN         = new Data( dataSizeRegion );
        _regionPredictionInhibition = new Data( dataSizeRegion );

        _regionPredictorContext = new Data( DataSize.create( hebbianPredictorInputs ) );
        _regionPredictorWeights = new Data( DataSize.create( hebbianPredictorWeights ) );
    }

    public Data getFfInput1() {
        return _ffInput1;
    }

    public Data getFfInput2() {
        return _ffInput2;
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
//                PlasticNeuralGas classifier = _classifiers.get( regionOffset );
                classifier.reset();
            }
        }

        float defaultPredictionInhibition = _rc.getDefaultPredictionInhibition();
        _regionPredictionInhibition.set( defaultPredictionInhibition );
    }

    public void update() {
        _transient = new RegionLayerTransient(); // this replaces all the transient data structures, ensuring they are truly transient

        updateSparseInput(); // region wide sparse input
//        updateOrganizer(); // does nothing if input unchanged
        updateOrganizerUniform();

        // update all the classifiers and thus the set of active cells in the region
        _regionActivity.set( 0.f ); // clear
//        _transient._regionActiveCells = new ArrayList< Integer >(); // clear
//        _transient._columnActiveCells.clear();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        _regionActivity1.set( 0.f );
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        updateClassifiers(); // adds to _transient._regionActiveCells, _transient._columnActiveCells, and _regionActivity

        boolean inputChanged = hasFfInputChanged();
        if( !inputChanged ) {
            _ffInput1Old.copy( _ffInput1 );
            _ffInput2Old.copy( _ffInput2 );
        }

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

        unfold( regionBits          , _output1UnfoldedActivityRaw  , _output2UnfoldedActivityRaw );
        unfold( _regionPredictionNew, _output1UnfoldedPredictionRaw, _output2UnfoldedPredictionRaw );

        // now threshold:
        _output1UnfoldedActivityRaw  .scaleRange( 0.f, 1.f );
        _output1UnfoldedPredictionRaw.scaleRange( 0.f, 1.f );
        _output1UnfoldedActivity  .copy( _output1UnfoldedActivityRaw );
        _output1UnfoldedPrediction.copy( _output1UnfoldedPredictionRaw );
        _output1UnfoldedActivity  .thresholdMoreThan( 0.5f, 1.f, 0.f );
        _output1UnfoldedPrediction.thresholdMoreThan( 0.5f, 1.f, 0.f );

        _output2UnfoldedActivityRaw  .scaleRange( 0.f, 1.f );
        _output2UnfoldedPredictionRaw.scaleRange( 0.f, 1.f );
        _output2UnfoldedActivity  .copy( _output2UnfoldedActivityRaw );
        _output2UnfoldedPrediction.copy( _output2UnfoldedPredictionRaw );
        _output2UnfoldedActivity  .thresholdMoreThan( 0.5f, 1.f, 0.f );
        _output2UnfoldedPrediction.thresholdMoreThan( 0.5f, 1.f, 0.f );

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
     * @param ffInput1
     * @param ffInput2
     */
    public void unfold( Data region, Data ffInput1, Data ffInput2 ) {

//        float threshold = 0.5f; // this is as meaningful as anything else..

        HashSet< Integer > regionBits = region.indicesMoreThan( 0.f ); // find all the active bits.

        unfold( regionBits, ffInput1, ffInput2 );
    }

    public void unfold( HashSet< Integer > regionBits, Data ffInput1, Data ffInput2 ) {

        ffInput1.set( 0.f );
        ffInput2.set( 0.f );

        if( regionBits.isEmpty() ) {
            return;
        }

        int ffInput1Area = ffInput1.getSize();
        int ffInput2Area = ffInput2.getSize();
        int weights = ffInput1Area + ffInput2Area;

//        float bitWeight = 1.f / (float)regionBits.size();

        for( Integer i : regionBits ) {

            Point xyRegion     = _rc.getRegionGivenOffset( i );
            Point xyOrganizer  = _rc.getOrganizerCoordinateGivenRegionCoordinate( xyRegion.x, xyRegion.y );
            Point xyColumn     = _rc.getColumnCoordinateGivenRegionCoordinate( xyRegion.x, xyRegion.y );
            Point xyClassifier = _rc.getClassifierCellGivenColumnCell( xyColumn.x, xyColumn.y ); // convert from column to classifier cell

            // TODO dont unfold unchanged columns (in activity, with prediction, cant tell)?
            int organizerOffset = _rc.getOrganizerOffset( xyOrganizer.x, xyOrganizer.y );
            GrowingNeuralGas classifier = _classifiers.get( organizerOffset );
//            PlasticNeuralGas classifier = _classifiers.get( organizerOffset );

            int cell = classifier._c.getCell( xyClassifier.x, xyClassifier.y );

            for( int w = 0; w < weights; ++w ) {

                int weightsOrigin = cell * weights;
                int weightsOffset = weightsOrigin +w;
                float weight = classifier._cellWeights._values[ weightsOffset ];

                if( w < ffInput1Area ) {
                    ffInput1._values[ w ] += weight; // either was zero, or was 1. Either way the update is correct.
                }
                else {
                    int w2 = w - ffInput1Area;
                    ffInput2._values[ w2 ] += weight; // either was zero, or was 1. Either way the update is correct.
                }
            }
        }
    }

    protected void updateSparseInput() {
        // Find the sparse input bits:
        _transient._ffInput1Active = _ffInput1.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._ffInput2Active = _ffInput2.indicesMoreThan( 0.f ); // find all the active bits.
        _transient._fbInputActive = _fbInput.indicesMoreThan( 0.f ); // find all the active bits.
    }

    public    int _organizerIntervalsInput1X = 3; // 3 * 2 = 6
    public    int _organizerIntervalsInput2X = 2; // 3 * 2 = 6   by 4 * 1 = 4
    public    int _organizerIntervalsInput1Y = 4;
    public    int _organizerIntervalsInput2Y = 1;

    protected void updateOrganizerUniform() {

//        Point input1Size = Data2d.getSize( _ffInput1 );
//        Point input2Size = Data2d.getSize( _ffInput2 );

        _organizer._cellMask.set( 1.f );

        // input 1          input 2
        // _1_1_1_          _1_  = 0.5
        // 0.25, 0.5, 0.75
        // = 1/(n+1)
        int w1 = _organizerIntervalsInput1X;
        int w2 = _organizerIntervalsInput2X;
        int h1 = _organizerIntervalsInput1Y;
        int h2 = _organizerIntervalsInput2Y;

        float x1Span = 1.f / (float)( w1+1 );
        float x2Span = 1.f / (float)( w2+1 );
        float y1Span = 1.f / (float)( h1+1 );
        float y2Span = 1.f / (float)( h2+1 );

        Point p = _rc.getOrganizerSizeCells();
        int stride = 2 * 2; // dimensions * inputs

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                int classifierOffset = _rc.getOrganizerOffset( x, y );
                int organizerOffset = classifierOffset * stride;//RegionLayerConfig.RECEPTIVE_FIELD_DIMENSIONS;

                // sparsely changing:
                int x1 = x / w2; // e.g. if w2 = 3, 0,1,2 = 0, 3,4,5=1,
                int y1 = y / h2;

                int x2 = x % w2;
                int y2 = y % h2;

                float x1u = ( x1 + 1 ) * x1Span;
                float y1u = ( y1 + 1 ) * y1Span;
                float x2u = ( x2 + 1 ) * x2Span;
                float y2u = ( y2 + 1 ) * y2Span;

                _organizer._cellWeights._values[ organizerOffset + 0 ] = x1u;
                _organizer._cellWeights._values[ organizerOffset + 1 ] = y1u;
                _organizer._cellWeights._values[ organizerOffset + 2 ] = x2u;
                _organizer._cellWeights._values[ organizerOffset + 3 ] = y2u;
            }
        }
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

        HashSet< Integer > ffInputActive1 = _transient._ffInput1Active;
        HashSet< Integer > ffInputActive2 = _transient._ffInput2Active;

        // If we only train on change, remove any bits that WERE already active.
        if( _rc.getOrganizerTrainOnChange() ) {
            HashSet< Integer > remove1 = new HashSet< Integer >();

            for( Integer i : ffInputActive1 ) {
                float old = _ffInput1Old._values[ i ];
                if( old > 0.f ) {
                    remove1.add( i );
                }
            }

            for( Integer i : remove1 ) {
                ffInputActive1.remove( i );
            }

            HashSet< Integer > remove2 = new HashSet< Integer >();

            for( Integer i : ffInputActive2 ) {
                float old = _ffInput2Old._values[ i ];
                if( old > 0.f ) {
                    remove2.add( i );
                }
            }

            for( Integer i : remove2 ) {
                ffInputActive2.remove( i );
            }
        }

        int nbrActiveInput1 = ffInputActive1.size();
        int nbrActiveInput2 = ffInputActive2.size();
        int nbrActiveInput = nbrActiveInput1 + nbrActiveInput2;
        if( nbrActiveInput == 0 ) {
            return; // can't train, ignore blank patterns.
        }

        Object[] activeInput1 = ffInputActive1.toArray();
        Object[] activeInput2 = ffInputActive2.toArray();

        Data inputValues = _organizer.getInput();

        Point input1Size = Data2d.getSize( _ffInput1 );
        Point input2Size = Data2d.getSize( _ffInput2 );

        // randomly sample a fixed number of input bits.
        float samplesFraction = _rc.getReceptiveFieldsTrainingSamples();
        float sampleArea1 = (float)_ffInput1.getSize();
        float sampleArea2 = (float)_ffInput2.getSize();
        float sampleArea = sampleArea1 + sampleArea2; // so kinda an average of the two
        int samples = (int)( samplesFraction * sampleArea ); // e.g. 0.1 (10%) of the input area

        // TODO: Consider limiting samples to the number of active input bits, to reduce overtraining on these.
        for( int s = 0; s < samples; ++s ) {

            // pick a pair of points: one from each receptive field
            // we are targeting clusters of inputs, which will occur more due to relative frequency
            Point.Float sample1 = getSample( activeInput1, _ffInput1._dataSize, input1Size );
            Point.Float sample2 = getSample( activeInput2, _ffInput2._dataSize, input2Size );

            inputValues._values[ 0 ] = sample1.x;
            inputValues._values[ 1 ] = sample1.y;
            inputValues._values[ 2 ] = sample2.x;
            inputValues._values[ 3 ] = sample2.y;

            _organizer.update(); // train the organizer to look for this value.
        }
    }

    protected Point.Float getSample( Object[] activeInput, DataSize dataSize, Point dataSize2d ) {
        int length = activeInput.length;

        float xUnit = 0.5f;
        float yUnit = 0.5f; // the centre of the input, so will make it more compatible with other samplings

        if( length > 0 ) {
            int sample = _rc._r.nextInt( length );
            Integer offset = ( Integer ) activeInput[ sample ];
            Point p = Data2d.getXY( dataSize, offset );

            xUnit = ( float ) p.x / ( float ) dataSize2d.x;
            yUnit = ( float ) p.y / ( float ) dataSize2d.y;
        }

        Point.Float sample = new Point2D.Float( xUnit, yUnit );
        return sample;
    }
    /**
     * Returns the receptive field centroid, in pixels, of the specified classifier.
     *
     * @param xClassifier
     * @param yClassifier
     * @return
     */
    public float[] getClassifierReceptiveField( int xClassifier, int yClassifier ) {
        int dimensions = 2;
        int inputs = 2;
        int elements = dimensions * inputs;
        float[] rf = new float[ elements ];

        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );
        int organizerOffset = classifierOffset * elements;//RegionLayerConfig.RECEPTIVE_FIELD_DIMENSIONS;

        Point inputSize1 = Data2d.getSize( _ffInput1 );
        Point inputSize2 = Data2d.getSize( _ffInput2 );

        float rf1_x = _organizer._cellWeights._values[ organizerOffset + 0 ];
        float rf1_y = _organizer._cellWeights._values[ organizerOffset + 1 ];
        float rf2_x = _organizer._cellWeights._values[ organizerOffset + 2 ];
        float rf2_y = _organizer._cellWeights._values[ organizerOffset + 3 ];

        rf1_x *= inputSize1.x;
        rf1_y *= inputSize1.y;

        rf2_x *= inputSize2.x;
        rf2_y *= inputSize2.y;

        rf[ 0 ] = rf1_x; // now in pixel coordinates, whereas it is trained as unit coordinates
        rf[ 1 ] = rf1_y;
        rf[ 2 ] = rf2_x; // now in pixel coordinates, whereas it is trained as unit coordinates
        rf[ 3 ] = rf2_y;

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

                rankClassifierReceptiveFields( x, y );
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
    protected void rankClassifierReceptiveFields( int xClassifier, int yClassifier ) {

        // find the closest N cols to each active input bit
        float[] rf = getClassifierReceptiveField( xClassifier, yClassifier ); // in pixels units
        float xField1 = rf[ 0 ];
        float yField1 = rf[ 1 ];
        float xField2 = rf[ 2 ];
        float yField2 = rf[ 3 ];

        int inputOffset1 = 0;
        int inputOffset2 = _ffInput1.getSize();

        rankClassifierReceptiveField( xClassifier, yClassifier, _ffInput1, _transient._ffInput1Active, xField1, yField1, inputOffset1 );
        rankClassifierReceptiveField( xClassifier, yClassifier, _ffInput2, _transient._ffInput2Active, xField2, yField2, inputOffset2 );
    }

    protected void rankClassifierReceptiveField( int xClassifier, int yClassifier, Data ffInput, HashSet< Integer > ffInputActive, float xField, float yField, int inputOffset ) {
        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );

        for( Integer i : ffInputActive ) {
            Point p = Data2d.getXY( ffInput._dataSize, i );

            float d = Geometry.distanceEuclidean2d( ( float ) p.getX(), ( float ) p.getY(), xField, yField );
            int inputBit = i + inputOffset;

            TreeMap< Float, ArrayList< Integer > > activeInputRanking = _transient.getRankingLazy( _transient._activeInputClassifierRanking, inputBit );

            // Rank by classifier:
            Ranking.add( activeInputRanking, d, classifierOffset ); // add classifier with quality d (distance) to i.
        }

    }

    protected void updateClassifierInput() {

        int classifiersPerBit1 = _rc.getClassifiersPerBit1();
        int classifiersPerBit2 = _rc.getClassifiersPerBit2();
        boolean max = false; // ie min [distance]

        int inputOffset1 = 0;
        int inputOffset2 = _ffInput1.getSize();

        Set< Integer > activeInputBits = _transient._activeInputClassifierRanking.keySet();
        for( Integer inputBit : activeInputBits ) {

            // pick the right spread of input through the region depending on which input it is from
            int maxRank = classifiersPerBit1;
            if( inputBit >= inputOffset2 ) {
                maxRank = classifiersPerBit2;
            }

            TreeMap< Float, ArrayList< Integer > > activeInputRanking = _transient.getRankingLazy( _transient._activeInputClassifierRanking, inputBit );

            ArrayList< Integer > activeInputClassifiers = Ranking.getBestValues( activeInputRanking, max, maxRank ); // ok now we got the current set of inputs for the column

            for( Integer classifierOffset : activeInputClassifiers ) {
                _transient.addClassifierActiveInput( classifierOffset, inputBit );
            }
        }
    }

    protected void updateClassifier( int xClassifier, int yClassifier ) {

        Point classifierOrigin = _rc.getRegionClassifierOrigin( xClassifier, yClassifier );
        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );
        Point columnSizeCells = _rc.getColumnSizeCells();

        ArrayList< Integer > activeInput = _transient.getClassifierActiveInput( classifierOffset );//_classifierActiveInput.get( classifierOffset );
        GrowingNeuralGas classifier = _classifiers.get( classifierOffset );
//        PlasticNeuralGas classifier = _classifiers.get( classifierOffset );

        Integer currentColumnCell = null; // in column
        Integer currentColumnCellX = null; // in column
        Integer currentColumnCellY = null; // in column
        Integer currentColumnCellRegionOffset = null; // in column

        // find the currently active cell.
        for( int yc = 0; yc < columnSizeCells.y; ++yc ) {

            // stop searching when found
            if( currentColumnCell != null ) {
                break;
            }

            for( int xc = 0; xc < columnSizeCells.x; ++xc ) {

                int regionX = classifierOrigin.x + xc;
                int regionY = classifierOrigin.y + yc;
                int regionOffset = _rc.getRegionOffset( regionX, regionY );

                float active = _regionActivityNew._values[ regionOffset ]; // old value
                if( active > 0.f ) {
//                        bestColumnCell = classifier._c.getCell( xc, yc ); // find the original best cell in this column (classifier)
                    currentColumnCellX = xc;
                    currentColumnCellY = yc;
                    currentColumnCell = Data2d.getOffset( columnSizeCells.x, xc, yc ); //yc * columnSizeCells.x + xc;
                    currentColumnCellRegionOffset = regionOffset;
                    break; // stop searching
                }
            }
        }

        // detect case where current cell isn't ever set. This happens the first time. Default to 0,0
        if( currentColumnCell == null ) {
            int xc = 0;
            int yc = 0;
            int regionX = classifierOrigin.x + xc;
            int regionY = classifierOrigin.y + yc;
            int regionOffset = _rc.getRegionOffset( regionX, regionY );
            currentColumnCellX = xc;
            currentColumnCellY = yc;
            currentColumnCell = Data2d.getOffset( columnSizeCells.x, xc, yc ); //yc * columnSizeCells.x + xc;
            currentColumnCellRegionOffset = regionOffset;
        }

        // OK now we found the CURRENT column cell, let's look for the new best:
        int bestColumnCell = 0; // in column

        if( activeInput.isEmpty() ) {
            // don't update this classifier. Let it ignore the current input.
            // For stability, preserve the current active cell, even though the classifier had no input.
            bestColumnCell = currentColumnCell;
        }
        else { // do classification on input
            boolean learn = _rc.getLearn();
            classifier._c.setLearn( learn );
            classifier.setSparseUnitInput( activeInput );
            classifier.update(); // trains with this sparse input.

            // check whether the classifier result has changed. If it hasn't, we keep the current cell.
            // If it has changed, find the least inhibited cell.
            Point xyClassifierCell = _rc.getClassifierCellGivenColumnCell( currentColumnCellX, currentColumnCellY );
            int currentClassifierCell = classifier._c.getCell( xyClassifierCell.x, xyClassifierCell.y );
            int    bestClassifierCell = classifier.getBestCell();

            // check whether same classifier cell:
            if( bestClassifierCell == currentClassifierCell ) {
                bestColumnCell = currentColumnCell;
            }
            else {
                bestColumnCell = findLeastInhibitedColumnCell( classifier, classifierOffset, currentColumnCell );//choose least inhibited
            }
        }

        // Check whether this is the same cell as last time:
        if( bestColumnCell == currentColumnCell ) {
            _transient._unchangedCells.add( currentColumnCellRegionOffset ); // extra step
            _transient._unchangedClassifiers.add( classifierOffset );
        }

        // Now deal with the best cell in the column:
        int bestColumnCellX = Data2d.getX( columnSizeCells.x, bestColumnCell );
        int bestColumnCellY = Data2d.getY( columnSizeCells.x, bestColumnCell );

        int regionX = classifierOrigin.x + bestColumnCellX;
        int regionY = classifierOrigin.y + bestColumnCellY;
        int regionOffset = _rc.getRegionOffset( regionX, regionY );

        _regionActivity._values[ regionOffset ] = 1.f;
        _transient._regionActiveCells.add( regionOffset );
        _transient._columnActiveCells.put( classifierOffset, bestColumnCell );
    }

    protected int findLeastInhibitedColumnCell( GrowingNeuralGas classifier, int classifierOffset, int currentColumnCell ) {
//    protected int findLeastInhibitedColumnCell( PlasticNeuralGas classifier, int classifierOffset, int currentColumnCell ) {
//        Point classifierSizeCells = _rc.getClassifierSizeCells();
        Point columnSizeCells = _rc.getColumnSizeCells();
        int depth = _rc.getDepthCells();

        int bestClassifierCell  = classifier.getBestCell();
        int bestClassifierCellX = classifier._c.getCellX( bestClassifierCell );
        int bestClassifierCellY = classifier._c.getCellY( bestClassifierCell );

        int bestColumnCell  = 0;
        float bestInhibition = Float.MAX_VALUE;

        int hebbianPredictorWeights = _predictor._weights.getSize();
        int offsetWeights = classifierOffset * hebbianPredictorWeights;
        _predictor._weights.copyRange( _regionPredictorWeights, 0, offsetWeights, hebbianPredictorWeights );

        for( int z = 0; z < depth; ++z ) {
            Point xyColumnCell = _rc.getColumnCellGivenClassifierCell( bestClassifierCellX, bestClassifierCellY, z );
            int columnCellX = xyColumnCell.x;
            int columnCellY = xyColumnCell.y;//bestClassifierCellY + z * classifierSizeCells.y;
            int columnCell = Data2d.getOffset( columnSizeCells.x, columnCellX, columnCellY );

            float inhibition = _predictor.getUnpredictedWeight( currentColumnCell, columnCell );

            if( inhibition < bestInhibition ) {
                bestInhibition = inhibition;
                bestColumnCell = columnCell;
            }
        }

        return bestColumnCell;
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
        return ( hasFfInput1Changed() || hasFfInput1Changed() );
    }

    protected boolean hasFfInput1Changed() {
        if( _ffInput1Old == null ) {
            return true;
        }

        int ffArea = _ffInput1.getSize();

        for( int i = 0; i < ffArea; ++i ) {
            float oldValue = _ffInput1Old._values[ i ];
            float newValue = 0.f;

            if( _transient._ffInput1Active.contains( i ) ) {
                newValue = 1.f;
            }

            if( oldValue != newValue ) {
                return true;
            }
        }

        return false;
    }

    protected boolean hasFfInput2Changed() {
        if( _ffInput2Old == null ) {
            return true;
        }

        int ffArea = _ffInput2.getSize();

        for( int i = 0; i < ffArea; ++i ) {
            float oldValue = _ffInput2Old._values[ i ];
            float newValue = 0.f;

            if( _transient._ffInput2Active.contains( i ) ) {
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
//        Point classifierSizeCells = _rc.getClassifierSizeCells();
        Point columnSizeCells = _rc.getColumnSizeCells();

        int stride = organizerSizeCells.x * columnSizeCells.x;

        boolean emitUnchangedCells = _rc.getEmitUnchangedCells();

        for( int yClassifier = 0; yClassifier < organizerSizeCells.y; ++yClassifier ) {
            for( int xClassifier = 0; xClassifier < organizerSizeCells.x; ++xClassifier ) {

                // copy weights, context, state
                int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );

                boolean unchanged = _transient._unchangedClassifiers.contains( classifierOffset );

                if( unchanged ) {
                    if( emitUnchangedCells ) {
                        continue; // leave FP/FN unchanged
                    }

                    // else:
                    for( int yc = 0; yc < columnSizeCells.y; ++yc ) {
                        for( int xc = 0; xc < columnSizeCells.x; ++xc ) {
                            int xr = ( xClassifier * columnSizeCells.x ) + xc;
                            int yr = ( yClassifier * columnSizeCells.y ) + yc;
                            int regionOffset = Data2d.getOffset( stride, xr, yr );

                            _regionPredictionFP._values[ regionOffset ] = 0.f;
                            _regionPredictionFN._values[ regionOffset ] = 0.f;
                        }
                    }

                    continue; // leave FP/FN unchanged
                }

                for( int yc = 0; yc < columnSizeCells.y; ++yc ) {
                    for( int xc = 0; xc < columnSizeCells.x; ++xc ) {

//        for( int y = 0; y < regionSizeCells.y; ++y ) {
//            for( int x = 0; x < regionSizeCells.x; ++x ) {
                        int xr = ( xClassifier * columnSizeCells.x ) + xc;
                        int yr = ( yClassifier * columnSizeCells.y ) + yc;
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

                            // Optionally suppress the predicted state, which is useful for static classification and for selective attention
                            float predictionInhibition = _regionPredictionInhibition._values[ regionOffset ];
                            //if( predictionInhibition > 0.f ) {
                            //    predictionOld = 0.f; // inhibit the cell from entering the predicted state.
                            //}
                            predictionOld *= ( 1.f - predictionInhibition ); // i.e. if inh. == 1, then pred *= 0. else if inh = 0, then 1-0=1 so *= 1.

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
//        Point classifierSizeCells = _rc.getClassifierSizeCells();
        Point columnSizeCells = _rc.getColumnSizeCells();

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
                Integer bestCell = _transient._columnActiveCells.get( classifierOffset );
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

                for( int yc = 0; yc < columnSizeCells.y; ++yc ) {
                    for( int xc = 0; xc < columnSizeCells.x; ++xc ) {
                        int offsetPredictor = ( yc * columnSizeCells.x ) + xc;

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
//        Point classifierSizeCells = _rc.getClassifierSizeCells();
        Point columnSizeCells = _rc.getColumnSizeCells();

        _regionPredictionNew.set( 0.f ); // clear

        int stride = regionSizeCols.x * columnSizeCells.x;

        for( int y = 0; y < regionSizeCols.y; ++y ) {
            for( int x = 0; x < regionSizeCols.x; ++x ) {

                float pMax = 0.f;
                int xMax = -1;
                int yMax = -1;

                for( int yc = 0; yc < columnSizeCells.y; ++yc ) {
                    for( int xc = 0; xc < columnSizeCells.x; ++xc ) {

                        int xr = ( x * columnSizeCells.x ) + xc;
                        int yr = ( y * columnSizeCells.y ) + yc;
                        int offset = Data2d.getOffset( stride, xr, yr );
                        float p = _regionPredictionRaw._values[ offset ];

                        if( p >= pMax ) {
                            pMax = p;
                            xMax = xc;
                            yMax = yc;
                        }
                    }
                }

                int xr = ( x * columnSizeCells.x ) + xMax;
                int yr = ( y * columnSizeCells.y ) + yMax;
                int offset = Data2d.getOffset( stride, xr, yr );
                _regionPredictionNew._values[ offset ] = 1.f;
            }
        }
    }

    protected void trainPredictor() {

        Point organizerSizeCells = _rc.getOrganizerSizeCells();
//        Point classifierSizeCells = _rc.getClassifierSizeCells();
        Point columnSizeCells = _rc.getColumnSizeCells();

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

                for( int yc = 0; yc < columnSizeCells.y; ++yc ) {
                    for( int xc = 0; xc < columnSizeCells.x; ++xc ) {
                        int offsetPredictor = ( yc * columnSizeCells.x ) + xc;

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

                if( classifierStateNew == classifierStateOld ) {
                    continue; // don't train unless there's a state change
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
