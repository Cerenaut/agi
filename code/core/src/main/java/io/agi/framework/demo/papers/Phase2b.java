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

import io.agi.core.ml.supervised.LogisticRegression;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.AnalyticsEntity;
import io.agi.framework.demo.mnist.AnalyticsEntityConfig;
import io.agi.framework.demo.mnist.ClassificationAnalysisEntity;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.references.DataRefUtil;

import java.io.File;

/**
 * Applies a supervised classifier to a matrix of features and a vector of labels to try to predict the labels given
 * a testing set of features.
 *
 * Created by dave on 21/10/17.
 */
public class Phase2b extends CreateEntityMain {

    public static void main( String[] args ) {
        Phase2b main = new Phase2b();
        main.mainImpl( args );
    }

    public String getInputFilesPath() {
//        return "/home/dave/Desktop/agi/data/";
//        return "/home/dave/Desktop/agi/data/fast_data_test/2";
//        return "/home/dave/Desktop/agi/data/Conv-GNG/1_epoch";
        return "/home/dave/Desktop/agi/data/batch_sparse/1_epoch";
    }

    public void createEntities( Node n ) {

        String fileNameReadFeatures = getInputFilesPath() + File.separator + "features.csv";
        String fileNameReadLabels = getInputFilesPath() + File.separator + "labels.csv";


        // 1) Define entities
        // ---------------------------------------------
        String experimentName = PersistenceUtil.GetEntityName( "experiment" );
//        String featureSeriesName = PersistenceUtil.GetEntityName( "feature-series" );
//        String labelSeriesName = PersistenceUtil.GetEntityName( "label-series" );
        String analyticsName = PersistenceUtil.GetEntityName( "analytics" );
        String logisticRegressionName = PersistenceUtil.GetEntityName( "logistic-regression" );
        String classificationAnalysisName = PersistenceUtil.GetEntityName( "classification-analysis" );


        // Create Entities
        // ---------------------------------------------
        String parentName = null;
        parentName = PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), parentName ); // experiment is the root entity
//        parentName = PersistenceUtil.CreateEntity( featureSeriesName, DataFileEntity.ENTITY_TYPE, n.getName(), parentName ); // experiment is the root entity
//        parentName = PersistenceUtil.CreateEntity( labelSeriesName, DataFileEntity.ENTITY_TYPE, n.getName(), parentName ); // experiment is the root entity
        parentName = PersistenceUtil.CreateEntity( analyticsName, AnalyticsEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( logisticRegressionName, LogisticRegressionEntity.ENTITY_TYPE, n.getName(), parentName );
        parentName = PersistenceUtil.CreateEntity( classificationAnalysisName, ClassificationAnalysisEntity.ENTITY_TYPE, n.getName(), parentName );


        // 2) Configuration values
        // ---------------------------------------------
        int trainingSamples = 60000;
        int testingSamples = 10000;
        float C = 1f;
        int labelClasses = 10;

        boolean cacheAllData = true;
        float trainingDropoutProbability = 0f;
        //String seriesPrefix = "PREFIX--";
        //String seriesPrefix = "";

        ExperimentEntityConfig experimentConfig = new ExperimentEntityConfig();
        experimentConfig.terminationEntityName = analyticsName;
        experimentConfig.terminationConfigPath = "terminate";
        experimentConfig.terminationAge = -1;       // wait for analytics entity to decide
        experimentConfig.reportingEntities = classificationAnalysisName;
        experimentConfig.reportingEntityConfigPath = "resultsSummary";

        AnalyticsEntityConfig analyticsEntityConfig = new AnalyticsEntityConfig();
        analyticsEntityConfig.batchMode = true;
        analyticsEntityConfig.trainSetOffset = 0;
        analyticsEntityConfig.testSetOffset = trainingSamples;
        analyticsEntityConfig.trainSetSize = trainingSamples;
        analyticsEntityConfig.testSetSize = testingSamples;
        analyticsEntityConfig.testingEntities = logisticRegressionName;
        analyticsEntityConfig.predictDuringTraining = true;
        analyticsEntityConfig.trainingDropoutProbability = trainingDropoutProbability;
        analyticsEntityConfig.useInputFiles = true;
        analyticsEntityConfig.fileNameFeatures = fileNameReadFeatures;
        analyticsEntityConfig.fileNameLabels = fileNameReadLabels;

        LogisticRegressionEntityConfig logisticRegressionEntityConfig = new LogisticRegressionEntityConfig();
        logisticRegressionEntityConfig.bias = true;
        logisticRegressionEntityConfig.C = C;
        logisticRegressionEntityConfig.labelClasses = labelClasses;

        // Read data files
        // ---------------------------------------------
//        boolean write = false;
//        boolean read = true;
//        boolean append = false;
//        String fileNameWrite = null;;
//        DataFileEntityConfig.Set( featureSeriesName, cacheAllData, write, read, append, ModelData.ENCODING_SPARSE_REAL, fileNameWrite, fileNameReadFeatures );
//        DataFileEntityConfig.Set( labelSeriesName, cacheAllData, write, read, append, ModelData.ENCODING_DENSE, fileNameWrite, fileNameReadLabels );


        // 3) Connect entities data
        // ---------------------------------------------
        // Connect the 'testing entities' data input to the training data set directly
////        DataRefUtil.SetDataReference( analyticsName, AnalyticsEntity.INPUT_FEATURES, seriesPrefix + "feature-series", "output" );
////        DataRefUtil.SetDataReference( analyticsName, AnalyticsEntity.INPUT_LABELS, seriesPrefix + "label-series", "output" );
//        DataRefUtil.SetDataReference( analyticsName, AnalyticsEntity.INPUT_FEATURES, featureSeriesName, DataFileEntity.OUTPUT_READ );
//        DataRefUtil.SetDataReference( analyticsName, AnalyticsEntity.INPUT_LABELS, labelSeriesName, DataFileEntity.OUTPUT_READ );

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
//        PersistenceUtil.SetConfig( featureSeriesName, "cache", String.valueOf( cacheAllData ) );
//        PersistenceUtil.SetConfig( labelSeriesName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( logisticRegressionName, "cache", String.valueOf( cacheAllData ) );
    }

}