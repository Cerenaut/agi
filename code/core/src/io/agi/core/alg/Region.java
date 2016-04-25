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

import io.agi.core.ann.supervised.FeedForwardNetwork;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.data.Ranking;
import io.agi.core.math.Geometry;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A Region of cortex, representing a small part of that surface and one hierarchical level.
 * <p/>
 * Has a 2-D matrix of feedforward input.
 * Has a 2-D matrix of feedback input.
 * <p/>
 * There are 2 intended outputs:
 * - Predictions
 * - False-Negative Prediction errors (a Predictive-Encoding of the state of the Region).
 * <p/>
 * It has 3 parts:
 * - Organizer x1
 * - Classifiers (numerous, arranged into a grid)
 * - Predictor x1
 * <p/>
 * Created by dave on 22/03/16.
 */
public class Region extends NamedObject {

    public static final int PREDICTOR_LAYERS = 2; // not obvious that extra layers would help
    public static final int RECEPTIVE_FIELD_DIMENSIONS = 2;

    // Data structures
    public Data _ffInput;
    public Data _fbInput;
    public Data _fbInputOld;

    public Data _fbOutputUnfoldedActivity;
    public Data _fbOutputUnfoldedPrediction;

    public Data _regionActivityOld;
    public Data _regionActivityNew;
    public Data _regionActivity;

    public Data _regionPredictionOld;
    public Data _regionPredictionNew;
    public Data _regionPredictionRaw;

    public Data _regionPredictionFP;
    public Data _regionPredictionFN;

    // Sparse structures
    public HashSet< Integer > _ffInputActive;
    public HashSet< Integer > _fbInputActive;
    public ArrayList< Integer > _regionActive;

    public HashMap< Integer, ArrayList< Integer > > _ffInputActiveClassifier = new HashMap< Integer, ArrayList< Integer > >();

    // Member objects
    public RegionFactory _rf;
    public RegionConfig _rc;
    public GrowingNeuralGas _organizer;
    public HashMap< Integer, GrowingNeuralGas > _classifiers = new HashMap< Integer, GrowingNeuralGas >();
    public FeedForwardNetwork _predictor;


    public Region( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( RegionFactory rf ) {
        _rf = rf;
        _rc = rf.getRegionConfig();
        _organizer = _rf.createOrganizer( this );
        _predictor = _rf.createPredictor( this );

        Point p = _rc.getOrganizerSizeCells();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                GrowingNeuralGas classifier = _rf.createClassifier( this );
                int regionOffset = _rc.getOrganizerOffset( x, y );
                _classifiers.put( regionOffset, classifier );
            }
        }

        Point ffInputSize = _rc.getFfInputSize();
        Point fbInputSize = _rc.getFbInputSize();
        Point regionSize = _rc.getRegionSizeCells();

        DataSize dataSizeInputFF = DataSize.create( ffInputSize.x, ffInputSize.y );
        DataSize dataSizeInputFB = DataSize.create( fbInputSize.x, fbInputSize.y );
        DataSize dataSizeRegion = DataSize.create( regionSize.x, regionSize.y );

        _ffInput = new Data( dataSizeInputFF );
        _fbInput = new Data( dataSizeInputFB );
        _fbInputOld = new Data( dataSizeInputFB );

        _fbOutputUnfoldedActivity = new Data( dataSizeInputFF );
        _fbOutputUnfoldedPrediction = new Data( dataSizeInputFF );

        _regionActivityOld = new Data( dataSizeRegion );
        _regionActivityNew = new Data( dataSizeRegion );
        _regionActivity = new Data( dataSizeRegion );

        _regionPredictionOld = new Data( dataSizeRegion );
        _regionPredictionNew = new Data( dataSizeRegion );
        _regionPredictionRaw = new Data( dataSizeRegion );

        _regionPredictionFP = new Data( dataSizeRegion );
        _regionPredictionFN = new Data( dataSizeRegion );

