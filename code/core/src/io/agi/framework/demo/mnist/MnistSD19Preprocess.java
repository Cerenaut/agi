/*
 * Copyright (c) 2017.
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This file will preprocess the MNIST SD-19 "by_class" dataset of alphabetic characters:
 *
 * https://www.nist.gov/srd/nist-special-database-19
 *
 * ... into the standard image format used for Yann LeCun's MNIST numerical digit dataset:
 *
 * http://yann.lecun.com/exdb/mnist/
 *
 * This program applies exactly the same operations as LeCun did to MNIST digits, to the characters.
 *
 * In theory you can then use these as part of a combined alphanumeric dataset.
 *
 * This set includes uppercase characters A-Z (ascii 0x41 to 0x5a / 65 to 90).
 * https://en.wikipedia.org/wiki/ASCII
 *
 * This program has no dependencies except standard Java libraries.
 *
 * Created by dave on 5/04/16.
 */
public class MnistSD19Preprocess {

    // *************************************************************************************************************
    // Image ASCII labels to process - uppercase A..Z
    // https://en.wikipedia.org/wiki/ASCII
    // *************************************************************************************************************
    int code1 = 65;
    int code2 = 90 +1;
//    int code2 = 66; // quick debug

    int set1 = 0;
    int set2 = 7 +1;
//    int set2 = 1; // quick debug

    // *************************************************************************************************************
    // IMAGE MANIPULATION PARAMETERS
    // NOTE: "The original black and white (bilevel) images from NIST were size normalized to fit in a 20x20 pixel box
    // while preserving their aspect ratio. The resulting images contain grey levels as a result of the anti-aliasing
    // technique used by the normalization algorithm. the images were centered in a 28x28 image by computing the
    // center of mass of the pixels, and translating the image so as to position this point at the center of the 28x28 field."
    // *************************************************************************************************************
    float threshold = 0.1f;
    int outputSize = 28;
    int outputPad = 4;

    int inputFileNameSequenceIdx1 = 6; // e.g. for hsf_0_00000.png, extract '00000'
    int inputFileNameSequenceIdx2 = 11;


    /**
     * Main method
     *
     * @param args
     */
    public static void main( String[] args ) {

        String srcDir = args[ 0 ];
        String tgtDir = args[ 1 ];
        String prefix = args[ 2 ];

        MnistSD19Preprocess m = new MnistSD19Preprocess();
        m.preprocess( srcDir, tgtDir, prefix );
    }

