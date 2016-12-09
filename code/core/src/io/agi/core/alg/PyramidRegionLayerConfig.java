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
import io.agi.core.ann.unsupervised.KSparseAutoencoderConfig;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by dave on 4/07/16.
 */
public class PyramidRegionLayerConfig extends NetworkConfig {

    public static final String INPUT_C1_WIDTH = "input-c1-width";
    public static final String INPUT_C1_HEIGHT = "input-c1-height";
    public static final String INPUT_C2_WIDTH = "input-c2-width";
    public static final String INPUT_C2_HEIGHT = "input-c2-height";
    public static final String INPUT_P1_WIDTH = "input-p1-width";
    public static final String INPUT_P1_HEIGHT = "input-p1-height";
    public static final String INPUT_P2_WIDTH = "input-p2-width";
    public static final String INPUT_P2_HEIGHT = "input-p2-height";

    public static final String PREDICTOR_LEARNING_RATE = "predictor-learning-rate";
//    public static final String PREDICTOR_TRACE_DECAY_RATE = "predictor-trace-decay-rate";

    public static final String OUTPUT_SPIKE_AGE_MAX = "output-spike-age-max";
//    public static final String INTEGRATION_DECAY_RATE = "integration-decay-rate";
//    public static final String INTEGRATION_SPIKE_WEIGHT = "integration-spike-weight";

    public static final String SUFFIX_CLASSIFIER = "classifier";
    public static final String SUFFIX_PREDICTOR = "predictor";

    public KSparseAutoencoderConfig _classifierConfig;

    public PyramidRegionLayerConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputC1Width,
            int inputC1Height,
            int inputC2Width,
            int inputC2Height,
            int inputP1Width,
            int inputP1Height,
            int inputP2Width,
            int inputP2Height,
            int widthCells,
            int heightCells,
            float classifierLearningRate,
            int classifierSparsity,// k, the number of active cells each step
            float classifierSparsityOutput, // a factor determining the output sparsity
            int classifierAgeMin, // age of disuse where we start to promote cells
            int classifierAgeMax, // age of disuse where we start to promote cells
            float classifierAgeScale, // age of disuse where we start to promote cells
            float classifierRateScale, // age of disuse where we start to promote cells
            float classifierRateMax, // age of disuse where we start to promote cells
            float classifierRateLearningRate, // age of disuse where we start to promote cells
            int outputSpikeAgeMax,
//            float integrationDecayRate, // how fast the accumulated
//            float integrationSpikeWeight,
            float predictorLearningRate ) {
//            float predictorTraceDecayRate ) {

        super.setup( om, name, r );

        setInputC1Size( inputC1Width, inputC1Height );
        setInputC2Size( inputC2Width, inputC2Height );
        setInputP1Size( inputP1Width, inputP1Height );
        setInputP2Size( inputP2Width, inputP2Height );

        KSparseAutoencoderConfig  classifierConfig = new KSparseAutoencoderConfig();

        String classifierName = getKey( SUFFIX_CLASSIFIER );
        int classifierInputs = getInputCArea();
        int classifierAge = 0;
        boolean classifierBinaryOutput = false;
        classifierConfig.setup(
                om, classifierName, _r,
                classifierInputs, widthCells, heightCells, classifierLearningRate,
                classifierBinaryOutput,
                classifierSparsityOutput, classifierSparsity, classifierSparsity, classifierSparsity,
            classifierAgeMin, classifierAgeMax, classifierAge, classifierAgeScale,
            classifierRateScale, classifierRateMax, classifierRateLearningRate );

        _classifierConfig = classifierConfig;

        setOutputSpikeAgeMax( outputSpikeAgeMax );

//        setIntegrationDecayRate( integrationDecayRate );
//        setIntegrationSpikeWeight( integrationSpikeWeight );

        setPredictorLearningRate( predictorLearningRate );
//        setPredictorTraceDecayRate( predictorTraceDecayRate );
    }

    public Random getRandom() {
        return _r;
    }

//    public float getIntegrationDecayRate() {
//        float r = _om.getFloat( getKey( INTEGRATION_DECAY_RATE ) );
//        return r;
//    }
//
//    public void setIntegrationDecayRate( float r ) {
//        _om.put( getKey( INTEGRATION_DECAY_RATE ), r );
//    }

    public int getOutputSpikeAgeMax() {
        int n = _om.getInteger( getKey( OUTPUT_SPIKE_AGE_MAX ) );
        return n;
    }

    public void setOutputSpikeAgeMax( int n ) {
        _om.put( getKey( OUTPUT_SPIKE_AGE_MAX ), n );
    }

//    public float getIntegrationSpikeWeight() {
//        float r = _om.getFloat( getKey( INTEGRATION_SPIKE_WEIGHT ) );
//        return r;
//    }
//
//    public void setIntegrationSpikeWeight( float r ) {
//        _om.put( getKey( INTEGRATION_SPIKE_WEIGHT ), r );
//    }

    public float getPredictorLearningRate() {
        float r = _om.getFloat( getKey( PREDICTOR_LEARNING_RATE ) );
        return r;
    }

    public void setPredictorLearningRate( float r ) {
        _om.put( getKey( PREDICTOR_LEARNING_RATE ), r );
    }

//    public float getPredictorTraceDecayRate() {
//        float r = _om.getFloat( getKey( PREDICTOR_TRACE_DECAY_RATE ) );
//        return r;
//    }
//
//    public void setPredictorTraceDecayRate( float r ) {
//        _om.put( getKey( PREDICTOR_TRACE_DECAY_RATE ), r );
//    }

    public int getInputCArea() {
        Point p1 = getInputC1Size();
        Point p2 = getInputC2Size();

        int area = p1.x * p1.y + p2.x * p2.y;
        return area;
    }

    public int getInputPArea() {
        Point p1 = getInputC1Size();
        Point p2 = getInputC2Size();

        int area = p1.x * p1.y + p2.x * p2.y;
        return area;
    }

    public Point getInputC1Size() {
        int inputWidth = _om.getInteger( getKey( INPUT_C1_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( INPUT_C1_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public Point getInputC2Size() {
        int inputWidth = _om.getInteger( getKey( INPUT_C2_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( INPUT_C2_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public void setInputC1Size( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( INPUT_C1_WIDTH ), ffInputWidth );
        _om.put( getKey( INPUT_C1_HEIGHT ), ffInputHeight );
    }

    public void setInputC2Size( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( INPUT_C2_WIDTH ), ffInputWidth );
        _om.put( getKey( INPUT_C2_HEIGHT ), ffInputHeight );
    }

    public Point getInputP1Size() {
        int inputWidth = _om.getInteger( getKey( INPUT_P1_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( INPUT_P1_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public Point getInputP2Size() {
        int inputWidth = _om.getInteger( getKey( INPUT_P2_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( INPUT_P2_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public void setInputP1Size( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( INPUT_P1_WIDTH ), ffInputWidth );
        _om.put( getKey( INPUT_P1_HEIGHT ), ffInputHeight );
    }

    public void setInputP2Size( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( INPUT_P2_WIDTH ), ffInputWidth );
        _om.put( getKey( INPUT_P2_HEIGHT ), ffInputHeight );
    }

}
