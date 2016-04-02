/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.framework.entities;

import io.agi.core.orm.ObjectMap;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSource;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.core.util.images.ImageScreenScraper;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.EntityConfig;

import java.util.Collection;

/**
 * Scrapes a rectangle defined by a receptor field @param{receptorField},
 * from within a BufferedImage, which can come from a variety of sources:
 * - at a specific resolution defined by @param{resolution}
 * - converts from 8 bit to unit values
 * - converts from 3 channel colour to ImageData, Data with 3x elements
 *
 * @author Gideon Kowadlo and David Rawlinson
 * @copyright Incubator 491
 */
public class ImageSensorEntity extends Entity {

    public static final String ENTITY_TYPE = "image-sensor";
    public static final String IMAGE_DATA = "image-data";

    ImageSensorConfig _properties = null;

    public ImageSensorEntity( String entityName, ObjectMap om, String type, Node n ) {
        super( entityName, om, type, n );
    }

    @Override
    public void getInputKeys( Collection< String > keys ) {
    }

    @Override
    public void getOutputKeys( Collection< String > keys, DataFlags flags ) {
        keys.add( IMAGE_DATA );
    }

    @Override
    public EntityConfig getConfig() {
        return _properties;
    }

    public void doUpdateSelf() {

        BufferedImageSource bufferedImageSource = BufferedImageSourceFactory.create(
                _properties.sourceType,
                _properties.sourceFilesPath );

        ImageScreenScraper imageScreenScraper = new ImageScreenScraper();

        if ( isReceptiveFieldSet( _properties.receptiveField.receptiveFieldX, _properties.receptiveField.receptiveFieldY,
                                  _properties.receptiveField.receptiveFieldW, _properties.receptiveField.receptiveFieldH) )  {
            imageScreenScraper.setup( bufferedImageSource, _properties.getReceptiveField(), _properties.getResolution(), _properties.greyscale );
        }
        else {
            imageScreenScraper.setup( bufferedImageSource, _properties.resolution.resolutionX, _properties.resolution.resolutionY, _properties.greyscale );
        }

        boolean inRange = bufferedImageSource.seek( _properties.imageIndex);
        if ( !inRange ) {
            _properties.imageIndex = 0;          // try to reset to beginning (may still be out of range)

            System.err.println( "ERROR: Could not get an image to scrape. Error with BufferedImageSource.seek() in ImageSensorEntity" );
        }
        else {
            imageScreenScraper.scrape();
            _properties.imageIndex = bufferedImageSource.nextImage();
        }

        setData( IMAGE_DATA, imageScreenScraper.getData() );
    }

    private boolean isReceptiveFieldSet( int receptiveFieldX, int receptiveFieldY, int receptiveFieldW, int receptiveFieldH ) {

        if (        receptiveFieldX >= 0
                &&  receptiveFieldY >= 0
                &&  receptiveFieldW >= 0
                &&  receptiveFieldH >= 0 ) {
            return true;
        }

        return false;
    }

}
