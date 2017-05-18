/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.core.async;

import io.agi.core.orm.Callback;
import io.agi.core.orm.CallbackCollection;

/**
 * A Java Thread with hooks to define all the actions via Callbacks.
 * This means your implementing class doesn't need to know whether it is running in a real thread, or simply being
 * iterated. In addition, you can add diagnostic and debug hooks dynamically to all thread events. For example, getting
 * data out for visualization.
 * <p/>
 * Created by dave on 27/12/15.
 */
public abstract class CallbackThread implements Runnable, Asynchronous {

    public CallbackCollection _preCb = new CallbackCollection();
    public CallbackCollection _stepCb = new CallbackCollection();
    public CallbackCollection _postCb = new CallbackCollection();

    protected boolean _running = false; // a flag indicating the thread is running
    protected boolean _paused = false;
    protected boolean _oneStep = false; // step once, then pause
    protected boolean _doStep = false; // set this to stop iterating

    public int _interval = 50; // 50ms per iter

    public CallbackThread() {
    }

    public CallbackThread( Callback c ) {
        _stepCb.add( c );
    }

    public synchronized void addPre( Callback c ) {
        _preCb.add( c );
    }

    public synchronized void addStep( Callback c ) {
        _stepCb.add( c );
    }

    public synchronized void addPost( Callback c ) {
        _postCb.add( c );
    }

    @Override
    public synchronized int getInterval() {
        return _interval;
    }

    @Override
    public synchronized void setInterval( int interval ) {
        _interval = interval;
    }

    @Override
    public void start() {
        Thread t = new Thread( this );
        t.start();
    }

    @Override
    public synchronized void stop() {
        _doStep = false;
    }

    @Override
    public synchronized boolean stopping() {
        return !_doStep;
    }

    @Override
    public synchronized boolean running() {
        return _running;
    }

    @Override
    public synchronized boolean paused() {
        return _paused;
    }

    @Override
    public synchronized void pause() {
        _paused = true;
    }

    @Override
    public synchronized void resume() {
        _paused = false;
    }

    public synchronized void singleStep() {
        _oneStep = true;
        resume();
    }

    /**
     * Implement Runnable interface. Creates a loop that iterates until thread
     * is instructed to stop.
     * <p/>
     * Callbacks are called on pre- and post- execution to allow setup and tidy
     * up.
     */
    @Override
    public void run() {

        synchronized( this ) {
            _doStep = true; // aka start
            _running = true;
        }

        _preCb.call();

        while( !stopping() ) {
            step();
        }
        // _doStep has become false

        _postCb.call();

        _running = false;
    }

    /**
     * Tries to stop, and blocks until the thread has finished.
     *
     * @param sleepInterval
     * @param timeout
     */
    public void stopWait( int sleepInterval, Long timeout ) {
        stop();

        long timeWait = System.currentTimeMillis();

        try {
            while( running() ) {
                Thread.sleep( sleepInterval );
                if( timeout != null ) {
                    long timeNow = System.currentTimeMillis();
                    long elapsed = timeNow - timeWait;
                    if( elapsed > timeout ) {
                        break;
                    }
                }
            }
        }
        catch( InterruptedException ie ) {
            System.err.print( ie );
        }
    }

    /**
     * This method is not synchronized so you can perform fine-grained atomicity
     * in your callbacks using the same object.
     * <p/>
     * Frame rate control is implemented if the interval is set to > 0.
     */
    public void step() {

        long timeStep = System.currentTimeMillis();

        if( !paused() ) {

            onStep();
            _stepCb.call();

            boolean doPause = false;

            synchronized( this ) {
                if( _oneStep ) {
                    _oneStep = false;
                    doPause = true;
                }
            }

            if( doPause ) {
                pause();
            }
        }

        // thread rate control
        long delay = 0;

        synchronized( this ) {
            if( _interval > 0 ) {

                long timeNow = System.currentTimeMillis();
                long elapsed = timeNow - timeStep;

                delay = Math.max( 0, _interval - elapsed );
            }
        }

        if( delay > 0 ) {
            try {
                Thread.sleep( delay );
            }
            catch( InterruptedException ie ) {
                //System.err.print( ie );
            }
        }
    }

    public void onStep() {
        // nothing
    }
}
