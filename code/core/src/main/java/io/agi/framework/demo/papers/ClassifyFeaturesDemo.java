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
import io.agi.framework.Naming;
import io.agi.framework.Node;
import io.agi.framework.demo.mnist.AnalyticsEntity;
import io.agi.framework.demo.mnist.AnalyticsEntityConfig;
import io.agi.framework.demo.mnist.ClassificationAnalysisEntity;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.util.Properties;

/**
 * PHASE 2.
 *
 * To do it manually:
 *
 * 1. Run Phase 1.
 * 2. Import Phase 1 data file.
 * 3. Set Analytics entity trainSetSize, testSetSize, testSetOffset to correct values via
 *    http://localhost:8000/config.html?entity=analytics
 * 4. Set analytics input data - features via
 *    http://localhost:8000/data.html?data=analytics-input-features
 * 5. Set analytics input data - labels via
 *    http://localhost:8000/data.html?data=analytics-input-labels
 *
 * Learns to assign labels to a set of recorded features.
 *
 * Assumes features and labels over time have already been recorded.
 *
 * Created by gideon on 08/01/17.
 */
public class ClassifyFeaturesDemo {

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
                Naming.SetEntityNamePrefix( prefix );
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
        String experimentName   = PersistenceUtil.GetEntityName( "experiment" );
        String analyticsName    = PersistenceUtil.GetEntityName( "analytics" );
        String logisticRegressionName = PersistenceUtil.GetEntityName( "logistic-regression" );
        String classificationAnalysisName = PersistenceUtil.GetEntityName( "classification-analysis" );


        // 2) Configuration values
        // ---------------------------------------------
        boolean cacheAllData = true;

        //float trainingDropoutProbability = 1f / 49f;
        float trainingDropoutProbability = 0f;

        int trainingSamples = 60000;
        int testingSamples = 10000;

        String seriesPrefix = "PASTASAUCE--";
        //String seriesPrefix = "";

        ExperimentEntityConfig experimentConfig = new ExperimentEntityConfig();
        experimentConfig.terminationEntityName = analyticsName;
        experimentConfig.terminationConfigPath = "terminate";
        experimentConfig.terminationAge = -1;       // wait for analytics entity to decide
        experimentConfig.reportingEntityName = classificationAnalysisName;
        experimentConfig.reportingEntityConfigPath = "resultsSummary";

        AnalyticsEntityConfig analyticsEntityConfig = new AnalyticsEntityConfig();
        analyticsEntityConfig.batchMode = true;
        analyticsEntityConfig.trainSetOffset = 0;
        analyticsEntityConfig.testSetOffset = trainingSamples;           // test on the training set as well
        analyticsEntityConfig.trainSetSize = trainingSamples;//60000;
        analyticsEntityConfig.testSetSize = testingSamples;// 70000;
        analyticsEntityConfig.testingEntities = logisticRegressionName;
        analyticsEntityConfig.predictDuringTraining = true;
        analyticsEntityConfig.trainingDropoutProbability = trainingDropoutProbability;

        LogisticRegressionEntityConfig logisticRegressionEntityConfig = new LogisticRegressionEntityConfig();
        logisticRegressionEntityConfig.bias = true;
        logisticRegressionEntityConfig.C = 1.f;
        logisticRegressionEntityConfig.labelClasses = 10;

        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        PersistenceUtil.CreateEntity( analyticsName, AnalyticsEntity.ENTITY_TYPE, n.getName(), experimentName );
        PersistenceUtil.CreateEntity( logisticRegressionName, LogisticRegressionEntity.ENTITY_TYPE, n.getName(), analyticsName );
        PersistenceUtil.CreateEntity( classificationAnalysisName, ClassificationAnalysisEntity.ENTITY_TYPE, n.getName(), logisticRegressionName );


        // 3) Connect entities
        // ---------------------------------------------
        // Connect the 'testing entities' data input to the training data set directly
        DataRefUtil.SetDataReference( analyticsName, AnalyticsEntity.INPUT_FEATURES, seriesPrefix + "feature-series", "output" );
        DataRefUtil.SetDataReference( analyticsName, AnalyticsEntity.INPUT_LABELS, seriesPrefix + "label-series", "output" );

        DataRefUtil.SetDataReference( logisticRegressionName, LogisticRegressionEntity.INPUT_FEATURES, analyticsName, AnalyticsEntity.OUTPUT_FEATURES );
        DataRefUtil.SetDataReference( logisticRegressionName, LogisticRegressionEntity.INPUT_LABELS, analyticsName, AnalyticsEntity.OUTPUT_LABELS );

        DataRefUtil.SetDataReference( classificationAnalysisName, ClassificationAnalysisEntity.INPUT_TRUTH, logisticRegressionName, LogisticRegressionEntity.OUTPUT_LABELS_TRUTH );
        DataRefUtil.SetDataReference( classificationAnalysisName, ClassificationAnalysisEntity.INPUT_PREDICTED, logisticRegressionName, LogisticRegressionEntity.OUTPUT_LABELS_PREDICTED );


        // 4) Set configurations
        // ---------------------------------------------
        PersistenceUtil.SetConfig( experimentName, experimentConfig );
        PersistenceUtil.SetConfig( analyticsName, analyticsEntityConfig );
        PersistenceUtil.SetConfig( logisticRegressionName, logisticRegressionEntityConfig );

        // cache all data for speed, when enabled (override this property in configs)
        PersistenceUtil.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( analyticsName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( logisticRegressionName, "cache", String.valueOf( cacheAllData ) );
    }

}
