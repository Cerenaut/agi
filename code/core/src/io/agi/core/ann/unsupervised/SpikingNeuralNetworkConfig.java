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

package io.agi.core.ann.unsupervised;

import com.sun.javaws.exceptions.InvalidArgumentException;
import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Architecture:
 *
 *          Excitatory ------------------> Output
 * Ext1 --> Excitatory --> Inhibitory
 * Ext2 -----------------> Inhibitory
 *          Excitatory <-- Inhibitory
 *                         Inhibitory <--> Inhibitory
 *
 * Created by dave on 24/09/16.
 */
public class SpikingNeuralNetworkConfig extends NetworkConfig {

    public static final int LEARNING_RULE_NONE = 0;
    public static final int LEARNING_RULE_OJA = 1;
    public static final int LEARNING_RULE_CM = 2;

    public static final int CELL_TYPE_EXCITATORY = 1;
    public static final int CELL_TYPE_INHIBITORY = 2;

    public static final int INPUT_TYPE_CELL = 1;
    public static final int INPUT_TYPE_EXTERNAL_1 = 2;
    public static final int INPUT_TYPE_EXTERNAL_2 = 3;

    // Order: exc, inh, ext.1, ext.2
    public int _excitatoryCells = 0;
    public int _inhibitoryCells = 0;
    public int _externals1 = 0;
    public int _externals2 = 0;

    public int _spikeThresholdBatchSize = 0;
    public int _spikeThresholdBatchIndex = 0;

    public float _timeConstantExcitatory = 0;
    public float _timeConstantInhibitory = 0;

    public float _targetSpikeRateExcitatory = 0;
    public float _targetSpikeRateInhibitory = 0;

    public float _resetSpikeThreshold = 0;

    public float _spikeRateLearningRate = 0;
    public float _spikeTraceLearningRate = 0;
    public float _spikeThresholdLearningRate = 0;

    public float _synapseLearningRateExternal1ToExcitatory   = 0;
    public float _synapseLearningRateExternal2ToExcitatory   = 0;
    public float _synapseLearningRateExcitatoryToExcitatory = 0;
    public float _synapseLearningRateInhibitoryToExcitatory = 0;
    public float _synapseLearningRateExternal1ToInhibitory   = 0;
    public float _synapseLearningRateExternal2ToInhibitory   = 0;
    public float _synapseLearningRateExcitatoryToInhibitory = 0;
    public float _synapseLearningRateInhibitoryToInhibitory = 0;

    public float _inputWeightExcitatory = 0;
    public float _inputWeightInhibitory = 0;
    public float _inputWeightExternal1  = 0;
    public float _inputWeightExternal2  = 0;

    public SpikingNeuralNetworkConfig() {

    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int excitatoryCells ,
            int inhibitoryCells ,
            int externals1 ,
            int externals2 ,

            int spikeThresholdBatchSize ,
            int spikeThresholdBatchIndex ,

            float timeConstantExcitatory ,
            float timeConstantInhibitory ,

            float targetSpikeRateExcitatory ,
            float targetSpikeRateInhibitory ,

            float resetSpikeThreshold ,

            float spikeRateLearningRate,
            float spikeTraceLearningRate,
            float spikeThresholdLearningRate,

            float synapseLearningRateExternal1ToExcitatory   ,
            float synapseLearningRateExternal2ToExcitatory   ,
            float synapseLearningRateExcitatoryToExcitatory ,
            float synapseLearningRateInhibitoryToExcitatory ,
            float synapseLearningRateExternal1ToInhibitory   ,
            float synapseLearningRateExternal2ToInhibitory   ,
            float synapseLearningRateExcitatoryToInhibitory ,
            float synapseLearningRateInhibitoryToInhibitory ,

            float inputWeightExcitatory,
            float inputWeightInhibitory,
            float inputWeightExternal1,
            float inputWeightExternal2 ) {

        super.setup( om, name, r );

        _excitatoryCells = excitatoryCells;
        _inhibitoryCells = inhibitoryCells;
        _externals1 = externals1;
        _externals2 = externals2;

        _spikeThresholdBatchSize = spikeThresholdBatchSize;
        _spikeThresholdBatchIndex = spikeThresholdBatchIndex;

        _timeConstantExcitatory = timeConstantExcitatory;
        _timeConstantInhibitory = timeConstantInhibitory;

        _targetSpikeRateExcitatory = targetSpikeRateExcitatory;
        _targetSpikeRateInhibitory = targetSpikeRateInhibitory;

        _resetSpikeThreshold = resetSpikeThreshold;

        _spikeRateLearningRate = spikeRateLearningRate;
        _spikeTraceLearningRate = spikeTraceLearningRate;
        _spikeThresholdLearningRate = spikeThresholdLearningRate;

        _synapseLearningRateExternal1ToExcitatory   = synapseLearningRateExternal1ToExcitatory;
        _synapseLearningRateExternal2ToExcitatory   = synapseLearningRateExternal2ToExcitatory;
        _synapseLearningRateExcitatoryToExcitatory = synapseLearningRateExcitatoryToExcitatory;
        _synapseLearningRateInhibitoryToExcitatory = synapseLearningRateInhibitoryToExcitatory;
        _synapseLearningRateExternal1ToInhibitory   = synapseLearningRateExternal1ToInhibitory;
        _synapseLearningRateExternal2ToInhibitory   = synapseLearningRateExternal2ToInhibitory;
        _synapseLearningRateExcitatoryToInhibitory = synapseLearningRateExcitatoryToInhibitory;
        _synapseLearningRateInhibitoryToInhibitory = synapseLearningRateInhibitoryToInhibitory;

        _inputWeightExcitatory = inputWeightExcitatory;
        _inputWeightInhibitory = inputWeightInhibitory;
        _inputWeightExternal1   = inputWeightExternal1;
        _inputWeightExternal2   = inputWeightExternal2;
    }

