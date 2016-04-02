package io.agi.framework.demo.region;

import io.agi.core.orm.Keys;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Entity;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.PropertyConverter;
import io.agi.framework.persistence.PropertyStringAccess;
import io.agi.framework.persistence.models.ModelEntity;

/**
 * Created by dave on 26/03/16.
 */
public class RegionDemo {


    // Debug GUI
    // file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/update.html?_entityName=image-source
    // file:///home/dave/workspace/agi.io/agi/experimental-framework/code/web-ui2/matrix.html?data=image-source-image-data

    public static void main( String[] args ) {

        // Provide classes for entities
        CommonEntityFactory ef = new CommonEntityFactory();

        // Create a Node
        Main m = new Main();
        m.setup( args[0], null, ef );

        // Create custom entities and references
        if( args.length > 1 ) {
            m.loadEntities(args[1]);
        }

        if( args.length > 2 ) {
            m.loadReferences( args[ 2 ] );
        }

        // Programmatic hook to Create entities and references..
        createEntities( m._n );

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

        // Define some entities
        String imageSourceName = "image-source";
        String constantMatrixName = "constant-matrix";
        String regionName = "region";

        ModelEntity imageSource = new ModelEntity( imageSourceName, ImageSensorEntity.ENTITY_TYPE, n.getName(), null );
        ModelEntity constantMatrix = new ModelEntity( constantMatrixName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageSourceName );
        ModelEntity region = new ModelEntity( regionName, RegionEntity.ENTITY_TYPE, n.getName(), constantMatrixName ); // linked, so we only need to update the problem
// disconnected:
//        ModelEntity region = new ModelEntity( regionName, GrowingNeuralGasEntity.ENTITY_TYPE, n.getName(), null ); // linked, so we only need to update the problem

        Persistence p = n.getPersistence();
        p.setEntity(imageSource);
        p.setEntity(constantMatrix);
        p.setEntity(region);

        // Connect the entities
        Entity.SetDataReference(p, regionName, RegionEntity.FF_INPUT, imageSourceName, ImageSensorEntity.OUTPUT_IMAGE_DATA);
        Entity.SetDataReference(p, regionName, RegionEntity.FB_INPUT, constantMatrixName, ConstantMatrixEntity.OUTPUT );

        // Set _configPathValues
        PropertyConverter propertyConverter = new PropertyConverter( (PropertyStringAccess) p );

        String resetKey = Keys.concatenate( regionName, Entity.SUFFIX_RESET );
        propertyConverter.setPropertyBoolean(resetKey, true); // reset on every run

        // TODO shouldn't have to set this, default doesn't work for string _configPathValues
        String sourceTypeKey = Keys.concatenate( imageSourceName, ImageSensorEntity.SOURCE_TYPE );
        propertyConverter.setPropertyString(sourceTypeKey, BufferedImageSourceFactory.TYPE_IMAGE_FILES );

        String sourceGreyKey = Keys.concatenate( imageSourceName, ImageSensorEntity.GREYSCALE );
        propertyConverter.setPropertyBoolean(sourceGreyKey, true);

        String filePathKey = Keys.concatenate( imageSourceName, ImageSensorEntity.SOURCE_FILES_PATH );
        p.setPropertyString( filePathKey, "/home/dave/workspace/agi.io/agi/algorithms/code/core/run/line" );
    }
}
