package io.agi.core.ann.supervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

/**
 * Created by dave on 10/01/16.
 */
public class FeedForwardNetworkConfig extends NetworkConfig {

    public String _keyNbrLayers = "nbr-layers"; // comma separated list of layer sizes
    public String _keyNbrInputs = "nbr-inputs"; // comma separated list of layer sizes
    public String _keyNbrOutputs = "nbr-outputs"; // comma separated list of layer sizes
    public String _keyLossFunction = "loss-function";
    public String _keyL2Regularization = "l2-regularization";

    public FeedForwardNetworkConfig() {
    }

    public void setup( ObjectMap om, String name, String lossFunction, int inputs, int outputs, int layers, float l2Regularization ) {
        super.setup(om, name);

        setLossFunction(lossFunction);
        setNbrLayers(layers);
        setNbrInputs(inputs);
        setNbrOutputs(outputs);
        setL2Regularization(l2Regularization);
    }

    public void setLossFunction( String costFunction ) {
        _om.put( getKey( _keyLossFunction ), costFunction );
    }

    public String getLossFunction() {
        return (String)_om.get(getKey( _keyLossFunction) );
    }

    public void setNbrLayers( int layers ) {
        _om.put(getKey(_keyNbrLayers), layers );
    }

    public Integer getNbrLayers() {
        return (Integer)_om.get(getKey(_keyNbrLayers) );
    }

    public void setNbrInputs( int layers ) {
        _om.put(getKey(_keyNbrInputs), layers );
    }

    public Integer getNbrInputs() {
        return (Integer)_om.get(getKey(_keyNbrInputs) );
    }

    public void setNbrOutputs( int layers ) {
        _om.put(getKey(_keyNbrOutputs), layers );
    }

    public Integer getNbrOutputs() {
        return (Integer)_om.get(getKey(_keyNbrOutputs) );
    }

    public void setL2Regularization( float r ) {
        _om.put(getKey(_keyL2Regularization), r );
    }

    public Float getL2Regularization() {
        return (Float)_om.get(getKey(_keyL2Regularization) );
    }
}
