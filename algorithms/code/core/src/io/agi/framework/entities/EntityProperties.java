package io.agi.framework.entities;

/**
 *
 * These 'Property' models are bags of primitives, and can be nested objects.
 * THIS IS STANDARD practice for models.
 *
 * Created by gideon on 1/04/2016.
 */
public class EntityProperties {
    public int age = -1;           // default = 'not set'      optional
    public long seed = -1;         // default = 'not set'      optional
    public boolean reset = false;  // default                  optional
    public boolean flush = false;  // default

}
