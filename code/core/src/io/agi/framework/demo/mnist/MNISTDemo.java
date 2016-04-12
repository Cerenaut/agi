package io.agi.framework.demo.mnist;

import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.ConstantMatrixEntity;
import io.agi.framework.entities.EncoderEntity;
import io.agi.framework.entities.ImageSensorEntity;
import io.agi.framework.entities.RegionEntity;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.Persistence;

/**
 * Created by gideon on 14/03/2016.
 */
public class MNISTDemo {

    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
        m.setup( args[ 0 ], null, new CommonEntityFactory() );

        // Create custom entities and references
        if ( args.length > 2 ) {
            Framework.LoadEntities( args[ 1 ] );
            Framework.LoadData( args[ 2 ] );
        }
        else {
            // Programmatic hook to create entities and references..
            createEntities( m._n );
        }

        // Start the system
        m.run();

    }


    public static void createEntities( Node n ) {
        // Define some entities
        String sensorName = "image-sensor";
        String encoderName = "binary-encoder";
        String constantName = "constant";
        String regionName = "region";

        Framework.CreateEntity( sensorName, ImageSensorEntity.ENTITY_TYPE, n.getName(), null );
        Framework.CreateEntity( encoderName, EncoderEntity.ENTITY_TYPE, n.getName(), sensorName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), encoderName );
        Framework.CreateEntity( regionName, RegionEntity.ENTITY_TYPE, n.getName(), constantName );

        // Connect the entities' data
        Persistence p = n.getPersistence();

        Framework.SetDataReference( encoderName, EncoderEntity.DATA_INPUT, sensorName, ImageSensorEntity.IMAGE_DATA );
        Framework.SetDataReference( regionName, RegionEntity.FF_INPUT, encoderName, EncoderEntity.DATA_OUTPUT );
        Framework.SetDataReference( regionName, RegionEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT );

        Framework.SetConfig( sensorName, "greyscale", "true" );
        Framework.SetConfig( sensorName, "invert", "true" );
        Framework.SetConfig( sensorName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( sensorName, "sourceFilesPath", "/Users/gideon/Development/AI/ProAGI/TestData/MNIST/mnist_output" );
        Framework.SetConfig( sensorName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( sensorName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( sensorName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( sensorName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( sensorName, "resolution.resolutionX", "28" );
        Framework.SetConfig( sensorName, "resolution.resolutionY", "28" );

        Framework.SetConfig( encoderName, "density", "1" );
        Framework.SetConfig( encoderName, "bits", "8" );
        Framework.SetConfig( encoderName, "encodeZero", "false" );

        Framework.SetConfig( regionName, Entity.SUFFIX_RESET, "true" );
    }


}
