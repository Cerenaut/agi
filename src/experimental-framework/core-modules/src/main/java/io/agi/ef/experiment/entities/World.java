package io.agi.ef.experiment.entities;

import io.agi.ef.experiment.actuators.Actuator;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *  One of the major modules of the AGIEF. This the base class World.
 *  Inherit it and create your own custom worlds.
 *
 * Created by gideon on 1/08/15.
 */
public class World extends AbstractEntity {

    private HashSet< Actuator > _actuators = new HashSet<>( );

    public World( String name ) throws Exception {
        super( name );
    }


    public void addActuator( Actuator actuator ) {
        _actuators.add( actuator );
    }

    private void stepActuators() {
        for ( Actuator actuator : _actuators ) {
            actuator.step();
        }
    }

    public void removeActuator( Actuator actuator ) {
        _actuators.remove( actuator );
    }

    public HashSet< Actuator > getActuators() {
        return _actuators;
    }

    public void run() {
    }

    public void step() {
        _logger.log( Level.FINER, "World stepped, time: {0}", getTime() );
        incTime();
        stepActuators();
    }

    public void stop() {

    }


}
