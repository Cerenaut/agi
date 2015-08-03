package io.agi.ef.core.apiInterfaces;

import io.agi.ef.core.UniversalState;

import java.util.Collection;

/**
 * Created by gideon on 30/07/15.
 */
public interface DataInterface {

    UniversalState getState();
    void setWorldState( UniversalState state );
    public void setAgentStates( Collection<UniversalState> agentStates );
}
