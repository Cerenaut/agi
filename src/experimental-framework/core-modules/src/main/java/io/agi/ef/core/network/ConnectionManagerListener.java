package io.agi.ef.core.network;

import io.agi.ef.clientapi.ApiException;

/**
 * Created by gideon on 1/08/15.
 */
public interface ConnectionManagerListener {

    /**
     * After connecting to server, the register-er gets this call back.
     * @param sc
     * @throws ApiException
     */
    void connectionAccepted( ServerConnection sc ) throws ApiException;
}
