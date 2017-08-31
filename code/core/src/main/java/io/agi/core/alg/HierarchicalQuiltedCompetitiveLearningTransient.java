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

package io.agi.core.alg;

import io.agi.core.data.Data;
import io.agi.core.data.Ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Created by dave on 22/10/16.
 */
public class HierarchicalQuiltedCompetitiveLearningTransient {

    public HashSet< Integer > _ffInput1Active;
    public HashSet< Integer > _ffInput2Active;
    public HashSet< Integer > _fbInputActive;

    public HashSet< Integer > _ffInput1ActiveOld;
    public HashSet< Integer > _ffInput2ActiveOld;
    public HashSet< Integer > _fbInputActiveOld;

    public ArrayList< Integer > _regionActiveCells = new ArrayList< Integer >();

    public HashMap< Integer, HashSet< Integer > > _classifierRegionCells = new HashMap< Integer, HashSet< Integer > >();

    public HashMap< Integer, ArrayList< Integer > > _classifierActiveInput = new HashMap< Integer, ArrayList< Integer > >();
    public HashMap< Integer, ArrayList< Integer > > _classifierActiveInputOld = new HashMap< Integer, ArrayList< Integer > >();

    public HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > _activeInputClassifierRanking = new HashMap< Integer, TreeMap< Float, ArrayList< Integer > > >();
    public HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > _activeInputClassifierRankingOld = new HashMap< Integer, TreeMap< Float, ArrayList< Integer > > >();
//    public HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > _classifierActiveInputRanking = new HashMap< Integer, TreeMap< Float, ArrayList< Integer > > >();

    public HierarchicalQuiltedCompetitiveLearningTransient() {

    }

    public static TreeMap< Float, ArrayList< Integer > > getRankingLazy( HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > rankingMap, int i ) {
        TreeMap< Float, ArrayList< Integer > > ranking = rankingMap.get( i );
        if( ranking == null ) {
            ranking = Ranking.CreateRanking();
            rankingMap.put( i, ranking );
        }
        return ranking;
    }

    public ArrayList< Integer > getClassifierActiveInput( int classifier ) {
        ArrayList< Integer > activeInput = _classifierActiveInput.get( classifier );
        if( activeInput == null ) {
            return new ArrayList< Integer >();
        }
        return activeInput;
    }

    public ArrayList< Integer > getClassifierActiveInputOld( int classifier ) {
        ArrayList< Integer > activeInput = _classifierActiveInputOld.get( classifier );
        if( activeInput == null ) {
            return new ArrayList< Integer >();
        }
        return activeInput;
    }

    public static void addClassifierActiveInput( int classifier, int activeInput, HashMap< Integer, ArrayList< Integer > > classifierActiveInput ) {
        ArrayList< Integer > al = classifierActiveInput.get( classifier );
        if( al == null ) {
            al = new ArrayList< Integer >();
            classifierActiveInput.put( classifier, al );
        }

        al.add( activeInput );
    }

}