    public int getInputTypeInputOffset( int inputType ) {
        if( inputType == INPUT_TYPE_CELL ) {
            return 0;
        }

        int cells = getCellsSize();

        if( inputType == INPUT_TYPE_EXTERNAL_1 ) {
            int inputOffset = cells;
            return inputOffset;
        }

        if( inputType == INPUT_TYPE_EXTERNAL_2 ) {
            int inputOffset = cells + _externals1;
            return inputOffset;
        }

        return -1;
    }


    public int getCellTypeInputOffset( int cellType ) {
        // Order: exc, inh, ext.
        if( cellType == CELL_TYPE_EXCITATORY ) {
            return 0;
        }
        else if( cellType == CELL_TYPE_INHIBITORY ) {
            return _excitatoryCells;
        }

        return -1;
    }

    public int getCellType( int cell ) {
        int cells = getCellsSize();
        if( cell >= cells ) {
            throw new IndexOutOfBoundsException();
        }

        if( cell < _excitatoryCells ) {
            return CELL_TYPE_EXCITATORY;
        }

        return CELL_TYPE_INHIBITORY;
    }

    public int getInputType( int input ) {

        // check bounds
        int inputs = getInputSize();
        if( input >= inputs ) {
            throw new IndexOutOfBoundsException();
        }

        int cells = getCellsSize();
        if( input < cells ) {
            return INPUT_TYPE_CELL;
        }

        int cellsAndExternals1 = cells + _externals1;
        if( input < cellsAndExternals1 ) {
            return INPUT_TYPE_EXTERNAL_1;
        }

        return INPUT_TYPE_EXTERNAL_2;
    }

    /**
     * Defines the architecture of the network.
     *
     * @param cell
     * @param input
     * @return
     */
    public int getSynapseLearningRule( int cell, int input ) {
        boolean connected = isConnected( cell, input );

        if( !connected ) {
            return LEARNING_RULE_NONE;
        }

        int inputType = getInputType( input );
        if( inputType == INPUT_TYPE_EXTERNAL_1 ) {
            return LEARNING_RULE_OJA;
        }

        if( inputType == INPUT_TYPE_EXTERNAL_2 ) {
            return LEARNING_RULE_OJA;
        }

        return LEARNING_RULE_OJA;
//        return LEARNING_RULE_CM;
    }

