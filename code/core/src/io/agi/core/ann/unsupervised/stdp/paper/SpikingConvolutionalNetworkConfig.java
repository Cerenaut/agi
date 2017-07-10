/*
 * Copyright (c) 2017.
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

package io.agi.core.ann.unsupervised.stdp.paper;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dave on 1/05/17.
 */
public class SpikingConvolutionalNetworkConfig extends NetworkConfig {

    // http://cs231n.github.io/convolutional-networks/
    // 3d input (e.g. x,y,{rgb})
    // 3d output (w,h,d)
    // depth, stride and zero-padding
    // depth = z, a param
    // stride = stride of the filter
    // zero-padding (like an offset)
    // filters always extend through the full depth of the input volume. For example, if the input is [32x32x3] then
    // doing 1x1 convolutions would effectively be doing 3-dimensional dot products (since the input depth is 3 channels).

    //Synaptic weights of convolutional neurons initi-
    //ate with random values drown from a normal dis-
    //tribution with the mean of 0.8 and STD of 0.05

    public static final String KEY_WEIGHTS_STD_DEV = "weights-std-dev";
    public static final String KEY_WEIGHTS_MEAN = "weights-mean";
    public static final String KEY_LEARNING_RATE_POS = "learning-rate-pos";
    public static final String KEY_LEARNING_RATE_NEG = "learning-rate-neg";

    public static final String KEY_AGE = "age";

    public static final String KEY_LAYERS = "layers";
    public static final String KEY_LAYER_TRAINING_AGE = "layer-training-age";
    public static final String KEY_LAYER_INTEGRATION_THRESHOLD = "layer-integration-threshold";
    public static final String KEY_LAYER_INPUT_PADDING = "layer-input-padding";
    public static final String KEY_LAYER_INPUT_STRIDE = "layer-input-stride";
    public static final String KEY_LAYER_WIDTH = "layer-width";
    public static final String KEY_LAYER_HEIGHT = "layer-height";
    public static final String KEY_LAYER_DEPTH = "layer-depth";
    public static final String KEY_LAYER_FIELD_WIDTH = "layer-field-width";
    public static final String KEY_LAYER_FIELD_HEIGHT = "layer-field-height";
    public static final String KEY_LAYER_FIELD_DEPTH = "layer-field-depth";
    public static final String KEY_LAYER_POOLING_WIDTH = "layer-pooling-width";
    public static final String KEY_LAYER_POOLING_HEIGHT = "layer-pooling-height";

    public SpikingConvolutionalNetworkConfig() {

    }

