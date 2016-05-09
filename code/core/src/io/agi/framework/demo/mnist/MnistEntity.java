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

/**
 * Has 2 sets of images, each being the contents of a folder, served serially:
 *  - Training set
 *  - Testing set
 *
 * Has 1 input:
 *  - An external classification (in this case produced by an algorithm)
 *
 * Has 2 outputs:
 *  - Image (an image from either the training or testing set
 *  - Classification (a Data containing a single number being 0,1,..,9 or 10 (no classification)
 *
 * In between images we present a classification.
 * For example, with time being lines (increasing downwards):
 *
 * Time      Image       Class. Out                        Class. In
 * t+0       i           10 (no class)
 * t+1       i           c(i) (a number between 0 and 9)   c(i)      Generated at previous timestep from image.
 * t+2       i+1         10
 * t+3       i+1         c(i+1)                            c(i+1)
 * t+4       i+2         10
 * t+5       i+2         c(i+2)
 *
 * Thus the problem is to predict the classification output given only the prior image output.
 *
 * During both training and testing these classifications are compared - the external input, which is one step behind,
 * and the current classification.
 *
 * e.g. at time t, we compare to see if
 *
 * On Reset we go back to the start of the training set.
 *
 * Once all the training images are served, it serves the testing images. At this point, we set [a list of] entities to
 * learning = false (this being our region).
 *
 * During training and testing, we output a
 * Created by dave on 4/05/16.
 */
public class MnistEntity extends Entity {

    public static final String ENTITY_TYPE = "mnist";

    public static final String INPUT_CLASSIFICATION = "input-class";
    public static final String OUTPUT_IMAGE = "output-image";
    public static final String OUTPUT_CLASSIFICATION = "output-class";
    public static final String OUTPUT_ERROR = "output-error";
    public static final String OUTPUT_ERROR_SERIES = "output-error-series";

    public static final int NO_CLASSIFICATION = 10;

    public MnistEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_CLASSIFICATION );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_IMAGE );
        attributes.add( OUTPUT_CLASSIFICATION );
        attributes.add( OUTPUT_ERROR );
        attributes.add( OUTPUT_ERROR_SERIES );
    }

    @Override
    public Class getConfigClass() {
        return MnistEntityConfig.class;
    }

    public void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        MnistEntityConfig config = (MnistEntityConfig) _config;

        if( config.reset ) {
            config.imageIndex = 0;
            config.imageStep = true;
            config.terminate = false;
        }

        // Load all files in training and testing folders.
        BufferedImageSourceImageFile bisTraining = new BufferedImageSourceImageFile( config.sourceFilesPathTraining );
        BufferedImageSourceImageFile bisTesting  = new BufferedImageSourceImageFile( config.sourceFilesPathTesting  );
        BufferedImageSourceImageFile bis = null;

        int trainingImages = bisTraining.getNbrImages();
        int testingImages = bisTesting.getNbrImages();

        // Decide which image set to use
        boolean learning = true;
        bis = bisTraining;
        if( config.imageIndex >= trainingImages ) {
            learning = false;
            bis = bisTesting;
        }

        // set learning status of entities
        try {
            Framework.SetConfig( config.learningEntityName, config.learningConfigPath, String.valueOf( learning ) );
            _logger.info( "Setting learning flag entity: " + config.learningEntityName + " config path: " + config.learningConfigPath + " training? " + learning );
        }
        catch( Exception e ) {
            // this is ok, the experiment is just not configured to have a termination condition
        }

        _logger.info( "Training set: " + trainingImages + " testing set: " + testingImages + " index: " + config.imageIndex + " training? " + learning );

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

        boolean inRange = bis.seek( config.imageIndex ); // next image
        if( !inRange ) {
            // stop the experiment:
            config.terminate = true; // experiment must be hooked up to listen to this.
            _logger.info( "MNIST dataset complete." );
            return;
        }

        // get all data
        imageScreenScraper.scrape(); // get the current image
        String imageFileName = bis.getImageFileName();
        Integer classification = getClassification( imageFileName );//, config.sourceFilesPrefix );

        Data  inputClass = getDataLazyResize(  INPUT_CLASSIFICATION, DataSize.create( 1 ) ); // an external attempt to classify
        Data outputClass = getDataLazyResize( OUTPUT_CLASSIFICATION, DataSize.create( 1 ) ); // the true classification
        Data outputError = getDataLazyResize( OUTPUT_ERROR         , DataSize.create( 1 ) ); // error in classification (0,1)
        Data outputErrorSeries = getData( OUTPUT_ERROR_SERIES ); // error in classification (0,1)
        if( outputErrorSeries == null ) {
            outputErrorSeries = new Data( DataSize.create( 1 ) );
        }

        Data image = imageScreenScraper.getData();

        // update the experiment
        // Time  S   Image       Class. Out                        Class. In
        // t+0   I   i           10 (no class)
        // t+1       i           c(i) (a number between 0 and 9)   c(i)        Generated at previous timestep from image
        // t+2   I   i+1         10
        // t+3       i+1         c(i+1)
        // t+4  -I-- i+2 ------  10      -------------------------
        // t+5       i+2         c(i+2)
        int digit = -1;
        int error = 0;

        if( config.imageStep ) {
            digit = NO_CLASSIFICATION;
            _logger.info( "Emitting image " + bis.getIdx() + " class. " + classification + " class.out: " + digit );
        }
        else { // classification step
            digit = classification;
            int inputDigit = (int)inputClass._values[ 0 ];
            if( inputDigit != digit ) {
                error = 1;
            }
            else {
                if( inputDigit != 0 ) {
                    int g = 0;
                    g++;
                }
            }
            //System.err.println( "Input class: " + inputDigit + " correct: " + digit  + " err : " + error );
            outputErrorSeries = updateErrorSeries( outputErrorSeries, (float)error );

            _logger.info( "Emitting image " + bis.getIdx() + " class. " + classification + " class.out: " + digit + " class.in: " + inputDigit + " error: " + error );
        }

        outputError._values[ 0 ] = (float)error;
        outputClass._values[ 0 ] = (float)digit;

        // Update the experiment:
        if( !config.imageStep ) {
            config.imageIndex = bis.nextImage(); // set to fetch next image
        }

        config.imageStep = !config.imageStep; // invert

        // write outputs back to persistence
        setData( OUTPUT_CLASSIFICATION, outputClass );
        setData( OUTPUT_ERROR, outputError );
        setData( OUTPUT_ERROR_SERIES, outputErrorSeries );
        setData( OUTPUT_IMAGE, image );
    }

    protected Data updateErrorSeries( Data errorSeries1, float error ) {
        int oldLength = errorSeries1.getSize();
        Data errorSeries2 = new Data( DataSize.create( oldLength +1 ) );
        for( int i = 0; i < oldLength; ++i ) {
            errorSeries2._values[ i ] = errorSeries1._values[ i ];
        }
        errorSeries2._values[ oldLength ] = error;
        return errorSeries2;
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
