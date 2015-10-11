/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.opt;

/**
 *
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
