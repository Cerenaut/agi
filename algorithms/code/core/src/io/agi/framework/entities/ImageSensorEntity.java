/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.framework.entities;

import io.agi.core.orm.ObjectMap;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSource;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.core.util.images.ImageScreenScraper;
import io.agi.framework.Entity;
import io.agi.framework.Node;

import java.awt.*;
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

    public static final String OUTPUT_IMAGE_DATA = "image-data";

    public static final String SOURCE_FILES_PATH = "source-files-path";
    public static final String SOURCE_TYPE = "source-type";
    public static final String RECEPTIVE_FIELD_X = "receptive-field-x";
    public static final String RECEPTIVE_FIELD_Y = "receptive-field-y";
    public static final String RECEPTIVE_FIELD_W = "receptive-field-w";
    public static final String RECEPTIVE_FIELD_H = "receptive-field-h";
    public static final String RESOLUTION_X = "resolution-x";
    public static final String RESOLUTION_Y = "resolution-y";
    public static final String GREYSCALE = "greyscale";
    public static final String CURRENT_IMAGE_LABEL = "current-image-label";

    public static final String BUFFERED_IMAGE_INDEX = "buffered-image-index";


    public ImageSensorEntity( String entityName, ObjectMap om, String type, Node n ) {
        super( entityName, om, type, n );
    }

    @Override
    public void getInputKeys( Collection< String > keys ) {
    }

    @Override
    public void getOutputKeys( Collection<String> keys ) {
        keys.add(OUTPUT_IMAGE_DATA);
    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {
        keys.add( SUFFIX_AGE );
        keys.add( SUFFIX_SEED );
        keys.add( SUFFIX_RESET );

        keys.add( SOURCE_FILES_PATH );
        keys.add( SOURCE_TYPE );
        keys.add( RECEPTIVE_FIELD_X );
        keys.add( RECEPTIVE_FIELD_Y );
        keys.add( RECEPTIVE_FIELD_W );
        keys.add( RECEPTIVE_FIELD_H );
        keys.add( RESOLUTION_X );
        keys.add( RESOLUTION_Y );
        keys.add( GREYSCALE );
        keys.add( CURRENT_IMAGE_LABEL );
        keys.add( BUFFERED_IMAGE_INDEX );
    }

    public void doUpdateSelf() {

        String filesPath = getPropertyString( SOURCE_FILES_PATH, "./" );
        String bufferedImageSourceType = getPropertyString( SOURCE_TYPE, BufferedImageSourceFactory.TYPE_IMAGE_FILES );

        BufferedImageSource bufferedImageSource = BufferedImageSourceFactory.create( bufferedImageSourceType, filesPath );

        int receptiveFieldX = getPropertyInt( RECEPTIVE_FIELD_X, -1 );
        int receptiveFieldY = getPropertyInt( RECEPTIVE_FIELD_Y, -1 );
        int receptiveFieldW = getPropertyInt( RECEPTIVE_FIELD_W, -1 );
        int receptiveFieldH = getPropertyInt( RECEPTIVE_FIELD_H, -1 );
        int resolutionX = getPropertyInt( RESOLUTION_X, 28 );
        int resolutionY = getPropertyInt( RESOLUTION_Y, 28 );
        boolean greyscale = getPropertyBoolean( GREYSCALE, true );

        ImageScreenScraper imageScreenScraper = new ImageScreenScraper();

        if ( isReceptiveFieldSet( receptiveFieldX, receptiveFieldY, receptiveFieldW, receptiveFieldH ) ) {
            Rectangle receptiveField = new Rectangle( receptiveFieldX, receptiveFieldY, receptiveFieldW, receptiveFieldH );
            Point point = new Point( resolutionX, resolutionY );
            imageScreenScraper.setup( bufferedImageSource, receptiveField, point, greyscale );
        }
        else {
            imageScreenScraper.setup( bufferedImageSource, resolutionX, resolutionY, greyscale );
        }

        int index = getPropertyInt( BUFFERED_IMAGE_INDEX, 0 );

        boolean inRange = bufferedImageSource.seek( index );
        if ( !inRange ) {
            index = 0;          // try to reset to beginning (may still be out of range)

            System.err.println( "ERROR: Could not get an image to scrape. Error with BufferedImageSource.seek() in ImageSensorEntity" );
        }
        else {
            imageScreenScraper.scrape();
            index = bufferedImageSource.nextImage();
        }

        setData(OUTPUT_IMAGE_DATA, imageScreenScraper.getData() );
        setPropertyInt( BUFFERED_IMAGE_INDEX, index );
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
