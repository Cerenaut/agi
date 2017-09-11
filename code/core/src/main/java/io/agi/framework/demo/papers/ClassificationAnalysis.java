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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.agi.core.data.Data;
import io.agi.core.data.FloatArray;
import io.agi.core.math.Statistics;
import io.agi.core.util.FileUtil;
import io.agi.framework.persistence.models.ModelData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by dave on 5/02/17.
 */
public class ClassificationAnalysis {

    /**
     * Usage: Expects some arguments. These are:
     * 0: data file (json)
     * 1: truth data name (actual, correct labels)
     * 2: predicted labels data name (i.e. predicted label output)
     *
     * It is expected both data specified are vectors of the same size.
     *
     * @param args
     */
    public static void main( String[] args ) {

        // Optionally set a global prefix for entities
        if( args.length != 5 ) {
            System.err.println( "Bad arguments. Should be: data_file data_name_truth data_name_predicted OFFSET LENGTH " );
            System.exit( -1 );
        }

        String dataFile = args[ 0 ];
        String dataNameTruth = args[ 1 ];
        String dataNamePredicted = args[ 2 ];
        int offset = Integer.parseInt( args[ 3 ] );
        int length = Integer.parseInt( args[ 4 ] );

        System.out.println( "Data file: " + dataFile );
        System.out.println( "Data name truth: " + dataNameTruth );
        System.out.println( "Data name predicted: " + dataNamePredicted );
        System.out.println( "Data offset: " + offset );
        System.out.println( "Data length: " + length );

        try {
            Gson gson = new Gson();

            String jsonData = FileUtil.readFile( dataFile );

            Type listType = new TypeToken< List< ModelData > >() {}.getType();

            List< ModelData > modelDatas = gson.fromJson( jsonData, listType );

            Data truth = null;
            Data predicted = null;

            for( ModelData modelData : modelDatas ) {
                if( modelData.name.equals( dataNameTruth ) ) {
                    System.out.println( "Found data: " + modelData.name );

                    truth = modelData.getData();
                }
                else if( modelData.name.equals( dataNamePredicted ) ) {
                    System.out.println( "Found data: " + modelData.name );

                    predicted = modelData.getData();
                }
                else {
                    System.out.println( "Skipping data: " + modelData.name );
                }
            }

            if( truth == null ) {
                System.err.println( "Couldn't find truth labels data." );
            }

            if( predicted == null ) {
                System.err.println( "Couldn't find predicted labels data." );
            }

            if( ( truth == null ) || ( predicted == null ) ) {
                System.exit( -1 );
            }

            ClassificationAnalysis ca = new ClassificationAnalysis();

            String errorMessage = ca.analyze( truth, predicted, offset, length );
            if( errorMessage != null ) {
                System.err.println( errorMessage );
                System.exit( -1 );
            }

            String results = ca.getResult();
            System.out.println( results );
        }
        catch( Exception e ) {
            System.err.println( e.toString() );
            System.exit( -1 );
        }

    }

    public FloatArray _errors;
    public ArrayList< Integer > _sortedLabels;
    public HashMap< Integer, HashMap< Integer, Integer > > _confusionMatrix;
    public HashMap< Integer, Integer > _labelFrequency;
//    public HashMap< Float, Integer > _labelPredictions;
    public HashMap< Integer, Integer > _labelErrorFP;
    public HashMap< Integer, Integer > _labelErrorFN;
    public int _sampleOffset = 0;
    public int _sampleLength = 0;

    /**
     * Centralize way to convert labels from float to integer.
     * For now let's assume we round them.
     * @param r
     * @return
     */
    public static int LabelFloat2int( Float r ) {
        float r2 = r;
        //int n = (int)r2;
        int n = Math.round( r2 );
        return n;
    }

    public int getErrorCount() {
        return (int)_errors.sum();
    }

    public int getSampleCount() {
        return _sampleLength; //_errors.getSize();
    }

    public float getErrorFraction() {
        return getErrorCount() / (float) getSampleCount();
    }

