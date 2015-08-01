package io.agi.ef.world;

import io.agi.ef.clientapi.ApiException;
import io.agi.ef.coordinator.CoordinatorClientServer;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.network.EndpointUtils;
import io.agi.ef.core.network.ServerConnection;

import javax.ws.rs.core.Response;
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

    public World( CommsMode commsMode ) {
        super( commsMode );
    }

    public World( CommsMode commsMode, String contextPath ) {
        super( commsMode, contextPath );
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
    public void state() {

    }
}
