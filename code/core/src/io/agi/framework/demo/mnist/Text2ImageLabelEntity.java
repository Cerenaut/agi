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
import io.agi.framework.*;
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
public class Text2ImageLabelEntity extends ImageClassEntity {

    public static final String ENTITY_TYPE = "text-2-image-label";

    public String _textTraining;
    public String _textTesting;

    public Text2ImageLabelEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public static String filenamesFolder = null;
    public static HashMap< Integer, ArrayList< Integer > > labelFileIndices = new HashMap< Integer, ArrayList< Integer > >();

    @Override
    public Class getConfigClass() {
        return Text2ImageLabelEntityConfig.class;
    }

    public void reset() {
        // Check for a reset (to start of sequence and re-train)
        Text2ImageLabelEntityConfig config = ( Text2ImageLabelEntityConfig ) _config;

        if( config.reset ) {
//            config.charIndex = 0;
//            config.digitIndex = 0; // 0, then 1
            config.terminate = false;
            config.trainingBatch = 0;
            config.phase = ImageClassEntityConfig.PHASE_TRAIN_ALGORITHM;

            resetDigitIndex();
        }
    }

    protected void createTexts() {
        // Check for a reset (to start of sequence and re-train)
        Text2ImageLabelEntityConfig config = (Text2ImageLabelEntityConfig ) _config;

        // Load all files in training and testing folders.
        _logger.info( "Training files folder: " + config.sourceTextFileTraining );
        _logger.info( "Testing files folder: " + config.sourceTextFileTesting );

        try {
            _textTraining = FileUtil.readFile( config.sourceTextFileTraining );
        }
        catch( IOException ioe ) {
            _logger.error( ioe.getMessage() );
        }
        try {
            _textTesting = FileUtil.readFile( config.sourceTextFileTesting );
        }
        catch( IOException ioe ) {
            _logger.error( ioe.getMessage() );
        }
    }

    protected String getText() {
        Text2ImageLabelEntityConfig config = ( Text2ImageLabelEntityConfig ) _config;

        String text = _textTraining;

        if( config.phase.equals( ImageClassEntityConfig.PHASE_TRAIN_ALGORITHM ) ) {
        }
        else if( config.phase.equals( ImageClassEntityConfig.PHASE_TRAIN_ANALYTICS ) ) {
        }
        else if( config.phase.equals( ImageClassEntityConfig.PHASE_TEST_ANALYTICS ) ) {
            text = _textTesting;
        }

        return text;
    }

    public int getDigit( String text ) {
        Text2ImageLabelEntityConfig config = ( Text2ImageLabelEntityConfig ) _config;

        char c = text.charAt( config.charIndex );
        int code = (int)c;

        config.character = String.valueOf( c );
        config.characterCode = code;

        code = Math.min( 255, code );
        code = Math.max( 0, code );

        String codeString = String.valueOf( code );

        while( codeString.length() < 3 ) {
            codeString = "0" + codeString;
        }

        String digitString = codeString.substring( config.digitIndex, config.digitIndex +1 );
        int digit = Integer.parseInt( digitString );
//        int digit1 = codeString.charAt( config.digitIndex );//code % 10; // 0..9
//        int digit2 = codeString.charAt( 1 );//( code - digit1 ) / 10; // 0,10,20,..,90
//        int digit3 = codeString.charAt( 2 );//( code - digit1 ) / 10;
//
//        int digit = digit1;
//
//        if(  == 0 ) {
//            digit = digit2;
//        }

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

        Text2ImageLabelEntityConfig config = ( Text2ImageLabelEntityConfig ) _config;

        config.imageIndex = fileIndex;
    }

    public void resetDigitIndex() {
        Text2ImageLabelEntityConfig config = ( Text2ImageLabelEntityConfig ) _config;

        config.digitIndex = 2; // will flip
        config.charIndex = -1; // will advance to 0
    }

    public void updateDigitIndex() {
        Text2ImageLabelEntityConfig config = ( Text2ImageLabelEntityConfig ) _config;

        config.digitIndex += 1;
        if( config.digitIndex > 2 ) {
            config.digitIndex = 0;
            config.charIndex += 1; // next character
        }
    }

    public void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        Text2ImageLabelEntityConfig config = ( Text2ImageLabelEntityConfig ) _config;

        reset();

        // catch end of images before it happens:
        createBufferedImageSources();
        createTexts();

        // pick a character from the text and convert it to a digit.
        String text = getText();

        // update the index:
        updateDigitIndex();

        if( config.charIndex >= text.length() ) {
            // text is complete.
            _logger.info( "End of text: Batch complete." );
            config.trainingBatch += 1;
            onBatchComplete();
            resetDigitIndex();
            updateDigitIndex();
        }

        text = getText();; // phase may have changed
        onPhaseChange();

        _logger.warn( "=======> Training text: " + _textTraining.length() + " testing text: " + _textTesting.length() + " index: " + config.charIndex + " digit: " + config.digitIndex + " phase " + config.phase );

        // detect finished one pass of test set:
        if( config.phase.equals( ImageClassEntityConfig.PHASE_TEST_ANALYTICS ) ) {
            if( config.trainingBatch > 0 ) {
                config.terminate = true; // Stop experiment. Experiment must be hooked up to listen to this.
                _logger.warn( "=======> Terminating on end of test set. (2)" );
            }
        }

        int textLength = text.length();
        boolean inRange = config.charIndex < textLength;

        if( !inRange ) { // occurs if no testing images
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

        // Setup screen scraper, grab image
        ImageScreenScraper imageScreenScraper = createImageScreenScraper( bis );
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
        config.imageClass = imageClass;
    }

}
