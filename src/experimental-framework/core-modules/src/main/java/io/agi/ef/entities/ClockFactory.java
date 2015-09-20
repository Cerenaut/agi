package io.agi.ef.entities;

import io.agi.core.AbstractFactory;
import io.agi.ef.Entity;

/**
 * Creates entities of type Clock. Clocks step at fixed real-time intervals.
 *
 * Created by dave on 19/09/15.
 */
public class ClockFactory implements AbstractFactory<Entity> {

    public Entity create() {
        return new Clock();
    }
}
