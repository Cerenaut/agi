package io.agi.core.alg;

import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;

/**
 * Created by dave on 2/01/16.
 */
public class ColumnData {

    GrowingNeuralGas _gng;

    public ColumnData() {

    }

    public void setup( GrowingNeuralGasConfig c ) {
        _gng = new GrowingNeuralGas( c._name, c._om );
        _gng.setup( c );
    }

}
