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
import io.agi.core.math.ShuffledIndex;
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
public class ImageLabelEntity extends Entity {

    public static final String ENTITY_TYPE = "image-label";

    public static final String OUTPUT_IMAGE = "output-image";
    public static final String OUTPUT_LABEL = "output-label";

    protected static ShuffledIndex _shuffledIndex = new ShuffledIndex();

    public ImageLabelEntity( ObjectMap om, Node n, ModelEntity model ) {
        super(om, n, model);
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
       return ImageLabelEntityConfig.class;
    }

    public int getShuffledIndex( BufferedImageSourceImageFile bis, long shuffleSeed, int imageIndex ) {
        int nbrImages = bis.getNbrImages();
        int shuffled = _shuffledIndex.getShuffledIndexLazy( nbrImages, shuffleSeed, imageIndex );
        _logger.warn( "Shuffle: Mapping index: " + imageIndex + " to index: " + shuffled + "." );
        return shuffled;
    }

    public void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        ImageLabelEntityConfig config = (ImageLabelEntityConfig ) _config;

        if( config.reset ) {
            config.shuffleSeed = _r.nextLong();
            config.imageIndex = 0;
            config.imageRepeat = 0;
            config.terminate = false;
            config.epoch = 0;
            config.phase = ImageLabelEntityConfig.PHASE_TRAINING;
        }

        // Load all files in training and testing folders.
        _logger.info( "Training files folder: " + config.sourceFilesPathTraining );
        _logger.info( "Testing files folder: " + config.sourceFilesPathTesting );
        BufferedImageSourceImageFile bisTraining = new BufferedImageSourceImageFile( config.sourceFilesPathTraining );
        BufferedImageSourceImageFile bisTesting  = new BufferedImageSourceImageFile( config.sourceFilesPathTesting  );
        BufferedImageSourceImageFile bis;

        int trainingImages = bisTraining.getNbrImages();
        int  testingImages = bisTesting .getNbrImages();

        if( isTraining() ) {
            bis = bisTraining;
            config.shuffle = true;
        }
        else if( isTesting() ) {
            bis = bisTesting;
            config.shuffle = false;
        }
        else {
            String msg = "phase - '" + config.phase + "' - not supported";
            _logger.error( msg );
            throw new java.lang.UnsupportedOperationException( msg );
        }

        // catch end of images before it happens:
        int images = bis.getNbrImages();
        if( config.imageIndex >= images ) {
                _logger.info( "End of image dataset: Epoch complete." );
            config.epoch += 1;
            config.shuffleSeed = _r.nextLong();
            config.imageIndex = 0;
            config.imageRepeat = 0;

            // check for phase change:
            if( isTraining() ) {
                if( config.epoch >= config.trainingEpochs ) { // say batches = 3, then 0 1 2 for training, then 3 for testing
                    config.phase = ImageLabelEntityConfig.PHASE_TESTING;
                }
            }
            else if( isTesting() ) {
                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
                _logger.warn( "=======> Terminating on end of test set. (1)" );
            }

        }

        // Decide which image set to use via phase
        // Also set learning status of entities
        // May have changed from training to testing.
        // This can happen because above we may roll over into a new batch
        _logger.warn( "=======> Phase " + config.phase + ", epoch: " + config.epoch + ", index: " + config.imageIndex + ", repeat: " + config.imageRepeat + "  [Training/Testing set size: (" + trainingImages + ", " + testingImages + ")" + "]" );

        boolean learnTraining = false;
        boolean learnTesting = false;

        if( isTraining() ) {
            bis = bisTraining;
            config.shuffle = true;
            learnTraining = true;
        }
        else if( isTesting() ) {
            bis = bisTesting;
            config.shuffle = false;
            learnTesting = true;
        }

        try {
            Collection< String > entityNames = getEntityNames( config.trainingEntities );
            for( String entityName : entityNames ) {
                Framework.SetConfig( entityName, "learn", String.valueOf( learnTraining ) );
            }
        }
        catch( Exception e ) {} // this is ok, the experiment is just not configured to have a learning flag

        try {
            Collection< String > entityNames = getEntityNames( config.testingEntities );
            for( String entityName : entityNames ) {
                Framework.SetConfig( entityName, "learn", String.valueOf( learnTesting ) );
            }
        }
        catch( Exception e ) {} // this is ok, the experiment is just not configured to have a learning flag

        // detect finished one pass of test set:
        int maxEpochs = config.trainingEpochs + config.testingEpochs;
        if( isTesting() ) {
            if( config.epoch >= maxEpochs ) {
                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
                _logger.warn( "=======> Terminating on end of test set. (2)" );
            }
        }

        int shuffledIndex = config.imageIndex;
        if( config.shuffle ) {
            shuffledIndex = getShuffledIndex(bis, config.shuffleSeed, config.imageIndex );
        }

        boolean inRange = bis.seek( shuffledIndex ); // next image
        if( !inRange ) { // occurs if no testing images
            config.shuffleSeed = _r.nextLong();
            config.imageIndex = 0; // reset the index to allow further updates:
            config.imageRepeat = 0;
            config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
            _logger.warn( "=======> Terminating on no more images to serve. (3)" );
            shuffledIndex = getShuffledIndex( bis, config.shuffleSeed, config.imageIndex );
            bis.seek( shuffledIndex ); // seek first image
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
            _logger.error("=======> !! Could not scrape image, so unable to do anything useful this update");
            return;
        }

        String imageFileName = bis.getImageFileName();
        Integer imageLabel = getClassification( imageFileName ); //, config.sourceFilesPrefix );

        if( imageLabel == null ) {
            _logger.error("=======> !! Could not get image classification, so unable to do anything useful this update");
            return;
        }

        Data image = imageScreenScraper.getData();

        _logger.info( "Emitting image " + bis.getIdx() + " label: " + imageLabel );

        config.imageRepeat += 1;
        if( config.imageRepeat == config.imageRepeats ) {
            config.imageRepeat = 0;
            config.imageIndex = config.imageIndex +1; // set to fetch next image
        }

        // write outputs back to persistence
        Data label = new Data( 1 );
        label.set( imageLabel );

        setData( OUTPUT_LABEL, label );
        setData( OUTPUT_IMAGE, image );

        // write classification
        config.imageLabel = imageLabel;
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

    public boolean isTraining() {
        ImageLabelEntityConfig config = ( ImageLabelEntityConfig ) _config;
        return config.phase.equals( ImageLabelEntityConfig.PHASE_TRAINING );
    }

    public boolean isTesting() {
        ImageLabelEntityConfig config = ( ImageLabelEntityConfig ) _config;
        return config.phase.equals( ImageLabelEntityConfig.PHASE_TESTING );
    }
}
