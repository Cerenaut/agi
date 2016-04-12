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

package io.agi.core.orm;

import java.util.HashMap;

/**
 * Key based lookup of shared objects to reduce reference passing.
 * <p/>
 * A global instance provided for convenience, or you can use specific instances.
 *
 * @author dave
 */
public class ObjectMap {

    protected HashMap< String, Object > _hm = new HashMap< String, Object >();

    protected static ObjectMap _instance;

    public ObjectMap() {
    }

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

    public Object remove( String key ) {
        Object o = _hm.remove( key );
        return o;
    }

    public static Object Get( String key ) {
        ObjectMap od = GetInstance();
        return od.get( key );
    }

    public Object get( String key ) {
        Object o = _hm.get( key );
        return o;
    }

    public static void Put( String key, Object o ) {
        ObjectMap od = GetInstance();
        od.put( key, o );
    }

    public void put( String key, Object o ) {
        _hm.put( key, o );
    }

    public static String GetString( String key ) {
        ObjectMap od = GetInstance();
        return od.getString( key );
    }

    public String getString( String key ) {
        try {
            Object o = get( key );
            String s = ( String ) o;
            return s;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Integer GetInteger( String key ) {
        ObjectMap od = GetInstance();
        return od.getInteger( key );
    }

    public Integer getInteger( String key ) {
        try {
            Object o = get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Integer.valueOf( ( String ) o );
            }
            if( o instanceof Integer ) {
                return ( Integer ) o;
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Long GetLong( String key ) {
        ObjectMap od = GetInstance();
        return od.getLong( key );
    }

    public Long getLong( String key ) {
        try {
            Object o = get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Long.valueOf( ( String ) o );
            }
            if( o instanceof Long ) {
                return ( Long ) o;
            }
            if( o instanceof Integer ) {
                return Long.valueOf( ( Integer ) o );
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Boolean GetBoolean( String key ) {
        ObjectMap od = GetInstance();
        return od.getBoolean( key );
    }

    public Boolean getBoolean( String key ) {
        try {
            Object o = get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Boolean.valueOf( ( String ) o );
            }
            if( o instanceof Boolean ) {
                return ( Boolean ) o;
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Float GetFloat( String key ) {
        ObjectMap od = GetInstance();
        return od.getFloat( key );
    }

    public Float getFloat( String key ) {
        try {
            Object o = get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Float.valueOf( ( String ) o );
            }
            if( o instanceof Float ) {
                return ( Float ) o;
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static Double GetDouble( String key ) {
        ObjectMap od = GetInstance();
        return od.getDouble( key );
    }

    public Double getDouble( String key ) {
        try {
            Object o = get( key );
            if( o == null ) {
                return null;
            }
            if( o instanceof String ) {
                return Double.valueOf( ( String ) o );
            }
            if( o instanceof Double ) {
                return ( Double ) o;
            }
            if( o instanceof Float ) {
                return Double.valueOf( ( Float ) o );
            }
            return null;
        }
        catch( Exception e ) {
            return null;
        }
    }

}
