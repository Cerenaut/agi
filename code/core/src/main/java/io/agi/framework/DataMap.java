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

package io.agi.framework;

import io.agi.core.data.Data;
import io.agi.framework.persistence.DataModelData;
import io.agi.framework.persistence.models.ModelData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * An in-memory cache of data structures, using their unique keys.
 * <p/>
 * Created by dave on 28/03/16.
 */
public class DataMap {

    HashMap< String, DataModelData> _cache = new HashMap< String, DataModelData >();

    public DataMap() {

    }

    public boolean hasData( String name ) {
        synchronized( _cache ) {
            return _cache.keySet().contains( name );
        }
    }

    public DataModelData getData( String name ) {
        synchronized( _cache ) {
            DataModelData d = _cache.get( name );
            return d;
        }
    }

    public void setData( String name, DataModelData d ) {
        synchronized( _cache ) {
            _cache.put( name, d );
        }
    }

    public void removeData( String name ) {
        synchronized( _cache ) {
            _cache.remove( name );
        }
    }
}
