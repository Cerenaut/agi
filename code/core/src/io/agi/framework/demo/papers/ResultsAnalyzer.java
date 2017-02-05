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
import io.agi.core.util.PropertiesUtil;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by dave on 5/02/17.
 */
public class ResultsAnalyzer {

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
        if( args.length != 3 ) {
            System.err.println( "Bad arguments. Should be: data_file data_name_truth data_name_predicted" );
            System.exit( -1 );
        }

        String dataFile = args[ 0 ];
        String dataNameTruth = args[ 1 ];
        String dataNamePredicted = args[ 2 ];

        System.out.println( "Data file: " + dataFile );
        System.out.println( "Data name truth: " + dataNameTruth );
        System.out.println( "Data name predicted: " + dataNamePredicted );

        try {
            Gson gson = new Gson();

            String jsonData = FileUtil.readFile( dataFile );

            Type listType = new TypeToken< List< ModelData > >() {}.getType();

            List< ModelData > modelDatas = gson.fromJson( jsonData, listType );

            Data truth = null;
            Data predicted = null;

            for( ModelData modelData : modelDatas ) {
                if( modelData.name.equals( dataNameTruth ) ) {
                    System.err.println( "Found data: " + modelData.name );

                    truth = modelData.getData();
                }
                else if( modelData.name.equals( dataNamePredicted ) ) {
                    System.err.println( "Found data: " + modelData.name );

                    predicted = modelData.getData();
                }
                else {
                    System.err.println( "Skipping data: " + modelData.name );
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

            analyze( truth, predicted );
        }
        catch( Exception e ) {
            System.err.println( e.getStackTrace() );
            System.exit( -1 );
        }

    }

    public static void analyze( Data truth, Data predicted ) {
        //
        // F-score (precision+recall), confusion matrix
        // find unique labels in truth
        HashSet< Float > labels = Statistics.unique( truth );

        ArrayList< Float > sorted = new ArrayList< Float >();
        sorted.addAll( labels );
        Collections.sort( sorted );

        // build an empty confusion matrix
        HashMap< Float, HashMap< Float, Integer > > errorTypeCount = new HashMap< Float, HashMap< Float, Integer > >();

        for( Float f : labels ) {
            HashMap< Float, Integer > hm = new HashMap< Float, Integer >();

            for( Float f2 : labels ) {
                hm.put( f2, 0 );
            }

            errorTypeCount.put( f, hm );
        }

        // calculate errors
        FloatArray errors = new FloatArray( truth.getSize() );

        for( int i = 0; i < truth._values.length; ++i ) {
            float t = truth._values[ i ];
            float p = predicted._values[ i ];

            float error = 0f;
            if( t != p ) {
                error = 1f;

                HashMap< Float, Integer > hm = errorTypeCount.get( t );

                Integer n1 = hm.get( p );
                int n2 = n1 +1; // increment frequency

                hm.put( p, n2 );
            }

            errors._values[ i ] = error;
        }

        // display stats.
        float sum = errors.sum();
        float count = errors.getSize();
        float pc = 1f - ( sum / count );
        pc *= 100f;
        System.out.println();
        System.out.println( "Errors: " + (int)sum + " of " + (int)count + " = " + pc + "% correct." );
        System.out.println();
        System.out.println( "Confusion:" );
        System.out.println();

        System.out.println( "           <--- PREDICTED ---> " );
        System.out.print( "      " );
        for( Float fP : sorted ) {
            System.out.print( String.format( "%.1f", fP ) + " , " );
        }
        System.out.println();

        int w = 6; // todo make it number of digits in max( error-type-count ) + , + whatever padding needed
        int paddingChars = w - 2;

        String padding = "";
        for( int i = 0; i < paddingChars; ++i ) {
            padding = padding + " ";
        }

        for( Float fT : sorted ) {
            HashMap< Float, Integer > hm = errorTypeCount.get( fT );

            System.out.print( " " + String.format( "%.1f", fT ) + ", "  );

            for( Float fP : sorted ) {

                int frequency = hm.get( fP );

                System.out.print( frequency + "," + padding );
            }

            System.out.println();
        }

    }
}
