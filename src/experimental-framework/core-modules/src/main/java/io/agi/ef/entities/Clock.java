package io.agi.ef.entities;

import io.agi.ef.Entity;

import java.util.Date;
import java.util.HashSet;

/**
 * An entity that can self-generate step events to iterate other entities.
 * Specify the entities that will receive step events on clock steps.
 *
 * You can have more than one clock.
 *
 * The clock can be woken up early by sending a step event to it.
 *
 * Created by dave on 19/09/15.
 */
public class Clock extends Entity {

    public static final String ENTITY_TYPE = "clock";

    public static final String PROPERTY_INTERVAL = "interval";
    public static final String PROPERTY_ENTITY = "entity";
    public static final String PROPERTY_ACTION = "action";

    public Clock() {
    }

    public void configure( String config ) {

        int interval = configureProperty(config, PROPERTY_INTERVAL, 100);
        addProperty( PROPERTY_INTERVAL, interval );

        String entityName = configureProperty(config, PROPERTY_ENTITY, "");
        addProperty(PROPERTY_ENTITY, entityName);

        String entityAction = configureProperty( config, PROPERTY_ACTION, "step" );
        addProperty(PROPERTY_ACTION, entityAction);
    }

    public synchronized void onStep() {
        // block until an event happens: entity=this, action=step
        // Entities always run step by step to allow synchronization to an external clock.
        // Of course, the clock can notify faster than old_entities can run.
        int interval = getPropertyAsInt( PROPERTY_INTERVAL );

        // TODO adjust clock to avoid drift?
//        Date d = new Date();
//        long now = d.getTime();
//
//        if( _last != null ) {
//            _last = now - (long)interval;
//        }

        long remaining = interval;

        try {
            wait( remaining );
        }
        catch( InterruptedException ie ) {
            // normal
        }

        HashSet< String > dirtyData = getClearDirtyData(); // these matrices are marked as dirty prior to start of step.

        doStep(dirtyData);

        postSelfEvent(EVENT_STEPPED);
    }

    /**
     * Overrides to post an event to an entity specified by the given property.
     * @param dirtyData
     */
    public void doStep( HashSet< String > dirtyData ) {
        super.doStep(dirtyData);

        // generate the necessary action:
        String entity = getProperty( PROPERTY_ENTITY );
        String action = getProperty( PROPERTY_ACTION );

        if( entity.isEmpty() || action.isEmpty() ) {
            return;
        }

        postEvent( entity, action );
    }

}
