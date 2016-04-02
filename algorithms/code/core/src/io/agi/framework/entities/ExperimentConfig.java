package io.agi.framework.entities;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 2/04/16.
 */
public class ExperimentConfig extends EntityConfig {

//    public int interval; do we actually want this
    public boolean pause = false;
    public boolean terminate = false;
    public String terminationEntityName;
    public String terminationConfigPath;

}
