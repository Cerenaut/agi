package io.agi.framework;

import io.agi.core.data.Data;

import java.util.HashMap;

/**
 * An in-memory cache of data structures, using their unique keys.
 *
 * Created by dave on 28/03/16.
 */
public class DataMap {

    HashMap< String, Data > _cache = new HashMap< String, Data>();

    public DataMap() {

    }

    public Data getData( String name ) {
        synchronized( _cache ) {
            Data d = _cache.get(name);
            return d;
        }
    }
    public void putData( String name, Data d ) {
        synchronized( _cache ) {
            _cache.put( name, d );
        }
    }
}
