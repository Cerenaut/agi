package io.agi.ef.experiment.entities;


import io.agi.ef.experiment.actuators.Actuator;
import io.agi.ef.experiment.sensors.Sensor;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * One of the major modules of the AGIEF. This the intelligent Agent.
 *
 * Created by gideon on 26/07/15.
 */
public abstract class Agent extends AbstractEntity {

    private HashSet< Sensor > _sensors = new HashSet<>( );
    private HashSet< Actuator > _actuators = new HashSet<>( );

    public Agent( String name ) throws Exception {
        super( name );
    }

    public void addSensor( Sensor sensor ) {
        _sensors.add( sensor );
    }

    public void removeSensor( Sensor sensor ) {
        _sensors.remove( sensor );
    }

    public void addActuator( Actuator actuator ) {
        _actuators.add( actuator );
    }

    public void removeActuator( Actuator actuator ) {
        _actuators.remove( actuator );
    }

    public HashSet< Sensor > getSensors() {
        return _sensors;
    }

    public HashSet< Actuator > getActuators() {
        return _actuators;
    }


    public final void step() {

        _logger.log( Level.FINE, "Agent received step at time: {0}", getTime() );

        incTime();
        updateSensors();        // Requires updated World 'state' (so the Coord must supply it)
        stepBody();             // todo: want a better pattern than requiring developer to only override stepBody, it's a bit opaque
        setActuatorInputs();    // This is overridden by concrete Agent
        stepActuators();        // Updates the Agents 'state'     (Coord can request 'state' now)

    }

    /**
     * It is up to the concrete Agent, to update it's actuators with appropriate inputs.
     */
    protected abstract void setActuatorInputs();

    /**
     * This should be overridden
     */
    protected abstract void stepBody();

    private void stepActuators() {
        for ( Actuator actuator : _actuators ) {
            actuator.step();
        }
    }

    private void updateSensors() {
        for ( Sensor sensor : _sensors ) {
            sensor.update();
        }
    }

    public final void stop() {
        // this shouldn't be implemented in derived classes
        // for reasons given for 'run()' above
    }

}
