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

        if( truth == null ) {
            return;
        }

        if( predicted == null ) {
            return;
        }

        int samples = truth.getSize();

        if( predicted.getSize() != samples ) {
            _logger.error( "Truth and predicted vectors have different length." );
            return;
        }

        ClassificationAnalysis ca = new ClassificationAnalysis();

        // Check for a reset (to start of sequence and re-train)
        ClassificationAnalysisEntityConfig config = ( ClassificationAnalysisEntityConfig ) _config;

        int offset = config.sampleOffset;
        int length = config.sampleLength;

        if( length <= 0 ) {
            length = samples - offset; // all remaining samples
            length = Math.max( 0, length );
        }

        String errorMessage = ca.analyze( truth, predicted, offset, length );

        if( errorMessage != null ) {
            _logger.error( errorMessage );
            return;
        }

        // put results in log
        String results = ca.getResult();
        _logger.info( results );

        // display stats.
        int errors = ca.getErrorCount();
        float fraction = ca.getErrorFraction();
        float pc = fraction;//1f - fraction
        pc *= 100f;
        config.errorCount = errors;
        config.errorFraction = fraction;
        config.errorPercentage = pc;
        config.samples = samples;

        config.sortedLabels.clear();
        for( Float label : ca._sortedLabels ) {
            String labelString = String.format( "%.0f", label );
            config.sortedLabels.add( labelString );
        }

//        System.out.print( "      " );
//        for( Float fP : _sortedLabels ) {
//            System.out.print( String.format( "%.1f", fP ) + " , " );
//        }
//        System.out.println();

        config.confusionMatrix.clear();
        config.confusionMatrix.add( "" ); // first col, labels, is null

        for( Float f : ca._sortedLabels ) {
            config.confusionMatrix.add( String.valueOf( f ) ); // first row is labels
        }

        for( Float fT : ca._sortedLabels ) {

            config.confusionMatrix.add( String.valueOf( fT ) ); // first col, labels, is null

            HashMap< Float, Integer > hm = ca._confusionMatrix.get( fT );

            for( Float fP : ca._sortedLabels ) {

                int frequency = hm.get( fP );

                config.confusionMatrix.add( String.valueOf( frequency ) ); // first col, labels, is null
            }
        }

        // F score and other stats, per label
        config.labelStatistics.clear();

        float b2 = config.betaSq;

        for( Float label : ca._sortedLabels ) {
            HashMap< String, String > labelStatistics = new HashMap< String, String >();

            int fp = ca._labelErrorFP.get( label );
            int fn = ca._labelErrorFN.get( label );
            int t = ca._labelFrequency.get( label );
            int f = samples - t;

            int tp = t-fn;
            int tn = f-fp;
            int e = fp + fn;
            float denominator = (1f + b2) * tp + b2 * fn + fp;
            float score = (1f + b2)* tp / denominator;

            labelStatistics.put( "errors", String.valueOf( e ) );
            labelStatistics.put( "label", String.valueOf( label ) );
            labelStatistics.put( "tp", String.valueOf( tp ) );
            labelStatistics.put( "fp", String.valueOf( fp ) );
            labelStatistics.put( "tn", String.valueOf( tn ) );
            labelStatistics.put( "fn", String.valueOf( fn ) );
            labelStatistics.put( "f-score", String.valueOf( score ) );

            config.labelStatistics.put( String.valueOf( label ), labelStatistics ); // first row is labels
        }
    }

}
