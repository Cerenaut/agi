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

import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceFactory;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;

/**
 * Created by gideon on 14/03/2016.
 */
public class MNISTDemo {

    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
        m.setup( args[ 0 ], null, new CommonEntityFactory() );

        // Create custom entities and references
        if( args.length > 2 ) {
            Framework.LoadEntities( args[ 1 ] );
            Framework.LoadData( args[ 2 ] );
        } else {
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
        String decoderNameActivity = "binary-decoder-activity";
        String decoderNamePredicted = "binary-decoder-predicted";

        Framework.CreateEntity( sensorName, ImageSensorEntity.ENTITY_TYPE, n.getName(), null );
        Framework.CreateEntity( encoderName, EncoderEntity.ENTITY_TYPE, n.getName(), sensorName );
        Framework.CreateEntity( constantName, ConstantMatrixEntity.ENTITY_TYPE, n.getName(), encoderName );
        Framework.CreateEntity( regionName, RegionEntity.ENTITY_TYPE, n.getName(), constantName );
        Framework.CreateEntity( decoderNameActivity, DecoderEntity.ENTITY_TYPE, n.getName(), regionName );
        Framework.CreateEntity( decoderNamePredicted, DecoderEntity.ENTITY_TYPE, n.getName(), regionName );

        // Connect the entities' data
        Framework.SetDataReference( encoderName, EncoderEntity.DATA_INPUT, sensorName, ImageSensorEntity.IMAGE_DATA );
        Framework.SetDataReference( regionName, RegionEntity.FF_INPUT, encoderName, EncoderEntity.DATA_OUTPUT_ENCODED );
        Framework.SetDataReference( regionName, RegionEntity.FB_INPUT, constantName, ConstantMatrixEntity.OUTPUT );
        Framework.SetDataReference( decoderNameActivity,  DecoderEntity.DATA_INPUT_ENCODED, regionName, RegionEntity.FB_OUTPUT_UNFOLDED_ACTIVITY );
        Framework.SetDataReference( decoderNamePredicted, DecoderEntity.DATA_INPUT_ENCODED, regionName, RegionEntity.FB_OUTPUT_UNFOLDED_PREDICTION );

        Framework.SetConfig( sensorName, "greyscale", "true" );
        Framework.SetConfig( sensorName, "invert", "true" );
        Framework.SetConfig( sensorName, "sourceType", BufferedImageSourceFactory.TYPE_IMAGE_FILES );
//        Framework.SetConfig( sensorName, "sourceFilesPath", "/home/dave/workspace/agi.io/data/mnist/cycle10" );
        Framework.SetConfig( sensorName, "sourceFilesPath", "/home/dave/workspace/agi.io/data/mnist/cycle3" );
        Framework.SetConfig( sensorName, "receptiveField.receptiveFieldX", "0" );
        Framework.SetConfig( sensorName, "receptiveField.receptiveFieldY", "0" );
        Framework.SetConfig( sensorName, "receptiveField.receptiveFieldW", "28" );
        Framework.SetConfig( sensorName, "receptiveField.receptiveFieldH", "28" );
        Framework.SetConfig( sensorName, "resolution.resolutionX", "28" );
        Framework.SetConfig( sensorName, "resolution.resolutionY", "28" );

        Framework.SetConfig( encoderName, "density", "1" );
        Framework.SetConfig( encoderName, "bits", "8" );
        Framework.SetConfig( encoderName, "encodeZero", "false" );

        Framework.SetConfig( decoderNameActivity, "density", "1" );
        Framework.SetConfig( decoderNameActivity, "bits", "8" );
        Framework.SetConfig( decoderNameActivity, "encodeZero", "false" );

        Framework.SetConfig( decoderNamePredicted, "density", "1" );
        Framework.SetConfig( decoderNamePredicted, "bits", "8" );
        Framework.SetConfig( decoderNamePredicted, "encodeZero", "false" );

        Framework.SetConfig( regionName, "receptiveFieldsTrainingSamples", "0.1" );
        Framework.SetConfig( regionName, "organizerStressThreshold", "0.0" );
        Framework.SetConfig( regionName, "organizerGrowthInterval", "1" );
        Framework.SetConfig( regionName, "organizerEdgeMaxAge", "1000" );
        Framework.SetConfig( regionName, "organizerNoiseMagnitude", "0.0" );
        Framework.SetConfig( regionName, "organizerLearningRate", "0.002" );
        Framework.SetConfig( regionName, "organizerLearningRateNeighbours", "0.001" );

        Framework.SetConfig( regionName, "classifierStressThreshold", "0.0" );
        Framework.SetConfig( regionName, "classifierGrowthInterval", "1" );

        Framework.SetConfig( regionName, Entity.SUFFIX_RESET, "true" );
//        Framework.SetConfig( regionName, Entity.SUFFIX_RESET, "false" );
    }

}
