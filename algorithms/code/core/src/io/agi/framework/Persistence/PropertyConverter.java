package io.agi.framework.persistence;

/**
 * Makes it easier get and set Strings using other data structures, to a delegated data structure.
 *
 * Created by dave on 16/02/16.
 */
public class PropertyConverter {

    PropertyStringAccess _delegate = null;

    public PropertyConverter( PropertyStringAccess delegate ) {
        _delegate = delegate;
    }

    public Float getPropertyFloat(String key, Float defaultValue ) {
        try {
            String defaultString = null;
            if( defaultValue != null ) {
                defaultString = String.valueOf( defaultValue );
            }
            String s = _delegate.getPropertyString(key, defaultString );
            return Float.valueOf( s );
        }
        catch( Exception e ) {
            return defaultValue;
        }
    }

    public void setPropertyFloat(String key, float value) {
        _delegate.setPropertyString( key, String.valueOf( value ) );
    }

    public Double getPropertyDouble(String key, Double defaultValue) {
        try {
            String defaultString = null;
            if( defaultValue != null ) {
                defaultString = String.valueOf( defaultValue );
            }
            String s = _delegate.getPropertyString(key, defaultString);
            return Double.valueOf( s );
        }
        catch( Exception e ) {
            return defaultValue;
        }
    }
    public void setPropertyDouble(String key, double value) {
        _delegate.setPropertyString( key, String.valueOf( value ) );
    }

    public Long getPropertyLong(String key, Long defaultValue) {
        try {
            String defaultString = null;
            if( defaultValue != null ) {
                defaultString = String.valueOf( defaultValue );
            }
            String s = _delegate.getPropertyString(key, defaultString );
            return Long.valueOf( s );
        }
        catch( Exception e ) {
            return defaultValue;
        }
    }
    public void setPropertyLong(String key, long value) {
        _delegate.setPropertyString( key, String.valueOf( value ) );
    }

    public Integer getPropertyInt(String key, Integer defaultValue) {
        try {
            String defaultString = null;
            if( defaultValue != null ) {
                defaultString = String.valueOf( defaultValue );
            }
            String s = _delegate.getPropertyString(key, defaultString);
            return Integer.valueOf( s );
        }
        catch( Exception e ) {
            return defaultValue;
        }
    }
    public void setPropertyInt(String key, int value) {
        _delegate.setPropertyString( key, String.valueOf( value ) );
    }

    public Boolean getPropertyBoolean(String key, Boolean defaultValue ) {
        try {
            String defaultString = null;
            if( defaultValue != null ) {
                defaultString = String.valueOf( defaultValue );
            }
            String s = _delegate.getPropertyString(key, defaultString);
            return Boolean.valueOf( s );
        }
        catch( Exception e ) {
            return defaultValue;
        }
    }

    public void setPropertyBoolean(String key, boolean value) {
        _delegate.setPropertyString( key, String.valueOf( value ) );
    }

}
