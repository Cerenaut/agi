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
import java.util.*;

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
    public ArrayList< Float > _sortedLabels;
    public HashMap< Float, HashMap< Float, Integer > > _confusionMatrix;
    public HashMap< Float, Integer > _labelFrequency;
//    public HashMap< Float, Integer > _labelPredictions;
    public HashMap< Float, Integer > _labelErrorFP;
    public HashMap< Float, Integer > _labelErrorFN;
    public int _sampleOffset = 0;
    public int _sampleLength = 0;

    public int getErrorCount() {
        return (int)_errors.sum();
    }

    public int getSampleCount() {
        return _sampleLength; //_errors.getSize();
    }

    public float getErrorFraction() {
        float count = getErrorCount();
        float samples = getSampleCount();
        float pc = ( count / samples );
        return pc;
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
        _sortedLabels = new ArrayList< Float >();
        _sortedLabels.addAll( labels ); // all unique labels in order
        Collections.sort( _sortedLabels );

        // build an empty confusion matrix and other structures
        _confusionMatrix = new HashMap< Float, HashMap< Float, Integer > >();
        _labelFrequency = new HashMap< Float, Integer >();
//        _labelPredictions = new HashMap< Float, Integer >();
        _labelErrorFP = new HashMap< Float, Integer >();
        _labelErrorFN = new HashMap< Float, Integer >();

        for( Float f : labels ) {

            _labelFrequency.put( f, 0 );
//            _labelPredictions.put( f, 0 );
            _labelErrorFP.put( f, 0 );
            _labelErrorFN.put( f, 0 );

            HashMap< Float, Integer > hm = new HashMap< Float, Integer >();

            for( Float f2 : labels ) {
                hm.put( f2, 0 );
            }

            _confusionMatrix.put( f, hm );
        }

        // calculate errors
        _errors = new FloatArray( truth.getSize() );

        int i0 = offset;
        int i1 = offset + length;

        if( offset < 0 ) {
            return new String( "Bad offset: Negative." );
        }
        if( i1 > truth._values.length ) {
            return new String( "Bad offset/length combination: Out of range (size=" + truth._values.length + ")." );
        }

        // per class:
        // prec = tp / all pos in test.
        // recall = tp / truth pos.

        for( int i = i0; i < i1; ++i ) {
            float t = truth._values[ i ];
            float p = predicted._values[ i ];

            float error = 0f;
            if( t != p ) {
                error = 1f;

                HashMap< Float, Integer > hm = _confusionMatrix.get( t );

                Integer n1 = hm.get( p );
                int n2 = n1 + 1; // increment frequency

                hm.put( p, n2 );

                Integer n1p = _labelErrorFP.get( p );
                int n2p = n1p + 1; // increment frequency
                _labelErrorFP.put( p, n2p );

                Integer n1n = _labelErrorFN.get( t );
                int n2n = n1n + 1; // increment frequency
                _labelErrorFN.put( t, n2n );
            }
//            else {
            Integer n1 = _labelFrequency.get( t );
            int n2 = n1 + 1; // increment frequency
            _labelFrequency.put( t, n2 );
//            }

            _errors._values[ i ] = error;
        }

        return null; // no error
    }

    public String getResult() {
        // display stats.
//        float sum = _errors.sum();
//        float count = length;//errors.getSize();
//        float pc = 1f - ( sum / count );
        int errors = getErrorCount();
        int samples = getSampleCount();
        float pc = 1f - getErrorFraction();
        pc *= 100f;

        String result = "";
        result += "\n";
        result += ( "Errors: " + errors + " of " + samples + " = " + pc + "% correct." );
        result += "\n";
        result += ( "Confusion:" );
        result += "\n";

        result += ( "           <--- PREDICTED ---> \n" );
        result += ( "      " );
        for( Float fP : _sortedLabels ) {
            result += ( String.format( "%.1f", fP ) + " , " );
        }
        result += "\n";

        int w = 6; // todo make it number of digits in max( error-type-count ) + , + whatever padding needed
        int paddingChars = w - 2;

        String padding = "";
        for( int i = 0; i < paddingChars; ++i ) {
            padding = padding + " ";
        }

        for( Float fT : _sortedLabels ) {
            HashMap< Float, Integer > hm = _confusionMatrix.get( fT );

            result += ( " " + String.format( "%.1f", fT ) + ", "  );

            for( Float fP : _sortedLabels ) {

                int frequency = hm.get( fP );

                result += ( frequency + "," + padding );
            }

            result += "\n";
        }

        result += "\n";
        result += ( "F-Score:" );
        result += "\n";
        // per class:
        // prec = tp / all pos in test.
        // recall = tp / truth pos.

        result += ( " Label, Err, TP, FP, TN, FN, T, F, F-Score\n"  );

        float b2 = 0;

        for( Float label : _sortedLabels ) {

            int fp = _labelErrorFP.get( label );
            int fn = _labelErrorFN.get( label );
            int t = _labelFrequency.get( label );
            int f = samples - t;

            int tp = t-fn;
            int tn = f-fp;
            int e = fp + fn;
            float denominator = (1f + b2) * tp + b2 * fn + fp;
            float score = (1f + b2)* tp / denominator;

            result += ( String.format( "%.1f", label ) + ", " + e  + ", " + tp + ", " + fp + ", " + tn + ", " + fn + ", " + t + ", " + f + ", " + score );
            result += "\n";
        }

        return result;
    }
}
