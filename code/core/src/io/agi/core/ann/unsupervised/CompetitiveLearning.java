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

package io.agi.core.ann.unsupervised;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray;
import io.agi.core.data.Ranking;
import io.agi.core.orm.Callback;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Utilities for functions shared by many Competitive Learning methods.
 * Created by dave on 29/12/15.
 */
public abstract class CompetitiveLearning extends NamedObject implements Callback {

    public CompetitiveLearning( String name, ObjectMap om ) {
        super( name, om );
    }

    public abstract void reset();
    public abstract void update();

    public void call() {
        update();
    }

    public ArrayList< Integer > createCellList(
            CompetitiveLearningConfig c,
            FloatArray cellMask ) {

        ArrayList< Integer > cellList = new ArrayList<>();

        int cells = c.getNbrCells();

        for( int cell = 0; cell < cells; ++cell ) { // for each som cell
            float maskValue = cellMask._values[ cell ];

            if( maskValue == 0.f ) {
                continue;
            }

            cellList.add( cell );
        }

        return cellList;
    }

    public static Data invert( int cell, DataSize inputSize, Data cellMask, Data cellWeights ) {
        Data inputValues = new Data( inputSize );

        float m = cellMask._values[ cell ];
        if( m > 0 ) {
            int inputs = inputValues.getSize();
            for( int i = 0; i < inputs; ++i ) { // for each input
                float weight = cellWeights._values[ cell * inputs + i ]; // error from ci to cell
                inputValues._values[ i ] = weight; // error from ci to cell
            }
        }

        return inputValues;
    }

    public static void sumSqError(
            CompetitiveLearningConfig c,
            FloatArray inputValues,
            FloatArray cellWeights,  // Size = cells * inputs
            FloatArray cellSumSqError ) { // size = cells

        int cells = c.getNbrCells();

        ArrayList< Integer > cellList = new ArrayList<>();

        for( int cell = 0; cell < cells; ++cell ) { // for each som cell
            cellList.add( cell );
        }

        sumSqError( c, cellList, inputValues, cellWeights, cellSumSqError );
    }

    public static void sumSqError(
            CompetitiveLearningConfig c,
            ArrayList< Integer > cells,
            FloatArray inputValues,
            FloatArray cellWeights,  // Size = cells * inputs
            FloatArray cellSumSqError ) { // size = cells

        int inputs = c.getNbrInputs();

        for( Integer cell : cells ) {

            // set all inputs to off, then recalculate based on som vector properties.
            float sumSqError = 0.f;

            for( int i = 0; i < inputs; ++i ) { // for each input

                float input = inputValues._values[ i ]; // error from ci to cell
                float weight = cellWeights._values[ cell * inputs + i ]; // error from ci to cell
                float diff = input - weight;

                sumSqError += ( diff * diff );
            }

            cellSumSqError._values[ cell ] = sumSqError;
        }
    }

    public static void sumSqErrorSparseUnit(
            CompetitiveLearningConfig c,
            ArrayList< Integer > cells,
            ArrayList< Integer > inputValues, // assume
            FloatArray cellWeights,  // Size = cells * inputs
            FloatArray cellSumSqError ) { // size = cells

        int inputs = c.getNbrInputs();

        for( Integer cell : cells ) {

            // set all inputs to off, then recalculate based on som vector properties.
            float sumSqError = 0.f;

            // We assume there are few inputs that are nonzero.
            // first sum all the weights and square them
            for( int i = 0; i < inputs; ++i ) { // for each input

                //float input       = inputValues         ._values[ i ]; // error from ci to cell
                float weight = cellWeights._values[ cell * inputs + i ]; // error from ci to cell
                float diff = 0 - weight;

                sumSqError += ( diff * diff );
            }

            // now go through the active ones and replace with the correct errors:
            for( Integer i : inputValues ) {
                float input = 1.f;
                float weight = cellWeights._values[ cell * inputs + i ]; // error from ci to cell

                float oldDiff = 0 - weight;
                float newDiff = input - weight;

                sumSqError -= ( oldDiff * oldDiff );
                sumSqError += ( newDiff * newDiff );
            }

            cellSumSqError._values[ cell ] = sumSqError;
        }
    }

    public static void findBestNCells(
            CompetitiveLearningConfig c,
            FloatArray cellMask,  // Size = cells
            FloatArray cellValues, // ie less is better
            FloatArray cellRanked, // a 1 if within top N ranks
            int maxRank, // max rank to keep, 0 based
            boolean findMaxima, // if false, rank minima
            TreeMap< Float, ArrayList< Integer > > ranking ) {
        int w = c.getWidthCells();
        int h = c.getHeightCells();
        findBestNCells( w, h, cellMask, cellValues, cellRanked, maxRank, findMaxima, ranking );
    }

    public static void findBestNCells(
            int w,
            int h,
            FloatArray cellMask,  // Size = cells
            FloatArray cellValues, // ie less is better
            FloatArray cellRanked, // a 1 if within top N ranks
            int maxRank, // max rank to keep, 0 based
            boolean findMaxima, // if false, rank minima
            TreeMap< Float, ArrayList< Integer > > ranking ) {

        ranking.clear();

        for( int y = 0; y < h; ++y ) { // for each som cell
            for( int x = 0; x < w; ++x ) { // for each som cell
                int cell = y * w + x;

                if( cellMask != null ) {
                    if( cellMask._values[ cell ] < 1.f ) { // not a live cell
                        continue; // not a live cell
                    }
                }

                // add each element, then prune the sorted list back down to size.
                float value = cellValues._values[ cell ];

                if( findMaxima ) {
                    if( value <= 0.f ) {
                        continue;
                    }
                }

                Ranking.add( ranking, value, cell );

                // prune the ranked list back down to size
                Ranking.truncate( ranking, maxRank, findMaxima );
            }
        }

        // now go through and set the winning values mask for each winner:
        for( int y = 0; y < h; ++y ) { // for each som cell
            for( int x = 0; x < w; ++x ) { // for each som cell
                int cell = y * w + x;

                if( cellMask != null ) {
                    if( cellMask._values[ cell ] < 1.f ) { // not a live cell
                        continue; // not a live cell
                    }
                }

                float ranked = 0.f;

                if( Ranking.containsValue( ranking, cell ) ) {
                    ranked = 1.f;
                }

                cellRanked._values[ cell ] = ranked;
            }
        }
    }
}
