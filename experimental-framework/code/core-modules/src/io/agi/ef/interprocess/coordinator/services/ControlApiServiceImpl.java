package io.agi.ef.interprocess.coordinator.services;

import io.agi.ef.interprocess.apiInterfaces.ControlInterface;
import io.agi.ef.serverapi.api.*;

import io.agi.ef.serverapi.api.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ControlApiServiceImpl extends ControlApiService {

        public ControlInterface _serviceDelegate = null;

        @Override
        public Response controlEntityEntityNameCommandCommandGet( String entityName, String command, SecurityContext securityContext )
                throws NotFoundException {

//            return _serviceDelegate.command( entityName, command );
                return null;
        }

        @Override
        public Response controlEntityEntityNameStatusStateGet( String entityName,  String state, SecurityContext securityContext )
                throws NotFoundException {

//            return _serviceDelegate.status( entityName, state );
                return null;
        }

}
