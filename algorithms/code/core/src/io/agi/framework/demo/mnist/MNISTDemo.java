package io.agi.framework.demo.mnist;

import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceMNIST;

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

        BufferedImageSourceMNIST imagesMNIST = new BufferedImageSourceMNIST(
                "/Users/gideon/Google Drive/Project AGI/Experimental Framework/Experiments/Tests/Images/MNIST/train-labels-idx1-ubyte",
                "/Users/gideon/Google Drive/Project AGI/Experimental Framework/Experiments/Tests/Images/MNIST/train-images-idx3-ubyte");

        String outputPath = "/Users/gideon/Development/AI/ProAGI/mnist_output/";

        HashMap< String, Integer > labelCount = new HashMap<>(  );
        for ( int i=0 ; i < 10 /*imagesMNIST.bufferSize()*/ ; i++ ) {
            imagesMNIST.nextImage();

            BufferedImage image = imagesMNIST.getImage();
            String label = imagesMNIST.getLabel();

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
