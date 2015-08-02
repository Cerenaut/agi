/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.util;

/**
 *
 * @author davidjr
 */
public class NanoTimer {

    long _start = 0;
    long _stop = 0;

    public NanoTimer() {
    }

    public void start() {
        _start = System.nanoTime();
    }

    public void stop() {
        _stop = System.nanoTime();
    }

    public long elapsed() {
        return _stop - _start;
    }

    public void printElapsed( String message ) {
        System.out.println( message + elapsed() / 1000000.0 + " ms" );
    }

}
