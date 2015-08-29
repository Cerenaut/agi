package io.agi.interprocess.coordinator.services;

import io.agi.interprocess.apiInterfaces.ConnectInterface;
import io.agi.ef.serverapi.api.*;


import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;

public class ConnectApiServiceImpl extends ConnectApiService {

    public ConnectInterface _serviceDelegate = null;

    @Override
    public Response connectAgentContextPathGet( String contextPath )
            throws NotFoundException {

        return _serviceDelegate.connectNode(contextPath);
    }

}
