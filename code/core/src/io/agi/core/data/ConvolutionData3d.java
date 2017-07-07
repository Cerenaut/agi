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

package io.agi.core.data;

/**
 * For convolutional models, we need Z to be contiguous values, then arranged in rows, then in cols.
 *
 * Created by dave on 1/05/17.
 */
public class ConvolutionData3d {

    public static int getOffset( int x, int y, int z, int w, int h, int d ) {
        //System.err.println( "x:" + x + " y: " + y + " z:" + z + " w: " + w + " h:" + h + " d: " + d );
        int offset = y * w * d
                   + x * d
                   + z;

        return offset;
    }

    public static int getOffset( int x, int y, int z, DataSize ds ) {
        Integer w = ds.getSize( DataSize.DIMENSION_X );
        Integer h = ds.getSize( DataSize.DIMENSION_Y );
        Integer d = ds.getSize( DataSize.DIMENSION_Z );
        return getOffset( x, y, z, w, h, d );
    }

    public static DataSize getDataSize( int w, int h, int d ) {
        DataSize ds = new DataSize( 3 );

        ds.set( 0, h, DataSize.DIMENSION_Y );
        ds.set( 1, w, DataSize.DIMENSION_X );
        ds.set( 2, d, DataSize.DIMENSION_Z ); // contiguous values in z

        return ds;
    }

    public static Int3d getSize( Data d ) {

       if( d == null ) {
            return null;
       }

        DataSize ds = d._dataSize;
        Integer ix = ds.getSize( DataSize.DIMENSION_X );
        Integer iy = ds.getSize( DataSize.DIMENSION_Y );
        Integer iz = ds.getSize( DataSize.DIMENSION_Z );

        int valid = 3;
        if( ix == null ) {
            ix = 1;
            valid--;
        }

        if( iy == null ) {
            iy = 1;
            valid--;
        }

        if( iz == null ) {
            iz = 1;
            valid--;
        }

        // if no known dims, then make it 1d
        if( valid == 0 ) {
            ix = ds.getVolume();
        }

        return new Int3d( ix, iy, iz );
    }

}
