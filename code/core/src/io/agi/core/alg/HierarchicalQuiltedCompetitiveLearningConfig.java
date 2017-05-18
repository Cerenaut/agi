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
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.ann.unsupervised.BinaryTreeQuiltConfig;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by dave on 22/10/16.
 */
public class HierarchicalQuiltedCompetitiveLearningConfig {}/*extends NetworkConfig {

    public static final String FF_INPUT_1_WIDTH = "ff-input-1-width";
    public static final String FF_INPUT_1_HEIGHT = "ff-input-1-height";
    public static final String FF_INPUT_2_WIDTH = "ff-input-2-width";
    public static final String FF_INPUT_2_HEIGHT = "ff-input-2-height";
    public static final String FB_INPUTS = "fb-inputs";

    public static final String ORGANIZER = "organizer";
    public static final String CLASSIFIER = "classifier";
    public static final String PREDICTOR = "predictor";

    public static final String PREDICTION_LEARNING_RATE = "prediction-learning-rate";
    public static final String PREDICTION_DECAY_RATE = "prediction-decay-rate";

    public static final String ERROR_HISTORY_LENGTH = "error-history-length";

    public static final String CLASSIFIERS_PER_BIT_1 = "classifiers-per-bit-1";
    public static final String CLASSIFIERS_PER_BIT_2 = "classifiers-per-bit-2";

    public BinaryTreeQuiltConfig _organizerConfig;
    public GrowingNeuralGasConfig _classifierConfig;

    public HierarchicalQuiltedCompetitiveLearningConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            BinaryTreeQuiltConfig organizerConfig,
            GrowingNeuralGasConfig classifierConfig,
            int ffInput1Width,
            int ffInput1Height,
            int ffInput2Width,
            int ffInput2Height,
            int fbInputArea,
            float predictionLearningRate,
            float predictionDecayRate,
            int errorHistoryLength,
            int classifiersPerBit1,
            int classifiersPerBit2 ) {
        super.setup( om, name, r );

        _organizerConfig = organizerConfig;
        _classifierConfig = classifierConfig;

        setPredictionLearningRate( predictionLearningRate );
        setPredictionDecayRate( predictionDecayRate );

        setErrorHistoryLength( errorHistoryLength );

        setClassifiersPerBit1( classifiersPerBit1 );
        setClassifiersPerBit2( classifiersPerBit2 );

        setFfInput1Size( ffInput1Width, ffInput1Height );
        setFfInput2Size( ffInput2Width, ffInput2Height );
        setFbInputArea( fbInputArea );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        HierarchicalQuiltedCompetitiveLearningConfig c = ( HierarchicalQuiltedCompetitiveLearningConfig ) nc;

        _organizerConfig.copyFrom( c._organizerConfig, c._name );
        _classifierConfig.copyFrom( c._classifierConfig, c._name );

        setPredictionLearningRate( c.getPredictionLearningRate() );
        setPredictionDecayRate( c.getPredictionDecayRate() );

        setErrorHistoryLength( c.getErrorHistoryLength() );

        setClassifiersPerBit1( c.getClassifiersPerBit1() );
        setClassifiersPerBit2( c.getClassifiersPerBit2() );

        setFfInput1Size( c.getFfInput1Size().x, c.getFfInput1Size().y );
        setFfInput2Size( c.getFfInput2Size().x, c.getFfInput2Size().y );
        setFbInputArea( c.getFbInputArea() );
    }

    public Random getRandom() {
        return _r;
    }

    public float getPredictionLearningRate() {
        float r = _om.getFloat( getKey( PREDICTION_LEARNING_RATE ) );
        return r;
    }

    public void setPredictionLearningRate( float r ) {
        _om.put( getKey( PREDICTION_LEARNING_RATE ), r );
    }

    public float getPredictionDecayRate() {
        float r = _om.getFloat( getKey( PREDICTION_DECAY_RATE ) );
        return r;
    }

    public void setPredictionDecayRate( float r ) {
        _om.put( getKey( PREDICTION_DECAY_RATE ), r );
    }

    public int getErrorHistoryLength() {
        int n = _om.getInteger( getKey( ERROR_HISTORY_LENGTH ) );
        return n;
    }

    public void setErrorHistoryLength( int n ) {
        _om.put( getKey( ERROR_HISTORY_LENGTH ), n );
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

    public Point getClassifierSizeCells() {
        int width = _classifierConfig.getWidthCells();
        int height = _classifierConfig.getHeightCells();
        return new Point( width, height );
    }

    public Point getOrganizerSizeCells() {
        int width = _organizerConfig.getQuiltWidth();//getWidthCells();
        int height = _organizerConfig.getQuiltHeight();//.getHeightCells();
        return new Point( width, height );
    }

    public int getOrganizerAreaCells() {
        int width = _organizerConfig.getQuiltWidth();//getWidthCells();
        int height = _organizerConfig.getQuiltHeight();//.getHeightCells();
        return ( width * height );
    }

//    public int getReceptiveFieldSize() {
//        int n = _om.getInteger( getKey( RECEPTIVE_FIELD_SIZE ) );
//        return n;
//    }
//
//    public void setReceptiveFieldSize( int receptiveFieldSize ) {
//        _om.put( getKey( RECEPTIVE_FIELD_SIZE ), receptiveFieldSize );
//    }

    public int getClassifiersPerBit1() {
        int n = _om.getInteger( getKey( CLASSIFIERS_PER_BIT_1 ) );
        return n;
    }

    public int getClassifiersPerBit2() {
        int n = _om.getInteger( getKey( CLASSIFIERS_PER_BIT_2 ) );
        return n;
    }

    public void setClassifiersPerBit1( int n ) {
        _om.put( getKey( CLASSIFIERS_PER_BIT_1 ), n );
    }

    public void setClassifiersPerBit2( int n ) {
        _om.put( getKey( CLASSIFIERS_PER_BIT_2 ), n );
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
     *
     * @param xRegion
     * @param yRegion
     * @return
     * /
    public Point getOrganizerCoordinateGivenRegionCoordinate( int xRegion, int yRegion ) {
        Point columnSize = getClassifierSizeCells();
        int xClassifier = xRegion / columnSize.x;
        int yClassifier = yRegion / columnSize.y;
        return new Point( xClassifier, yClassifier ); // the
    }

    /**
     * Calculates the coordinates of the cell within a classifier that represents the given region cell.
     *
     * @param xRegion
     * @param yRegion
     * @return
     * /
    public Point getColumnCoordinateGivenRegionCoordinate( int xRegion, int yRegion ) {
        Point columnSize = getClassifierSizeCells();

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
        Point columnSize = getClassifierSizeCells();
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
        Point columnSize = getClassifierSizeCells();
        Point organizerSize = getOrganizerSizeCells();

        int width = columnSize.x * organizerSize.x;
        int height = columnSize.y * organizerSize.y;

        Point regionSize = new Point( width, height );
        return regionSize;
    }

}*/