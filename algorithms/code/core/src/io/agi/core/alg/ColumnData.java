package io.agi.core.alg;

import io.agi.core.data.Data;
import io.agi.core.unsupervised.GrowingNeuralGas;
import io.agi.core.unsupervised.GrowingNeuralGasConfig;

/**
 * Created by dave on 2/01/16.
 */
public class ColumnData {

//    public Data _inputValues;
    GrowingNeuralGas _gng;

    public ColumnData() {

    }

    public void setup(GrowingNeuralGasConfig c) {
        _gng = new GrowingNeuralGas();
        _gng.setup( c );
    }

}
