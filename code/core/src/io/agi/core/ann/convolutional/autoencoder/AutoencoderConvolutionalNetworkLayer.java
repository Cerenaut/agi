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

package io.agi.core.ann.convolutional.autoencoder;

import io.agi.core.ann.convolutional.ConvolutionalNetworkLayer;
import io.agi.core.ann.convolutional.ConvolutionalNetworkLayerConfig;
import io.agi.core.ann.unsupervised.LifetimeSparseAutoencoder;
import io.agi.core.data.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Created by dave on 19/08/17.
 */
public class AutoencoderConvolutionalNetworkLayer extends ConvolutionalNetworkLayer {

    public LifetimeSparseAutoencoder _classifier;

    public Data _convError;
    public Data _convBest;

    public Data _poolError;
    public Data _poolBest;

    public AutoencoderConvolutionalNetworkLayer() {

    }

    public void setup( ConvolutionalNetworkLayerConfig config ) {
        super.setup( config );

        AutoencoderConvolutionalNetworkLayerConfig clcnl = (AutoencoderConvolutionalNetworkLayerConfig)config;

        _classifier = new LifetimeSparseAutoencoder( clcnl._classifierConfig._name, clcnl._classifierConfig._om );
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
        return _poolBest; // these cells fire
    }

    public void update( boolean train ) {
        convolve( train );
        pool( train );
    }

    public void pool( boolean train ) {
        // pool with min error? make that one active
        Data cellMask = new Data( _classifier._cellSpikes._dataSize );
        cellMask.set( 1f );
        poolMin( _config, _convError, cellMask, _poolError, _poolBest );
    }

    public void convolve( boolean train ) {

        _classifier._c.setLearn( train );

        Data input = _input;
        Int3d i3d = ConvolutionData3d.getSize( input );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        assert( id == _config._fieldDepth );

        int kernelSize = _config._fieldWidth * _config._fieldHeight * _config._fieldDepth;



        // for each model position (shifts by stride pixels each time)
        for( int cy = 0; cy < _config._height; cy++ ) {
            for( int cx = 0; cx < _config._width; cx++ ) {

                Data classifierInput = new Data( DataSize.create( kernelSize ) );

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
                } // field y

                // classify...
                _classifier.setInput( classifierInput );
                _classifier.update( train );

                // foreach( model in the kernel ): copy classifier output
                HashSet< Integer > spikesKA = _classifier._cellSpikes.indicesMoreThan( 0f );

                for( int cz = 0; cz < _config._depth; ++cz ) {

                    int convOffset = ConvolutionData3d.getOffset( cx, cy, cz, _config._width, _config._height, _config._depth );

                    float bestValue = 0f;
                    if( spikesKA.contains( cz ) ) {
                        bestValue = 1f;
                    }

                    _convError._values[ convOffset ] = _classifier._cellErrors._values[ cz ];
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
//                float max = 0f;
//                int maxAt = -1;
                TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

                for( int cz = 0; cz < config._depth; ++cz ) {

                    int convolvedOffset = ConvolutionData3d.getOffset( cx, cy, cz, config._width, config._height, config._depth );
                    float c = convInverted._values[ convolvedOffset ];

                    if( c > 0f ) {
                        Ranking.add( ranking, c, cz );
                    }
//                    if( c >= max ) { // over threshold
//                        max = c;
//                        maxAt = cz;
//                    }
                }

//                if( maxAt < 0 ) {
                if( Ranking.isEmpty( ranking ) ) {
                    continue; // nothing to invert, in spiky case
                }

                HashSet< Integer > bestCells = new HashSet< Integer >();
                int sparsity = _classifier._c.getSparsity();
                int maxRank = sparsity;
                boolean findMaxima = true; // biggest activity

                Ranking.getBestValuesRandomTieBreak( ranking, findMaxima, maxRank, bestCells, _config._r );

                //int cz = maxAt;
                // invert cz:
                // for each element in the field

//                Data classifierInput = CompetitiveLearning.invert( cz, dataSizeClassifierInput, _classifier._cellWeights );
                Data classifierOutput = new Data( _classifier._cellSpikes._dataSize );

                for( Integer cz : bestCells ) {
                    int convolvedOffset = ConvolutionData3d.getOffset( cx, cy, cz, config._width, config._height, config._depth );
                    float c = convInverted._values[ convolvedOffset ];
                    classifierOutput._values[ cz ] = c;
                }

                Data classifierInput = new Data( dataSizeClassifierInput );
                _classifier.reconstruct( classifierOutput, classifierInput );

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
                            float invInputValue = weightValue; // the weight of the outputs are considered during inversion to become inputs
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
