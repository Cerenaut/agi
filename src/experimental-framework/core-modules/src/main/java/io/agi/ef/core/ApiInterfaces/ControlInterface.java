package io.agi.ef.core.ApiInterfaces;

import javax.ws.rs.core.Response;

/**
 * Created by gideon on 30/07/15.
 */
public interface ControlInterface {

    Response run();

    Response step();

    Response stop();
}
