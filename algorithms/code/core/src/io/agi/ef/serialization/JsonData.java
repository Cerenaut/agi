package io.agi.ef.serialization;

import io.agi.core.data.Data;
import io.agi.ef.Entity;

/**
 * Conversion to JSON for serialization.
 *
 * Created by dave on 16/02/16.
 */
public class JsonData {

    public String _key;
    public String _sizes;
    public String _elements;

    public JsonData( String key, Data d ) {
        _key = key;
        _sizes = d._d.toString();
        _elements = d._values.toString();
    }

    public JsonData( String key, String sizes, String elements ) {
        _key = key;
        _sizes = sizes;
        _elements = elements;
    }

    public Data getData() {
        return null; // TODO
    }
}
