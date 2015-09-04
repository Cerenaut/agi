package io.agi.ef.experiment;

import io.agi.ef.experiment.entities.AbstractEntity;
import io.agi.ef.interprocess.coordinator.ControlCommand;
import io.agi.ef.interprocess.coordinator.CoordinatorSlave;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by gideon on 30/08/15.
 */
public class Experiment extends AbstractEntity {

    private boolean _running = false;
    private int _interval = 1;
    private Collection< String > _agents = null;
    String _world = null;

    private class RunExperiment implements Runnable {
        public void run() {
            while ( true ) {
                if ( _running == true ) {
                    try {
                        step();
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep( 200 );
                }
                catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Experiment( String name, String world, Collection< String > agents ) throws Exception {
        super( name );

        _world = world;
        _agents = agents;

        Thread expThread = new Thread( new RunExperiment() );
        expThread.start();
    }

    @Override
    public void start() {

        // setup whatever needs to be set up

        _running = true;

    }

    @Override
    public void stop() {
        _running = false;

        // tear down whatever needs tearing down

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
        _running = false;
    }

    @Override
    public void resume() {
        _running = true;
    }

    @Override
    public boolean paused() {
        return !_running;
    }

    @Override
    public void step() throws Exception {

        System.out.println( "Experiment: Step all entities in this experiment." );

//        CoordinatorSlave coordinator = CoordinatorSlave.getInstance();
//
//        coordinator.command( _world, ControlCommand.STEP );
//        for ( String agent : _agents ) {
//            coordinator.command( agent, ControlCommand.STEP );
//        }
    }

    @Override
    public int getInterval() {
        return _interval;
    }

    @Override
    public void setInterval( int interval ) {
        _interval = interval;
    }

}
