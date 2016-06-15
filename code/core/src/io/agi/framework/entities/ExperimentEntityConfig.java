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
 * Created by dave on 2/04/16.
 */
public class ExperimentEntityConfig extends EntityConfig {

    //    public int interval; do we actually want this? No, it's slow enough already..!
    public boolean pause = false;
    public boolean terminate = false; // you can change this, an instruction to terminate
    public boolean terminating = false; // notices the terminate flag
    public boolean terminated = false; // set when the has terminated and update finished.
    public String terminationEntityName;
    public String terminationConfigPath;
    public int terminationAge = -1; // if negative, then never terminates unless via termination condition.

}
