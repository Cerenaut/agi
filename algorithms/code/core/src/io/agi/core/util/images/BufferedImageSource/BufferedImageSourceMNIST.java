package io.agi.core.util.images.BufferedImageSource;

import io.agi.core.orm.AbstractPair;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Adapted from: http://stackoverflow.com/questions/17279049/reading-a-idx-file-type-in-java
 * <p>
 * Created by gideon on 14/03/2016.
 */
public class BufferedImageSourceMNIST extends BufferedImageSource {

    private int _numberOfImages = 0;
    private int _numberOfRows = 0;
    private int _numberOfColumns = 0;
    private int _numberOfPixels = 0;
    private int _idx = 0;

    private String _labelFilePath = null;
    private String _imageFilePath = null;
    private FileInputStream _inImage = null;
    private FileInputStream _inLabel = null;

    private BufferedImage _image;
    private String _label;

    public BufferedImageSourceMNIST( String labelFilePath, String imageFilePath ) {

        _labelFilePath = labelFilePath;
        _imageFilePath = imageFilePath;

        setup();
    }

    private void setup() {
        try {
            _inLabel = new FileInputStream( _labelFilePath );
            _inImage = new FileInputStream( _imageFilePath );

            int magicNumberImages = ( _inImage.read() << 24 ) | ( _inImage.read() << 16 ) | ( _inImage.read() << 8 ) | ( _inImage.read() );
            _numberOfImages = ( _inImage.read() << 24 ) | ( _inImage.read() << 16 ) | ( _inImage.read() << 8 ) | ( _inImage.read() );
            _numberOfRows = ( _inImage.read() << 24 ) | ( _inImage.read() << 16 ) | ( _inImage.read() << 8 ) | ( _inImage.read() );
            _numberOfColumns = ( _inImage.read() << 24 ) | ( _inImage.read() << 16 ) | ( _inImage.read() << 8 ) | ( _inImage.read() );

            int magicNumberLabels = ( _inLabel.read() << 24 ) | ( _inLabel.read() << 16 ) | ( _inLabel.read() << 8 ) | ( _inLabel.read() );
            int numberOfLabels = ( _inLabel.read() << 24 ) | ( _inLabel.read() << 16 ) | ( _inLabel.read() << 8 ) | ( _inLabel.read() );

            _numberOfPixels = _numberOfRows * _numberOfColumns;
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * NOTE: This advances the image stream, so DOES advance to the next image.
     * This breaks the API of the interface.
     * <p>
     * Needs fixing if this is to be used for anything other than pre-processing.
     *
     * @return
     */
    @Override
    public BufferedImage getImage() {

        if ( _image != null ) {
            return _image;
        }

        try {
            _image = new BufferedImage( _numberOfColumns, _numberOfRows, BufferedImage.TYPE_INT_ARGB );
            int[] imgPixels = new int[ _numberOfPixels ];

            for ( int p = 0; p < _numberOfPixels; p++ ) {
                int gray = 255 - _inImage.read();
                imgPixels[ p ] = 0xFF000000 | ( gray << 16 ) | ( gray << 8 ) | gray;
            }

            _image.setRGB( 0, 0, _numberOfColumns, _numberOfRows, imgPixels, 0, _numberOfColumns );
            Integer labelInt = _inLabel.read();
            _label = labelInt.toString();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }

        return _image;
    }

    public String getLabel() {
        return _label;
    }

    @Override
    public AbstractPair< Integer, Integer > getImageSize() {
        int w = 0;
        int h = 0;
        getImage();

        if ( _image != null ) {
            w = _image.getWidth();
            h = _image.getHeight();
        }

        AbstractPair< Integer, Integer > ap = new AbstractPair< Integer, Integer >( w, h );
        return ap;
    }

    @Override
    public int nextImage() {
        _idx++;

        if ( _idx >= _numberOfImages ) { // wrap around
            _idx = 0;
        }

        _image = null;      // clear 'cached' copy
        return _idx;
    }


    /**
     * THIS IS INEFFICIENT. It re-sets-up the file streams and seeks from the start each time.
     * It is a quick implementation as the class is not currently used for anything but pre-processing.
     */
    @Override
    public boolean seek( int index ) {

        setup();
        return skip( index );
    }


    public boolean skip( int index ) {
        if ( index >= 0 && index < _numberOfImages ) {
            _idx = index;

            long numBytes = _idx * _numberOfPixels;
            try {
                _inImage.skip( numBytes );
                _inLabel.skip( _idx );
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }

            _image = null;      // clear 'cached' copy

            return true;
        }

        return false;
    }

    @Override
    public int bufferSize() {
        return _numberOfImages;
    }
}
