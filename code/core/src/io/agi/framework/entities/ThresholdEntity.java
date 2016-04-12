package io.agi.framework.entities;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Set value to  when property <LOGIC> threshold
 * Property XXX-logic : One of <, <= , =, >=, >
 * Property XXX-key: A string being an external property key.
 * Property XXX-threshold: a number value, real or int.
 * Property XXX-test: A boolean value, true or false, the output
 * <p>
 * This can be used to implement termination on age (steps), or score, or anything really.
 * <p>
 * Created by dave on 2/04/16.
 */
public class ThresholdEntity extends Entity {

    public static final String ENTITY_TYPE = "threshold";

    public ThresholdEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
    }

    public Class getConfigClass() {
        return ThresholdEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        ThresholdEntityConfig config = ( ThresholdEntityConfig ) _config;

        Persistence p = _n.getPersistence();

        String stringValue = Framework.GetConfig( config.entityName, config.configPath );
        Float newValue = Float.valueOf( stringValue );

        if ( newValue == null ) {
            newValue = 0.f;
        }

        config.result = false;

        if ( config.logic.equals( "<" ) ) {
            if ( newValue < config.threshold ) {
                config.result = true;
            }
        }
        if ( config.logic.equals( "<=" ) ) {
            if ( newValue <= config.threshold ) {
                config.result = true;
            }
        }
        if ( config.logic.equals( "=" ) ) {
            if ( newValue == config.threshold ) {
                config.result = true;
            }
        }
        if ( config.logic.equals( ">=" ) ) {
            if ( newValue >= config.threshold ) {
                config.result = true;
            }
        }
        if ( config.logic.equals( ">" ) ) {
            if ( newValue > config.threshold ) {
                config.result = true;
            }
        }
    }
}