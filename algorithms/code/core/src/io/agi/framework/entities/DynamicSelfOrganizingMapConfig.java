package io.agi.framework.entities;

import io.agi.core.ann.unsupervised.CompetitiveLearningConfig;
import io.agi.framework.Entity;
import io.agi.framework.EntityConfig;

/**
 * Created by dave on 2/04/16.
 */
public class DynamicSelfOrganizingMapConfig extends EntityConfig {

    boolean reset = false;
    float learningRate = 0.5f;
    float elasticity = 1.0f;
    int widthCells = 8;
    int heightCells = 8;

}
