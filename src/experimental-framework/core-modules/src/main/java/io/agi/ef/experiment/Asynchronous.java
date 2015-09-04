/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.ef.experiment;

/**
 *
 * @author dave
 */
public interface Asynchronous {

    /**
     * Starts the thread.
     */
    public void start();
    
    /**
     * Requests a graceful, permanent termination of the thread.
     */
    public void stop();

    /**
     * Returns true if a stop() has been requested, even if not honoured yet.
     */
    public boolean stopping();
    
    /**
     * Returns actual state of thread rather than commanded state. 
     * i.e. whether it is still executing.
     * @return 
     */
    public boolean running();

    /**
     * Temporarily pauses the thread.
     */
    public void pause();
    
    /**
     * Resumes the thread if running and paused
     */
    public void resume(); // redundant with start()?
    
    /**
     * Returns true if the thread is temporarily paused while running.
     * @return 
     */
    public boolean paused();

    /**
     * The alternative mode of operation is single-step: Every time a step is 
     * issued, the program iterates once and pauses. Resume() to continue with
     * asynchronous running.
     * @return 
     */
    public void step();
    
    /**
     * Rate control. This interface allows the thread to be run at a specified
     * rate, if possible.
     * @return 
     */
    public int getInterval();

    /**
     * Rate control. This interface allows the thread to be run at a specified
     * rate, if possible.
     * @param interval 
     */
    public void setInterval( int interval );
    
}
