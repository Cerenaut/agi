/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.framework.entities;

import io.agi.core.orm.ObjectMap;
import io.agi.core.util.images.ImageScreenScraper;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSource;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;

import java.util.Collection;

/**
 *
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

    private static final String SOURCE_FILES_PATH = "source-files-path";
    private static final String SOURCE_TYPE = "source-type";
    private static final String RESOLUTION_X = "resolution-x";
    private static final String RESOLUTION_Y = "resolution-y";
    private static final String GREYSCALE = "greyscale";

    private static final String BUFFERED_IMAGE_INDEX = "buffered-image-index";


    public ImageSensorEntity( String entityName, ObjectMap om, String type, Node n) {
        super( entityName, om, type, n );

        // create db entries for the properties that we will use later
        setPropertyString( SOURCE_FILES_PATH, "./" );
        setPropertyString( SOURCE_TYPE, BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        setPropertyInt( BUFFERED_IMAGE_INDEX, 0 );
    }

    @Override
    public void getInputKeys( Collection<String> keys ) {}

    @Override
    public void getOutputKeys( Collection<String> keys ) {
        keys.add( IMAGE_DATA );
    }

    public void doUpdateSelf() {

        String filesPath = getPropertyString( SOURCE_FILES_PATH, "./" );
        String bufferedImageSourceType = getPropertyString( SOURCE_TYPE, BufferedImageSourceFactory.TYPE_IMAGE_FILES );

        BufferedImageSource bufferedImageSource = BufferedImageSourceFactory.create( bufferedImageSourceType, filesPath );

        int resolutionX = getPropertyInt( RESOLUTION_X, 28 );
        int resolutionY = getPropertyInt( RESOLUTION_Y, 28 );
        boolean greyscale = getPropertyBoolean( GREYSCALE, true );

        ImageScreenScraper imageScreenScraper = new ImageScreenScraper();
        imageScreenScraper.setup( bufferedImageSource, resolutionX, resolutionY, greyscale );

        int index = getPropertyInt( BUFFERED_IMAGE_INDEX, 0 );

        boolean inRange = bufferedImageSource.seek( index );
        if ( !inRange ) {
            index = 0;          // try to reset to beginning (may still be out of range)

            System.out.println( "ERROR: Could not get an image to scrape. Error with BufferedImageSource.seek() in ImageSensorEntity" );
        }
        else {
            imageScreenScraper.scrape();
            index = bufferedImageSource.nextImage();
        }

        setData( IMAGE_DATA, imageScreenScraper.getData() );
        setPropertyInt( BUFFERED_IMAGE_INDEX, index );
    }

}