    /**
     * Defines the architecture of the network.
     *
     * @param cell
     * @param input
     * @return
     */
    public float getSynapseLearningRate( int cell, int input ) {
        int inputType = getInputType( input );
        int postCellType = getCellType( cell );

        if( postCellType == CELL_TYPE_EXCITATORY ) {
            if( inputType == INPUT_TYPE_EXTERNAL_1 ) {
                return _synapseLearningRateExternal1ToExcitatory;
            }
            else if( inputType == INPUT_TYPE_EXTERNAL_2 ) {
                return _synapseLearningRateExternal2ToExcitatory;
            }
            else {
                int preCell = getCellOffset( input );
                int preCellType = getCellType( preCell );
                if( preCellType == CELL_TYPE_EXCITATORY ) {
                    return _synapseLearningRateExcitatoryToExcitatory;
                }
                else { // inhibitory
                    return _synapseLearningRateInhibitoryToExcitatory;
                }
            }
        }
        else { // post cell is inhibitory
            if( inputType == INPUT_TYPE_EXTERNAL_1 ) {
                return _synapseLearningRateExternal1ToInhibitory;
            }
            else if( inputType == INPUT_TYPE_EXTERNAL_2 ) {
                return _synapseLearningRateExternal2ToInhibitory;
            }
            else {
                int preCell = getCellOffset( input );
                int preCellType = getCellType( preCell );
                if( preCellType == CELL_TYPE_EXCITATORY ) {
                    return _synapseLearningRateExcitatoryToInhibitory;
                }
                else { // inhibitory
                    return _synapseLearningRateInhibitoryToInhibitory;
                }
            }
        }
    }

    /**
     * Defines the architecture of the network.
     *
     * @param input
     * @return
     */
    public float getInputWeight( int input ) {
        int inputType = getInputType( input );
        if( inputType == INPUT_TYPE_EXTERNAL_1 ) {
            return _inputWeightExternal1;
        }
        else if( inputType == INPUT_TYPE_EXTERNAL_2 ) {
            return _inputWeightExternal2;
        }
        else {
            int cell = getCellOffset( input );
            int cellType = getCellType( cell );
            if( cellType == CELL_TYPE_EXCITATORY ) {
                return _inputWeightExcitatory;
            }
            else { // inhibitory
                return _inputWeightInhibitory;
            }
        }
    }

    public float getResetCellPotential( int cell, float spikeThreshold ) {
        float p = _r.nextFloat() * spikeThreshold;
        return p;
    }

    /**
     * Zero weight if unconnected. Also won't learn. Otherwise, randomly initialize.
     *
     * @param cell
     * @param input
     * @param inDegree
     * @return
     */
    public float getResetSynapseWeight( int cell, int input, int inDegree ) {

        boolean connected = isConnected( cell, input );

        if( !connected ) {
            return 0f;
        }

        // if the average inputs active at any one time to this cell is inDensity
        // and there are inDegree inputs in total
        // then each weight should contribute
        // e.g. if 700 inputs
        // 1/700 = 0.001 per input
        float r = _r.nextFloat() / inDegree;
        return r;
    }

    /**
     * Defines the architecture of the network.
     *
     * @param cell
     * @param input
     * @return
     */
    public boolean isConnected( int cell, int input ) {
        int inputType = getInputType( input );
        int postCellType = getCellType( cell );

        if( postCellType == CELL_TYPE_EXCITATORY ) {
            if( inputType == INPUT_TYPE_EXTERNAL_1 ) {
                return true; // ext.1 -> E
            }
            else if( inputType == INPUT_TYPE_EXTERNAL_2 ) {
                return false; // ext.2 -> E
            }
            else {
                int preCell = getCellOffset( input );
                int preCellType = getCellType( preCell );
                if( preCellType == CELL_TYPE_EXCITATORY ) {
                    return false; // E -> E
                }
                else { // inhibitory
                    return true; // I -> E
                }
            }
        }
        else { // post cell is inhibitory
            if( inputType == INPUT_TYPE_EXTERNAL_1 ) {
                return false; // ext. -> I
            }
            else if( inputType == INPUT_TYPE_EXTERNAL_2 ) {
                return true; // ext.2 -> I
            }
            else {
                int preCell = getCellOffset( input );
                int preCellType = getCellType( preCell );
                if( preCellType == CELL_TYPE_EXCITATORY ) {
                    return true; // E -> I
                }
                else { // inhibitory
                    return true; // I -> I
                }
            }
        }

    }

    public float getSumInputWeight( int cell ) {
        float sumInputWeight = 0;

        int inputs = getInputSize();
        for( int i = 0; i < inputs; ++i ) {
            boolean c = isConnected( cell, i );

//            if( i == 4000 ) {
//                int g = 0;
//                g++;
//            }
//            if( i == 5001 ) {
//                int g = 0;
//                g++;
//            }

            if( c ) {
                float weight = getInputWeight( i );
                sumInputWeight += weight;
                System.err.println( c + ": " + i + " = " + weight );
            }
        }

        return sumInputWeight;
    }

