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

package io.agi.core.ann.convolutional;

import io.agi.core.ann.unsupervised.stdp.paper.SpikingConvolutionalNetworkLayerConfig;
import io.agi.core.data.*;
import io.agi.core.math.Unit;
import io.agi.core.math.Useful;

import java.util.HashMap;

/**
 * Created by dave on 11/08/17.
 */
public abstract class ConvolutionalNetworkLayer {

    public ConvolutionalNetworkLayerConfig _config;

    public Data _input;
    public Data _inputInverse;

    public ConvolutionalNetworkLayer() {

    }

    public void setup( ConvolutionalNetworkLayerConfig config ) {
        _config = config;
    }

    public abstract Data getOutput();
    public abstract void update( boolean train );
    public abstract Data invert( Data pooled );

    public void setInput( Data input ) {
        resize( input );
        _input.copy( input );
    }

    public void resize( Data input ) {
        if( ( _input == null ) || ( _input.getSize() != input.getSize() ) ) {
            _input = new Data( input );
            _inputInverse = new Data( input._dataSize );
        }
    }

    public void reset() {
        clear();
    }

    public void clear() {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Utility functions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static Data invertPooling( ConvolutionalNetworkLayerConfig config, Data poolInput ) {

        Int3d convSize = config.getConvSize();
        int cw = convSize.getWidth();
        int ch = convSize.getHeight();
        int cd = convSize.getDepth();

        int pw = config._poolingWidth;
        int ph = config._poolingHeight;

        Int3d poolSize = config.getPoolSize();
        int ow = poolSize.getWidth();
        int oh = poolSize.getHeight();
        int od = poolSize.getDepth();

        DataSize convDataSize = config.getConvDataSize();
        Data convData = new Data( convDataSize );

        // 1. undo the pooling, by duplicating them to each input.
        for( int oy = 0; oy < oh; oy++ ) {
            for( int ox = 0; ox < ow; ox++ ) {
                for( int oz = 0; oz < od; ++oz ) {

                    int poolOffset = ConvolutionData3d.getOffset( ox, oy, oz, ow, oh, od );
                    float poolValue = poolInput._values[ poolOffset ];

                    if( poolValue == 0f ) {
                        continue; // avoid work, if spiky input
                    }

                    for( int py = 0; py < ph; py++ ) {
                        for( int px = 0; px < pw; px++ ) {

                            int cx = ox * pw + px;
                            int cy = oy * ph + py;

                            if( ( cx >= cw ) || ( cy >= ch ) ) {
                                continue;
                            }

                            int convolvedOffset = ConvolutionData3d.getOffset( cx, cy, oz, cw, ch, cd );

                            convData._values[ convolvedOffset ] = poolValue;
                        } // px
                    } // py

                } // out z
            } // out x
        } // out y

        return convData;
    }
    
    protected static void poolMin(
            ConvolutionalNetworkLayerConfig config,
            Data conv,
            Data convMask,
            Data poolValue,
            Data poolBest ) {

        Int3d i3d = ConvolutionData3d.getSize( conv );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        int pw = config._poolingWidth;
        int ph = config._poolingHeight;

        int ow = Useful.DivideRoundUp( iw, pw );
        int oh = Useful.DivideRoundUp( ih, ph );
        int od = id;

        poolBest.set( 0f );

        for( int oy = 0; oy < oh; oy++ ) {
            for( int ox = 0; ox < ow; ox++ ) {

                float zMin = Float.MAX_VALUE;
                int zMinAt = 0;

                for( int oz = 0; oz < od; ++oz ) {

                    float maskValue = convMask._values[ oz ];
                    if( maskValue < 1f ) {
                        continue;
                    }
                    
                    int poolOffset = ConvolutionData3d.getOffset( ox, oy, oz, ow, oh, od );

                    // find max or some other operator within a spatial area
                    float min = Float.MAX_VALUE;

                    for( int py = 0; py < ph; py++ ) {
                        for( int px = 0; px < pw; px++ ) {

                            int ix = ox * pw + px;
                            int iy = oy * ph + py;

                            if( ( ix >= iw ) || ( iy >= ih ) ) {
                                continue;
                            }

                            int convolvedOffset = ConvolutionData3d.getOffset( ix, iy, oz, iw, ih, id );

                            float convolvedValue = conv._values[ convolvedOffset ];
                            min = Math.min( convolvedValue, min );

                        } // px
                    } // py

                    poolValue._values[ poolOffset ] = min; // max integrated value for concept z in the pooled input area.

                    if( min <= zMin ) {
                        zMin = min;
                        zMinAt = oz;
                    }

                } // out z

                int poolOffset = ConvolutionData3d.getOffset( ox, oy, zMinAt, ow, oh, od );

                poolBest._values[ poolOffset ] = 1f;
            } // out x
        } // out y
    }

    protected static void poolMax(
            ConvolutionalNetworkLayerConfig config,
            Data conv,
            Data poolValue,
            Data poolBest  ) {

        Int3d i3d = ConvolutionData3d.getSize( conv );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        int pw = config._poolingWidth;
        int ph = config._poolingHeight;

        int ow = Useful.DivideRoundUp( iw, pw );
        int oh = Useful.DivideRoundUp( ih, ph );
        int od = id;

        for( int oy = 0; oy < oh; oy++ ) {
            for( int ox = 0; ox < ow; ox++ ) {

                float zMax = 0f;
                int zMaxAt = 0;

                for( int oz = 0; oz < od; ++oz ) {

                    // neighbourhood inhibition
                    // Note this allows other z to still fire. Unclear if this is what was intended.
                    int poolOffset = ConvolutionData3d.getOffset( ox, oy, oz, ow, oh, od );

                    // find max or some other operator within a spatial area
                    float max = 0f;

                    for( int py = 0; py < ph; py++ ) {
                        for( int px = 0; px < pw; px++ ) {

                            int ix = ox * pw + px;
                            int iy = oy * ph + py;

                            if( ( ix >= iw ) || ( iy >= ih ) ) {
                                continue;
                            }

                            int convolvedOffset = ConvolutionData3d.getOffset( ix, iy, oz, iw, ih, id );

                            float convolvedValue = conv._values[ convolvedOffset ];
                            max = Math.max( convolvedValue, max );

                        } // px
                    } // py

                    if( max <= zMax ) {
                        zMax = max;
                        zMaxAt = oz;
                    }

                    poolValue._values[ poolOffset ] = max; // max integrated value for concept z in the pooled input area.

                } // out z

                int poolOffset = ConvolutionData3d.getOffset( ox, oy, zMaxAt, ow, oh, od );

                poolBest._values[ poolOffset ] = 1f;
            } // out x
        } // out y
    }

    protected static void poolSum(
            ConvolutionalNetworkLayerConfig config,
            Data conv,
            Data pool ) {
        Int3d i3d = ConvolutionData3d.getSize( conv );
        int iw = i3d.getWidth();
        int ih = i3d.getHeight();
        int id = i3d.getDepth();

        int pw = config._poolingWidth;
        int ph = config._poolingHeight;

        int ow = Useful.DivideRoundUp( iw, pw );
        int oh = Useful.DivideRoundUp( ih, ph );
        int od = id;

        for( int oy = 0; oy < oh; oy++ ) {
            for( int ox = 0; ox < ow; ox++ ) {
                for( int oz = 0; oz < od; ++oz ) {

                    // neighbourhood inhibition
                    // Note this allows other z to still fire. Unclear if this is what was intended.
                    int poolOffset = ConvolutionData3d.getOffset( ox, oy, oz, ow, oh, od );

                    // find max or some other operator within a spatial area
                    float sum = 0f;

                    for( int py = 0; py < ph; py++ ) {
                        for( int px = 0; px < pw; px++ ) {

                            int ix = ox * pw + px;
                            int iy = oy * ph + py;

                            if( ( ix >= iw ) || ( iy >= ih ) ) {
                                continue;
                            }

                            int convolvedOffset = ConvolutionData3d.getOffset( ix, iy, oz, iw, ih, id );

                            float convolvedValue = conv._values[ convolvedOffset ];
                            sum += convolvedValue;

                        } // px
                    } // py

                    pool._values[ poolOffset ] = sum; // max integrated value for concept z in the pooled input area.

                } // out z
            } // out x
        } // out y
    }

}
