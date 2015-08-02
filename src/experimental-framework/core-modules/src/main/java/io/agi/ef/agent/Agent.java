package io.agi.ef.agent;


import io.agi.ef.core.UniversalState;
import io.agi.ef.core.actuators.Actuator;
import io.agi.ef.core.sensors.Sensor;
import io.agi.ef.coordinator.CoordinatorClientServer;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.network.EndpointUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * One of the major modules of the AGIEF. This the intelligent Agent.
 *
 * When CommsMode == NETWORK, all communications between entities occurs over the network
 *      CommsMode == NON_NETWORK, the Agent does not start a Server, and does not request the Coordinator connect to it
 *
 * Created by gideon on 26/07/15.
 */
public class Agent extends CoordinatorClientServer {

    private static final Logger _logger = Logger.getLogger( Agent.class.getName() );
    private HashSet< Sensor > _sensors = new HashSet<>( );
    private HashSet< Actuator > _actuators = new HashSet<>( );
    private UniversalState _worldState = null;

    public Agent( CommsMode commsMode ) {
        super( commsMode );    // Default context path. This will conflict with other agents if not customised.
    }

    public Agent( CommsMode commsMode, String agentContextPath ) {
        super( commsMode, agentContextPath );
    }

    protected int listenerPort() {
        return EndpointUtils.agentListenPort();
    }

    @Override
    protected Logger getLogger() {
        return _logger;
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


    @Override
    public final Response run() {
        // To Discuss:
        // I don't think this should be an option in AGENT - only in Coordinator for synchronisation
        // Dave will disagree
        return null;
    }

    @Override
    /**
     * Assumes that the _worldState object is up-to-date
     */
    public final Response step() {

        updateSensors();        // requires updated World 'state' (so the Coord must supply it)

        incTime();
        _logger.log( Level.FINE, "Agent received step at time: {0}", getTime() );

        stepBody();     // todo: want a better pattern than requiring developer to only override stepBody, it's a bit opaque

        updateActuators();      // updates the Agents 'state'     (Coord must request 'state' now)

        return null;
    }

    /**
     * This should be overrided
     */
    protected void stepBody() {
    }

    private void updateActuators() {
        for ( Actuator actuator : _actuators ) {
            actuator.update();
        }
    }

    private void updateSensors() {

        for ( Sensor sensor : _sensors ) {
            sensor.update( _worldState );
        }
    }

    @Override
    public final Response stop() {
        // this shouldn't be implemented in derived classes
        // for reasons given for 'run()' above
        return null;
    }

    @Override
    public UniversalState getState() {
        return null;
    }

    @Override
    public void setWorldState( UniversalState state ) {
        _worldState = state;
    }

}
