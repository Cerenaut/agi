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

package io.agi.core.math.dynamic;

/**
 * 4th Order Runge-Kutta Integrator
 * http://www.myphysicslab.com/runge_kutta.html
 * Based on http://gafferongames.com/game-physics/integration-basics/
 * Created by dave on 20/07/16.
 */
public class RungeKutta4 {

    public float update( Derivative d, float x, float t, float dt ) {

        // need to propagate all variables forward for this version
//        float da = d.evaluate( t           ,      0.0 );
//        float db = d.evaluate( t + dt * 0.5, dt * 0.5 );
//        float dc = d.evaluate( t + dt * 0.5, dt * 0.5 );
//        float dd = d.evaluate( t + dt      , dt       );
//
//        float dx_dt = ( 1.0f/6.0f ) * ( da + 2.0f * ( db + dc ) + dd );
//        float x2 = x + ( dx_dt * dt );
//        return x2;
        return 0f;
    }
}
