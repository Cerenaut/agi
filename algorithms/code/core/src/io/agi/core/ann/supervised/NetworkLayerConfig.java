package io.agi.core.ann.supervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

/**
 * Created by dave on 3/01/16.
 */
public class NetworkLayerConfig extends NetworkConfig {

//    public ObjectMap _om;

    public String _keyActivationFunction = "activation-function";
    public String _keyLearningRate = "learning-rate";
    public String _keyInputs = "i";
    public String _keyCells = "w";

    public NetworkLayerConfig() {
    }

    public void setup( ObjectMap om, String name, int inputs, int cells, float learningRate, String activationFunction ) {
        super.setup( om, name);

        setInputs(inputs);
        setCells(cells);
        setLearningRate( learningRate );
        setActivationFunction(activationFunction);
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom(nc, name);

        NetworkLayerConfig c = (NetworkLayerConfig)nc;

        setInputs( c.getInputs() );
        setCells( c.getCells() );
        setLearningRate( c.getLearningRate() );
        setActivationFunction( c.getActivationFunction() );
    }

    public int getInputs() {
        Integer i = _om.getInteger( getKey( _keyInputs ) );
        return i.intValue();
    }

    public void setInputs( int inputs ) {
        _om.put( getKey( _keyInputs ), inputs);
    }

    public void setCells( int cells ) {
        _om.put( getKey( _keyCells ), cells);
    }

    public int getCells() {
        Integer w = _om.getInteger( getKey( _keyCells ) );
        return w.intValue();
    }

    public void setLearningRate( float learningRate ) {
        _om.put( getKey( _keyLearningRate ), learningRate );
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( _keyLearningRate ) );
        return r.floatValue();
    }

    public void setActivationFunction(String costFunction) {
        _om.put( getKey( _keyActivationFunction ), costFunction );
    }

    public String getActivationFunction() {
        return (String)_om.get( getKey( _keyActivationFunction ) );
    }
}
