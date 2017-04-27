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

package io.agi.framework.demo.mnist;

import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.demo.papers.ClassificationAnalysis;
import io.agi.framework.persistence.models.ModelEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by dave on 14/02/17.
 */
public class ClassificationAnalysisEntity extends Entity {

    public static final String ENTITY_TYPE = "classification-analysis";

    public static final String INPUT_TRUTH = "input-truth";
    public static final String INPUT_PREDICTED = "input-predicted";

    protected static final Logger _logger = LogManager.getLogger();

    public ClassificationAnalysisEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_TRUTH );
        attributes.add( INPUT_PREDICTED );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
    }

    @Override
    public Class getConfigClass() {
        return ClassificationAnalysisEntityConfig.class;
    }

    protected void doUpdateSelf() {
        Data truth = getData( INPUT_TRUTH );
        Data predicted = getData( INPUT_PREDICTED );
        if( truth == null || predicted == null ) {
            return;
        }

        ClassificationAnalysis ca = new ClassificationAnalysis();

        // Check for a reset (to start of sequence and re-train)
        ClassificationAnalysisEntityConfig config = ( ClassificationAnalysisEntityConfig ) _config;

        String errorMessage = ca.analyze( truth, predicted, config.sampleOffset, config.sampleLength );
        if( errorMessage != null ) {
            _logger.error( errorMessage );
            return;
        }

        // put results in log and config for easy collection
        String results = ca.getResult();
        _logger.info( results );
        if( config.resultsSummary == null ) {
            config.resultsSummary = "";
        }
        config.resultsSummary += results + "\n";

        // display stats.
        config.errorCount = ca.getErrorCount();
        config.errorFraction = ca.getErrorFraction();
        config.errorPercentage = config.errorFraction * 100;
        config.samples = ca.getSampleCount();

        config.sortedLabels.clear();
        for( Float label : ca._sortedLabels ) {
            config.sortedLabels.add( String.format( "%.0f", label ) );
        }

        config.confusionMatrix.clear();
        config.confusionMatrix.add( "" ); // first col, labels, is null
        for( Float label : ca._sortedLabels ) {
            config.confusionMatrix.add( String.valueOf( label ) ); // first row is labels
        }
        for( Float trueLabel : ca._sortedLabels ) {
            config.confusionMatrix.add( String.valueOf( trueLabel ) ); // first col, labels, is null
            HashMap< Float, Integer > trueLabelMap = ca._confusionMatrix.get( trueLabel );
            for( Float predictedLabel : ca._sortedLabels ) {
                config.confusionMatrix.add( String.valueOf( trueLabelMap.get( predictedLabel ) ) );
            }
        }

        // F score and other stats, per label
        config.labelStatistics.clear();
        for( Float label : ca._sortedLabels ) {
            ClassificationAnalysis.ClassificationStats labelStats = ca.new ClassificationStats(label);
            // TODO: these should be declared with the interfaces, not concrete classes
            // TODO: why use strings?
            HashMap< String, String > labelStatMap = new HashMap<>();
            labelStatMap.put( "label", String.valueOf( label ) );
            labelStatMap.put( "errors", String.valueOf( labelStats.getNumErrors() ) );
            labelStatMap.put( "tp", String.valueOf( labelStats.getNumTruePositives() ) );
            labelStatMap.put( "fp", String.valueOf( labelStats.getNumFalsePositives() ) );
            labelStatMap.put( "tn", String.valueOf( labelStats.getNumTrueNegatives() ) );
            labelStatMap.put( "fn", String.valueOf( labelStats.getNumFalseNegatives() ) );
            labelStatMap.put( "f-score", String.valueOf( labelStats.getFScore( config.betaSq ) ) );
            config.labelStatistics.put( String.valueOf( label ), labelStatMap );
        }
    }

}
