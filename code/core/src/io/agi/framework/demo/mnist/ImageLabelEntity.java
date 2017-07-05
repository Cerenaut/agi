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
 * Images are emitted in order and the label is obtained for each image from the filename.
 *
 * You can optionally shuffle the image order every epoch: This randomizes the image ordering.
 *
 * Created by dave on 8/07/16.
 */
public class ImageLabelEntity extends Entity {

    public static final String ENTITY_TYPE = "image-label";

    public static final String OUTPUT_IMAGE = "output-image";
    public static final String OUTPUT_LABEL = "output-label";

    protected static ShuffledIndex _shuffledIndex = new ShuffledIndex();

    protected BufferedImageSourceImageFile _bisTraining;
    protected BufferedImageSourceImageFile _bisTesting;

    public ImageLabelEntity(ObjectMap om, Node n, ModelEntity model) {
        super(om, n, model);
    }

    @Override
    public void getInputAttributes(Collection<String> attributes) {
    }

    @Override
    public void getOutputAttributes(Collection<String> attributes, DataFlags flags) {
        attributes.add( OUTPUT_IMAGE );
        attributes.add( OUTPUT_LABEL );
    }

    @Override
    public Class getConfigClass() {
        return ImageLabelEntityConfig.class;
    }


    public int getImageIndex() {
        ImageLabelEntityConfig config = (ImageLabelEntityConfig) _config;

        BufferedImageSourceImageFile bis = getBufferedImageSource();

        int imageIndex = config.imageIndex;
        if( config.shuffle ) {
            imageIndex = getShuffledIndex( bis, config.shuffleSeed, config.imageIndex );
        }

        return imageIndex;
    }

    public int getShuffledIndex( BufferedImageSourceImageFile bis, long shuffleSeed, int imageIndex ) {
        int nbrImages = bis.getNbrImages();
        int shuffled = _shuffledIndex.getShuffledIndexLazy( nbrImages, shuffleSeed, imageIndex );
        _logger.debug( "Shuffle: Mapping index: " + imageIndex + " to index: " + shuffled + "." );
        return shuffled;
    }

    protected void createBufferedImageSources() {
        // Check for a reset (to start of sequence and re-train)
        ImageLabelEntityConfig config = (ImageLabelEntityConfig) _config;

        // Load all files in training and testing folders.
        _logger.debug( "Training files folder[s]: " + config.sourceFilesPathTraining );
        _logger.debug( "Testing files folder[s]: " + config.sourceFilesPathTesting );

        _bisTraining = new BufferedImageSourceImageFile( config.sourceFilesPathTraining );
        _bisTesting = new BufferedImageSourceImageFile( config.sourceFilesPathTesting );
    }

    protected BufferedImageSourceImageFile getBufferedImageSource() {
        BufferedImageSourceImageFile bis = null;

        if( isTraining() ) {
            bis = _bisTraining;
        }
        else if( isTesting() ) {
            bis = _bisTesting;
        }

        return bis;
    }

    public void checkReset() {

        ImageLabelEntityConfig config = (ImageLabelEntityConfig) _config;

        if( config.reset ) {
            config.shuffleSeed = _r.nextLong();
            config.imageIndex = 0;
            config.imageRepeat = 0;
            config.terminate = false;
            config.epoch = 0;
            onPhaseChange( ImageLabelEntityConfig.PHASE_TRAINING );
        }
    }

    protected void onPhaseChange( String phase ) {
        ImageLabelEntityConfig config = ( ImageLabelEntityConfig ) _config;
        config.phase = phase;
    }

    protected void onEpochComplete() {
        ImageLabelEntityConfig config = ( ImageLabelEntityConfig ) _config;

        config.epoch += 1;

        // check for phase change:
//        int maxEpochs = config.trainingEpochs + config.testingEpochs;

        if( isTraining() ) { // phase = training
            if( config.epoch >= config.trainingEpochs ) { // say batches = 3, then 0 1 2 for training, then 3 for testing
                onPhaseChange( ImageLabelEntityConfig.PHASE_TESTING );
            }
        }
// The termination condition is picked up in another place
//        else if( isTesting() ) {
//            if( config.epoch >= maxEpochs ) {
//                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
//                _logger.warn("=======> Terminating on end of test set. (1)");
//            }
//        }
    }