    public String analyze( Data truth, Data predicted, int offset, int length ) {

        int samples = truth.getSize();

        if( predicted.getSize() != samples ) {
            return "Truth and predicted vectors have different length.";
        }

        if( length <= 0 ) {
            length = samples - offset; // all remaining samples
            length = Math.max( 0, length );
        }

        _sampleOffset = offset;
        _sampleLength = length;

        // F-score (precision+recall), confusion matrix
        // find unique labels in truth
        HashSet< Float > labels = Statistics.unique( truth );
        _sortedLabels = new ArrayList< Integer >();
        //_sortedLabels.addAll( labels ); // all unique labels in order
        for( Float r : labels ) {
            _sortedLabels.add( LabelFloat2int( r ) );
        }
        Collections.sort( _sortedLabels );

        // build an empty confusion matrix and other structures
        _confusionMatrix = new HashMap< Integer, HashMap< Integer, Integer > >();
        _labelFrequency = new HashMap< Integer, Integer >();
//        _labelPredictions = new HashMap< Float, Integer >();
        _labelErrorFP = new HashMap< Integer, Integer >();
        _labelErrorFN = new HashMap< Integer, Integer >();

        for( Float f : labels ) {

            int l = LabelFloat2int( f );
            _labelFrequency.put( l, 0 );
//            _labelPredictions.put( f, 0 );
            _labelErrorFP.put( l, 0 );
            _labelErrorFN.put( l, 0 );

            HashMap< Integer, Integer > hm = new HashMap< Integer, Integer >();

            for( Float f2 : labels ) {
                int l2 = LabelFloat2int( f2 );
                hm.put( l2, 0 );
            }

            _confusionMatrix.put( l, hm );
        }

        // calculate errors
        _errors = new FloatArray( truth.getSize() );

        int i0 = offset;
        int i1 = offset + length;

        if( offset < 0 ) {
            return "Bad offset: Negative.";
        }
        if( i1 > truth._values.length ) {
            return "Bad offset/length combination: Out of range (size=" + truth._values.length + ").";
        }

        // per class:
        // prec = tp / all pos in test.
        // recall = tp / truth pos.

        for( int i = i0; i < i1; ++i ) {
            float rt = truth._values[ i ];
            float rp = predicted._values[ i ];

            int t = LabelFloat2int( rt );
            int p = LabelFloat2int( rp );

            HashMap< Integer, Integer > hm = _confusionMatrix.get( t );
            Integer np = hm.get( p );

            float error = 0f;
            if( t != p ) { // error
                error = 1f;

                int n2 = np + 1; // increment frequency

                hm.put( p, n2 );

                Integer n1p = _labelErrorFP.get( p );
                int n2p = n1p + 1; // increment frequency
                _labelErrorFP.put( p, n2p );

                Integer n1n = _labelErrorFN.get( t );
                int n2n = n1n + 1; // increment frequency
                _labelErrorFN.put( t, n2n );
            }
            else { // correct
                int n2 = np + 1; // increment frequency

                hm.put( p, n2 );
            }

            Integer n1 = _labelFrequency.get( t );
            int n2 = n1 + 1; // increment frequency
            _labelFrequency.put( t, n2 );

            _errors._values[ i ] = error;
        }

        return null; // no error
    }

    public static class ClassificationStats {
        private final int numSamples;
        private final int numFalsePositives;
        private final int numFalseNegatives;
        private final int numPositives;

        public ClassificationStats( int numSamples, int numFalsePositives, int numFalseNegatives, int numPositives ) {
            this.numSamples = numSamples;
            this.numFalsePositives = numFalsePositives;
            this.numFalseNegatives = numFalseNegatives;
            this.numPositives = numPositives;
        }

        public int getNumFalsePositives() {
            return numFalsePositives;
        }

        public int getNumFalseNegatives() {
            return numFalseNegatives;
        }

        public int getNumPositives() {
            return numPositives;
        }

        public int getNumNegatives() {
            return numSamples - numPositives;
        }

        public int getNumTruePositives() {
            // TP = I predicted a 1, and it was a 1.
            // If it was a 1 and I predicted 0, it is a FN error.
            // If it was a 0 and I predicted 1, it is a FP error.
            // So TP = T - FN
            //    TN = N - FP
            return numPositives - numFalseNegatives;//numFalsePositives;
        }

