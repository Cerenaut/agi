/*
 * Copyright (c) 2016.
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
import java.util.HashMap;

public class ImageSensorEntity extends Entity {

    public static final String ENTITY_TYPE = "image-sensor";
    public static final String IMAGE_DATA = "image-data";

    protected static HashMap< String, BufferedImageSource > PathBufferedImageSource = new HashMap<>();

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
        return ImageSensorEntityConfig.class;
    }

    public void doUpdateSelf() {

        ImageSensorEntityConfig config = ( ImageSensorEntityConfig ) _config;

        BufferedImageSource bufferedImageSource = PathBufferedImageSource.get( config.sourceFilesPath );

        if( bufferedImageSource == null ) {
            bufferedImageSource = BufferedImageSourceFactory.create(
                    config.sourceType,
                    config.sourceFilesPath );
            PathBufferedImageSource.put( config.sourceFilesPath, bufferedImageSource );
        }

        ImageScreenScraper imageScreenScraper = new ImageScreenScraper();

        if( isReceptiveFieldSet( config.receptiveField.receptiveFieldX, config.receptiveField.receptiveFieldY,
                config.receptiveField.receptiveFieldW, config.receptiveField.receptiveFieldH ) ) {
            imageScreenScraper.setup( bufferedImageSource, config.getReceptiveField(), config.getResolution(), config.greyscale, config.invert );
        } else {
            imageScreenScraper.setup( bufferedImageSource, config.resolution.resolutionX, config.resolution.resolutionY, config.greyscale, config.invert );
        }

        boolean inRange = bufferedImageSource.seek( config.imageIndex );
        if( !inRange ) {
            config.imageIndex = 0;          // try to reset to beginning (may still be out of range)

            _logger.error( "Could not get an image to scrape. Error with BufferedImageSource.seek() in ImageSensorEntity" );
        } else {
            imageScreenScraper.scrape();
            config.imageIndex = bufferedImageSource.nextImage();
        }

        setData( IMAGE_DATA, imageScreenScraper.getData() );
    }

    public static boolean isReceptiveFieldSet( int receptiveFieldX, int receptiveFieldY, int receptiveFieldW, int receptiveFieldH ) {

        if( receptiveFieldX >= 0
                && receptiveFieldY >= 0
                && receptiveFieldW >= 0
                && receptiveFieldH >= 0 ) {
            return true;
        }

        return false;
    }

}
