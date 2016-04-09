package io.agi.framework.entities;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 2/04/16.
 */
public class RegionConfig extends EntityConfig {

    int organizerWidthCells = 10;
    int organizerHeightCells = 10;

    // Column Sizing
    int classifierWidthCells = 6;
    int classifierHeightCells = 6;

    // Organizer training
    int receptiveFieldsTrainingSamples = 6;
    int receptiveFieldSize = 8;
    float organizerLearningRate = 0.02f;
    float organizerLearningRateNeighbours = 0.01f;
    float organizerNoiseMagnitude = 0.0f;
    int organizerEdgeMaxAge = 500;
    float organizerStressLearningRate = 0.01f;
    float organizerStressThreshold = 0.1f;
    int organizerGrowthInterval = 100;

    // Classifier training
    float classifierLearningRate = 0.02f;
    float classifierLearningRateNeighbours = 0.01f;
    float classifierNoiseMagnitude = 0.0f;
    int classifierEdgeMaxAge = 500;
    float classifierStressLearningRate = 0.01f;
    float classifierStressThreshold = 0.1f;
    int classifierGrowthInterval = 100;

    // Predictor
    float predictorHiddenLayerScaleFactor = 0.1f; // Note, this assumes that the input data can be easily modelled by a few causes
    float predictorLearningRate = 0.1f;
    float predictorRegularization = 0.0f;

}
