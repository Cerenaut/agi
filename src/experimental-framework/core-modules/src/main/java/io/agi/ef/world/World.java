package io.agi.ef.world;

import io.agi.ef.core.UniversalState;
import io.agi.ef.core.actuators.Actuator;
import io.agi.ef.clientapi.ApiException;
import io.agi.ef.coordinator.CoordinatorClientServer;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 *
 *  One of the major modules of the AGIEF. This the base class World.
 *  Inherit it and create your own custom worlds.
 *
 * Created by gideon on 1/08/15.
 */
public class World extends CoordinatorClientServer {

    private static final Logger _logger = Logger.getLogger( World.class.getName() );
    private HashSet< Actuator > _actuators = new HashSet<>( );
    private Collection<UniversalState> _agentStates = null;

    public World( CommsMode commsMode ) {
        super( commsMode );
    }

    public World( CommsMode commsMode, String contextPath ) {
        super( commsMode, contextPath );
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
    protected int listenerPort() {
        return EndpointUtils.worldListenPort();
    }

    @Override
    protected Logger getLogger() {
        return null;
    }

    @Override
    public void connectionAccepted( ServerConnection sc ) throws ApiException {

    }

    @Override
    public Response run() {
        return null;
    }

    @Override
    public Response step() {
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
        // not relevant here
    }

    public void setAgentStates( Collection<UniversalState> agentStates ) {
        _agentStates = agentStates;
    }
}
