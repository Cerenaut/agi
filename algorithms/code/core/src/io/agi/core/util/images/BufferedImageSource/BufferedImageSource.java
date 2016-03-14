package io.agi.core.util.images.BufferedImageSource;


import io.agi.core.orm.AbstractPair;
import java.awt.image.BufferedImage;

/**
 * 
 * Created by gideon on 1/10/14.
 */
public abstract class BufferedImageSource {

    /**
     * Return nextImage bufferedImage.
     * Return null if couldn't get an getImage.
     * @return
     */
    public abstract BufferedImage getImage();

    /**
     * Get ImageSize of expected image stream
     * @return 
     */
    public abstract AbstractPair< Integer, Integer > getImageSize();
  
    /**
     * Advance the source to nextImage image
     */
    public abstract int nextImage();

    /**
     * Jump the source to the image specified by index for buffer.
     * @return false if the index is out of range
     */
    public abstract boolean jumpToImage( int index );

    /**
     * Return the current buffer size. This may not be a constant depending on the implementation.
     */
    public abstract int bufferSize( );
}
