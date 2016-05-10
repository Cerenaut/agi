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

import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceMNIST;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * Created by dave on 5/04/16.
 */
public class MnistPreprocess {

    public static void main( String[] args ) {
        Integer max = new Integer( args[ 3 ] );
        boolean randomize = false;
        if( args.length > 4 ) {
            randomize = true;
        }
        preprocess( args[ 0 ], args[ 1 ], args[ 2 ], max, randomize );
    }

    /**
     * @param labelFile
     * @param imageFile
     * @param outputPath
     * @param maxNumImages if < 0 or > number of images, then go through all images
     */
    private static void preprocess( String labelFile, String imageFile, String outputPath, int maxNumImages, boolean randomize ) {

        int randomCharacters = 6;

        BufferedImageSourceMNIST imagesMNIST = new BufferedImageSourceMNIST( labelFile, imageFile );

        if( ( maxNumImages <= 0                        ) ||
            ( maxNumImages >= imagesMNIST.bufferSize() ) ) {
            maxNumImages = imagesMNIST.bufferSize();
        }

        HashMap< String, Integer > labelCount = new HashMap<>();

        for( int i = 0; i < maxNumImages; i++ ) {

            BufferedImage image = imagesMNIST.getImage();
            String label = imagesMNIST.getLabel();

            imagesMNIST.nextImage();

            Integer count = 0;
            if( labelCount.containsKey( label ) ) {
                count = labelCount.get( label );
                count++;
            }

            labelCount.put( label, count );

            String random = "";
            if( randomize ) {
                String uuid = UUID.randomUUID().toString().replaceAll( "-", "" );
                random = "_" + uuid.substring( 0, randomCharacters );
            }

//            String outputFileName = outputPath + File.separator + random + "_" + label + "_" + labelCount.get( label ) + ".png";
            String outputFileName = outputPath + random + "_" + label + "_" + labelCount.get( label ) + ".png";
            File outputfile = new File( outputFileName ); // I removed the leading zero because there may be more needed so it becomes a malformed number

            try {
                ImageIO.write( image, "png", outputfile );
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }
    }

}
