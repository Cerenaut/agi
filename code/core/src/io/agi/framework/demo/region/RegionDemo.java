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

package io.agi.framework.demo.region;

import io.agi.core.util.PropertiesUtil;
import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.ConstantMatrixEntity;
import io.agi.framework.entities.ImageSensorEntity;
import io.agi.framework.entities.RegionEntity;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.Persistence;

import java.util.Properties;

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
        Properties p = PropertiesUtil.load( args[ 0 ] );
        m.setup( p, null, ef );

        // Create custom entities and references
        if( args.length > 1 ) {
            Framework.LoadEntities( args[ 1 ] );
        }

        if( args.length > 2 ) {
            Framework.LoadDataReferences( args[ 2 ] );
        }

        if( args.length > 3 ) {
            Framework.LoadConfigs( args[ 3 ] );
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

        Framework.CreateEntity( imageSourceName, ImageSensorEntity.ENTITY_TYPE, n.getName(), null );
        Framework.CreateEntity( constantMatrixName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), imageSourceName );
        Framework.CreateEntity( regionName, RegionEntity.ENTITY_TYPE, n.getName(), constantMatrixName );

        // Connect the entities' data
        Persistence p = n.getPersistence();

        Framework.SetDataReference( regionName, RegionEntity.FF_INPUT, imageSourceName, ImageSensorEntity.IMAGE_DATA );
        Framework.SetDataReference( regionName, RegionEntity.FB_INPUT, constantMatrixName, ConstantMatrixEntity.OUTPUT );

        // Set properties
        Framework.SetConfig( regionName, Entity.SUFFIX_RESET, "true" );
        Framework.SetConfig( imageSourceName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
        Framework.SetConfig( imageSourceName, "greyscale", "true" );
        Framework.SetConfig( imageSourceName, "sourceFilesPath", "/home/dave/workspace/agi.io/agi/algorithms/code/core/run/line" );
    }
}