        reset();
    }

    public Data getFfInput() {
        return _ffInput;
    }

    public Data getFbInput() {
        return _fbInput;
    }

    public void reset() {
        _ffInputActive = null;
        _fbInputActive = null;
        _regionActive = null;
        _ffInputActiveClassifier.clear();

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
        updateSparseInput();
        updateOrganizerReceptiveFields();

        Point p = _rc.getOrganizerSizeCells();

        _regionActivity.set( 0.f ); // clear
        _regionActive = new ArrayList< Integer >(); // clear

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                int organizerOffset = _rc.getOrganizerOffset( x, y );
                float mask = _organizer._cellMask._values[ organizerOffset ];
                if( mask != 1.f ) {
                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
                }

                updateClassifierReceptiveField( x, y );
                updateClassifier( x, y ); // adds to _regionActive and _regionActivity
            }
        }

        boolean classificationChanged = getClassificationChanged();
        if( classificationChanged ) {
            _regionActivityOld.copy( _regionActivityNew );
            _regionActivityNew.copy( _regionActivity );

            // Note: Must train predictor based on old activation and prediction output, before generating a new prediction
            trainPredictor(); // based on previous activation state and weights.
            updateRegionOutput(); // based on previous prediction and current classification
        }

        boolean feedbackChanged = getFeedbackChanged();
        if( classificationChanged || feedbackChanged ) {
            updatePrediction(); // make a new prediction
        }

        updateUnfoldedOutput();
    }

    public void updateUnfoldedOutput() {
        unfold( _regionActivityNew, _fbOutputUnfoldedActivity );
        unfold( _regionPredictionNew, _fbOutputUnfoldedPrediction );
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
        ffInput.set( 0.f );

        float threshold = 0.5f; // this is as meaningful as anything else..

        int weights = ffInput.getSize();

        HashSet< Integer > activeBits = region.indicesMoreThan( 0.f ); // find all the active bits.

        for( Integer i : activeBits ) {

            Point xyRegion = _rc.getRegionGivenOffset( i );
            Point xyOrganizer = _rc.getOrganizerCoordinateGivenRegionCoordinate( xyRegion.x, xyRegion.y );
            Point xyClassifier = _rc.getClassifierCoordinateGivenRegionCoordinate( xyRegion.x, xyRegion.y );

            int organizerOffset = _rc.getOrganizerOffset( xyOrganizer.x, xyOrganizer.y );
            GrowingNeuralGas classifier = _classifiers.get( organizerOffset );

            int cell = classifier._c.getCell( xyClassifier.x, xyClassifier.y );

            for( int w = 0; w < weights; ++w ) {

                int weightsOrigin = cell * weights;
                int weightsOffset = weightsOrigin +w;
                float weight = classifier._cellWeights._values[ weightsOffset ];

                if( weight > threshold ) {
                    ffInput._values[ w ] = 1.f; // either was zero, or was 1. Either way the update is correct.
                }
            }
        }
    }

    public void updateSparseInput() {
        // Find the sparse input bits:
        _ffInputActive = _ffInput.indicesMoreThan( 0.f ); // find all the active bits.
        _fbInputActive = _fbInput.indicesMoreThan( 0.f ); // find all the active bits.
    }

    /**
     * Trains the receptive fields of the classifiers via a specified number of samples.
     */
    public void updateOrganizerReceptiveFields() {

        int nbrActiveInput = _ffInputActive.size();

        if( nbrActiveInput == 0 ) {
            return; // can't train, ignore blank patterns.
        }

        Object[] activeInput = _ffInputActive.toArray();

        Data inputValues = _organizer.getInput();

        Point inputSize = Data2d.getSize( _ffInput );

        // randomly sample a fixed number of input bits.
        float samplesFraction = _rc.getReceptiveFieldsTrainingSamples();
        float sampleArea = (float)_ffInput.getSize();
        int samples = (int)( samplesFraction * sampleArea ); // e.g. 0.1 (10%) of the input area

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
        float[] rf = new float[ Region.RECEPTIVE_FIELD_DIMENSIONS ];

        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );
        int organizerOffset = classifierOffset * Region.RECEPTIVE_FIELD_DIMENSIONS;

        Point inputSize = Data2d.getSize( _ffInput );

        float rf_x = _organizer._cellWeights._values[ organizerOffset + 0 ];
        float rf_y = _organizer._cellWeights._values[ organizerOffset + 1 ];

        rf_x *= inputSize.x;
        rf_y *= inputSize.y;

        rf[ 0 ] = rf_x; // now in pixel coordinates, whereas it is trained as unit coordinates
        rf[ 1 ] = rf_y;

        return rf;
    }

    public void updateClassifierReceptiveField( int xClassifier, int yClassifier ) {

        // find the closest N active input to col.
        // Do this with ranking.
        int columnInputs = _rc.getReceptiveFieldSize();

        float[] rf = getClassifierReceptiveField( xClassifier, yClassifier ); // in pixels units
        float rf_x = rf[ 0 ];
        float rf_y = rf[ 1 ];

        Ranking r = new Ranking();

        for( Integer i : _ffInputActive ) {
            Point p = Data2d.getXY( _ffInput._dataSize, i );

            float d = Geometry.distanceEuclidean2d( ( float ) p.getX(), ( float ) p.getY(), rf_x, rf_y );
            Ranking.add( r._ranking, d, i ); // add input i with quality d (distance) to r.
        }

        boolean max = false; // ie min
        int maxRank = columnInputs;
        //Ranking.truncate( r._ranking, _inputsPerColumn, max );
        ArrayList< Integer > classifierActiveInput = Ranking.getBestValues( r._ranking, max, maxRank ); // ok now we got the current set of inputs for the column

        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );

        _ffInputActiveClassifier.put( classifierOffset, classifierActiveInput );
    }

    public void updateClassifier( int xClassifier, int yClassifier ) {

        int classifierOffset = _rc.getOrganizerOffset( xClassifier, yClassifier );
        ArrayList< Integer > activeInput = _ffInputActiveClassifier.get( classifierOffset );
        GrowingNeuralGas classifier = _classifiers.get( classifierOffset );

        classifier.setSparseUnitInput( activeInput );
        classifier.update(); // trains with this sparse input.

        int bestCell = classifier.getBestCell();
        int bestCellX = classifier._c.getCellX( bestCell );
        int bestCellY = classifier._c.getCellY( bestCell );

        Point classifierOrigin = _rc.getRegionClassifierOrigin( xClassifier, yClassifier );
        int regionX = classifierOrigin.x + bestCellX;
        int regionY = classifierOrigin.y + bestCellY;

        int regionOffset = _rc.getRegionOffset( regionX, regionY );
        _regionActivity._values[ regionOffset ] = 1.f;
        _regionActive.add( regionOffset );
    }

    public boolean getClassificationChanged() {
        // Since there can be only 1 bit per classifier, we can check if any bit has changed by only checking the
        // new '1' bits. They should all be the same in the old structure if there is no change.
        for( Integer i : _regionActive ) {
            float lastValue = _regionActivityNew._values[ i ];
            if( lastValue != 1.f ) {
                return true;
            }
        }

        return false;
    }

    public boolean getFeedbackChanged() {
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

    public void updateRegionOutput() {
        Point regionSizeCells = _rc.getRegionSizeCells();

        for( int y = 0; y < regionSizeCells.y; ++y ) {
            for( int x = 0; x < regionSizeCells.x; ++x ) {

                // Rules:
                // Prediction must START and CONTINUE until cell becomes ACTIVE.
                // If prediction ends too early - FP, then when cell active, FN.
                // If prediction ends too late -
                // If the cell becomes active after the prediction, OK
                // If the cell doesnt become active during prediction, Bad.
                int regionOffset = y * regionSizeCells.x + x;

                // update regional output
                float errorFP = 0.f;
                float errorFN = 0.f;

                float activeNew = _regionActivityNew._values[ regionOffset ];
                float predictionOld = _regionPredictionNew._values[ regionOffset ]; // we didn't update the prediction yet, so use current prediction

                // FN
                if( ( activeNew == 1.f ) && ( predictionOld == 0.f ) ) {
                    errorFN = 1.f;
                }

                // FP
                if( ( activeNew == 0.f ) && ( predictionOld == 1.f ) ) {
                    errorFP = 1.f;
                }

                // store updated error state
                _regionPredictionFP._values[ regionOffset ] = errorFP;
                _regionPredictionFN._values[ regionOffset ] = errorFN;
            }
        }
    }

    public void updatePrediction() {

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
        Data input = _predictor.getInput();

        input.set( 0.f );

        // put the local cells first:
        for( Integer i : _regionActive ) { // this is the latest set of active cells, used for a new prediction
            input._values[ i ] = 1.f;
        }

        // copy feedback:
        int regionSizeCells = _rc.getRegionAreaCells();
        for( Integer i : _fbInputActive ) {
            int iOffset = i + regionSizeCells;
            input._values[ iOffset ] = 1.f;
        }

        _predictor.feedForward(); // generate a prediction

        Data output = _predictor.getOutput();

        _regionPredictionOld.copy( _regionPredictionNew );
        _regionPredictionRaw.copy( output );

        //do a
        findPredictionLocalMaxima();
        //_regionPredictionNew.copy( output );
        //_regionPredictionNew.thresholdMoreThan( 0.5f, 1.0f, 0.0f ); // make it binary
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

    public void trainPredictor() {
        // Note: Must train predictor based on old activation and prediction output, before generating a new prediction
        Data ideal = _predictor.getIdeal();

        ideal.set( 0.f );

        for( Integer i : _regionActive ) { // this is the correct value, the new set of active cells.
            ideal._values[ i ] = 1.f;
        }

        _predictor.feedBackward(); // train based on a transition
    }

}