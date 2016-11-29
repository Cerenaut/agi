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

package io.agi.framework.entities;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 30/09/16.
 */
public class SpikingRegionLayerEntityConfig extends EntityConfig {

    public int excitatoryCells = 0;
    public int inhibitoryCells = 0;

    public int spikeThresholdBatchSize = 0;
    public int spikeThresholdBatchIndex = 0;

    public float timeConstantExcitatory = 0;
    public float timeConstantInhibitory = 0;

    public float targetSpikeRateExcitatory = 0;
    public float targetSpikeRateInhibitory = 0;

    public float resetSpikeThreshold = 0;

    public float spikeRateLearningRate = 0;
    public float spikeTraceLearningRate = 0;
    public float spikeThresholdLearningRate = 0;

    public float synapseLearningRateExternal1ToExcitatory   = 0;
    public float synapseLearningRateExternal2ToExcitatory   = 0;
    public float synapseLearningRateExcitatoryToExcitatory = 0;
    public float synapseLearningRateInhibitoryToExcitatory = 0;
    public float synapseLearningRateExternal1ToInhibitory   = 0;
    public float synapseLearningRateExternal2ToInhibitory   = 0;
    public float synapseLearningRateExcitatoryToInhibitory = 0;
    public float synapseLearningRateInhibitoryToInhibitory = 0;

    public float inputWeightExcitatory = 0;
    public float inputWeightInhibitory = 0;
    public float inputWeightExternal1  = 0;
    public float inputWeightExternal2  = 0;

}
