package io.agi.ef.experiment.entities;


import io.agi.ef.experiment.Asynchronous;
import io.agi.ef.interprocess.coordinator.ControlCommand;
import io.agi.ef.interprocess.coordinator.CoordinatorSlave;
import io.agi.ef.interprocess.apiInterfaces.ControlInterface;
import io.agi.ef.serverapi.api.ApiResponseMessage;


import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Base class for Agent and World (referred to generically as 'entities').
 *
 * Created by gideon on 3/08/15.
 */
public abstract class AbstractEntity implements Asynchronous {

    protected Logger _logger = null;
    private String _name = null;
    private int _time = 0;                      // time step
    CoordinatorSlave _slave = null;

    public AbstractEntity( String name ) throws Exception {
        _logger = Logger.getLogger( this.getClass().getPackage().getName() );

        _name = name;
        _slave = CoordinatorSlave.getInstance();

        _slave.addEntity( this );
    }

    public int getTime() {
        return _time;
    }

    protected void incTime() {
        ++_time;
    }

    public String name() {
        return _name;
    }

    public void command( String command ) throws Exception {

        if ( command.equalsIgnoreCase( ControlCommand.START ) ) {
            start();
        }
        else if ( command.equalsIgnoreCase( ControlCommand.STOP ) ) {
            stop();
        }
        else if ( command.equalsIgnoreCase( ControlCommand.STEP ) ) {
            step();
        }
        else if ( command.equalsIgnoreCase( ControlCommand.PAUSE ) ) {
            pause();
        }
        else if ( command.equalsIgnoreCase( ControlCommand.RESUME ) ) {
            resume();
        }

    }

    public void status( String state ) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean stopping() {
        return false;
    }

    @Override
    public boolean running() {
        return false;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean paused() {
        return false;
    }

    @Override
    public void step() throws Exception {
    }

    @Override
    public int getInterval() {
        return 0;
    }

    @Override
    public void setInterval( int interval ) {
        return;
    }

}
