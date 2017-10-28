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

package io.agi.framework.references;

import io.agi.core.data.Data;
import io.agi.framework.Node;

/**
 * A Data object that can be a reference to other Data objects.
 *
 * Created by dave on 24/10/17.
 */
public class DataRef {

    public String _key;
    public String _encoding; // hint as to how to serialize
    public String _refKeys;
    public Data _data;

    public DataRef( String key, String encoding, String refKeys, Data d ) {
        _key = key;
        _encoding = encoding;
        _refKeys = refKeys;
        _data = d;
    }

    public boolean isReference() {
        if( _refKeys != null ) {
            if( !_refKeys.isEmpty() ) {
                return true;
            }
        }
        return false;
    }

    public Data getData( Node n, DataRefResolver drr ) {
        return drr.getData( n, this );
    }

}
