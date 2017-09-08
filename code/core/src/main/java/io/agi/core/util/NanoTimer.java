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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.util;

/**
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
