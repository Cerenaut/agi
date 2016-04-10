package io.agi.framework.entities;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 2/04/16.
 */
public class ValueSeriesConfig extends EntityConfig {

    public int period = 100; // number of samples before it wraps
    public String entityName;
    public String configPath;

}
