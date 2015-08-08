package io.agi.ef.core.network.coordinator.services;

import io.agi.ef.core.apiInterfaces.ConnectInterface;
import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.network.coordinator.Coordinator;
import io.agi.ef.serverapi.api.*;


import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;

public class ConnectApiServiceImpl extends ConnectApiService {

    public ConnectInterface _serviceDelegate = null;

    @Override
    public Response connectAgentContextPathGet( String contextPath )
            throws NotFoundException {

        return _serviceDelegate.connectAgent(contextPath);
    }

    @Override
    public Response connectWorldContextPathGet( String contextPath )
            throws NotFoundException {

        return _serviceDelegate.connectWorld( contextPath );
    }
}
