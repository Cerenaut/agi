/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.util.images;


import io.agi.core.data.Data;
import io.agi.core.data.ImageData;
import io.agi.core.orm.AbstractPair;
import io.agi.core.orm.Callback;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSource;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Scrape a BufferedImage for consumption by the hierarchy.
 * <p>
 * Scrapes a rectangle defined by a receptor field @param{receptorField}, from within a BufferedImage
 * - at a specific resolution defined by @param{resolution}
 * - converts from 8 bit to unit values
 * - converts from 3 channel color to floatarray with 3x elements
 *
 * @author Gideon Kowadlo and David Rawlinson
 * @copyright Gideon Kowadlo and David Rawlinson
 */
public class ImageScreenScraper implements Callback {

    protected Rectangle _receptiveField;
    protected BufferedImageSource _bis;
    protected ImageData _imageData;
    protected boolean _invert;

    public ImageScreenScraper() {
    }

    /**
     * Overload setup which will scrape the whole image, at a size determined by /pxPerReceptor
     *
     * @param bis
     * @param greyscale
     */
    public void setup( BufferedImageSource bis, int xResolution, int yResolution, boolean greyscale, boolean invert ) {
        AbstractPair< Integer, Integer > ap = bis.getImageSize();
        int w = ap._first;
        int h = ap._second;

        Rectangle receptiveField = new Rectangle( 0, 0, w, h ); // the area of the image scanned
        Point resolution = new Point( xResolution, yResolution );

        setup( bis, receptiveField, resolution, greyscale, invert );
    }

    /**
     * Setup the ImageScreenScraper.
     * The @param{receptorField} defines which area of the image is analysed,
     * and the output is at @param{resolution} and set in the FF volume.
     *
     * @param bis            the BufferedImageSource that provides the images to be scraped
     * @param receptiveField the area of the getImage to be analysed
     * @param resolution     the resolution of the sensor (output of the analysis)
     */
    public void setup( BufferedImageSource bis, Rectangle receptiveField, Point resolution, boolean greyscale, boolean invert ) {

        _bis = bis;
        _receptiveField = new Rectangle( receptiveField );

        int channels = 3; // RGB
        if ( greyscale ) {
            channels = 1;
        }

        _imageData = new ImageData( resolution, channels );

        _invert = invert;
    }

    /**
     * Iterate: scrape the current image.
     * In case there are multiple scrapers, the source should only be updated once.
     * Solution is to iterate the BIS on the same clock, and not explicitly here.
     */
    @Override
    public void call() {
        scrape();
    }

    /**
     * Read in sub image at the receptive field, scale it to sensor resolution
     * and convert to the appropriate format for the ImageData field
     */
    public void scrape() {
        BufferedImage bi = _bis.getImage();
        BufferedImage biTgt = null;

        if ( bi == null ) {
            System.out.println( "ERROR: ImageScreenScraper.scrape() - could not scrape image with name: " + _bis.getImageName() );
            return;
        }

        // 1) crop to receptor field
        // --------------------------------------
        bi = bi.getSubimage( _receptiveField.x, _receptiveField.y, _receptiveField.width, _receptiveField.height );

        // 2) downsample to resolution
        // --------------------------------------
        int vw = _imageData.getWidth(); // vol. width
        int vh = _imageData.getHeight();

        Image image = bi.getScaledInstance( vw, vh, java.awt.Image.SCALE_SMOOTH );

        biTgt = new BufferedImage( vw, vh, bi.getType() );
        biTgt.getGraphics().drawImage( image, 0, 0, null );

        // 3) convert to ImageData float array
        // --------------------------------------
        _imageData._d.set( 0.0f );

        _imageData.setWithBufferedImage( biTgt );

        if( _invert ) {
            _imageData.getData().argSub(1.f);
        }
    }

    public BufferedImageSource getSource() {
        return _bis;
    }

    public Data getData() {
        return _imageData._d;
    }

    /**
     * Get resolution of sensor
     */
    public AbstractPair< Integer, Integer > getResolution() {
        int vw = _imageData.getWidth();
        int vh = _imageData.getHeight();
        return new AbstractPair( vw, vh );
    }

    /**
     * Get size of receptor field (not output resolution)
     */
    public AbstractPair< Integer, Integer > getReceptiveFieldSize() {
        return new AbstractPair( _receptiveField.width, _receptiveField.height );
    }

    public BufferedImageSource getBufferedImageSource() {
        return _bis;
    }

//    public MultiMarkov getMultiMarkovIfPresent() {
//        MultiMarkov mm = null;
//        if ( _bis instanceof MarkovChainImageSource ) {
//            mm = ( ( MarkovChainImageSource ) _bis ).getMultiMarkov();
//        }
//        return mm;
//    }

}
