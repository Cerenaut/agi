package io.agi.framework.demo.mnist;

import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceMNIST;
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

        // Programmatic hook to create entities and references..
        createEntities( m._n );

        // Start the system
        m.run();

    }


    public static void createEntities( Node n ) {
        // Define some entities
        String sensorName = "image-sensor";
        String encoderName = "binary-encoder";
        String constantName = "constant";
        String regionName = "region";

        Framework.CreateEntity( n, sensorName, ImageSensorEntity.ENTITY_TYPE, n.getName(), null );
        Framework.CreateEntity( n, encoderName, EncoderEntity.ENTITY_TYPE, n.getName(), sensorName );
        Framework.CreateEntity( n, constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), encoderName );
        Framework.CreateEntity( n, regionName, RegionEntity.ENTITY_TYPE, n.getName(), constantName );

        // Connect the entities' data
        Persistence p = n.getPersistence();

        Framework.SetDataReference( p, encoderName, EncoderEntity.DATA_INPUT, sensorName, ImageSensorEntity.IMAGE_DATA );
        Framework.SetDataReference( p, regionName, RegionEntity.FF_INPUT, encoderName, EncoderEntity.DATA_OUTPUT );
        Framework.SetDataReference(p, regionName, RegionEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT);

        Framework.SetConfig(p, sensorName, "greyscale", "true");
        Framework.SetConfig(p, sensorName, "invert", "true");
        Framework.SetConfig( p, sensorName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
//        Framework.SetConfig( p, sensorName, "sourceFilesPath", "/home/dave/workspace/agi.io/data/mnist/cycle10" );
        Framework.SetConfig( p, sensorName, "sourceFilesPath", "/home/dave/workspace/agi.io/data/mnist/cycle3" );
        Framework.SetConfig( p, sensorName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( p, sensorName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( p, sensorName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( p, sensorName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( p, sensorName, "resolution.resolutionX", "28" );
        Framework.SetConfig( p, sensorName, "resolution.resolutionY", "28" );

        Framework.SetConfig( p, encoderName, "density", "1" );
        Framework.SetConfig( p, encoderName, "bits", "8" );
        Framework.SetConfig( p, encoderName, "encodeZero", "false" );

        Framework.SetConfig( p, regionName, "organizerStressThreshold", "0.01" );
        Framework.SetConfig( p, regionName, "organizerGrowthInterval", "5" );

        Framework.SetConfig( p, regionName, "classifierStressThreshold", "0.01" );
        Framework.SetConfig( p, regionName, "classifierGrowthInterval", "5" );

        Framework.SetConfig( p, regionName, Entity.SUFFIX_RESET, "true" );

    }


}
