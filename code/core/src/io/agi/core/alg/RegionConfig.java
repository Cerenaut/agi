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
import io.agi.core.ann.supervised.FeedForwardNetworkConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Parameters and geometric description of a Region.
 * <p/>
 * Created by dave on 22/03/16.
 */
public class RegionConfig extends NetworkConfig {

    public static final String FF_INPUT_WIDTH = "ff-input-width";
    public static final String FF_INPUT_HEIGHT = "ff-input-height";
    public static final String FB_INPUTS = "fb-inputs";

    public static final String RECEPTIVE_FIELDS_TRAINING_SAMPLES = "receptive-fields-training-samples";
    public static final String RECEPTIVE_FIELD_SIZE = "receptive-field-size";

    public static final String SUFFIX_ORGANIZER = "organizer";
    public static final String SUFFIX_CLASSIFIER = "classifier";
    public static final String SUFFIX_PREDICTOR = "predictor";

    public GrowingNeuralGasConfig _classifierConfig;
    public GrowingNeuralGasConfig _organizerConfig;
    public FeedForwardNetworkConfig _predictorConfig;

    public RegionConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            GrowingNeuralGasConfig organizerConfig,
            GrowingNeuralGasConfig classifierConfig,
            FeedForwardNetworkConfig predictorConfig,
            int ffInputWidth,
            int ffInputHeight,
            int fbInputArea,
            float receptiveFieldsTrainingSamples,
            int receptiveFieldSize ) {
        super.setup( om, name, r );

        _organizerConfig = organizerConfig;
        _classifierConfig = classifierConfig;
        _predictorConfig = predictorConfig;

        setReceptiveFieldsTrainingSamples( receptiveFieldsTrainingSamples );
        setReceptiveFieldSize( receptiveFieldSize );
        setFfInputSize( ffInputWidth, ffInputHeight );
        setFbInputArea( fbInputArea );
    }

    public Random getRandom() {
        return _r;
    }

    public Point getFfInputSize() {
        int inputWidth = _om.getInteger( getKey( FF_INPUT_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( FF_INPUT_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public void setFfInputSize( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( FF_INPUT_WIDTH ), ffInputWidth );
        _om.put( getKey( FF_INPUT_HEIGHT ), ffInputHeight );
    }

    public int getFfInputArea() {
        Point p = getFfInputSize();
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

    public int getReceptiveFieldSize() {
        int n = _om.getInteger( getKey( RECEPTIVE_FIELD_SIZE ) );
        return n;
    }

    public void setReceptiveFieldSize( int receptiveFieldSize ) {
        _om.put( getKey( RECEPTIVE_FIELD_SIZE ), receptiveFieldSize );
    }

    public float getReceptiveFieldsTrainingSamples() {
        float r = _om.getFloat( getKey( RECEPTIVE_FIELDS_TRAINING_SAMPLES ) );
        return r;
    }

    public void setReceptiveFieldsTrainingSamples( float receptiveFieldsTrainingSamples ) {
        _om.put( getKey( RECEPTIVE_FIELDS_TRAINING_SAMPLES ), receptiveFieldsTrainingSamples );
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

    public Point getRegionClassifierOrigin( int xClassifier, int yClassifier ) {
        Point classifierSize = getClassifierSizeCells();
        int xRegion = xClassifier * classifierSize.x;
        int yRegion = yClassifier * classifierSize.y;

        Point regionOrigin = new Point( xRegion, yRegion );
        return regionOrigin;
    }

    public int getRegionAreaCells() {
        Point regionSize = getRegionSizeCells();
        int regionArea = regionSize.x * regionSize.y;
        return regionArea;
    }

    public Point getRegionSizeCells() {
        Point classifierSize = getClassifierSizeCells();
        Point organizerSize = getOrganizerSizeCells();

        int width = classifierSize.x * organizerSize.x;
        int height = classifierSize.y * organizerSize.y;

        Point regionSize = new Point( width, height );
        return regionSize;
    }

}