    public void preprocess( String sourceFolder, String targetFolder, String fileNamePrefix ) {

        // 1. Create some data structures
        // -------------------------------------------------------------------------------------------------------------
        HashMap< Integer, ArrayList< String > > codeFileNames = new HashMap< Integer, ArrayList< String > >();
        HashMap< String, BufferedImage > fileNameTxImages = new HashMap< String, BufferedImage >();

        // 2. Discover all the files to process.
        // -------------------------------------------------------------------------------------------------------------
        for( int code = code1; code < code2; ++code ) {

            String hexString = Integer.toHexString( code );
            String codeFolder = sourceFolder + File.separator + hexString;
            String charString = Character.toString( ( char ) code );
            System.out.println( "Looking for character '" + charString + "' + code '" + code + "' in folder: " + codeFolder );

            ArrayList< String > allFileNames = new ArrayList< String >();

            for( int set = set1; set < set2; ++set ) {
                String setFolder = codeFolder + File.separator + "hsf_" + set;
                ArrayList< String > fileNames = getFileNames( setFolder );

                if( !fileNames.isEmpty() ) {
                    System.out.println( "First file:" + fileNames.iterator().next() );
                }

                allFileNames.addAll( fileNames );
            }

            codeFileNames.put( code, allFileNames );
        }

        // 3. Postprocesss each image.
        // -------------------------------------------------------------------------------------------------------------
        for( int code = code1; code < code2; ++code ) {
            ArrayList< String > allFileNames = codeFileNames.get( code );

            for( String fileName : allFileNames ) {

                System.out.println( "Processing: " + fileName );

                BufferedImage bi = getImage( fileName );

                if( bi == null ) {
                    continue;
                }

                Float[] greyscale = getGreyscaleValues( bi );

                // 1. find centre of mass. This will be the centre of the new image.
                Point p = getCentreOfMass( bi, greyscale, threshold );

                // 2. find the bounding box of the pixels.
                Rectangle r = getBoundingBox( bi, greyscale, threshold );

                // 3. Compute the transform needed to centre the image on the mass-centre and scale to not crop any useful pixels
                Rectangle r2 = getCropBoundingBox( bi, r, p );

                // 4. Generate the new image by cropping the window, then scaling the crop.
                BufferedImage biTx = cropAndScale( bi, r2, outputSize, outputPad );

                fileNameTxImages.put( fileName, biTx );
            }

        }

        // 4. Now write out the new images with modified filenames..
        // -------------------------------------------------------------------------------------------------------------
        for( int code = code1; code < code2; ++code ) {
            ArrayList< String > allFileNames = codeFileNames.get( code );

            for( String filePath : allFileNames ) {

                System.out.println( "Processing: " + filePath );

                BufferedImage bi = fileNameTxImages.get( filePath );

                // calculate the new filename:
                // old filename format is hsf_0_00000.png
                // new filename format is with integer base 10 labels which are easier to deal with.
                // i.e. 65 .. 90
                // Files are all in 1 folder with names prefix_label_sequence.png
                // The sequence is carried over from the old filenames.

                File f = new File( filePath );
                String fileName = f.getName(); // e.g. hsf_0_00000
                String fileSequence = fileName.substring( inputFileNameSequenceIdx1, inputFileNameSequenceIdx2 );

                String label = String.valueOf( code );
                String outputFileName = targetFolder + File.separator + fileNamePrefix + "_" + label + "_" + fileSequence + ".png";

                // write the image to file.
                File outputfile = new File( outputFileName ); // I removed the leading zero because there may be more needed so it becomes a malformed number

                try {
                    ImageIO.write( bi, "png", outputfile );
                }
                catch( IOException e ) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     * Returns the file-paths of all the files in the specified folder.
     *
     * @param folderName
     * @return
     */
    private static ArrayList< String > getFileNames( String folderName ) {
        ArrayList< String > fileNames = new ArrayList<>();

        try {
            File folder = new File( folderName );
            boolean isFile = folder.isFile();

            if( !isFile ) {
                DirectoryStream< Path > directoryStream = null;
                try {

                    directoryStream = Files.newDirectoryStream( Paths.get( folderName ) );
                    for( Path path : directoryStream ) {
                        fileNames.add( path.toAbsolutePath().toString() );
                    }
                }
                catch( IOException ex ) {
                    System.err.println( "IO Exception reading files in folder: " + folderName );
                }
                finally {
                    if( directoryStream != null ) {
                        try {
                            directoryStream.close();
                        }
                        catch( Exception e ) {
                            System.err.println( "Exception closing directory stream from folder: " + folderName );
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                System.err.println( "ERROR: " + folderName + " is a file but should be a directory." );
            }
        }
        catch( Exception e ) {
            System.err.println( "Exception opening folder: " + folderName );
            e.printStackTrace();
        }

        return fileNames;
    }

    /**
     * Reads a BufferedImage from a file-path as a String.
     *
     * @param filePath
     * @return
     */
    public static BufferedImage getImage( String filePath ) {

            InputStream stream = null;
            BufferedImage image;

            try {
                // Java bug:
                // https://bugs.openjdk.java.net/browse/JDK-7166379
                // http://stackoverflow.com/questions/10441276/jdk-1-7-too-many-open-files-due-to-posix-semaphores
                stream = new FileInputStream( filePath );
                image = ImageIO.read( stream );
            }
            catch( IOException e ) {
                System.err.println( "ERROR: Unable to get image from file: " + filePath );

                image = null;
            }
            finally {
                if( stream != null ) {
                    try {
                        stream.close();
                    }
                    catch( IOException ex ) {
                        System.err.println( "ERROR closing image input stream: " + filePath + ex.getMessage() );
                    }
                }
            }

        return image;
    }

    /**
     * Crops a square out of an image with bounds checking. Then scales and pastes that square into an output image
     * of fixed size and margin padding. Returns the result of these operations.
     *
     * @param bi
     * @param cropBox
     * @param outputSize
     * @param outputPad
     * @return
     */
    public static BufferedImage cropAndScale( BufferedImage bi, Rectangle cropBox, int outputSize, int outputPad ) {
        int w = bi.getWidth();
        int h = bi.getHeight();

        int xc = Math.max( 0, cropBox.x );
        int yc = Math.max( 0, cropBox.y );

        int wcMax = w - xc; // e.g. w=10, x=2, w-x=8. 2,3,4,5, 6,7,8,9 OK
        int hcMax = h - yc;
        int wc = Math.min( wcMax, cropBox.width  );
        int hc = Math.min( hcMax, cropBox.height );

        BufferedImage cropped = bi.getSubimage( xc, yc, wc, hc );

        // generate a white image to paste into.
        BufferedImage padded = new BufferedImage( cropBox.width, cropBox.height, BufferedImage.TYPE_3BYTE_BGR );

        int px = 0;
        int py = 0;
        int pw = padded.getWidth();
        int ph = padded.getHeight();

        // paste the cotent into a padded image
//        padded.getGraphics().setColor( Color.green ); // debug
        padded.getGraphics().setColor( Color.white );
        padded.getGraphics().fillRect( 0, 0, pw, ph );
        padded.getGraphics().drawImage( cropped, px, py, null );

        // scaled the padded image to produce the final output
        // e.g. padded = 50, ideal = 28, scale = 28/50 = 0.56
        int contentSize = outputSize - outputPad * 2;
        float scale = (float)contentSize / (float)pw;
        BufferedImage scaled = new BufferedImage( outputSize, outputSize, BufferedImage.TYPE_INT_ARGB );

        // background fill
        scaled.getGraphics().setColor( Color.white );
        scaled.getGraphics().fillRect( 0, 0, pw, ph );

        // paste the scaled image at the origin of the content area
        AffineTransform at = new AffineTransform();
        at.translate( outputPad, outputPad );
        at.scale( scale, scale );
        AffineTransformOp scaleOp = new AffineTransformOp( at, AffineTransformOp.TYPE_BILINEAR );
        scaled = scaleOp.filter( padded, scaled );
//        return padded; // debug
        return scaled;
    }

    /**
     * Returns the bounding box of the area that we should crop out of the image to get a square centered on the centre
     * of mass.
     *
     * This method assumes the desired rect is a square.
     *
     * @param bi The image dimensions
     * @param r Bounding box of interesting px
     * @param p Centre of mass of interesting px
     * @return Bounding box of area to retain
     */
    public static Rectangle getCropBoundingBox( BufferedImage bi, Rectangle r, Point p ) {
        // find the smallest square, centred on the centre of mass.
        // I'm sure this can be calculated directly but I can't be bothered thinking about it and I'm in a rush
        int w = bi.getWidth();
        int h = bi.getHeight();
        int size = Math.max( w, h );

        int minRadius = size;

        for( int radius = 1; radius < size; ++radius  ) {

            // current limits
            int x1 = p.x - radius;
            int x2 = p.x + radius;
            int y1 = p.y - radius;
            int y2 = p.y + radius;

            // must include this px
            int xbb1 = r.x;
            int xbb2 = r.x + r.width;
            int ybb1 = r.y;
            int ybb2 = r.y + r.height;

            // e.g. bb = 5, x1 = 3. 3 > 5? no
            if( x1 > xbb1 ) continue; // bb1 = 5; x1 = 5 (ok). x1 <= bb1 is OK
            if( y1 > ybb1 ) continue;
            if( x2 < xbb2 ) continue;
            if( y2 < ybb2 ) continue; // bb2 = 10; x2 must be >= 9 (ok). So x2 <= bb2

            minRadius = radius;
            break;
        }

        Rectangle r2 = new Rectangle();
        int rx = p.x - minRadius;
        int ry = p.y - minRadius;
        int rw = minRadius * 2;
        int rh = minRadius * 2;

        r2.setBounds( rx, ry, rw, rh );

        return r2;
    }

    /**
     * Returns the centre of mass of pixels with grey intensity <= threshold using Hu Moments
     *
     * @param bi
     * @param greyscale
     * @param threshold
     * @return
     */
    public static Point getCentreOfMass( BufferedImage bi, Float[] greyscale, float threshold ) {
        int w = bi.getWidth(); // vol. width
        int h = bi.getHeight();

        float m00 = 0.f;
        float m01 = 0.f;
        float m10 = 0.f;

        for( int y = 0; y < h; ++y ) {

            for( int x = 0; x < w; ++x ) {

                int offset = y * w +x;

                float g = greyscale[ offset ];

                if( g > threshold ) {
                    continue; // non character (white background)
                }

                float weight = 1f; // or make it based on greyscale? dunno

                m00 += weight;
                m10 += ( weight * ( float ) x );
                m01 += ( weight * ( float ) y );
            }
        }

        int xc = (int)( m10 / m00 );
        int yc = (int)( m01 / m00 );

        if( m00 <= 0.f ) {
            xc = (int)( (float)w * 0.5f );
            yc = (int)( (float)h * 0.5f );
        }

        Point p = new Point( xc, yc );
        return p;
    }

    /**
     * Returns the bounding box of pixels with grey intensity <= threshold.
     *
     * @param bi
     * @param greyscale
     * @param threshold
     * @return
     */
    public static Rectangle getBoundingBox( BufferedImage bi, Float[] greyscale, float threshold ) {
        int w = bi.getWidth(); // vol. width
        int h = bi.getHeight();

        int xMin = w-1;
        int xMax = 0;

        int yMin = h-1;
        int yMax = 0;

        for( int y = 0; y < h; ++y ) {

            for( int x = 0; x < w; ++x ) {

                int offset = y * w +x;

                float g = greyscale[ offset ];

                if( g > threshold ) {
                    continue; // non character (white background)
                }

                xMin = Math.min( x, xMin );
                xMax = Math.min( x, xMax );

                yMin = Math.min( y, yMin );
                yMax = Math.min( y, yMax );

            }
        }

        Rectangle r = new Rectangle();
        int rw = xMax - xMin +1;
        int rh = yMax - yMin +1;
        r.setBounds( xMin, yMin, rw, rh );

        return r;
    }

    /**
     * Converts a BufferedImage into a Float array of pixel values (greyscale). For more convenient access to intensities.
     *
     * @param bi
     * @return
     */
    public static Float[] getGreyscaleValues( BufferedImage bi ) {

        int w = bi.getWidth(); // vol. width
        int h = bi.getHeight();

        Float[] greyscale = new Float[ w * h ];

        int[] sample = new int[ 4 ]; // RGB24

        for( int y = 0; y < h; ++y ) {

            for( int x = 0; x < w; ++x ) {

                bi.getRaster().getPixel( x, y, sample );

                int r = sample[ 0 ];
                int g = sample[ 1 ];
                int b = sample[ 2 ];

                // scale the pixel values to normalize them
                float grey = (float)b + (float)g + (float)r;
                grey *= (1f / 3f);
                grey = Math.min( 255.0f, Math.max( 0f, grey ) );

                greyscale[ y * w + x ] = grey;
            }
        }

        return greyscale;
    }

}
