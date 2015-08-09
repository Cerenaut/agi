package io.agi.ef.core.network.entities;


import io.agi.ef.core.UniversalState;
import io.agi.ef.core.actuators.Actuator;
import io.agi.ef.core.network.ServerConnection;
import io.agi.ef.core.sensors.Sensor;
import io.agi.ef.core.network.EndpointUtils;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * One of the major modules of the AGIEF. This the intelligent Agent.
 *
 * When CommsMode == NETWORK, all communications between entities occurs over the network
 *      CommsMode == NON_NETWORK, the Agent does not start a Server, and does not request the Coordinator connect to it
 *
 * Created by gideon on 26/07/15.
 */
public abstract class Agent extends AbstractEntity {

    private static final Logger _logger = Logger.getLogger( Agent.class.getName() );
    private HashSet< Sensor > _sensors = new HashSet<>( );
    private HashSet< Actuator > _actuators = new HashSet<>( );
    private UniversalState _worldState = null;

    public Agent() {
        super();
    }

    public Agent( String contextPath ) throws Exception {
        super( contextPath, EndpointUtils.agentListenPort(), ServerConnection.ServerType.Agent );
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
     * Assumes that the _worldState object is up-to-date.
     * Updates the Actuator outputs.
     */
    public final Response step() {

        _logger.log( Level.FINE, "Agent received step at time: {0}", getTime() );

        incTime();
        updateSensors();        // Requires updated World 'state' (so the Coord must supply it)
        stepBody();             // todo: want a better pattern than requiring developer to only override stepBody, it's a bit opaque
        setActuatorInputs();    // This is overriden by concrete Agent
        stepActuators();        // Updates the Agents 'state'     (Coord can request 'state' now)

        return null;
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

        UniversalState uState = new UniversalState();

        // todo: refactor this code to the state object

        for ( Actuator actuator : _actuators ) {
            uState._state.put( actuator.getClass().getName(), actuator.getOutput().toString() );
        }

        for ( Sensor sensor : _sensors ) {
            uState._state.put( sensor.getClass().getName(), sensor.getOutput().toString() );
        }

        return uState;
    }

    @Override
    public void setWorldState( UniversalState state ) {
        _worldState = state;
    }

    @Override
    public void setAgentStates( Collection<UniversalState> agentStates ) {
        // useful if the agent uses other agents states (not most common use case)
    }

}
