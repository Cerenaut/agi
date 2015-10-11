package io.agi.ef.entities;

import io.agi.core.AbstractFactory;
import io.agi.ef.Entity;

/**
 * Created by dave on 19/09/15.
 */
public class RelayFactory implements AbstractFactory<Entity> {

    public Entity create() {
        return new Relay();
    }
}
