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

package io.agi.core.ann;

import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * A separate object to an actual algorithm implementation to allow configuration and instantiation to be divorced.
 * Since algorithms may be composed out of several smaller networks, need a hierarchical way to generate networks within
 * networks. Hence, let's use a systematic way of naming things to avoid clashes.
 * <p/>
 * Created by dave on 10/01/16.
 */
public class NetworkConfig {

    public static final String KEY_LEARN = "learn";

    public String _name;
    public ObjectMap _om;
    public Random _r;

    /**
     * Use the specified ObjectMap with prefix name to generate keys for parameters.
     *
     * @param om
     * @param name
     */
    public void setup( ObjectMap om, String name, Random r ) {
        _om = om;
        _name = name;
        _r = r;

        setLearn( true );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        _om = nc._om;
        _name = name;
        _r = nc._r;
    }

    public String getKey( String suffix ) {
        return Keys.concatenate( _name, suffix );
    }

    public void setLearn( boolean b ) {
        _om.put( getKey( KEY_LEARN ), b );
    }

    public boolean getLearn() {
        Boolean b = _om.getBoolean( getKey( KEY_LEARN ) );
        return b.booleanValue();
    }

}
