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

import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by abdel on 12/10/17.
 */
public class LogisticRegressionConfig extends SupervisedBatchTrainingConfig {

    private String _keyAddBias = "addBias";  // add a 'constant' feature, so that there is a bias term in the hypothesis (otherwise linear decision boundary goes through origin)

    public void setup( ObjectMap om,
                       String name,
                       Random r,
                       String modelString,
                       float constraintsViolation,
                       boolean addBias) {
        super.setup( om, name, r, modelString, constraintsViolation);
        setAddBias( addBias );
    }

    public void setAddBias( boolean addBias ) {
        _om.put( getKey( _keyAddBias ), addBias );
    }

    public boolean getAddBias() {
        return _om.getBoolean( getKey( _keyAddBias ) );
    }

}
