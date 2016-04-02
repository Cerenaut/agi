package io.agi.framework.persistence.models;

/**
 * Allows a serial representation of a set of separate
 * Created by gideon on 23/03/2016.
 */
public class ModelEntityConfigPath {

    public String _entityName;
    public String _configPath;
    public String _configValue;

    public ModelEntityConfigPath( String entityName, String configPath, String configValue ) {
        this._entityName = entityName;
        this._configPath = configPath;
        this._configValue = configValue;
    }
}