    public float getResetCellSpikeThreshold( int cell, float targetSpikeRate ) {
        return _resetSpikeThreshold;
        // per step = average 50 * 0.5 = 25
        // but we only want to fire 0.02 of the time
        // or every 50 steps on average
        // so
        // 25 / 0.02 = 1250
        // 1/0.02 = 50
        // 25 * 50 = 1250
        // so it will fire every 50 steps, on average.
//        float sumInputWeight = getSumInputWeight( cell );
//        float averageSynapseWeight = 0.5f;
//        float perStep = sumInputWeight * averageSynapseWeight; // this is the per-step contribution, on average
//        float rateScaled = perStep / targetSpikeRate;
//        float threshold = rateScaled;
//        return threshold;
    }

    public float getCellTimeConstant( int cell ) {
        int cellType = getCellType( cell );
        if( cellType == CELL_TYPE_EXCITATORY ) {
            return _timeConstantExcitatory;
        }
        return _timeConstantInhibitory;
    }

    public float getCellTargetSpikeRate( int cell ) {
        int cellType = getCellType( cell );
        if( cellType == CELL_TYPE_EXCITATORY ) {
            return _targetSpikeRateExcitatory;
        }
        return _targetSpikeRateInhibitory;
    }

    public int getExternalsSize() {
        return _externals1 + _externals2;
    }
    public int getExternal1Size() {
        return _externals1;
    }
    public int getExternal2Size() {
        return _externals2;
    }

    public int getExcitatorySize() {
        return _excitatoryCells;
    }

    public int getInhibitorySize() {
        return _inhibitoryCells;
    }

    public int getCellsSize() {
        return _excitatoryCells + _inhibitoryCells;
    }

    public int getInputSize() {
        int cells = getCellsSize();
        int externals = getExternalsSize();
        return cells + externals;
    }

    public int getCellInputDegree( int cell ) {
        int degree = 0;

        int inputs = getInputSize();
        for( int i = 0; i < inputs; ++i ) {
            if( isConnected( cell, i ) ) {
                ++degree;
            }
        }

        return degree;
    }
//    public int getSpikeHistorySize() {
//        return _spikeHistorySize;
//    }
//
//    public void updateSpikeHistoryIndex() {
//        _spikeHistoryIndex = getNextSpikeHistoryIndex( _spikeHistoryIndex, _spikeHistorySize );
//    }
//
//    public int getNextSpikeHistoryIndex( int historyIndex, int historyLength ) {
//        historyIndex += 1; // advance
//        if( historyIndex >= historyLength ) {
//            historyIndex = 0;
//        }
//
//        return historyIndex;
//    }

    public int getSpikeThresholdBatchSize() {
        return _spikeThresholdBatchSize;
    }

    public void updateSpikeThresholdBatchIndex() {
        _spikeThresholdBatchIndex = getNextSpikeThresholdBatchIndex( _spikeThresholdBatchIndex, _spikeThresholdBatchSize );
    }

    public int getNextSpikeThresholdBatchIndex( int batchIndex, int batchSize ) {
        batchIndex += 1; // advance
        if( batchIndex >= batchSize ) {
            batchIndex = 0;
        }

        return batchIndex;
    }

    public boolean doSpikeThresholdBatchUpdate() {
        return( _spikeThresholdBatchIndex == 0 );
    }

    public void resetSpikeThresholdBatchIndex() {
        _spikeThresholdBatchIndex = 0;
    }

    /**
     * Convert an input index into a cell index
     * @param inputOffset
     * @return
     */
    public int getCellOffset( int inputOffset ) {
        int cells = getCellsSize();
        if( inputOffset >= cells ) {
            throw new IndexOutOfBoundsException();
        }
        int cellOffset = inputOffset;
        return cellOffset;
    }

    /**
     * Convert a cell index into an input index
     * @param cell
     * @return
     */
    public int getCellInputOffset( int cell ) {
        int cells = getCellsSize();
        if( cell >= cells ) {
            throw new IndexOutOfBoundsException();
        }
        int inputOffset = cell;
        return inputOffset;
    }

    /**
     * 2D access into the cells x inputs structure
     * @param cell
     * @param input
     * @return
     */
    public int getCellsInputsOffset( int cell, int input ) {
        int inputs = getInputSize();
        int cellInputOffset = cell * inputs + input;
        return cellInputOffset;
    }
}
