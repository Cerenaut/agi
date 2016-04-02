package io.agi.framework.demo.mnist;

import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceMNIST;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.CommonEntityFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by gideon on 14/03/2016.
 */
public class MNISTDemo {

    public static void main( String[] args ) {

        boolean preprocess = false;
        if ( preprocess ) {
            Integer max = new Integer( args[ 3 ] );
            preprocess( args[ 0 ], args[ 1 ], args[ 2 ], max );
            return;
        }

        // Create a Node
        Main m = new Main();
        m.setup( args[ 0 ], null, new CommonEntityFactory() );

        // Create custom entities and references
        if ( args.length > 1 ) {
            m.loadEntities( args[ 1 ] );
        }

        if ( args.length > 2 ) {
            m.loadReferences( args[ 2 ] );
        }

        if ( args.length > 3 ) {
            m.loadConfigs(args[3]);
        }

        // Programmatic hook to create entities and references..
        createEntities( m._n );

        // Start the system
        m.run();

    }


    public static void createEntities( Node n ) {
    }


    /**
     * @param labelFile
     * @param imageFile
     * @param outputPath
     * @param maxNumImages if < 0 or > number of images, then go through all images
     */
    private static void preprocess( String labelFile, String imageFile, String outputPath, int maxNumImages ) {

        BufferedImageSourceMNIST imagesMNIST = new BufferedImageSourceMNIST( labelFile, imageFile );

        if ( ( maxNumImages < 0 ) ||
                ( maxNumImages >= imagesMNIST.bufferSize() ) ) {
            maxNumImages = imagesMNIST.bufferSize();
        }

        HashMap< String, Integer > labelCount = new HashMap<>();
        for ( int i = 0; i < maxNumImages; i++ ) {

            BufferedImage image = imagesMNIST.getImage();
            String label = imagesMNIST.getLabel();

            imagesMNIST.nextImage();

            Integer count = 0;
            if ( labelCount.containsKey( label ) ) {
                count = labelCount.get( label );
                count++;
            }
            labelCount.put( label, count );

            File outputfile = new File( outputPath + label + "_0" + labelCount.get( label ) + ".png" );
            try {
                ImageIO.write( image, "png", outputfile );
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
}
