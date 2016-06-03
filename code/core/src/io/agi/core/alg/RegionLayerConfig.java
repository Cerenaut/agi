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

import io.agi.core.ann.NetworkConfig;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMapConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.ann.unsupervised.ParameterLessSelfOrganizingMapConfig;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by dave on 14/05/16.
 */
public class RegionLayerConfig extends NetworkConfig {

//    public static final int RECEPTIVE_FIELD_DIMENSIONS = 2;

    public static final String FF_INPUT_1_WIDTH = "ff-input-1-width";
    public static final String FF_INPUT_1_HEIGHT = "ff-input-1-height";
    public static final String FF_INPUT_2_WIDTH = "ff-input-2-width";
    public static final String FF_INPUT_2_HEIGHT = "ff-input-2-height";
    public static final String FB_INPUTS = "fb-inputs";

    public static final String RECEPTIVE_FIELDS_TRAINING_SAMPLES = "receptive-fields-training-samples";
//    public static final String RECEPTIVE_FIELD_SIZE = "receptive-field-size";
    public static final String CLASSIFIERS_PER_BIT = "classifiers-per-bit";
    public static final String ORGANIZER_TRAIN_ON_CHANGE = "organizer-train-on-change";
    public static final String PREDICTOR_LEARNING_RATE = "predictor-learning-rate";
    public static final String DEPTH_CELLS = "depth-cells";
    public static final String DEFAULT_PREDICTION_INHIBITION = "default-prediction-inhibition";

    public static final String SUFFIX_ORGANIZER = "organizer";
    public static final String SUFFIX_CLASSIFIER = "classifier";
    public static final String SUFFIX_PREDICTOR = "predictor";

    public GrowingNeuralGasConfig _classifierConfig;
//    public GrowingNeuralGasConfig _organizerConfig;
    public ParameterLessSelfOrganizingMapConfig _organizerConfig;

