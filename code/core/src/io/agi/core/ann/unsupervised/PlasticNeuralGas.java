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
import io.agi.core.data.Ranking;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Basic concept:
 *
 * 1. Relate cell mobility / motility to age (how long it is disused)
 * 2. Pull mobile cells towards the most under-represented areas
 *
 * Created by dave on 29/12/15.
 */
public class PlasticNeuralGas extends CompetitiveLearning {

    public PlasticNeuralGasConfig _c;
    public ArrayList< Integer > _sparseUnitInput;
    public Data _inputValues;
    public Data _cellWeights;
    public Data _cellErrors;
    public Data _cellActivity;
    public Data _cellMask;
    public Data _cellAges;
    public Data _cellStress;

    protected int _bestCell = 0;

    public PlasticNeuralGas( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( PlasticNeuralGasConfig c ) {
        _c = c;

        int inputs = c.getNbrInputs();
        int w = c.getWidthCells();
        int h = c.getHeightCells();

        _inputValues = new Data( inputs );
        _cellWeights = new Data( w, h, inputs );
        _cellErrors = new Data( w, h );
        _cellActivity = new Data( w, h );
        _cellMask = new Data( w, h );
        _cellAges = new Data( w, h );
        _cellStress = new Data( w, h );
    }

    public void reset() {
        _cellWeights.setRandom( _c._r );
        _cellMask.set( 1.f ); // enable all cells
        _cellAges.set( 1.f ); // enable all cells
        _cellStress.set( 0.f ); // enable all cells
    }

    public void update() {

        boolean learn = _c.getLearn();
        float stressLearningRate = _c.getStressLearningRate(); //0.01f;
        float rankLearningRate = _c.getRankLearningRate();
        float rankScale = _c.getRankScale();
        int maxAge = (int)_c.getAgeMax();
        float ageDecay = _c.getAgeDecay(); //0.7f;
        float ageScale = _c.getAgeScale();

        // Compute error
        ArrayList< Integer > liveCells = createCellList( _c, _cellMask );
        ArrayList< Integer > sparseUnitInput = getSparseUnitInput();
        if( sparseUnitInput != null ) {
            // Sparse input
            sumSqErrorSparseUnit( _c, liveCells, sparseUnitInput, _cellWeights, _cellErrors );
        } else {
            // Dense input
            sumSqError( _c, liveCells, _inputValues, _cellWeights, _cellErrors );
        }

        // Rank all cells
        int maxRank = _c.getNbrCells() +1; // keep em all
        boolean findMaxima = false; // find minima

        TreeMap< Float, ArrayList< Integer > > ranking1 = new TreeMap< Float, ArrayList< Integer > >();
        findBestNCells( _c, _cellMask, _cellErrors, _cellActivity, maxRank, findMaxima, ranking1 ); // activity is whether ranked
        ArrayList< Integer > bestValues1 = Ranking.getBestValues( ranking1, findMaxima, maxRank );

        int bestCell1 = bestValues1.get( 0 );

        float relativeStress = getRelativeStress( bestCell1, liveCells );

        // Re-rank all cells with promotion of old cells
        promoteOldCells( liveCells, maxAge, ageScale, relativeStress );

        TreeMap< Float, ArrayList< Integer > > ranking2 = new TreeMap< Float, ArrayList< Integer > >();
        findBestNCells( _c, _cellMask, _cellErrors, _cellActivity, maxRank, findMaxima, ranking2 ); // activity is whether ranked
        ArrayList< Integer > bestValues2 = Ranking.getBestValues( ranking2, findMaxima, maxRank );

        _bestCell = bestValues2.get( 0 );

        _cellActivity.set( 0.f );
        _cellActivity._values[ _bestCell ] = 1.f;

        if( !learn ) {
            return;
        }

        float bestError = _cellErrors._values[ _bestCell ];
        float rootBestError = (float)Math.sqrt( bestError );

////            float ageScale = 17.0f; // how nonlinear it is. Should be a param
//            float ageScale = 12.0f; // how nonlinear it is. Should be a param
        updateStress( _bestCell, rootBestError, stressLearningRate );

        int inputs = _c.getNbrInputs();
        int w = _c.getWidthCells();
        int h = _c.getHeightCells();
        int cells = w * h;
        int ranks = bestValues2.size();

        for( int rank = 0; rank < ranks; ++rank ) {
            int cell = bestValues2.get( rank );

            updateAge( cell, cells, rank, maxAge, ageDecay );

//            float age = _cellAges._values[ cell ]; // after update

            if( sparseUnitInput != null ) {
                // Sparse input
                for( int i = 0; i < inputs; ++i ) {

                    float inputValue = 0.f;
                    if( sparseUnitInput.contains( i ) ) {
                        inputValue = 1.f;
                    }

                    updateWeight( cell, inputs, i, rank, inputValue, rootBestError, rankLearningRate, rankScale );
                }
            }
            else {
                // Dense input
                for( int i = 0; i < inputs; ++i ) {

                    float inputValue = _inputValues._values[ i ];

                    updateWeight( cell, inputs, i, rank, inputValue, rootBestError, rankLearningRate, rankScale );
                }
            }
        }
    }

    protected void promoteOldCells( Collection< Integer > cells, int maxAge, float ageScale, float relativeStress ) {
        // old cells
        // as cells age, their error becomes smaller (they become more sensitive)
        // when very old, they become hypersensitive
        // They will eventually out-compete other cells and become winners in their own right
        // http://www.wolframalpha.com/input/?i=plot+1-e%5E(-17(1-x))+for+x+%3D+0.6+to+1
        float stressFactor = relativeStress;

        for( Integer cell : cells ) {
            float age = _cellAges._values[ cell ];
////            float ageScale = 17.0f; // how nonlinear it is. Should be a param
//            float ageScale = 12.0f; // how nonlinear it is. Should be a param
            float unitAge = age / (float)maxAge; // 1 iff max age

            // Without
//            float factor = (float)( 1.0 - Math.exp( -ageScale * ( 1.0f - unitAge ) ) ); // so == 1 if young, close to zero if old
            float factor = (float)( Math.exp( -ageScale * ( 1.0f - unitAge ) ) ); // 1 if old, zero if young

            // The comment below allows debugging to target highly motile cells
//            if( (int)age == 200 ) {
//                if( stressFactor > 0.9f ) {
//                    int g = 0;
//                    g++;
//                }
//            }
            factor *= stressFactor; // 1 if old and current cell is stressed. So if stress is low, cells will not be promoted.
            factor = 1.f - factor;
            float oldError = _cellErrors._values[ cell ]; // since inputs are unit, max error = 1
            float newError = oldError * factor;

            _cellErrors._values[ cell ] = newError;
        }
    }

    protected float getRelativeStress( int bestCell, Collection< Integer > cells ) {
//        float sumStress = 0.f;
        float bestStress = 0.f;
        float maxStress = 0.f;

        for( Integer cell : cells ) {
            float stress = _cellStress._values[ cell ];
            if( cell.equals( bestCell ) ) {
                bestStress = stress;
            }
//            sumStress += stress;
            maxStress = Math.max( stress, maxStress );
        }

        if( maxStress <= 0.f ) {
            return 0.f;
        }

//        float relativeStress = bestStress / sumStress;
        float relativeStress = bestStress / maxStress;
        return relativeStress;
    }

    protected void updateStress( int cell, float error, float stressLearningRate ) {
        float b = stressLearningRate;
        float a = 1.f - b;
        float oldStress = _cellStress._values[ cell ];
        float newStress = oldStress * a + error * b;
        _cellStress._values[ cell ] = newStress;
    }

    protected void updateAge( int cell, int cells, int rank, int maxAge, float ageDecay ) {
        float oldAge = _cellAges._values[ cell ];
        float newAge = Math.min( oldAge +1, maxAge );
        if( cell == _bestCell ) {
            newAge = oldAge * ageDecay;
        }

        _cellAges._values[ cell ] = newAge;
    }


    protected void updateWeight( int cell, int inputs, int i, int rank, float inputValue, float rootBestError, float learningRate, float neighbourhoodScale ) {// float minDistance ) {
//        float rankDistance = 1.f - minDistance;

        // http://www.wolframalpha.com/input/?i=plot+e%5E(-7(1-x))+for+x+%3D+0+to+1
        // http://www.wolframalpha.com/input/?i=plot+e%5E(-17(1-x))+for+x+%3D+0.6+to+1
//        float ageScale = 7.0f; // how nonlinear it is. Should be a param
//        float unitAge = (float)age / (float)maxAge; // 1 iff max age
//        unitAge = 1.f - Math.min( 1.f, unitAge ); // 1 iff min age
//        float ageWeight = (float)Math.exp( - ageScale * unitAge ); // 1 iff max age

        float neighbourhoodFactor = neighbourhoodScale * rootBestError; // allows convergence because when error = 0, the system doesn't pull other cells
        float rankWeight = 0.f;
        if( neighbourhoodFactor > 0.f ) {
            rankWeight = (float)Math.exp( - rank / neighbourhoodFactor );
        }

        // rank = 0
        // e^( - 0/(5*1  ) ) = e^ (-0.2 ) = 1
        // e^( - 0/(5*0.5) ) = e^ (-0.4 ) = 1
        // e^( - 0/(5*0.1) ) = e^ (-0.4 ) = 1

        // rank = 1
        // e^( - 1/(5*1  ) ) = e^ (-0.2 ) = 0.81   error = 1.0
        // e^( - 1/(5*0.5) ) = e^ (-0.4 ) = 0.67   error = 0.5
        // e^( - 1/(5*0.1) ) = e^ (-0.4 ) = 0.13   error = 0.1
        // e^( - 1/(5*0.01) ) = e^ (-0.4 ) = 0.0   error = 0.01

        float motility = rankWeight;
        int offset = cell * inputs + i;
        float weightOld = _cellWeights._values[ offset ];
        float signedError = ( inputValue - weightOld );
        float weightNew = weightOld + learningRate * motility * signedError;
        _cellWeights._values[ offset ] = weightNew;
    }

    public int getBestCell() {
        return _bestCell;
    }

    public ArrayList< Integer > getSparseUnitInput() {
        return _sparseUnitInput;
    }

    public void setSparseUnitInput( ArrayList< Integer > inputIndices ) {
        _sparseUnitInput = inputIndices;
    }

    public Data getInput() {
        return _inputValues;
    }

}
