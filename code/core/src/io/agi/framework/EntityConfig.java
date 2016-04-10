package io.agi.framework;

/**
 * These 'Property' models are bags of primitives, and can be nested objects.
 * THIS IS STANDARD practice for models.
 * <p>
 * Created by gideon on 1/04/2016.
 */
public class EntityConfig {

    public int age = 0;            // default = 'not set'      optional
    public Long seed = null;       // default = 'not set'      optional
    public boolean reset = false;  // default                  optional
    public boolean flush = false;  // default

}
