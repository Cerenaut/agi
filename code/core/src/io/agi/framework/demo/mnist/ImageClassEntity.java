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
import io.agi.core.orm.ObjectMap;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceImageFile;
import io.agi.core.util.images.ImageScreenScraper;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.entities.ImageSensorEntity;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A generic setup for a source of images that can be classified.
 * We have one set of training images and one set of testing images.
 *
 * We perform N passes over the training images and then one pass over the testing images.
 *
 * At each step, one image is emitted (as a Data structure) and one class label (as a config property).
 *
 * This entity can terminate an experiment when the images are all processed, or it can loop around and process forever.
 *
 * Created by dave on 8/07/16.
 */
public class ImageClassEntity extends Entity {

    public static final String ENTITY_TYPE = "image-class";

    public static final String OUTPUT_IMAGE = "output-image";
    public static final String OUTPUT_LABEL = "output-label";

    public ImageClassEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_IMAGE );
        attributes.add( OUTPUT_LABEL );
    }

    @Override
    public Class getConfigClass() {
       return ImageClassEntityConfig.class;
    }

    public Collection< String > getEntityNames( String configValue ) {
        String[] names = configValue.split( "," );
        Collection< String > c = new ArrayList< String >();
        for( String s : names ) {
            c.add( s );
        }
        return c;
    }
    public void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        ImageClassEntityConfig config = (ImageClassEntityConfig ) _config;

        if( config.reset ) {
            config.imageIndex = 0;
            config.imageRepeat = 0;
            config.terminate = false;
            config.trainingBatch = 0;
            config.phase = ImageClassEntityConfig.PHASE_TRAIN_ALGORITHM;
        }

        // Load all files in training and testing folders.
        _logger.info( "Training files folder: " + config.sourceFilesPathTraining );
        _logger.info( "Testing files folder: " + config.sourceFilesPathTesting );
        BufferedImageSourceImageFile bisTraining = new BufferedImageSourceImageFile( config.sourceFilesPathTraining );
        BufferedImageSourceImageFile bisTesting  = new BufferedImageSourceImageFile( config.sourceFilesPathTesting  );
        BufferedImageSourceImageFile bis = null;

        int trainingImages = bisTraining.getNbrImages();
        int  testingImages = bisTesting .getNbrImages();

        bis = bisTraining;
        if( config.phase.equals( ImageClassEntityConfig.PHASE_TRAIN_ALGORITHM ) ) {
        }
        else if( config.phase.equals( ImageClassEntityConfig.PHASE_TRAIN_ANALYTICS ) ) {
        }
        else if( config.phase.equals( ImageClassEntityConfig.PHASE_TEST_ANALYTICS ) ) {
            bis = bisTesting;
        }

        // catch end of images before it happens:
        int images = bis.getNbrImages();
        if( config.imageIndex >= images ) {
            _logger.info( "End of image dataset: Batch complete." );
            config.trainingBatch += 1;
            config.imageIndex = 0;
            config.imageRepeat = 0;

            // check for phase change:
            if( config.phase.equals( ImageClassEntityConfig.PHASE_TRAIN_ALGORITHM ) ) {
                if( config.trainingBatch >= config.trainingBatches ) { // say batches = 3, then 0 1 2 for training, then 3 for testing
                    if( config.trainAnalytics ) {
                        config.phase = ImageClassEntityConfig.PHASE_TRAIN_ANALYTICS;
                        config.trainingBatch = 0;
                    }
                    else { // move straight onto testing
                        config.phase = ImageClassEntityConfig.PHASE_TEST_ANALYTICS;
                        config.trainingBatch = 0;
                    }
                }
            }
            else if( config.phase.equals( ImageClassEntityConfig.PHASE_TRAIN_ANALYTICS ) ) {
                config.phase = ImageClassEntityConfig.PHASE_TEST_ANALYTICS;
                config.trainingBatch = 0;
            }
            else if( config.phase.equals( ImageClassEntityConfig.PHASE_TEST_ANALYTICS ) ) {
                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
                _logger.warn( "=======> Terminating on end of test set. (1)" );
            }
        }

        // Decide which image set to use via phase
        // Also set learning status of entities
        // May have changed from training to testing.
        // This can happen because above we may roll over into a new batch
        _logger.warn( "=======> Training set: " + trainingImages + " testing set: " + testingImages + " index: " + config.imageIndex + " repeat: " + config.imageRepeat + " phase " + config.phase );

        bis = bisTraining;
        boolean learnAlgorithm = false;
        boolean learnAnalytics = false;

        if( config.phase.equals( ImageClassEntityConfig.PHASE_TRAIN_ALGORITHM ) ) {
            learnAlgorithm = true;
//            learnAnalytics = true; 
        }
        else if( config.phase.equals( ImageClassEntityConfig.PHASE_TRAIN_ANALYTICS ) ) {
            learnAnalytics = true;
        }
        else if( config.phase.equals( ImageClassEntityConfig.PHASE_TEST_ANALYTICS ) ) {
            bis = bisTesting;
        }

        try {
            Collection< String > entityNames = getEntityNames( config.learningEntitiesAlgorithm );
            for( String entityName : entityNames ) {
                Framework.SetConfig( entityName, "learn", String.valueOf( learnAlgorithm ) );
            }
        }
        catch( Exception e ) {} // this is ok, the experiment is just not configured to have a learning flag

        try {
            Collection< String > entityNames = getEntityNames( config.learningEntitiesAnalytics );
            for( String entityName : entityNames ) {
                Framework.SetConfig( entityName, "learn", String.valueOf( learnAnalytics ) );
            }
        }
        catch( Exception e ) {} // this is ok, the experiment is just not configured to have a learning flag

        // detect finished one pass of test set:
        if( config.phase.equals( ImageClassEntityConfig.PHASE_TEST_ANALYTICS ) ) {
            if( config.trainingBatch > 0 ) {
                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
                _logger.warn( "=======> Terminating on end of test set. (2)" );
            }
        }

        boolean inRange = bis.seek( config.imageIndex ); // next image
        if( !inRange ) { // occurs if no testing images
            config.imageIndex = 0; // reset the index to allow further updates:
            config.imageRepeat = 0;
            config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
            _logger.warn( "=======> Terminating on no more images to serve. (3)" );
            bis.seek( config.imageIndex ); // seek first image
        }

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
        boolean scraped = imageScreenScraper.scrape(); // get the current image

        if (!scraped) {
            _logger.error("Could not scrape image, so unable to do anything useful this update");
            return;
        }

        String imageFileName = bis.getImageFileName();
        Integer imageClass = getClassification( imageFileName ); //, config.sourceFilesPrefix );

        if (imageClass == null) {
            _logger.error("Could not get image classification, so unable to do anything useful this update");
            return;
        }

        Data image = imageScreenScraper.getData();

        _logger.info( "Emitting image " + bis.getIdx() + " class.: " + imageClass );

        // Update the experiment:
        config.imageRepeat += 1;
        if( config.imageRepeat == config.imageRepeats ) {
            config.imageRepeat = 0;
            config.imageIndex = config.imageIndex +1; // set to fetch next image
        }

        // write outputs back to persistence
        Data label = new Data( 1 );
        label.set( imageClass );

        setData( OUTPUT_LABEL, label );
        setData( OUTPUT_IMAGE, image );

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
            _logger.error( "Unable to get image classification" );
            _logger.error( e.toString(), e );
            return null;
        }
    }
}
