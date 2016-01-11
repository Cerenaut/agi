package io.agi.core.ann.supervised;

/**
 * Allows you to derive this part independently, to add extra functions.
 *
 * Created by dave on 3/01/16.
 */
public class ActivationFunctionFactory {

    public static final String LOG_SIGMOID = "log-sigmoid";
    public static final String TAN_H = "tan-h";

    public ActivationFunction create( String function ) {
        if( function.equals( LOG_SIGMOID ) ) {
            return ActivationFunction.createLogisticSigmoid();
        }
        if( function.equals( TAN_H ) ) {
            return ActivationFunction.createTanh();
        }
        return null;
    }

}
