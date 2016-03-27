package io.agi.core.sdr;

import io.agi.core.data.Data;
import io.agi.core.data.ImageData;

import java.awt.*;

/**
 *
 * An encoder that emulates a biological retina. It carries a simplified version
 * of the major functions of signal processing believed to occur in the retina:
 * - contrast
 * - brightness
 * - colour
 * - on centre, off surround
 * - off centre, on surround
 *
 * CURRENTLY limited to 1 channel, and it is not possible to set the w,h
 *
 *
 * Created by gideon on 14/03/15.
 */
public class RetinalEncoder implements SparseDistributedEncoder {

    private int _channels = 0;

    enum CentreSurroundType {
        CentreOn,
        CentreOff
    }

    public int _bits = 0;
    public int _density = 0;

    public RetinalEncoder() {
    }

    public void setup( int bits, int density ) {
        _bits = bits;
        _density = density;

        _channels = 1;
    }

    /**
     * Create a suitably sized output structure for the given input.
     */
    @Override
    public Data createEncodingOutput( Data encodingInput ) {
        ImageData encodingInputImage = new ImageData( encodingInput );
        return encodingInputImage.getData();
    }
    
    /**
     * This implementation expects float arrays that can be interpreted as images
     * in the format specified during construction. That is, it must have the width,
     * height and channels provided. Furthermore, the ordering of pixel values in the
     * float array follows the convention followed by ImageData class.
     *
     * @param encodingInput
     * @param encodingOutput
     */
    @Override
    public void encode( Data encodingInput, Data encodingOutput ) {

        ImageData encodingInputImage = new ImageData( encodingInput );
        ImageData encodingOutputImage = new ImageData( encodingOutput );

        // on centre off surround
        centreSurround( CentreSurroundType.CentreOn, encodingInputImage, encodingOutputImage );
    }

    private void centreSurround( CentreSurroundType type, ImageData encodingInput, ImageData encodingOutput ) {

        // perform centre-surround processing by convolving the appropriate patch with with the image
        ImageData mask = createKernel( type );
        encodingInput.convolve2D( mask, encodingInput, encodingOutput );
    }


    /**
     * Create a patch that servers as a mask for processing the image.
     * This is usually for convolution.
     * @param type The type of processing to occur.
     * @return the image mask
     */
    private ImageData createKernel( CentreSurroundType type ) {

        // hard code patch to 3x3 pixels for now
        int w = 3;
        int h = 3;

        ImageData patch = new ImageData( new Point(w, h), _channels );

        if ( type == CentreSurroundType.CentreOn ) {
            patch.set( 0 );                    // surround off
            patch.set( 1, 1, 0, 1 );            // centre on
        }
        else { // if ( type == CentreSurroundType.CentreOff ) {
            patch.set( 1 );                     // surround on
            patch.set( 1, 1, 0, 0 );           // centre off
        }

        return patch;
    }

    @Override
    public void decode( Data decodingInput, Data decodingOutput ) {

        assert( true );
    }
}
