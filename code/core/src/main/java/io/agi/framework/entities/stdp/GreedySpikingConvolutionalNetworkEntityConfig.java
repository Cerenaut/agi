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

package io.agi.framework.entities.stdp;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 6/05/17.
 */
public class GreedySpikingConvolutionalNetworkEntityConfig extends EntityConfig {

    public boolean clear = false;

    public String clearFlagEntityName = "";
    public String clearFlagConfigPath = "";

    public int trainingAge = 0;
    public float weightsStdDev = 0;
    public float weightsMean = 0;
    public float learningRatePos = 0;
    public float learningRateNeg = 0;
//    public float integrationThreshold = 0;
    public int nbrLayers = 0;
    public String layerTrainingAge = "";
    public String layerIntegrationThreshold = "";
    public String layerInputPadding = "";
    public String layerInputStride = "";
    public String layerWidth = "";
    public String layerHeight = "";
    public String layerDepth = "";
    public String layerfieldWidth = "";
    public String layerfieldHeight = "";
    public String layerfieldDepth = "";
    public String layerPoolingWidth = "";
    public String layerPoolingHeight = "";

}
