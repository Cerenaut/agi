package io.agi.framework.serialization;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gideon on 23/03/2016.
 */
public class ModelPropertySet {

    public String entity;
    public HashMap< String, String > properties = new HashMap< String, String >(  );

    public ModelPropertySet( String entity, HashMap<String, String> properties) {
        this.entity = entity;
        this.properties = new HashMap< String, String >( properties );
    }
}