    public RegionLayerConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
//            DynamicSelfOrganizingMapConfig organizerConfig,
            ParameterLessSelfOrganizingMapConfig organizerConfig,
            GrowingNeuralGasConfig classifierConfig,
            int ffInput1Width,
            int ffInput1Height,
            int ffInput2Width,
            int ffInput2Height,
            int fbInputArea,
            float predictorLearningRate,
            float receptiveFieldsTrainingSamples,
            float defaultPredictionInhibition,
            boolean organizerTrainOnChange,
            int classifiersPerBit,
            int depthCells ) {
        super.setup( om, name, r );

        _organizerConfig = organizerConfig;
        _classifierConfig = classifierConfig;

        setOrganizerTrainOnChange( organizerTrainOnChange );
        setPredictorLearningRate( predictorLearningRate );
        setReceptiveFieldsTrainingSamples( receptiveFieldsTrainingSamples );
//        setReceptiveFieldSize( receptiveFieldSize );
        setClassifiersPerBit( classifiersPerBit );
        setFfInput1Size( ffInput1Width, ffInput1Height );
        setFfInput2Size( ffInput2Width, ffInput2Height );
        setFbInputArea( fbInputArea );
        setDepthCells( depthCells );
        setDefaultPredictionInhibition( defaultPredictionInhibition );
    }

    public Random getRandom() {
        return _r;
    }

    public Point getFfInput1Size() {
        int inputWidth = _om.getInteger( getKey( FF_INPUT_1_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( FF_INPUT_1_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public Point getFfInput2Size() {
        int inputWidth = _om.getInteger( getKey( FF_INPUT_2_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( FF_INPUT_2_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public void setFfInput1Size( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( FF_INPUT_1_WIDTH ), ffInputWidth );
        _om.put( getKey( FF_INPUT_1_HEIGHT ), ffInputHeight );
    }

    public void setFfInput2Size( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( FF_INPUT_2_WIDTH ), ffInputWidth );
        _om.put( getKey( FF_INPUT_2_HEIGHT ), ffInputHeight );
    }

    public int getFfInputArea() {
        return getFfInput1Area() + getFfInput2Area();
    }

    public int getFfInput1Area() {
        Point p = getFfInput1Size();
        int inputArea = p.x * p.y;
        return inputArea;
    }

    public int getFfInput2Area() {
        Point p = getFfInput2Size();
        int inputArea = p.x * p.y;
        return inputArea;
    }

    public Point getFbInputSize() {
        int inputSize = _om.getInteger( getKey( FB_INPUTS ) );
        return new Point( inputSize, 1 );
    }

    public int getFbInputArea() {
        int inputSize = _om.getInteger( getKey( FB_INPUTS ) );
        return inputSize;
    }

    public void setFbInputArea( int fbInputArea ) {
        _om.put( getKey( FB_INPUTS ), fbInputArea );
    }

    public boolean getOrganizerTrainOnChange() {
        boolean b = _om.getBoolean( getKey( ORGANIZER_TRAIN_ON_CHANGE ) );
        return b;
    }

    public void setOrganizerTrainOnChange( boolean b ) {
        _om.put( getKey( ORGANIZER_TRAIN_ON_CHANGE ), b );
    }

    public float getPredictorLearningRate() {
        float r = _om.getFloat( getKey( PREDICTOR_LEARNING_RATE ) );
        return r;
    }

    public void setPredictorLearningRate( float learningRate ) {
        _om.put( getKey( PREDICTOR_LEARNING_RATE ), learningRate );
    }

    public int getPredictorInputs() {
        int inputArea = getFbInputArea();
        int regionArea = getRegionAreaCells();
        int predictorInputs = inputArea + regionArea;
        return predictorInputs;
    }

    public Point getClassifierSizeCells() {
        int width = _classifierConfig.getWidthCells();
        int height = _classifierConfig.getHeightCells();
        return new Point( width, height );
    }

    public Point getOrganizerSizeCells() {
        int width = _organizerConfig.getWidthCells();
        int height = _organizerConfig.getHeightCells();
        return new Point( width, height );
    }

//    public int getReceptiveFieldSize() {
//        int n = _om.getInteger( getKey( RECEPTIVE_FIELD_SIZE ) );
//        return n;
//    }
//
//    public void setReceptiveFieldSize( int receptiveFieldSize ) {
//        _om.put( getKey( RECEPTIVE_FIELD_SIZE ), receptiveFieldSize );
//    }

    public int getClassifiersPerBit() {
        int n = _om.getInteger( getKey( CLASSIFIERS_PER_BIT ) );
        return n;
    }

    public void setClassifiersPerBit( int depthCells ) {
        _om.put( getKey( CLASSIFIERS_PER_BIT ), depthCells );
    }

    public int getDepthCells() {
        int n = _om.getInteger( getKey( DEPTH_CELLS ) );
        return n;
    }

    public void setDepthCells( int depthCells ) {
        _om.put( getKey( DEPTH_CELLS ), depthCells );
    }

    public float getReceptiveFieldsTrainingSamples() {
        float r = _om.getFloat( getKey( RECEPTIVE_FIELDS_TRAINING_SAMPLES ) );
        return r;
    }

    public void setReceptiveFieldsTrainingSamples( float receptiveFieldsTrainingSamples ) {
        _om.put( getKey( RECEPTIVE_FIELDS_TRAINING_SAMPLES ), receptiveFieldsTrainingSamples );
    }

    public float getDefaultPredictionInhibition() {
        float r = _om.getFloat( getKey( DEFAULT_PREDICTION_INHIBITION ) );
        return r;
    }

    public void setDefaultPredictionInhibition( float r ) {
        _om.put( getKey( DEFAULT_PREDICTION_INHIBITION ), r );
    }

    public int getOrganizerOffset( int xClassifier, int yClassifier ) {
        Point p = getOrganizerSizeCells();
        int stride = p.x;
        return Data2d.getOffset( stride, xClassifier, yClassifier );
    }

    public int getRegionOffset( int xRegion, int yRegion ) {
        Point p = getRegionSizeCells();
        int stride = p.x;
        return Data2d.getOffset( stride, xRegion, yRegion );
    }

    public Point getRegionGivenOffset( int regionOffset ) {
        Point regionSize = getRegionSizeCells();
        int yRegion = regionOffset / regionSize.x;
        int xRegion = regionOffset % regionSize.x;
        return new Point( xRegion, yRegion );
    }

    /**
     * Calculates the coordinates of the classifier that owns the given region cell.
     * @param xRegion
     * @param yRegion
     * @return
     */
    public Point getOrganizerCoordinateGivenRegionCoordinate( int xRegion, int yRegion ) {
        Point columnSize = getColumnSizeCells();
        int xClassifier = xRegion / columnSize.x;
        int yClassifier = yRegion / columnSize.y;
        return new Point( xClassifier, yClassifier ); // the
    }

    public Point getColumnSizeCells() {
        Point classifierSize = getClassifierSizeCells();
        int depthCells = getDepthCells();
        int wColumn = classifierSize.x;
        int hColumn = classifierSize.y * depthCells;
        return new Point( wColumn, hColumn );
    }
    /**
     * Calculates the coordinates of the cell within a classifier that represents the given region cell.
     * @param xRegion
     * @param yRegion
     * @return
     */
    public Point getColumnCoordinateGivenRegionCoordinate( int xRegion, int yRegion ) {
//        Point classifierSize = getClassifierSizeCells();
        Point columnSize = getColumnSizeCells();

        int xClassifier = ( xRegion / columnSize.x ) * columnSize.x;
        int yClassifier = ( yRegion / columnSize.y ) * columnSize.y;

        int xCell = xRegion - xClassifier;
        int yCell = yRegion - yClassifier; // note: may include depth

        return new Point( xCell, yCell ); // the
    }

    public Point getClassifierCellGivenColumnCell( int xColumnCell, int yColumnCell ) {
        Point classifierSizeCells = getClassifierSizeCells();
        int yOrigin = ( yColumnCell / classifierSizeCells.y ) * classifierSizeCells.y; // excludes any fractional part

        int xClassifierCell = xColumnCell;
        int yClassifierCell = yColumnCell - yOrigin;

        return new Point( xClassifierCell, yClassifierCell ); // the
    }

    public Point getColumnCellGivenClassifierCell( int xClassifierCell, int yClassifierCell, int zDepth ) {
        Point classifierSizeCells = getClassifierSizeCells();
        int yOrigin = zDepth * classifierSizeCells.y;

        int xColumnCell = xClassifierCell;
        int yColumnCell = yClassifierCell + yOrigin;

        return new Point( xColumnCell, yColumnCell ); // the
    }

    public Point getRegionClassifierOrigin( int xClassifier, int yClassifier ) {
//        Point classifierSize = getClassifierSizeCells();
        Point columnSize = getColumnSizeCells();
        int xRegion = xClassifier * columnSize.x;
        int yRegion = yClassifier * columnSize.y;

        Point regionOrigin = new Point( xRegion, yRegion );
        return regionOrigin;
    }

    public int getRegionAreaCells() {
        Point regionSize = getRegionSizeCells();
        int regionArea = regionSize.x * regionSize.y;
        return regionArea;
    }

    public Point getRegionSizeCells() {
//        Point classifierSize = getClassifierSizeCells();
        Point columnSize = getColumnSizeCells();
        Point organizerSize = getOrganizerSizeCells();

        int width  = columnSize.x * organizerSize.x;
        int height = columnSize.y * organizerSize.y;

        Point regionSize = new Point( width, height );
        return regionSize;
    }

    public int getHebbianPredictorStates() {
//        int hebbianStates = getClassifierSizeCells().x * getClassifierSizeCells().y;
        Point columnSize = getColumnSizeCells();
        int hebbianStates = columnSize.x * columnSize.y;
        return hebbianStates;
    }
    public int getHebbianPredictorContext( int fbInputArea ) {
        int regionArea = getRegionAreaCells();
        int feedbackVolume = fbInputArea;//_fbInput.getSize();
        int hebbianContext = regionArea + feedbackVolume; // this is the contextual info it uses to work out the next state
        return hebbianContext;
    }
    public int getHebbianPredictorContextSizeRegion( int predictorContextSize ) {
        Point organizerSizeCells = getOrganizerSizeCells();
        int nbrClassifiers = organizerSizeCells.x * organizerSizeCells.y;
        int hebbianPredictorInputs  = nbrClassifiers * predictorContextSize;//_predictor._context.getSize();
        return hebbianPredictorInputs;
    }
    public int getHebbianPredictorWeightsSizeRegion( int predictorWeightsSize ) {
        Point organizerSizeCells = getOrganizerSizeCells();
        int nbrClassifiers = organizerSizeCells.x * organizerSizeCells.y;
        int hebbianPredictorWeights = nbrClassifiers * predictorWeightsSize;//_predictor._weights.getSize();
        return hebbianPredictorWeights;
    }
}
