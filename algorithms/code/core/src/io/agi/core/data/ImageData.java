/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.data;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 *
 * @author dave
 */
public class ImageData {

    public Data _d = null;

    public ImageData( Data d ) {
        _d = d;
    }

    public ImageData( Point size, int channels ) {
        DataSize d = DataSize.create( size.x, size.y, channels );
        _d = new Data( d );
    }

    public Data getData() {
        return _d;
    }
    
    public int getWidth() {
        return _d._dataSize.getSize( DataSize.DIMENSION_X );
    }

    public int getHeight() {
        return _d._dataSize.getSize( DataSize.DIMENSION_Y );
    }

    public int getChannels()  {
        Integer n = _d._dataSize.getSize( DataSize.DIMENSION_Z );
        if( n != null ) {
            return n;
        }
        return 1;
    }

    public int getOffset( int x, int y, int channel ) {
        Coordinate2 c = _d.begin();
        c.set( DataSize.DIMENSION_X, x );
        c.set( DataSize.DIMENSION_Y, y );
        c.set( DataSize.DIMENSION_Z, channel );
        int offset = c.offset();
        return offset;
    }

    public float get( int x, int y, int c ) {
        int offset = getOffset( x, y, c );
        return _d._values[ offset ];
    }
    
    public void set( int x, int y, int c, int value ) {
        int offset = getOffset( x, y, c );
        _d._values[ offset ] = value;
    }
    
    public void setWithBufferedImage( BufferedImage bi ) {

        // retrieve resolution of image
        int vw = getWidth();
        int vh = getHeight();

        int[] sample = new int[ 4 ]; // RGB24

        Coordinate2 c = _d.begin();

        c.set( DataSize.DIMENSION_Z, 0 );

        int channels = getChannels( );
        int elements = _d.getSize();
        int pixelMaxValue = 255;
        float vReciprocal = 1.0f / (float)pixelMaxValue;

        for( int y = 0; y < vh; ++y ) {

            c.set( DataSize.DIMENSION_Y, y );

            for( int x = 0; x < vw; ++x ) {

                c.set( DataSize.DIMENSION_X, x );

                int offset = c.offset(); // don't want to eval the 3d coord more than necessary..

                bi.getRaster().getPixel( x, y, sample );

                int r = sample[ 0 ];
                int g = sample[ 1 ];
                int b = sample[ 2 ];

                // scale the pixel values to normalize them
                float bScaled = (float)b * vReciprocal;
                float gScaled = (float)g * vReciprocal;
                float rScaled = (float)r * vReciprocal;

                assert( (offset+2) < elements );

                if( channels == 3 ) {
                    _d._values[ offset + 0 ] = rScaled;
                    _d._values[ offset + 1 ] = gScaled;
                    _d._values[ offset + 2 ] = bScaled;
                }
                else { // greyscale
                    _d._values[ offset + 0 ] = ( rScaled + gScaled + bScaled ) / 3.0f;
                }
            }
        }
    }

}
