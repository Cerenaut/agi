package io.agi.framework.Persistence;

/**
 * Created by gideon on 21/03/2016.
 */
public interface PropertyStringAccess {
    String getPropertyString( String key, String defaultValue );
    void setPropertyString( String key, String value );
}
