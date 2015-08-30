package io.agi.interprocess.apiInterfaces;

import javax.ws.rs.core.Response;

/**
 * Created by gideon on 1/08/15.
 */
public interface ConnectInterface {
    Response connectCoordinator( String contextPath );
}
