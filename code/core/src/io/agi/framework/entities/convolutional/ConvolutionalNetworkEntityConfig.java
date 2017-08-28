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

package io.agi.framework.entities.convolutional;

import io.agi.framework.EntityConfig;

/**
 * Created by dave on 22/08/17.
 */
public class ConvolutionalNetworkEntityConfig extends EntityConfig {

    public String invertSelection = "";

    public int nbrLayers = 0;

    public String layerInputPadding = "";
    public String layerInputStride = "";
    public String layerWidth = "";
    public String layerHeight = "";
    public String layerDepth = "";
    public String layerfieldWidth = "";
    public String layerfieldHeight = "";
    public String layerfieldDepth = "";
    public String layerPoolingWidth = "";
    public String layerPoolingHeight = "";

    public static void Set(
            ConvolutionalNetworkEntityConfig entityConfig,
            int inputWidth,
            int inputHeight,
            int inputDepth,
            int nbrLayers,
            int[] layerInputPaddings,
            int[] layerInputStrides,
            int[] layerDepths,
            int[] layerPoolingSize,
            int[] layerFieldSize ) {

        entityConfig.nbrLayers = nbrLayers;

        int iw = inputWidth;
        int ih = inputHeight;
        int id = inputDepth;

        // Generate config properties from these values:
        for( int layer = 0; layer < entityConfig.nbrLayers; ++layer ) {

            // Geometry of layer
            String prefix = "";
            if( layer > 0 ) prefix = ",";

            int layerInputPadding = layerInputPaddings[ layer ];
            int layerInputStride = layerInputStrides[ layer ];
            int ld = layerDepths[ layer ];
            int pw = layerPoolingSize[ layer ];
            int ph = pw;
            int fw = layerFieldSize[ layer ];
            int fh = fw;
            int fd = id;

//            int lw = iw - fw +1;//layerWidths[ layer ];;
//            int lh = ih - fh +1;//layerHeights[ layer ];;

            int lw = getLayerSize( iw, layerInputPadding, layerInputStride, fw );
            int lh = getLayerSize( ih, layerInputPadding, layerInputStride, fh );

            // Geometric parameters:
            entityConfig.layerInputPadding += prefix + layerInputPadding;
            entityConfig.layerInputStride  += prefix + layerInputStride;
            entityConfig.layerWidth  += prefix + lw;
            entityConfig.layerHeight += prefix + lh;
            entityConfig.layerDepth  += prefix + ld;
            entityConfig.layerfieldWidth += prefix + fw;
            entityConfig.layerfieldHeight += prefix + fh;
            entityConfig.layerfieldDepth += prefix + fd;
            entityConfig.layerPoolingWidth += prefix + pw;
            entityConfig.layerPoolingHeight += prefix + ph;

            // Auto calculate layer widths and heights
            iw = lw / pw;
            ih = lh / ph;
            id = ld;
        }
    }

    public static int getLayerSize( int inputSize, int inputPadding, int inputStride, int fieldSize ) {
        //int totalInputsize = inputSize + ( inputPadding * 2 ); // pad both sides

        int layerSize = 0; // at least 1
        boolean enlarge = true;

        while( enlarge ) {

            ++layerSize; // so at least 1

            int fieldStart = (layerSize-1) * inputStride - inputPadding;
            int fieldEnd = fieldStart + fieldSize; // last included pixel +1

            if( fieldEnd >= inputSize ) {
                enlarge = false;
            }
        }

        return layerSize;
    }
}

//     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 |
//  F1 -- -- -- -- -- --                                                                     |
//  F2          -- -- -- -- -- --                                                            |
//  F3                   -- -- -- -- -- --                                                   |
//  F4                            -- -- -- -- -- --                                          |
//  F5                                     -- -- -- -- -- --                                 |
//  F6                                              -- -- -- -- -- --                        |
//  F7                                                       -- -- -- -- -- --
//  F8                                                                -- -- -- -- -- --
//  F9                                                                         -- -- -- -- xx xx
