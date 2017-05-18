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
 * @author davidjr
 */
public class GradientDescentOptimizer extends Optimizer {

    public GradientDescentOptimizer( OptimizerFn f, double x0, double dx ) {
        super( f, x0, dx );
    }

    public double update( double x, double dx ) {

        double d1fx = d1fx( x, dx ); // return 1st derivative of function of x
        double x1 = x;
        double x2 = x1 - dx * d1fx;
//        double x2 = x1 - ( fx / dfx );

        return x2;
        // x(t+1) = x(t) - fx(t) / d1( fx(t) )
    }

}
