/*
 * Copyright (c) 2018.
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

import io.agi.framework.Node;
import io.agi.framework.demo.CreateEntityMain;
import io.agi.framework.demo.mnist.AnalyticsEntity;
import io.agi.framework.demo.mnist.AnalyticsEntityConfig;
import io.agi.framework.demo.mnist.ClassificationAnalysisEntity;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.io.File;

/**
 * Applies multiple supervised classifiers concurrently to a matrix of features and a vector of labels to try
 * to predict the labels given a testing set of features.
 *
 * Refer to the following entity tree structure for a concrete example.
 *
 * ExperimentEntity (root)
 *
 * -> AnalyticsEntity
 *
 * ----> Classifier #1 (e.g. SVM)
 * --------> ClassificationAnalysisEntity
 *
 * -----> Classifier #2 (e.g. Logistic Regression)
 * --------> ClassificationAnalysisEntity
 *
 * Created by abdel on 17/01/18.
 */
public class Phase2bDual extends CreateEntityMain {

    public static void main( String[] args ) {
        Phase2bDual main = new Phase2bDual();
        main.mainImpl( args );
    }

    public String getInputFilesPath() {
        return "/Users/Abdel/Developer/code/ProjectAGI/data/batch_sparse/1_epoch";
    }

    public void createEntities( Node n ) {

        String fileNameReadFeatures = getInputFilesPath() + File.separator + "features.csv";
        String fileNameReadLabels = getInputFilesPath() + File.separator + "labels.csv";


        // 1) Define entities
        // ---------------------------------------------
        String experimentName = PersistenceUtil.GetEntityName( "experiment" );
        String analyticsName = PersistenceUtil.GetEntityName( "analytics" );
        String svmName = PersistenceUtil.GetEntityName( "svm" );
        String logisticRegressionName = PersistenceUtil.GetEntityName( "logistic-regression" );
        String classificationAnalysisName1 = PersistenceUtil.GetEntityName( "classification-analysis-svm" );
        String classificationAnalysisName2 = PersistenceUtil.GetEntityName( "classification-analysis-reg" );


        // Create Entities
        // ---------------------------------------------
        String parentName;
        String childSvmName;
        String childLogisticRegressionName;

        parentName = PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity
        parentName = PersistenceUtil.CreateEntity( analyticsName, AnalyticsEntity.ENTITY_TYPE, n.getName(), parentName );

        childSvmName = PersistenceUtil.CreateEntity( svmName, SvmEntity.ENTITY_TYPE, n.getName(), parentName );
        PersistenceUtil.CreateEntity( classificationAnalysisName1, ClassificationAnalysisEntity.ENTITY_TYPE, n.getName(), childSvmName );

        childLogisticRegressionName = PersistenceUtil.CreateEntity( logisticRegressionName, LogisticRegressionEntity.ENTITY_TYPE, n.getName(), parentName );
        PersistenceUtil.CreateEntity( classificationAnalysisName2, ClassificationAnalysisEntity.ENTITY_TYPE, n.getName(), childLogisticRegressionName );


        // 2) Configuration values
        // ---------------------------------------------
        int trainingSamples = 60000;
        int testingSamples = 10000;
        int labelClasses = 10;
        float gamma = 0.1f;
        float C = 1f;

        boolean cacheAllData = true;
        float trainingDropoutProbability = 0f;

        ExperimentEntityConfig experimentConfig = new ExperimentEntityConfig();
        experimentConfig.terminationEntityName = analyticsName;
        experimentConfig.terminationConfigPath = "terminate";
        experimentConfig.terminationAge = -1;       // wait for analytics entity to decide
        experimentConfig.reportingEntities = classificationAnalysisName1  + "," + classificationAnalysisName2;
        experimentConfig.reportingEntityConfigPath = "resultsSummary";

        AnalyticsEntityConfig analyticsEntityConfig = new AnalyticsEntityConfig();
        analyticsEntityConfig.batchMode = true;
        analyticsEntityConfig.trainSetOffset = 0;
        analyticsEntityConfig.testSetOffset = trainingSamples;
        analyticsEntityConfig.trainSetSize = trainingSamples;
        analyticsEntityConfig.testSetSize = testingSamples;
        analyticsEntityConfig.testingEntities = logisticRegressionName + "," + svmName;
        analyticsEntityConfig.predictDuringTraining = true;
        analyticsEntityConfig.trainingDropoutProbability = trainingDropoutProbability;
        analyticsEntityConfig.useInputFiles = true;
        analyticsEntityConfig.fileNameFeatures = fileNameReadFeatures;
        analyticsEntityConfig.fileNameLabels = fileNameReadLabels;

        SvmEntityConfig svmEntityConfig = new SvmEntityConfig();
        svmEntityConfig.labelClasses = labelClasses;
        svmEntityConfig.gamma = gamma;
        svmEntityConfig.C = C;

        LogisticRegressionEntityConfig logisticRegressionEntityConfig = new LogisticRegressionEntityConfig();
        logisticRegressionEntityConfig.labelClasses = labelClasses;
        logisticRegressionEntityConfig.bias = true;
        logisticRegressionEntityConfig.C = C;

        // 3) Connect entities data
        // ---------------------------------------------
        DataRefUtil.SetDataReference( svmName, SvmEntity.INPUT_FEATURES, analyticsName, AnalyticsEntity.OUTPUT_FEATURES );
        DataRefUtil.SetDataReference( svmName, SvmEntity.INPUT_LABELS, analyticsName, AnalyticsEntity.OUTPUT_LABELS );

        DataRefUtil.SetDataReference( classificationAnalysisName1, ClassificationAnalysisEntity.INPUT_TRUTH, svmName, SvmEntity.OUTPUT_LABELS_TRUTH );
        DataRefUtil.SetDataReference( classificationAnalysisName1, ClassificationAnalysisEntity.INPUT_PREDICTED, svmName, SvmEntity.OUTPUT_LABELS_PREDICTED );

        DataRefUtil.SetDataReference( logisticRegressionName, LogisticRegressionEntity.INPUT_FEATURES, analyticsName, AnalyticsEntity.OUTPUT_FEATURES );
        DataRefUtil.SetDataReference( logisticRegressionName, LogisticRegressionEntity.INPUT_LABELS, analyticsName, AnalyticsEntity.OUTPUT_LABELS );

        DataRefUtil.SetDataReference( classificationAnalysisName2, ClassificationAnalysisEntity.INPUT_TRUTH, logisticRegressionName, LogisticRegressionEntity.OUTPUT_LABELS_TRUTH );
        DataRefUtil.SetDataReference( classificationAnalysisName2, ClassificationAnalysisEntity.INPUT_PREDICTED, logisticRegressionName, LogisticRegressionEntity.OUTPUT_LABELS_PREDICTED );


        // 4) Set configurations
        // ---------------------------------------------
        PersistenceUtil.SetConfig( experimentName, experimentConfig );
        PersistenceUtil.SetConfig( analyticsName, analyticsEntityConfig );
        PersistenceUtil.SetConfig( svmName, svmEntityConfig);
        PersistenceUtil.SetConfig( logisticRegressionName, logisticRegressionEntityConfig );

        // cache all data for speed, when enabled (override this property in configs)
        PersistenceUtil.SetConfig( experimentName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( analyticsName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( svmName, "cache", String.valueOf( cacheAllData ) );
        PersistenceUtil.SetConfig( logisticRegressionName, "cache", String.valueOf( cacheAllData ) );
    }

}