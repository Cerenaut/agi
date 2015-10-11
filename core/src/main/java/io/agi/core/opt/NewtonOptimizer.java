/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.opt;

/**
 * http://en.wikipedia.org/wiki/Newton%27s_method_in_optimization
 * @author davidjr
 */
public class NewtonOptimizer  extends Optimizer {

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
