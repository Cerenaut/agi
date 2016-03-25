package io.agi.framework.persistence.models;

/**
 * Created by gideon on 23/03/2016.
 */
public class ModelDataReference {
    public String dataKey;
    public String refKey;

    ModelDataReference( String dataKey, String refKey ) {
        this.dataKey = dataKey;
        this.refKey = refKey;
    }
}
