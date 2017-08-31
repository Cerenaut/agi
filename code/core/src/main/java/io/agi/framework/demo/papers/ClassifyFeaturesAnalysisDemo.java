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

package io.agi.framework.demo.papers;

import io.agi.core.util.PropertiesUtil;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.demo.mnist.ClassificationAnalysisEntity;
import io.agi.framework.entities.ExperimentEntity;
import io.agi.framework.entities.ExperimentEntityConfig;
import io.agi.framework.factories.CommonEntityFactory;

import java.util.Properties;

/**
 * When you already have the classification, but need to apply some statistics to see how good it is...
 *
 * Created by dave on 19/02/17.
 */
public class ClassifyFeaturesAnalysisDemo {

    /**
     * Usage: Expects some arguments. These are:
     * 0: node.properties file
     * 1 to n: 'create' flag and/or 'prefix' flag
     * @param args
     */
    public static void main( String[] args ) {

        // Create a Node
        Main m = new Main();
        Properties p = PropertiesUtil.load( args[ 0 ] );
        m.setup( p, null, new CommonEntityFactory() );

        // Optionally set a global prefix for entities
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "prefix" ) ) {
                String prefix = args[ i+1 ];
                Framework.SetEntityNamePrefix( prefix );
//                Framework.SetEntityNamePrefixDateTime();
            }
        }

        // Optionally create custom entities and references
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "create" ) ) {
                createEntities( m._n );
            }
        }

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

        // 1) Define entities
        // ---------------------------------------------
        String experimentName   = Framework.GetEntityName( "experiment" );
        String classificationAnalysisName = Framework.GetEntityName( "classification-analysis" );


        // 2) Configuration values
        // ---------------------------------------------
        boolean cacheAllData = true;

        ExperimentEntityConfig experimentConfig = new ExperimentEntityConfig();
        experimentConfig.terminationAge = 1;       // wait for analytics entity to decide

        Framework.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        Framework.CreateEntity( classificationAnalysisName, ClassificationAnalysisEntity.ENTITY_TYPE, n.getName(), experimentName );


        // 3) Connect entities
        // ---------------------------------------------
        // Connect the 'testing entities' data input to the training data set directly
        // TODO: Set this programmatically
        Framework.SetDataReference( classificationAnalysisName, ClassificationAnalysisEntity.INPUT_TRUTH, "classifier", "truth" );
        Framework.SetDataReference( classificationAnalysisName, ClassificationAnalysisEntity.INPUT_PREDICTED, "classifier", "predicted" );


        // 4) Set configurations
        // ---------------------------------------------
        Framework.SetConfig( experimentName, experimentConfig );

        // cache all data for speed, when enabled (override this property in configs)
        Framework.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
    }

}
