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

package io.agi.core.ann.reinforcement;

import io.agi.core.data.Data;
import io.agi.core.data.Ranking;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dave on 19/05/17.
 */
public class EpsilonGreedyQLearningPolicy implements QLearningPolicy {

    public Random _r;
    public boolean _learn = true;
    public float _epsilon = 0;

//    public ArrayList< Integer > _selectionSetSizes = new ArrayList< Integer >();

//    public int _simultaneousActions = 1; // this is a major assist. Should be pseudorandom

    public EpsilonGreedyQLearningPolicy() {

    }

    /**
     * @param r
     * @param epsilon
     */
    public void setup( Random r, float epsilon ) {//, ArrayList< Integer > selectionSetSizes ) {
        _r = r;
        _epsilon = epsilon;
//        _simultaneousActions = simultaneousActions;
//        _selectionSetSizes.addAll( selectionSetSizes );
    }

    public void selectActions( Data state, Data actionQuality, Data action ) {
        // either we can select random action with epsilon probability and we can select an action with 1-epsilon
        // probability that gives maximum reward in given state

        // TODO initially just one action at a time?
        boolean random = false;
        if( _learn ) {
            float r = _r.nextFloat();
            if( r < _epsilon ) {
                random = true;
            }
        }

        action.set( 0f );

//        int actionOffset = 0;
//        int sets = _selectionSetSizes.size();
//
//        for( int i = 0; i < sets; ++i ) {
//            Integer setSize = _selectionSetSizes.get( i );
            Integer selection = null;
//
            if( random ) {
                selection = selectRandomActions( state, actionQuality );
            }
            else {
                selection = selectBestActions( state, actionQuality );
            }
//
            if( selection != null ) {
//                action._values[ actionOffset + selection ] = 1f;
                action._values[ selection ] = 1f;
            }

//            actionOffset += setSize;
//        }
    }

    public Integer selectRandomActions( Data state, Data actionQuality ) {//, int actionOffset, int actionSetSize ) {
        int nbrActions = actionQuality.getSize();
        int a = _r.nextInt( nbrActions );
        return a;
    }

    public Integer selectBestActions( Data state, Data actionQuality ) {//, int actionOffset, int actionSetSize ) {
        int nbrActions = actionQuality.getSize();
        Ranking r = new Ranking();

        for( int i = 0; i < nbrActions; ++i ) {
            int a = i;
            float q = actionQuality._values[ a ];
            Ranking.add( r._ranking, q, a ); // add input a with quality q
        }

        boolean max = true; // ie min
        int maxRank = 1;
        ArrayList< Integer > selectedActions = Ranking.getBestValues( r._ranking, max, maxRank ); // ok now we got the current set of inputs for the column

        Integer selected = selectedActions.get( 0 );
        return selected;
    }

}