    public void setup(
            ObjectMap om, String name, Random r,
            int age,
            float weightsStdDev,
            float weightsMean,
            float learningRatePos,
            float learningRateNeg,
//            float integrationThreshold,
            int nbrLayers,
            String layerTrainingAges,
            String layerIntegrationThresholds,
            String layerInputPadding,
            String layerInputStride,
            String layerWidth,
            String layerHeight,
            String layerDepth,
            String layerfieldWidth,
            String layerfieldHeight,
            String layerfieldDepth,
            String layerPoolingWidth,
            String layerPoolingHeight
            ) {
        super.setup( om, name, r );

        setAge( age );
        setWeightsStdDev( weightsStdDev );
        setWeightsMean( weightsMean );
        setLearningRatePos( learningRatePos );
        setLearningRateNeg( learningRateNeg );
        //setIntegrationThreshold( integrationThreshold );

        setNbrLayers( nbrLayers );
        setLayerValues( KEY_LAYER_TRAINING_AGE, layerTrainingAges );
        setLayerValues( KEY_LAYER_INTEGRATION_THRESHOLD, layerIntegrationThresholds );
        setLayerValues( KEY_LAYER_INPUT_PADDING, layerInputPadding );
        setLayerValues( KEY_LAYER_INPUT_STRIDE, layerInputStride );
        setLayerValues( KEY_LAYER_WIDTH, layerWidth );
        setLayerValues( KEY_LAYER_HEIGHT, layerHeight );
        setLayerValues( KEY_LAYER_DEPTH, layerDepth );
        setLayerValues( KEY_LAYER_FIELD_WIDTH, layerfieldWidth );
        setLayerValues( KEY_LAYER_FIELD_HEIGHT, layerfieldHeight );
        setLayerValues( KEY_LAYER_FIELD_DEPTH, layerfieldDepth );
        setLayerValues( KEY_LAYER_POOLING_WIDTH, layerPoolingWidth );
        setLayerValues( KEY_LAYER_POOLING_HEIGHT, layerPoolingHeight );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        SpikingConvolutionalNetworkConfig c = ( SpikingConvolutionalNetworkConfig ) nc;

        setAge( c.getAge() );
        setWeightsStdDev( c.getWeightsStdDev() );
        setWeightsMean( c.getWeightsMean() );
        setLearningRatePos( c.getLearningRatePos() );
        setLearningRateNeg( c.getLearningRateNeg() );
//        setIntegrationThreshold( c.getIntegrationThreshold() );

        setNbrLayers( c.getNbrLayers() );
        setLayerValues( KEY_LAYER_TRAINING_AGE, c.getLayerValues( KEY_LAYER_TRAINING_AGE ) );
        setLayerValues( KEY_LAYER_INPUT_PADDING, c.getLayerValues( KEY_LAYER_INPUT_PADDING ) );
        setLayerValues( KEY_LAYER_INTEGRATION_THRESHOLD, c.getLayerValues( KEY_LAYER_INTEGRATION_THRESHOLD ) );
        setLayerValues( KEY_LAYER_INPUT_STRIDE, c.getLayerValues( KEY_LAYER_INPUT_STRIDE ) );
        setLayerValues( KEY_LAYER_WIDTH, c.getLayerValues( KEY_LAYER_WIDTH ) );
        setLayerValues( KEY_LAYER_HEIGHT, c.getLayerValues( KEY_LAYER_HEIGHT ) );
        setLayerValues( KEY_LAYER_DEPTH, c.getLayerValues( KEY_LAYER_DEPTH ) );
        setLayerValues( KEY_LAYER_FIELD_WIDTH, c.getLayerValues( KEY_LAYER_FIELD_WIDTH ) );
        setLayerValues( KEY_LAYER_FIELD_HEIGHT, c.getLayerValues( KEY_LAYER_FIELD_HEIGHT ) );
        setLayerValues( KEY_LAYER_FIELD_DEPTH, c.getLayerValues( KEY_LAYER_FIELD_DEPTH ) );
        setLayerValues( KEY_LAYER_POOLING_WIDTH, c.getLayerValues( KEY_LAYER_POOLING_WIDTH ) );
        setLayerValues( KEY_LAYER_POOLING_HEIGHT, c.getLayerValues( KEY_LAYER_POOLING_HEIGHT ) );
    }

    public float getIntegrationThreshold( int layer ) {
//        Float r = _om.getFloat( getKey( KEY_INTEGRATION_THRESHOLD ) );
        float r = Float.valueOf( getLayerValue( KEY_LAYER_INTEGRATION_THRESHOLD, layer ) );
        return r;
    }

    public void setIntegrationThreshold( int layer, float r ) {
        setLayerValue( KEY_LAYER_INTEGRATION_THRESHOLD, layer, String.valueOf( r ), "0" );
//        _om.put( getKey( KEY_INTEGRATION_THRESHOLD ), r );
    }

    public float getWeightsMean() {
        Float r = _om.getFloat( getKey( KEY_WEIGHTS_MEAN ) );
        return r.floatValue();
    }

    public void setWeightsMean( float r ) {
        _om.put( getKey( KEY_WEIGHTS_MEAN ), r );
    }

    public float getWeightsStdDev() {
        Float r = _om.getFloat( getKey( KEY_WEIGHTS_STD_DEV ) );
        return r.floatValue();
    }

    public void setWeightsStdDev( float r ) {
        _om.put( getKey( KEY_WEIGHTS_STD_DEV ), r );
    }

    public float getLearningRatePos() {
        Float r = _om.getFloat( getKey( KEY_LEARNING_RATE_POS ) );
        return r.floatValue();
    }

    public void setLearningRatePos( float r ) {
        _om.put( getKey( KEY_LEARNING_RATE_POS ), r );
    }

    public float getLearningRateNeg() {
        Float r = _om.getFloat( getKey( KEY_LEARNING_RATE_NEG ) );
        return r.floatValue();
    }

