package io.agi.ef.entities.experiment;

import io.agi.core.AbstractFactory;
import io.agi.ef.Entity;

/**
 * Created by dave on 17/09/15.
 */
public class SensorFactory implements AbstractFactory<Entity> {

    public Entity create() {
        return new Sensor();
    }
}
