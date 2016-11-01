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

import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;

import java.awt.*;

/**
 * Hierarchical quilt with a 2:1 branching factor.
 *
 * Therefore, there are 2 inputs.
 *
 * We assume the inputs are 2D (or at least must project down to 2D).
 *
 * Created by dave on 22/10/16.
 */
public class HierarchicalQuilt extends CompetitiveLearning {

    public HierarchicalQuiltConfig _config;

    public Data _cellWeights;
    public Data _cellMask;

    public HierarchicalQuilt( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( HierarchicalQuiltConfig config ) {
        _config = config;

        int inputs = _config.getNbrInputs();
        int w = _config.getWidthCells();
        int h = _config.getHeightCells();

        _cellWeights = new Data( w, h, inputs );
        _cellMask = new Data( w, h );
    }

    public void reset() {
        _cellMask.set( 1.f );

        // input 1          input 2
        // _1_1_1_          _1_  = 0.5
        // 0.25, 0.5, 0.75
        // = 1/(n+1)
        int w1 = _config.getIntervalInput1X();
        int h1 = _config.getIntervalInput1Y();
        int w2 = _config.getIntervalInput2X();
        int h2 = _config.getIntervalInput2Y();

        float x1Span = 1.f / (float)( w1+1 );// say w = 1, then span = 1/(1+1) = 1/2 = 0.5
        float x2Span = 1.f / (float)( w2+1 );
        float y1Span = 1.f / (float)( h1+1 );
        float y2Span = 1.f / (float)( h2+1 );

        Point p = _config.getSizeCells();
        int dimensions = 2;
        int inputs = 2;
        int stride = dimensions * inputs; // dimensions * inputs

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                int classifierOffset = _config.getCell( x, y );
                int organizerOffset = classifierOffset * stride;

                // sparsely changing:
                int x1 = x / w2; // e.g. if w2 = 3, 0,1,2 = 0, 3,4,5=1,
                int y1 = y / h2;

                int x2 = x % w2;
                int y2 = y % h2;

                float x1u = ( x1 + 1 ) * x1Span;
                float y1u = ( y1 + 1 ) * y1Span;
                float x2u = ( x2 + 1 ) * x2Span;
                float y2u = ( y2 + 1 ) * y2Span;

                _cellWeights._values[ organizerOffset + 0 ] = x1u;
                _cellWeights._values[ organizerOffset + 1 ] = y1u;
                _cellWeights._values[ organizerOffset + 2 ] = x2u;
                _cellWeights._values[ organizerOffset + 3 ] = y2u;
            }
        }
    }

    public void update() {
        // Nothing - currently fixed uniform distribution at specified intervals
    }

}
