package io.agi.framework.persistence;

/**
 * Created by gideon on 21/03/2016.
 */
public interface PropertyStringAccess {
    String getPropertyString( String key, String defaultValue );

    void setPropertyString( String key, String value );
}
