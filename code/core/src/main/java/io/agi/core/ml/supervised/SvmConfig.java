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

package io.agi.core.ml.supervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by abdel on 12/10/17.
 */
public class SvmConfig extends SupervisedBatchTrainingConfig {

    private String _keyGamma = "gamma";

    public void setup( ObjectMap om,
                       String name,
                       Random r,
                       String modelString,
                       float constraintsViolation,
                       double gamma) {
        super.setup( om, name, r, modelString, constraintsViolation);
        setModelString( modelString );
        setGamma( gamma );
    }

    public void setGamma( double gamma ) {
        _om.put( getKey( _keyGamma ), gamma );
    }

    public double getGamma() {
        return _om.getDouble( getKey( _keyGamma ) );
    }

}
