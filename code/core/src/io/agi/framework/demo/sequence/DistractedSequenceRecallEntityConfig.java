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

package io.agi.framework.demo.sequence;

import io.agi.core.data.Data;
import io.agi.framework.EntityConfig;

/**
 * Created by dave on 19/05/17.
 */
public class DistractedSequenceRecallEntityConfig extends EntityConfig {

    public int epoch = 0;
    public int sequence = 0;
    public int sequenceLength = 0;

    public int distractors = 0;
    public int targets = 0;
    public int prompts = 0;

    public float reward = 0;

}
