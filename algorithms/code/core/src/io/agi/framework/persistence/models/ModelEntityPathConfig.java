package io.agi.framework.persistence.models;

import java.util.HashMap;

/**
 * Allows a serial representation of a set of separate
 * Created by gideon on 23/03/2016.
 */
public class ModelEntityPathConfig {

    public String _entityName;
    public String _configPath;
    public String _configValue;

    public ModelEntityPathConfig(String entityName, String configPath, String configValue) {
        this._entityName = entityName;
        this._configPath = configPath;
        this._configValue = configValue;
    }
}