    public void setLearningRateNeg( float r ) {
        _om.put( getKey( KEY_LEARNING_RATE_NEG ), r );
    }

    public int getAge() {
        Integer n = _om.getInteger( getKey( KEY_AGE ) );
        return n.intValue();
    }


    public int getNbrLayers() {
        Integer n = _om.getInteger( getKey( KEY_LAYERS ) );
        return n.intValue();
    }

    public int getLayerTrainingAge( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_TRAINING_AGE, layer ) );
        return n;
    }

    public int getLayerInputPadding( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_INPUT_PADDING, layer ) );
        return n;
    }

    public int getLayerInputStride( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_INPUT_STRIDE, layer ) );
        return n;
    }

    public int getLayerWidth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_WIDTH, layer ) );
        return n;
    }

    public int getLayerHeight( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_HEIGHT, layer ) );
        return n;
    }

    public int getLayerDepth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_DEPTH, layer ) );
        return n;
    }

    public int getLayerFieldWidth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_FIELD_WIDTH, layer ) );
        return n;
    }

    public int getLayerFieldHeight( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_FIELD_HEIGHT, layer ) );
        return n;
    }

    public int getLayerFieldDepth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_FIELD_DEPTH, layer ) );
        return n;
    }

    public int getLayerPoolingWidth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_POOLING_WIDTH, layer ) );
        return n;
    }

    public int getLayerPoolingHeight( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_POOLING_HEIGHT, layer ) );
        return n;
    }

    public void setNbrLayers( int layers ) {
        _om.put( getKey( KEY_LAYERS ), layers );
    }

    public void setAge( int layers ) {
        _om.put( getKey( KEY_AGE ), layers );
    }

    public void setLayerInputPadding( int layer, int n ) {
        setLayerValue( KEY_LAYER_INPUT_PADDING, layer, String.valueOf( n ), "0" );
    }

    public void setLayerInputStride( int layer, int n ) {
        setLayerValue( KEY_LAYER_INPUT_STRIDE, layer, String.valueOf( n ), "0" );
    }

    public void setLayerWidth( int layer, int n ) {
        setLayerValue( KEY_LAYER_WIDTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerHeight( int layer, int n ) {
        setLayerValue( KEY_LAYER_HEIGHT, layer, String.valueOf( n ), "0" );
    }

    public void setLayerDepth( int layer, int n ) {
        setLayerValue( KEY_LAYER_DEPTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerFieldWidth( int layer, int n ) {
        setLayerValue( KEY_LAYER_FIELD_WIDTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerFieldHeight( int layer, int n ) {
        setLayerValue( KEY_LAYER_FIELD_HEIGHT, layer, String.valueOf( n ), "0" );
    }

    public void setLayerFieldDepth( int layer, int n ) {
        setLayerValue( KEY_LAYER_FIELD_DEPTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerPoolingWidth( int layer, int n ) {
        setLayerValue( KEY_LAYER_POOLING_WIDTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerPoolingHeight( int layer, int n ) {
        setLayerValue( KEY_LAYER_POOLING_HEIGHT, layer, String.valueOf( n ), "0" );
    }

    public String getLayerValues( String key ) {
        return (String)_om.get( key );
    }

    public String getLayerValue( String key, int layer ) {
        String values = _om.getString( getKey( key ) );
        String[] sizes = values.split( "," );
        if( layer >= sizes.length ) {
            return "";
        }
        String value = sizes[ layer ];
        return value;
    }

    public void setLayerValues( String key, String values ) {
        _om.put( getKey( key ), values );
    }

    public void setLayerValue( String key, int layer, String value, String defaultValue ) {
        int layers = getNbrLayers();
        if( layer >= layers ) {
            return;
        }

        // get old value
        String oldValues = _om.getString( getKey( key ) );
        String[] oldValueArray = oldValues.split( "," );

        ArrayList< String > newValues = new ArrayList< String >();
        while( newValues.size() < layers ) {
            newValues.add( defaultValue );
        }

        for( int i = 0; i < oldValueArray.length; ++i ) {
            String oldValue = oldValueArray[ i ];
            newValues.set( i, oldValue );
        }

        // replace new value
        newValues.set( layer, value );

        String newValuesString = String.join( ",", newValues );

        _om.put( getKey( key ), newValuesString );
    }

}
