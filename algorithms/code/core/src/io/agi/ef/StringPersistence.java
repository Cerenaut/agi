package io.agi.ef;

/**
 * Makes it easier to write a persistence library because
 *
 * Created by dave on 16/02/16.
 */
public abstract class StringPersistence implements Persistence {

//    public abstract String getDataString(String key );
//    public abstract String setDataString(String key, String value);

    public Float getPropertyFloat(String key) {
        try {
            String s = getPropertyString(key);
            return Float.valueOf( s );
        }
        catch( Exception e ) {
            return null;
        }
    }
    public void setPropertyFloat(String key, float value) {
        setPropertyString( key, String.valueOf( value ) );
    }

    public Double getPropertyDouble(String key) {
        try {
            String s = getPropertyString(key);
            return Double.valueOf( s );
        }
        catch( Exception e ) {
            return null;
        }
    }
    public void setPropertyDouble(String key, double value) {
        setPropertyString( key, String.valueOf( value ) );
    }

    public Long getPropertyLong(String key) {
        try {
            String s = getPropertyString(key);
            return Long.valueOf( s );
        }
        catch( Exception e ) {
            return null;
        }
    }
    public void setPropertyLong(String key, long value) {
        setPropertyString( key, String.valueOf( value ) );
    }

    public Integer getPropertyInt(String key) {
        try {
            String s = getPropertyString(key);
            return Integer.valueOf( s );
        }
        catch( Exception e ) {
            return null;
        }
    }
    public void setPropertyInt(String key, int value) {
        setPropertyString( key, String.valueOf( value ) );
    }

    public Boolean getPropertyBoolean(String key ) {
        try {
            String s = getPropertyString(key);
            return Boolean.valueOf( s );
        }
        catch( Exception e ) {
            return null;
        }
    }

    public void setPropertyBoolean(String key, boolean value) {
        setPropertyString( key, String.valueOf( value ) );
    }

//    public Data getData(String key) {
//        try {
//            String s = getDataString(key);
//            Data d = JsonData.getData( s );
//            return d;
//        }
//        catch( Exception e ) {
//            return null;
//        }
//    }
//    public void setData(String key, Data value) {
//        String s = JsonData.getString( value );
//        setDataString(key, s);
//    }

}
