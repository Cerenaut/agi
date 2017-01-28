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
import io.agi.core.util.FileUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceImageFile;
import io.agi.core.util.images.ImageScreenScraper;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
 * Created by dave on 10/11/16.
 */
public class NumberSequence2ImageLabelEntity extends ImageLabelEntity {

    public static final String ENTITY_TYPE = "number-sequence-2-image-label";

    public String _textTraining;
    public String _textTesting;

    public NumberSequence2ImageLabelEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public static String filenamesFolder = null;
    public static HashMap< Integer, ArrayList< Integer > > labelFileIndices = new HashMap< Integer, ArrayList< Integer > >();

    @Override
    public Class getConfigClass() {
        return NumberSequence2ImageLabelEntityConfig.class;
    }

    public void resetDigitIndex() {
        NumberSequence2ImageLabelEntityConfig config = ( NumberSequence2ImageLabelEntityConfig ) _config;
        config.charIndex = -1; // will advance to 0
    }

    public void updateDigitIndex() {
        NumberSequence2ImageLabelEntityConfig config = ( NumberSequence2ImageLabelEntityConfig ) _config;
        config.charIndex += 1; // next character
    }

    public void resetSelf() {
        // Check for a reset (to start of sequence and re-train)
        super.resetSelf();

        NumberSequence2ImageLabelEntityConfig config = ( NumberSequence2ImageLabelEntityConfig ) _config;

        if( config.reset ) {
            resetDigitIndex();
        }
    }

    protected void createTexts() {
        // Check for a reset (to start of sequence and re-train)
        NumberSequence2ImageLabelEntityConfig config = (NumberSequence2ImageLabelEntityConfig ) _config;

        // Load all files in training and testing folders.
        _logger.info( "Training files folder: " + config.sourceTextFileTraining );
        _logger.info( "Testing files folder: " + config.sourceTextFileTesting );

        try {
            _textTraining = FileUtil.readFile( config.sourceTextFileTraining ).trim();
        }
        catch( IOException ioe ) {
            _logger.error( ioe.getMessage() );
        }
        try {
            _textTesting = FileUtil.readFile( config.sourceTextFileTesting ).trim();
        }
        catch( IOException ioe ) {
            _logger.error( ioe.getMessage() );
        }
    }

    protected String getText() {
        NumberSequence2ImageLabelEntityConfig config = ( NumberSequence2ImageLabelEntityConfig ) _config;

        String text = "?";

        if( config.phase.equals( ImageLabelEntityConfig.PHASE_TRAINING ) ) {
            text = _textTraining;
        }
        else if( config.phase.equals( ImageLabelEntityConfig.PHASE_TESTING ) ) {
            text = _textTesting;
        }

        return text;
    }

    public int getDigit( String text ) {
        NumberSequence2ImageLabelEntityConfig config = ( NumberSequence2ImageLabelEntityConfig ) _config;

        char c = text.charAt( config.charIndex );
        int digit = Integer.parseInt( Character.toString( c ) );

        config.character = String.valueOf( c );
//        config.digit = digit;

        return digit;
    }

    protected void findRandomImageForDigit( BufferedImageSourceImageFile bis, int digit ) {
        // there are 3 ways I could do this.
        // 1. I could cache all the images (chosen method)
        // 2. I could randomly pick images til I find one with the right label. Assumes not very many labels.
        // 3. I could build a list of filename labels every step (too slow)

        // 1/ build the cache
        if( ( filenamesFolder == null ) || ( !bis.getFolderName().equals( filenamesFolder ) ) ) {
            filenamesFolder = bis.getFolderName();
            labelFileIndices.clear();

            int nbrImages = bis.getNbrImages();

            for( int i = 0; i < nbrImages; ++i ) {
                String imageFileName = bis.getImageFileName( i );
                Integer label = getClassification( imageFileName );
                if( label != null ) {
                    ArrayList< Integer > fileIndices = labelFileIndices.get( label );
                    if( fileIndices == null ) {
                        fileIndices = new ArrayList< Integer >();
                        labelFileIndices.put( label, fileIndices );
                    }

                    fileIndices.add( i );
                }
            }
        }

        // 2/ select a random exemplar
        ArrayList< Integer > fileIndices = labelFileIndices.get( digit );

        int files = fileIndices.size();
        int r = _r.nextInt( files );

        int fileIndex = fileIndices.get( r );

        boolean b = bis.seek( fileIndex );

        NumberSequence2ImageLabelEntityConfig config = ( NumberSequence2ImageLabelEntityConfig ) _config;

        config.imageIndex = fileIndex;
    }

    public void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        NumberSequence2ImageLabelEntityConfig config = ( NumberSequence2ImageLabelEntityConfig ) _config;

        resetSelf();

        // catch end of images before it happens:
        createBufferedImageSources();
        createTexts();

        // pick a character from the text and convert it to a digit.
        String text = getText();

        // update the index:
        updateDigitIndex();

        if( config.charIndex >= text.length() ) {
            _logger.info( "End of text: Epoch complete." );
            onImageOutOfBounds();
            config.epoch += 1;
            onEpochComplete();

            // text is complete.
            resetDigitIndex();
            updateDigitIndex();
        }

        text = getText(); // phase may have changed
        onPhaseChange();

        _logger.warn( "=======> Training text: " + _textTraining.length() + " testing text: " + _textTesting.length() + " index: " + config.charIndex + " phase " + config.phase );

        checkEpochsComplete();
/*        // detect finished one pass of test set:
        int maxEpochs = config.trainingEpochs + config.testingEpochs;
        if( config.phase.equals( ImageLabelEntityConfig.PHASE_TESTING ) ) {
            if( config.epoch >= maxEpochs ) {
                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
                _logger.warn( "=======> Terminating on end of test set. (2)" );
            }
        }*/

        int textLength = text.length();
        boolean inRange = config.charIndex < textLength;

        if( !inRange ) { // occurs if no testing images
            onImageOutOfBounds();
            resetDigitIndex();
            updateDigitIndex();
            config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
            _logger.warn( "=======> Terminating on no more images to serve. (3)" );
        }

        // get the digits
        int digit = getDigit( text ); // 0..9

        BufferedImageSourceImageFile bis = getBufferedImageSource();

        // convert the digit to a random image for the specific label
        findRandomImageForDigit( bis, digit );

        String imageFileName = bis.getImageFileName();
        Integer imageLabel = getClassification(imageFileName); //, config.sourceFilesPrefix );

        updateImageLabelOutput( bis, imageLabel );

        // Setup screen scraper, grab image
/*        ImageScreenScraper imageScreenScraper = createImageScreenScraper( bis );
        boolean scraped = imageScreenScraper.scrape(); // get the current image
        if( !scraped ) {
            _logger.error( "Could not scrape image, so unable to do anything useful this update" );
            return;
        }

        String imageFileName = bis.getImageFileName();
        Integer imageClass = getClassification( imageFileName ); //, config.sourceFilesPrefix );

        if( imageClass == null ) {
            _logger.error( "Could not get image classification, so unable to do anything useful this update" );
            return;
        }

        Data image = imageScreenScraper.getData();

        // write outputs back to persistence
        Data label = new Data( 1 );
        label.set( imageClass );

        setData( OUTPUT_LABEL, label );
        setData( OUTPUT_IMAGE, image );

        _logger.info( "Emitting image " + bis.getIdx() + " class.: " + imageClass );

        // write classification
        config.imageClass = imageClass;*/
    }

}
