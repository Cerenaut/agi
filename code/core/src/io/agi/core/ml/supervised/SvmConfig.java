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

package io.agi.core.ml.supervised;

import io.agi.core.ann.NetworkConfig;

/**
 * Created by gideon on 14/12/16.
 */
public class SvmConfig extends NetworkConfig {

    public String _keyRegularisation = "regularisation";
    public String _keyModelString = "modelString";

    public void setup( float C ) {
        setRegularisation( C );
    }

    public void setRegularisation( float C ) {
        _om.put( getKey( _keyRegularisation ), C );
    }

    public float getRegularisation() {
        return _om.getFloat( _keyRegularisation );
    }

    public void setModelString( String modelString ) {
        _om.put( getKey( _keyModelString ), modelString );
    }

    public String getModelString() {
        return _om.getString( _keyModelString );
    }

}
