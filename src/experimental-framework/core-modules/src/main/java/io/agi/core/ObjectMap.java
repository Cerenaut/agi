package io.agi.core;

import java.util.HashMap;

/**
 * Key based lookup of shared objects to reduce reference passing.
 *
 * A global instance provided for convenience, or you can use specific instances.
 *
 * @author dave
 */
public class ObjectMap {

    protected HashMap< String, Object > _hm = new HashMap< String, Object >();

    protected static ObjectMap _instance;

    public static ObjectMap GetInstance() {
        if( _instance == null ) {
            _instance = new ObjectMap();
        }
        return _instance;
    }

    public static Object Remove( String key ) {
        ObjectMap od = GetInstance();
        return od.remove( key );
    }

    public static Object Get( String key ) {
        ObjectMap od = GetInstance();
        return od.get( key );
    }

    public static void Put( String key, Object o ) {
        ObjectMap od = GetInstance();
        od.put( key, o );
    }

    public ObjectMap() {
    }

    public synchronized Object get( String key ) {
        Object o = _hm.get( key );
        return o;
    }

    public synchronized void put( String key, Object o ) {
        _hm.put( key, o );
    }

    public synchronized Object remove( String key ) {
        Object o = _hm.remove( key );
        return o;
    }

}
