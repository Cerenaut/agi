package io.agi.framework.demo.region;

import io.agi.core.orm.Keys;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.*;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.Persistence;
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
        m.setup( args[ 0 ], null, ef );

        // Create custom entities and references
        if( args.length > 1 ) {
            Framework.LoadEntities( m._n, args[1] );
        }

        if( args.length > 2 ) {
            Framework.LoadDataReferences(m._p, args[2]);
        }

        if( args.length > 3 ) {
            Framework.LoadConfigs( m._p, args[3] );
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

        Framework.CreateEntity( n, imageSourceName, ImageSensorEntity.ENTITY_TYPE, n.getName(), null );
        Framework.CreateEntity( n, constantMatrixName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageSourceName );
        Framework.CreateEntity( n, regionName, RegionEntity.ENTITY_TYPE, n.getName(), constantMatrixName );

        // Connect the entities' data
        Persistence p = n.getPersistence();

        Framework.SetDataReference( p, regionName, RegionEntity.FF_INPUT, imageSourceName, ImageSensorEntity.IMAGE_DATA );
        Framework.SetDataReference(p, regionName, RegionEntity.FB_INPUT, constantMatrixName, ConstantMatrixEntity.OUTPUT);

        // Set properties
        Framework.SetConfig( p, regionName, Entity.SUFFIX_RESET, "true" );
        Framework.SetConfig( p, imageSourceName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( p, imageSourceName, "greyscale", "true" );
        Framework.SetConfig( p, imageSourceName, "sourceFilesPath", "/home/dave/workspace/agi.io/agi/algorithms/code/core/run/line" );
    }
}
