package io.agi.framework.persistence.models;

/**
 * Created by gideon on 23/03/2016.
 */
public class ModelDataReference {
    public String dataKey;
    public String refKeys;

    ModelDataReference( String dataKey, String refKeys ) {
        this.dataKey = dataKey;
        this.refKeys = refKeys;
    }
}
