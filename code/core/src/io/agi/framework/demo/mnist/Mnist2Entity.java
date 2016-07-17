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

package io.agi.framework.demo.mnist;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceImageFile;
import io.agi.core.util.images.ImageScreenScraper;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.entities.ImageSensorEntity;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by dave on 8/07/16.
 */
public class Mnist2Entity extends Entity {

//               ---> image   --> fn --> class/feat ---> error
//     mnist     ---> class   --------->            \-->
//               --->
//
//    mnist: produces image (data) and class (config)
//    fn: takes image produces features (data)
//    class x feat: takes [features and class] and produces configs: [predicted, error?, truth]
//    vec entity takes scalar configs, produces: pred-series, error-series,truth-series

    public static final String ENTITY_TYPE = "mnist2";

    public static final String OUTPUT_DIGIT_IMAGE = "output-image";

    public Mnist2Entity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_DIGIT_IMAGE );
    }

    @Override
    public Class getConfigClass() {
       return Mnist2EntityConfig.class;
    }

    public void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        Mnist2EntityConfig config = (Mnist2EntityConfig) _config;

        if( config.reset ) {
            config.imageIndex = 0;
            config.terminate = false;
            config.batch = 0;
        }

        // Load all files in training and testing folders.
        BufferedImageSourceImageFile bisTraining = new BufferedImageSourceImageFile( config.sourceFilesPathTraining );
        BufferedImageSourceImageFile bisTesting  = new BufferedImageSourceImageFile( config.sourceFilesPathTesting  );
        BufferedImageSourceImageFile bis = null;

        int trainingImages = bisTraining.getNbrImages();
        int  testingImages = bisTesting .getNbrImages();

        // Decide which image set to use
        boolean training = true;
        bis = bisTraining;
        if( config.batch == config.trainingBatches ) { // i.e. training has finished
            training = false; // testing time
            bis = bisTesting;
        }

        // catch end of images before it happens:
        int images = bis.getNbrImages();
        if( config.imageIndex >= images ) {
            if( config.batch < config.trainingBatches ) { // say batches = 3, then 0 1 2 for training, then 3 for testing
                config.batch += 1;
                config.imageIndex = 0;
            }
            else {
                config.imageIndex = 0; // reset the index to allow further updates:
                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
                _logger.info( "MNIST dataset complete." );
                return;
            }
        }

        // may have changed from training to testing.
        // This can happen because above we may roll over into a new batch
        if( config.batch == config.trainingBatches ) {
            training = false; // testing time
            bis = bisTesting;
        }

        boolean inRange = bis.seek( config.imageIndex ); // next image
        if( !inRange ) { // occurs if no testing images
            config.imageIndex = 0; // reset the index to allow further updates:
            config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
            _logger.info( "MNIST dataset complete." );
        }

        // set learning status of entities
        try {
            Framework.SetConfig( config.learningEntityName, config.learningConfigPath, String.valueOf( training ) );
            _logger.info( "Setting learning flag entity: " + config.learningEntityName + " config path: " + config.learningConfigPath + " training? " + training );
        }
        catch( Exception e ) {
            // this is ok, the experiment is just not configured to have a learning flag
        }

        _logger.info( "Training set: " + trainingImages + " testing set: " + testingImages + " index: " + config.imageIndex + " training? " + training );

        // Setup screen scraper
        ImageScreenScraper imageScreenScraper = new ImageScreenScraper();

        boolean receptiveFieldSet = ImageSensorEntity.isReceptiveFieldSet(
                config.receptiveField.receptiveFieldX, config.receptiveField.receptiveFieldY,
                config.receptiveField.receptiveFieldW, config.receptiveField.receptiveFieldH );
        if( receptiveFieldSet ) {
            imageScreenScraper.setup( bis, config.getReceptiveField(), config.getResolution(), config.greyscale, config.invert );
        }
        else {
            imageScreenScraper.setup( bis, config.resolution.resolutionX, config.resolution.resolutionY, config.greyscale, config.invert );
        }

        // get all data
        imageScreenScraper.scrape(); // get the current image
        String imageFileName = bis.getImageFileName();
        Integer imageClass = getClassification( imageFileName );//, config.sourceFilesPrefix );
        Data image = imageScreenScraper.getData();

        _logger.info( "Emitting image " + bis.getIdx() + " class.: " + imageClass );

        // Update the experiment:
        config.imageIndex = config.imageIndex +1; // set to fetch next image

        // write outputs back to persistence
        setData( OUTPUT_DIGIT_IMAGE, image );

        // write classification
        config.imageClass = imageClass;
    }

    public static Integer getClassification( String filename ) {//}, String prefix ) {
        try {
            // Example file: mnist10k_0a1320_8_932.png
            String[] parts = filename.split( "_" );
            String classification = parts[ 2 ];
            Integer c = Integer.valueOf( classification );
            return c;
        }
        catch( Exception e ) {
            return null;
        }
    }
}
