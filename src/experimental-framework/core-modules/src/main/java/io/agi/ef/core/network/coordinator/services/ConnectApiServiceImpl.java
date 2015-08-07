package io.agi.ef.core.network.coordinator.services;

import io.agi.ef.core.apiInterfaces.ConnectInterface;
import io.agi.ef.core.apiInterfaces.ControlInterface;
import io.agi.ef.core.network.coordinator.Coordinator;
import io.agi.ef.serverapi.api.*;


import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;

public class ConnectApiServiceImpl extends ConnectApiService {

    public ConnectInterface _coordinator = null;

    @Override
    public Response connectAgentBaseurlGet( String contextPath )
            throws NotFoundException {

        return _coordinator.connectAgentBaseurl( contextPath );
    }

}
