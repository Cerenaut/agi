package io.agi.ef.interprocess.coordinator.services;


import io.agi.ef.serverapi.api.*;


import io.agi.ef.serverapi.api.NotFoundException;

import io.agi.ef.interprocess.apiInterfaces.ConnectInterface;

import javax.ws.rs.core.Response;


public class ConnectApiServiceImpl extends ConnectApiService {

    public ConnectInterface _serviceDelegate = null;

    @Override
    public Response connectHostHostPortPortContextPathContextPathGet(String host, String port, String contextPath)
            throws NotFoundException {

        return _serviceDelegate.connect( host, port, contextPath );
    }

}