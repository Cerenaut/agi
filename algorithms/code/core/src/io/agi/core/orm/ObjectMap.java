
package io.agi.core.orm;

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
        return od.remove(key);
    }
    
    public static Object Get( String key ) {
        ObjectMap od = GetInstance();
        return od.get(key);
    }

    public static String GetString( String key ) {
        ObjectMap od = GetInstance();
        try {
            Object o = od.get( key );
            String s = (String) o;
            return s;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Integer GetInteger( String key ) {
        ObjectMap od = GetInstance();
        try {
            Object o = Get(key);
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Integer.valueOf( (String)o );
            }
            if( o instanceof Integer ) {
                return (Integer)o;
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Long GetLong( String key ) {
        ObjectMap od = GetInstance();
        try {
            Object o = Get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Long.valueOf( (String)o );
            }
            if( o instanceof Long ) {
                return (Long)o;
            }
            if( o instanceof Integer ) {
                return Long.valueOf( (Integer)o );
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Boolean GetBoolean( String key ) {
        ObjectMap od = GetInstance();
        try {
            Object o = Get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Boolean.valueOf( (String)o );
            }
            if( o instanceof Boolean ) {
                return (Boolean)o;
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Float GetFloat( String key ) {
        ObjectMap od = GetInstance();
        try {
            Object o = Get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Float.valueOf( (String)o );
            }
            if( o instanceof Float ) {
                return (Float)o;
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Double GetDouble( String key ) {
        ObjectMap od = GetInstance();
        try {
            Object o = Get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Double.valueOf( (String)o );
            }
            if( o instanceof Double ) {
                return (Double)o;
            }
            if( o instanceof Float ) {
                return Double.valueOf( (Float)o );
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static void Put( String key, Object o ) {
        ObjectMap od = GetInstance();
        od.put( key, o );
    }

    public ObjectMap() {
    }

    public Object get( String key ) {
        Object o = _hm.get( key );
        return o;
    }

    public void put( String key, Object o ) {
        _hm.put( key, o );
    }

    public Object remove( String key ) {
        Object o = _hm.remove( key );
        return o;
    }
    
}
