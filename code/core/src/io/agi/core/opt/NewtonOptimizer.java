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

/**
 * http://en.wikipedia.org/wiki/Newton%27s_method_in_optimization
 *
 * @author davidjr
 */
public class NewtonOptimizer extends Optimizer {

    public NewtonOptimizer( OptimizerFn f, double x0, double dx ) {
        super( f, x0, dx );
    }

    public double update( double x, double dx ) {
        // x = 5
        // f(x) = x^3
        // f'(x) = 3x^2 = 3*5*5=75
        // f''(x) = 6x = 30
        double d1fx = d1fx( x, dx ); // return 1st derivative of function of x
        double d2fx = d2fx( x, dx ); // return 1st derivative of function of x
        double ratio = ( d1fx / d2fx );
        double x2 = x - ratio;
//        double x2 = x1 - ( fx / dfx );
        // x(t+1) = x(t) - fx(t) / d1( fx(t) )
        return x2;
    }

}
