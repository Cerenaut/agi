/*
 * Copyright (c) 2017.
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

package io.agi.core.alg;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.HashSet;

/**
 * Using C and P predict C'.
 *
 * Created by dave on 4/07/16.
 */
public class QuiltPredictor extends NamedObject {

    public Data _inputC; // real, unit
    public Data _inputP; // real, unit

    public Data _inputPOld; // all input
    public Data _inputPNew;

    public Data _predictionOld;
    public Data _predictionNew;
    public Data _predictionNewUnit;

    // Member objects
    public QuiltPredictorConfig _rc;
    public PyramidRegionLayerPredictor _predictor;

    public QuiltPredictor( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( QuiltPredictorConfig rc ) {
        _rc = rc;

        Point inputCSize = _rc.getInputCSize();
        int inputPSize = _rc.getInputPSize();

        DataSize dataSizeInputC = DataSize.create( inputCSize.x, inputCSize.y );
        DataSize dataSizeInputP = DataSize.create( inputPSize );


        // Predictor
        int cells = dataSizeInputC.getVolume(); //_rc._classifierConfig.getNbrCells();
        int predictorInputs = dataSizeInputP.getVolume() + cells;
        int predictorOutputs = cells;
        float predictorLearningRate = rc.getPredictorLearningRate();
//        float predictorDecayRate = rc.getPredictorTraceDecayRate();
        int predictorHiddenCells = _rc.getPredictorHiddenCells();
        float predictorLeakiness = _rc.getPredictorLeakiness();
        float predictorRegularization = _rc.getPredictorRegularization();
        int predictorBatchSize = _rc.getPredictorBatchSize();
        _predictor = new PyramidRegionLayerPredictor();//SpikeOrderLearning();
        _predictor.setup( _rc.getKey( PyramidRegionLayerConfig.SUFFIX_PREDICTOR ), _rc._om, _rc._r, predictorInputs, predictorHiddenCells, predictorOutputs, predictorLearningRate, predictorLeakiness, predictorRegularization, predictorBatchSize );//, predictorDecayRate );

        DataSize dataSizeCells = DataSize.create( inputCSize.x, inputCSize.y );

        _inputP = new Data( dataSizeInputP );

        _inputPOld = new Data( predictorInputs );
        _inputPNew = new Data( predictorInputs );

        _predictionOld = new Data( dataSizeCells );
        _predictionNew = new Data( dataSizeCells );
        _predictionNewUnit = new Data( dataSizeCells );
    }

    public void reset() {

        int density = 0; // hmm, not sure where this should come from on a reset.
        Point inputSize = _rc.getInputCSize();
        int regionWidth = inputSize.x;
        int regionHeight = inputSize.y;
        Point inputColumnSize = _rc.getInputCColumnSize();
        int columnWidth = inputColumnSize.x;
        int columnHeight = inputColumnSize.y;

        _predictor.reset( density, regionWidth, regionHeight, columnWidth, columnHeight );

        _predictionOld.set( 0.f );
        _predictionNew.set( 0.f );
    }

    public void update() {

//        _transient = new PyramidRegionLayerTransient();
//        _transient._spikesNew = _inputC.indicesMoreThan( 0.5f );
//
        Data classifierSpikesNew = _inputC;

//        updatePrediction( _inputC );
//    }
//
//    protected void updatePrediction( Data classifierSpikesNew ) {
        int density = classifierSpikesNew.indicesMoreThan( 0f ).size();

        int inputPSize = _rc.getInputPSize();
        int cells = _rc.getInputCArea();
        Point inputSize = _rc.getInputCSize();
        int regionWidth = inputSize.x;
        int regionHeight = inputSize.y;
        Point inputColumnSize = _rc.getInputCColumnSize();
        int columnWidth = inputColumnSize.x;
        int columnHeight = inputColumnSize.y;

        // copy the input spike data.
        _inputPOld.copy( _inputPNew ); // a complete copy

        int offset = 0;
        _inputPNew.copyRange( classifierSpikesNew, offset, 0, cells );
        offset += cells;
        _inputPNew.copyRange( _inputP, offset, 0, inputPSize );
//        _inputPNew.copyRange( _inputP1, offset, 0, inputP1Volume );
//        offset += inputP1Volume;
//        _inputPNew.copyRange( _inputP2, offset, 0, inputP2Volume );
        //offset += inputP2Volume;

        // train the predictor
        _predictor.train( _inputPOld, classifierSpikesNew, density, regionWidth, regionHeight, columnWidth, columnHeight );//_inputOld, _inputNew, _classifierSpikesOld, _classifierSpikesNew );

        // generate a new prediction
        _predictionOld.copy( _predictionNew );
        _predictor.predict( _inputPNew, _predictionNewUnit, _predictionNew, density, regionWidth, regionHeight, columnWidth, columnHeight );
//        _predictionNew .copy( _predictor._outputPredicted ); // copy the new prediction
//        _predictionNewReal.copy( _predictor._outputPredictedReal ); // copy the new prediction
    }

}
