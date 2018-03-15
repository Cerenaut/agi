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

package io.agi.core.ann.convolutional.competitive;

import io.agi.core.ann.convolutional.ConvolutionalNetworkLayer;
import io.agi.core.ann.convolutional.ConvolutionalNetworkLayerConfig;
import io.agi.core.ann.unsupervised.CompetitiveLearning;
import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.data.ConvolutionData3d;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.Int3d;
import io.agi.core.orm.ObjectMap;

/**
 * Created by dave on 11/08/17.
 */
public class CompetitiveLearningConvolutionalNetworkLayer extends ConvolutionalNetworkLayer {

    public GrowingNeuralGas _classifier;

    public Data _convError;
    public Data _convBest;

    public Data _poolError;
    public Data _poolBest;

    public CompetitiveLearningConvolutionalNetworkLayer() {

    }

    public void setup( ConvolutionalNetworkLayerConfig config ) {
        super.setup( config );

        CompetitiveLearningConvolutionalNetworkLayerConfig clcnl = (CompetitiveLearningConvolutionalNetworkLayerConfig)config;

        _classifier = new GrowingNeuralGas( clcnl._classifierConfig._name, clcnl._classifierConfig._om );
        _classifier.setup( clcnl._classifierConfig );

        DataSize convDataSize = DataSize.create( _config._width, _config._height, _config._depth );

        _convError = new Data( convDataSize );
        _convBest = new Data( convDataSize );

        int pw = _config.getPooledWidth();
        int ph = _config.getPooledHeight();
        int pd = _config.getPooledDepth();

        DataSize poolDataSize = DataSize.create( pw, ph, pd );

        _poolError = new Data( poolDataSize );
        _poolBest = new Data( poolDataSize );
    }

    public Data getOutput() {
        return _poolError; // max inverse error
//        return _poolBest; // these cells fire
    }

    public void update( boolean train ) {
        convolve( train );
        pool( train );
    }

    public void pool( boolean train ) {
        // pool with min error? make that one active
//        poolMin( _config, _convError, _classifier._cellMask, _poolError, _poolBest );
//        poolMax( _config, _convError, _classifier._cellMask, _poolError, _poolBest );
        //poolMaxRanking( _config, _convError, _classifier._cellMask, _poolError, _poolBest );
        poolMaxInvRelativeError( _config, _convError, _classifier._cellMask, _poolError, _poolBest );
    }

    public void convolve( boolean train ) {

        _classifier._c.setLearn( train );

        Data input = _input;
        Int3d i3d = ConvolutionData3d.getSize( input );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        assert( id == _config._fieldDepth );

//        int kernelSize = _config._fieldWidth * _config._fieldHeight * _config._fieldDepth;

        // for each model position (shifts by stride pixels each time)
        for( int cy = 0; cy < _config._height; cy++ ) {
            for( int cx = 0; cx < _config._width; cx++ ) {

/*                Data classifierInput = new Data( DataSize.create( kernelSize ) );

                // build the input receptive field and copy to classifier for this x,y, position in layer
                // for each element in the field
                for( int fy = 0; fy < _config._fieldHeight; fy++ ) {
                    for( int fx = 0; fx < _config._fieldWidth; fx++ ) {

                        // e.g. padding = 2 stride = 1
                        // image:         0  1  2
                        //                1  1  1
                        // padded:  0  0  1  1  1
                        //          0  1  2  3  4
                        //         -2 -1  0
                        // i = 0 := -2
                        int ix = cx * _config._inputStride - _config._inputPadding + fx;
                        int iy = cy * _config._inputStride - _config._inputPadding + fy;

                        if( ( ix < 0 ) || ( iy < 0 ) || ( ix >= iw ) || ( iy >= ih ) ) {
                            continue; // add nothing, because outside image bounds
                        }

                        for( int iz = 0; iz < id; iz++ ) {

                            int fz = iz;
                            int inputOffset = ConvolutionData3d.getOffset( ix, iy, iz, iw, ih, id );
                            int kernelOffset = ConvolutionData3d.getOffset( fx, fy, fz, _config._fieldWidth, _config._fieldHeight, _config._fieldDepth );

                            float inputValue = input._values[ inputOffset ];
                            classifierInput._values[ kernelOffset ] = inputValue;
                        } // input z

                    } // field x
                } // field y */

                Data classifierInput = GetReceptiveFieldInput( _config, input, iw, ih, id, cx, cy );

                // classify...
                _classifier.setInput( classifierInput );
                _classifier.update();

                // foreach( model in the kernel ): copy classifier output
                int best1 = _classifier.getBestCell();
                int best2 = _classifier.get2ndBestCell();

                // max error:
                float maxError = _classifier._cellErrors.max();

                for( int cz = 0; cz < _config._depth; ++cz ) {

                    int convOffset = ConvolutionData3d.getOffset( cx, cy, cz, _config._width, _config._height, _config._depth );

                    float bestValue = 0f;
                    if( cz == best1 ) {
                        bestValue = 1f;
                    }
                    else if( cz == best2 ) {
                        bestValue = 0.5f;
                    }

                    // normalize error:
/*                    float error = _classifier._cellErrors._values[ cz ];
                    if( maxError > 0.0f ) {
                        error = error / maxError; // i.e. error = 0, ans =
                    }
                    // if error = 0, error / max err = 0
                    // if error = max, max/max = 1
                    // 1-err = 0 => 1
                    error = 1f - error; // ie higher if LESS error

weird scaling, for ranking its ok but for positive its weird
                    _convError._values[ convOffset ] = error;
*/

                    double sumSqError = _classifier._cellErrors._values[ cz ];
                    float output = 1f;
                    if( sumSqError > 0.0 ) {
                        double inputs = (double)_classifier.getInput().getSize();
                        double sumError = Math.sqrt( sumSqError );
                        double error = sumError / inputs; // error now at most 1.
//                        if( error > 1f ) {
//                            int g = 0;
//                            g++;
//                        }
                        // http://www.wolframalpha.com/input/?i=plot+-log(x)+for+x+%3D+0+to+1
                        double negLogLikelihood = -Math.log( error );
                        double scaled = negLogLikelihood * 0.1;
//                        if( scaled >1.0 ) {
//                            int g= 0;
//                            g++;
//                        }
                        double unit = Math.min( 1.0, scaled );
                        output = (float)unit;
                    }

                    _convError._values[ convOffset ] = output;
                    _convBest ._values[ convOffset ] = bestValue;
                }
            }
        }
    }

