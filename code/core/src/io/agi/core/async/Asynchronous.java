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

/**
 * This interface represents an asynchronous thread of execution. We assume that a thread object is a relatively heavy
 * object, i.e. we should minimize creation and deletion. Therefore, we distinguish between start/stop events (heavy) and
 * pause/resume (lightweight). The concept of "reseting" the thread to a known state is assumed to be external to this
 * interface. i.e. no guarantees are given and no interface is provided.
 * <p/>
 * The thread has 2 properties:
 * - running
 * - paused
 * <p/>
 * The thread is running from start until stopped.
 * The thread can be paused and resumed at any time, though it only has effect while running.
 * <p/>
 * In addition, execution is assumed to be iterative, with step() called each iteration.
 * <p/>
 * Created by dave on 12/09/15.
 */
public interface Asynchronous {

    /**
     * Starts the thread.
     * Sets running to true.
     */
    public void start();

    /**
     * Requests a graceful, permanent termination of the thread.
     * Sets running to false.
     */
    public void stop();

    /**
     * Returns true if a stop() has been requested, even if not honoured yet.
     */
    public boolean stopping();

    /**
     * Returns actual state of thread rather than commanded state.
     * i.e. whether it is still executing.
     *
     * @return
     */
    public boolean running();

    /**
     * Sets paused to true. No effect if not running.
     */
    public void pause();

    /**
     * Sets paused to false. No effect if not running.
     * Will not start if not started.
     */
    public void resume();

    /**
     * Returns true if the thread is paused.
     *
     * @return
     */
    public boolean paused();

    /**
     * The alternative mode of operation is single-step: Every time a step is
     * issued, the program iterates once and pauses. Resume() to continue with
     * asynchronous running.
     *
     * @return
     */
    public void step();

    /**
     * Rate control. This interface allows the thread to be run at a specified
     * rate, if possible.
     *
     * @return
     */
    public int getInterval();

    /**
     * Rate control. This interface allows the thread to be run at a specified
     * rate, if possible.
     *
     * @param interval
     */
    public void setInterval( int interval );

}
