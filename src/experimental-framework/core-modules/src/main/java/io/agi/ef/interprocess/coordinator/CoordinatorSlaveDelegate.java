package io.agi.ef.interprocess.coordinator;

/**
 * Created by gideon on 30/08/15.
 */
public interface CoordinatorSlaveDelegate {

    public void receivedEvent( String eventName );
}