    protected void checkPhase() {
        ImageLabelEntityConfig config = ( ImageLabelEntityConfig ) _config;

        boolean learnTraining = false;
        boolean learnTesting = false;

        if( isTraining() ) {
            config.shuffle = config.shuffleTraining;
            learnTraining = true;
        }
        else if( isTesting() ) {
            config.shuffle = config.shuffleTesting;
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
    }

    protected ImageScreenScraper createImageScreenScraper( BufferedImageSourceImageFile bis ) {
        ImageLabelEntityConfig config = ( ImageLabelEntityConfig ) _config;

        ImageScreenScraper imageScreenScraper = new ImageScreenScraper();

        boolean receptiveFieldSet = ImageSensorEntity.isReceptiveFieldSet(
                config.receptiveField.receptiveFieldX, config.receptiveField.receptiveFieldY,
                config.receptiveField.receptiveFieldW, config.receptiveField.receptiveFieldH );
        if( receptiveFieldSet ) {
            imageScreenScraper.setup( bis, config.getReceptiveField(), config.getResolution(), config.greyscale, config.invert );
        } else {
            imageScreenScraper.setup( bis, config.resolution.resolutionX, config.resolution.resolutionY, config.greyscale, config.invert );
        }

        return imageScreenScraper;
    }

    protected void onImageOutOfBounds() {
        ImageLabelEntityConfig config = (ImageLabelEntityConfig) _config;
        config.shuffleSeed = _r.nextLong();
        config.imageIndex = 0;
        config.imageRepeat = 0;
    }

    protected void checkEpochComplete() {
        ImageLabelEntityConfig config = (ImageLabelEntityConfig) _config;

        BufferedImageSourceImageFile bis = getBufferedImageSource(); // for current phase
        int images = bis.getNbrImages();
        if( config.imageIndex >= images ) { // the raw count
            _logger.info( "End of image dataset: Epoch complete." );
            onImageOutOfBounds();
            onEpochComplete();
        }
    }

    protected void checkAllEpochsComplete() {
        ImageLabelEntityConfig config = (ImageLabelEntityConfig) _config;

        // detect finished one pass of test set:
        if( isTesting() ) {
            int maxEpochs = config.trainingEpochs + config.testingEpochs;
            if( config.epoch >= maxEpochs ) {
                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
                _logger.warn( "=======> Terminating on end of test set. (2)" );
            }
        }
    }

    public void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        ImageLabelEntityConfig config = (ImageLabelEntityConfig) _config;

        checkReset();

        // Load all files in training and testing folders.
        createBufferedImageSources();

        int trainingImages = _bisTraining.getNbrImages(); // for info only
        int  testingImages = _bisTesting .getNbrImages(); // for info only

        // Decide which image set to use via phase
        // Also set learning status of entities
        // May have changed from training to testing.
        // This can happen because above we may roll over into a new batch
        if( ( config.age % 100 ) == 0 ) {
            _logger.info( "Update. Age: " + config.age + " Training set: " + trainingImages + " testing set: " + testingImages + " epoch: " + config.epoch + " index: " + config.imageIndex + " repeat: " + config.imageRepeat + " phase " + config.phase );
        }

        checkEpochComplete();
        checkPhase(); // sets up learn flags for entities. Phase may have changed due to completed epoch
        checkAllEpochsComplete(); // all epochs may have finished

        // Now get whatever image we wanted:
        BufferedImageSourceImageFile bis = getBufferedImageSource(); // phase may have changed

        int imageIndex = getImageIndex(); // potentially shuffled.
        boolean inRange = bis.seek( imageIndex ); // next image
        if( !inRange ) { // does occur?
            onImageOutOfBounds();
            config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
            _logger.info( "Terminating on no more images to serve. (3)" );
            //shuffledIndex = getShuffledIndex( bis, config.shuffleSeed, config.imageIndex );
            //bis.seek( shuffledIndex ); // seek first image
            imageIndex = getImageIndex();
            bis.seek( imageIndex ); // next image
        }

        config.imageChanged = false;
        if( config.imageRepeat == 0 ) {
            config.imageChanged = true; // first instance of a new image, however many repeats we have
            //System.err.println( " New image !!! " + bis.getImageFileName() );
        }
        else {
            //System.err.println( " Old image !!! " + bis.getImageFileName() );
        }

        // Setup screen scraper, grab image
        String imageFileName = bis.getImageFileName();
        Integer imageLabel = getClassification( imageFileName ); //, config.sourceFilesPrefix );

        updateImageLabelOutput( bis, imageLabel );

        // update auto config properties
        config.imageRepeat += 1;
        if( config.imageRepeat == config.imageRepeats ) {
            config.imageRepeat = 0;
            config.imageIndex = config.imageIndex +1; // set to fetch next image
        }
    }

    protected void updateImageLabelOutput( BufferedImageSourceImageFile bis, Integer imageLabel ) {

        if( imageLabel == null ) {
            _logger.error( "Could not get image classification, so unable to output an image or label. (a)" );
            return;
        }

        ImageScreenScraper imageScreenScraper = createImageScreenScraper( bis );
        boolean scraped = imageScreenScraper.scrape(); // get the current image
        if( !scraped ) {
            _logger.error( "Could not scrape image, so unable to output an image or label. (b)" );
            return;
        }

        Data image = imageScreenScraper.getData();

        if( image == null ) {
            _logger.error( "Could not get image, so unable to output an image or label. (c)" );
            return;
        }

        // write outputs back to persistence
        ImageLabelEntityConfig config = (ImageLabelEntityConfig ) _config;

        Data label = new Data( 1 );
        label.set( imageLabel );

        setData( OUTPUT_LABEL, label );
        setData( OUTPUT_IMAGE, image );

        // write classification
        config.imageLabel = imageLabel;
    }

    public Integer getClassification( String filename ) {//}, String prefix ) {
        try {
            // Example file: mnist10k_0a1320_8_932.png
            ImageLabelEntityConfig config = ( ImageLabelEntityConfig ) _config;
            String[] parts = filename.split( config.sourceFilesLabelDelimiter );
            String classification = parts[ config.sourceFilesLabelIndex ];
            Integer c = Integer.valueOf( classification );
            return c;
        }
        catch( Exception e ) {
            _logger.error( "Unable to get image classification from filename." );
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
