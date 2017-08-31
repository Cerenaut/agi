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

package io.agi.core.opt;

import io.agi.core.orm.Callback;

/**
 * Generic optimization function for 1-d.
 * Uses empirical derivatives rather than closed-form calculations.
 *
 * @author davidjr
 */
public abstract class Optimizer implements Callback {

    public OptimizerFn _f = null;
    public double _x0 = 0.0;
    public double _dx0 = 1.0;
    public double _x = 0.0;
    public double _dx = 0.0;
    public double _t = 0.001; // tolerance

    public Optimizer( OptimizerFn f, double x0, double dx0 ) {
        _f = f;
        _x0 = x0;
        _dx0 = dx0;
        reset();
    }

    public void reset() {
        _x = _x0;
        _dx = _dx0;
    }

    public void apply() {
        double dfx = _t;
        int iteration = 0;
        while( dfx >= _t ) {
            double x1 = _x;
            double fx1 = _f.f( _x );
            update();
            double x2 = _x;
            double fx2 = _f.f( _x );
            dfx = Math.abs( fx2 - fx1 );
            ++iteration;
            System.out.println( "iter=" + iteration + " x1=" + x1 + " x2=" + x2 + " f(x1)=" + fx1 + " f(x2)=" + fx2 );
        }
    }

    public void call() {
        update();
    }

    public void update() {
        _x = update( _x, _dx );
    }

    public abstract double update( double x, double dx );

    public double d1fx( double x, double dx ) { // return 1st derivative of function of x

        double xa = x - ( dx * 0.5 );
        double xb = x + ( dx * 0.5 ); // ie span of dx
        double fxa = _f.f( xa );
        double fxb = _f.f( xb );
        double dfx = ( fxb - fxa ) / ( xb - xa );
        return dfx;
    }

    public double d2fx( double x, double dx ) { // return 1st derivative of function of x

        // http://www.math.montana.edu/frankw/ccp/modeling/continuous/heatflow2/secondder.htm
        double xa = x - ( dx );
        double xb = x + ( dx ); // ie span of dx
        double fxa = _f.f( xa );
        double fx = _f.f( x );
        double fxb = _f.f( xb );

//        double dxReciprocal = 1.0 / dx;
//        double d1fx1 = fx - fxa * dxReciprocal;
//        double d1fx2 = fxb - fx * dxReciprocal;

//        double d2fx = ( d1fx2 - d1fx1  ) * dxReciprocal;
        double d2fx = ( fxb - 2.0 * fx + fxa ) / ( dx * dx );
        return d2fx;
    }
}