    public Data invert( Data pooled ) {

        Data convInverted = invertPooling( _config, pooled );

        ConvolutionalNetworkLayerConfig config = _config;

        int kernelSize = _config._fieldWidth * _config._fieldHeight * _config._fieldDepth;
        DataSize dataSizeClassifierInput = DataSize.create( kernelSize );

        Int3d i3d = ConvolutionData3d.getSize( _input );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        Data inputInverted = new Data( _input._dataSize );

        // 2. find the max model z at each conv. x,y, and invert it.
        for( int cy = 0; cy < _config._height; cy++ ) {
            for( int cx = 0; cx < _config._width; cx++ ) {

                // foreach( model in the kernel )
                float max = 0f;
                int maxAt = -1;

                for( int cz = 0; cz < config._depth; ++cz ) {

                    int convolvedOffset = ConvolutionData3d.getOffset( cx, cy, cz, config._width, config._height, config._depth );

                    float c = convInverted._values[ convolvedOffset ];

                    if( c >= max ) { // over threshold
                        max = c;
                        maxAt = cz;
                    }
                }

                if( maxAt < 0 ) {
                    continue; // nothing to invert, in spiky case
                }

                int cz = maxAt;
                // invert cz:
                // for each element in the field

                Data classifierInput = CompetitiveLearning.invert( cz, dataSizeClassifierInput, _classifier._cellWeights );

                for( int fy = 0; fy < config._fieldHeight; fy++ ) {
                    for( int fx = 0; fx < config._fieldWidth; fx++ ) {

                        // e.g. padding = 2 stride = 1
                        // image:         0  1  2
                        //                1  1  1
                        // padded:  0  0  1  1  1
                        //          0  1  2  3  4
                        //         -2 -1  0
                        // i = 0 := -2
                        int ix = cx * config._inputStride - config._inputPadding + fx;
                        int iy = cy * config._inputStride - config._inputPadding + fy;

                        if( ( ix < 0 ) || ( iy < 0 ) || ( ix >= iw ) || ( iy >= ih ) ) {
                            continue;
                        }

                        for( int iz = 0; iz < id; iz++ ) {

                            int fz = iz;
                            int inputOffset = ConvolutionData3d.getOffset( ix, iy, iz, iw, ih, id );
                            int kernelOffset = ConvolutionData3d.getOffset( fx, fy, fz, config._fieldWidth, config._fieldHeight, config._fieldDepth );

                            float weightValue = classifierInput._values[ kernelOffset ];

                            float oldInputValue = inputInverted._values[ inputOffset ];
                            float invInputValue = weightValue * max; // invert the output weight through the kernel
                            float newInputValue = oldInputValue + invInputValue;
                            inputInverted._values[ inputOffset ] = newInputValue;
                        } // input z

                    } // field x
                } // field y
            }
        }

        inputInverted.scaleRange( 0f, 1f );

        return inputInverted;
    }

    public void reset() {
        super.reset();
        _classifier.reset();
    }

}
