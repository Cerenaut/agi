package io.agi.ef.core.network.entities;

import io.agi.ef.core.UniversalState;
import io.agi.ef.core.actuators.Actuator;
import io.agi.ef.core.network.EndpointUtils;

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
    private Collection<UniversalState> _agentStates = null;

    public World() {
        super();
    }

    public World( String contextPath, int port ) throws Exception {
        super( contextPath, port );
    }

    @Override
    protected Logger getLogger() {
        return _logger;
    }

    public void addActuator( Actuator actuator ) {
        _actuators.add( actuator );
    }

    public void removeActuator( Actuator actuator ) {
        _actuators.remove( actuator );
    }

    public HashSet< Actuator > getActuators() {
        return _actuators;
    }

    @Override
    public Response run() {
        return null;
    }

    @Override
    public Response step() {
        incTime();
        System.out.println( "World stepped at time: " + getTime() );
        return null;
    }

    @Override
    public Response stop() {
        return null;
    }

    @Override
    public UniversalState getState() {
        return null;
    }

    @Override
    public void setWorldState( UniversalState state ) {
        // wipe out the current world state, adn replace with state
    }

    public void setAgentStates( Collection<UniversalState> agentStates ) {
        _agentStates = agentStates;
    }
}