        public int getNumTrueNegatives() {
            return getNumNegatives() - numFalsePositives;//numFalseNegatives;
        }

        public int getNumErrors() {
            return numFalsePositives + numFalseNegatives;
        }

        public float calculateFScore( float betaSquared ) {
            float denominator = ( 1f + betaSquared ) * getNumTruePositives() +
                                    betaSquared * numFalseNegatives +
                                    numFalsePositives;
            return denominator == 0 ? 0 : ( 1f + betaSquared ) * getNumTruePositives() / denominator;
        }
    }

    public ClassificationStats getClassificationStats( Integer label ) {
        return new ClassificationStats( getSampleCount(),
                                        _labelErrorFP.get( label ),
                                        _labelErrorFN.get( label ),
                                        _labelFrequency.get( label ) );
    }

    public enum FScoreAverageType { MICRO, MACRO };

    public float calculateFScore( float betaSquared, FScoreAverageType averageType ) {
        switch( averageType ) {
            case MICRO:
                int numFalsePositives = 0;
                int numFalseNegatives = 0;
                for( Integer label : _sortedLabels ) {
                    numFalsePositives += _labelErrorFP.get( label );
                    numFalseNegatives += _labelErrorFN.get( label );
                }
                return new ClassificationStats( getSampleCount(),
                                                numFalsePositives,
                                                numFalseNegatives,
                                                getSampleCount() ).calculateFScore( betaSquared );
            case MACRO:
                float sumFScores = 0;
                for ( Integer label : _sortedLabels ) {
                    sumFScores += getClassificationStats( label ).calculateFScore( betaSquared );
                }
                return sumFScores / _sortedLabels.size();
        }
        throw new IllegalArgumentException( "Unsupported averageType: " + averageType );
    }

    public String getResult() {
        StringBuilder result = new StringBuilder();
        result.append( "\nErrors: " ).append( getErrorCount() )
              .append( " of " ).append( getSampleCount() )
              .append( " = " ).append( ( 1f - getErrorFraction() ) * 100 ).append( "% correct." );
        result.append( "\nConfusion:\n           <--- PREDICTED ---> \n   " );
        for( Integer label : _sortedLabels ) {
            result.append( String.format( " %6d", label ) );
        }
        result.append( "\n" );
        for( Integer trueLabel : _sortedLabels ) {
            HashMap< Integer, Integer > trueLabelClassificationCounts = _confusionMatrix.get( trueLabel );
            result.append( String.format( "%6d", trueLabel ) );
            for( Integer predictedLabel : _sortedLabels ) {
                result.append( String.format( " %6d", trueLabelClassificationCounts.get( predictedLabel ) ) );
            }
            result.append( "\n" );
        }

        result.append( String.format( "\nF-Score:\n%-6s %6s %6s %6s %6s %6s %6s %6s %8s\n",
                                      "Label",
                                      "Err",
                                      "TP",
                                      "FP",
                                      "TN",
                                      "FN",
                                      "T",
                                      "F",
                                      "F-Score" ) );
        for( Integer label : _sortedLabels ) {
            ClassificationStats labelStats = getClassificationStats( label );
            result.append( String.format( "%-6d %6d %6d %6d %6d %6d %6d %6d %8.4f\n",
                                          label,
                                          labelStats.getNumErrors(),
                                          labelStats.getNumTruePositives(),
                                          labelStats.getNumFalsePositives(),
                                          labelStats.getNumTrueNegatives(),
                                          labelStats.getNumFalseNegatives(),
                                          labelStats.getNumPositives(),
                                          labelStats.getNumNegatives(),
                                          labelStats.calculateFScore( 0 ) ) );
        }

        result.append( String.format( "\nOverall F-Score: %.4f (micro) %.4f (macro)\n",
                                      calculateFScore( 0, FScoreAverageType.MICRO ),
                                      calculateFScore( 0, FScoreAverageType.MACRO ) ) );

        return result.toString();
    }
}
