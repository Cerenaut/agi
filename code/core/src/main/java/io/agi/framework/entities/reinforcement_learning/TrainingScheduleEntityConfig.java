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

package io.agi.framework.entities.reinforcement_learning;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 2/06/17.
 */
public class TrainingScheduleEntityConfig extends EntityConfig {

    public int totalEpochs = 1;
    public int trainingEpochs = 1;
    public int testingEpochs = 1;

    public String rewardEntityName;
    public String rewardConfigPath;

    public String epochEntityName;
    public String epochConfigPath;

    public String trainingEntities;
    public String testingEntities;

    public boolean terminate = false; // trigger to stop generating images

    public float rewardSumTraining = 0;
    public int rewardCountTraining = 0;
    public float rewardTraining = 0;

    public float rewardSumTesting = 0;
    public int rewardCountTesting = 0;
    public float rewardTesting = 0;

}
