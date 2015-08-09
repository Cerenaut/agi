package io.agi.ef.core.network.entities;

import io.agi.ef.core.UniversalState;
import io.agi.ef.core.actuators.Actuator;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;
import io.agi.ef.core.sensors.Sensor;

import javax.ws.rs.core.Response;
import java.util.Collection;
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

    private static final Logger _logger = Logger.getLogger( World.class.getName() );
    private HashSet< Actuator > _actuators = new HashSet<>( );
    private Collection< UniversalState > _agentStates = null;
    private UniversalState _worldState = new UniversalState();

    public World() {
        super();
    }

    public World( String contextPath ) throws Exception {
        super( contextPath, EndpointUtils.worldListenPort(), ServerConnection.ServerType.World );
    }

    @Override
    protected Logger getLogger() {
        return _logger;
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

    public void setAgentStates( Collection< UniversalState > agentStates ) {
        _agentStates = agentStates;
    }

    public Collection< UniversalState > getAgentStates( ) {
        return _agentStates;
    }

    @Override
    public Response run() {
        return null;
    }

    @Override
    public Response step() {
        _logger.log( Level.FINER, "World stepped, time: {0}", getTime() );
        incTime();
        stepActuators();
        return null;
    }

    @Override
    public Response stop() {
        return null;
    }

    @Override
    public UniversalState getState() {

        _worldState.reset();

        // todo: refactor this code to the state object

        for ( Actuator actuator : _actuators ) {
            Float val = actuator.getOutput().max();
            _worldState._state.put( actuator.getClass().getName(), val.toString() );
        }

        return _worldState;
    }

    @Override
    public void setWorldState( UniversalState state ) {
        // wipe out the current world state, and replace with state
        _worldState = state;
    }

}
