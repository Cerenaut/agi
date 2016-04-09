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
import io.agi.framework.persistence.models.ModelEntity;

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

    public ImageSensorEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( IMAGE_DATA );
    }

    @Override
    public Class getConfigClass() {
        return ImageSensorConfig.class;
    }

    public void doUpdateSelf() {

        ImageSensorConfig config = ( ImageSensorConfig ) _config;

        BufferedImageSource bufferedImageSource = BufferedImageSourceFactory.create(
                config.sourceType,
                config.sourceFilesPath );

        ImageScreenScraper imageScreenScraper = new ImageScreenScraper();

        if ( isReceptiveFieldSet( config.receptiveField.receptiveFieldX, config.receptiveField.receptiveFieldY,
                config.receptiveField.receptiveFieldW, config.receptiveField.receptiveFieldH ) ) {
            imageScreenScraper.setup( bufferedImageSource, config.getReceptiveField(), config.getResolution(), config.greyscale, config.invert );
        }
        else {
            imageScreenScraper.setup( bufferedImageSource, config.resolution.resolutionX, config.resolution.resolutionY, config.greyscale, config.invert );
        }

        boolean inRange = bufferedImageSource.seek( config.imageIndex );
        if ( !inRange ) {
            config.imageIndex = 0;          // try to reset to beginning (may still be out of range)

            _logger.error( "Could not get an image to scrape. Error with BufferedImageSource.seek() in ImageSensorEntity" );
        }
        else {
            imageScreenScraper.scrape();
            config.imageIndex = bufferedImageSource.nextImage();
        }

        setData( IMAGE_DATA, imageScreenScraper.getData() );
    }

    private boolean isReceptiveFieldSet( int receptiveFieldX, int receptiveFieldY, int receptiveFieldW, int receptiveFieldH ) {

        if ( receptiveFieldX >= 0
                && receptiveFieldY >= 0
                && receptiveFieldW >= 0
                && receptiveFieldH >= 0 ) {
            return true;
        }

        return false;
    }

}
