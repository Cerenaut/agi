package io.agi.framework.entities;

/**
 *
 * These 'Property' models are bags of primitives, and can be nested objects.
 * THIS IS STANDARD practice for models.
 *
 * Created by gideon on 1/04/2016.
 */
public class EntityProperties {
    int age = -1;           // default = 'not set'      optional
    long seed = -1;         // default = 'not set'      optional
    boolean reset = false;  // default                  optional
    boolean flush = false;  // default
}
