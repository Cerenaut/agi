package io.agi.framework.entities;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 2/04/16.
 */
public class GrowingNeuralGasConfig extends EntityConfig {

    float learningRate = 0.1f;
    int widthCells = 8;
    int heightCells = 8;

    float learningRateNeighbours = 0.05f;
    float noiseMagnitude = 0.005f;
    int edgeMaxAge = 200;
    float stressLearningRate = 0.15f;
    float stressThreshold = 0.01f;
    int growthInterval = 2;

}
