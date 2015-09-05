package io.agi.ef.interprocess.apiInterfaces;

import javax.ws.rs.core.Response;

/**
 * Created by gideon on 1/08/15.
 */
public interface ConnectInterface {

    Response connect( String host, String port, String contextPath );
}
