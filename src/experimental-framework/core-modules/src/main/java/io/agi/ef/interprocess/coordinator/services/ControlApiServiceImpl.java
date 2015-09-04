package io.agi.ef.interprocess.coordinator.services;

import io.agi.ef.interprocess.apiInterfaces.ControlInterface;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;

public class ControlApiServiceImpl extends ControlApiService {

        public ControlInterface _serviceDelegate = null;

        @Override
        public Response controlEntityEntityNameCommandCommandGet( String entityName, String command )
                throws NotFoundException {

            return _serviceDelegate.command( entityName, command );
        }

        @Override
        public Response controlEntityEntityNameStatusStateGet( String entityName,  String state )
                throws NotFoundException {

            return _serviceDelegate.status( entityName, state );
        }

}
