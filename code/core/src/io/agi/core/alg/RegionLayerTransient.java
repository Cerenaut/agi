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

import io.agi.core.data.Ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * A wrapper for all the transient (don't persist from step to step) data structures in the RegionLayer class.
 *
 * This helps to check that they're not accidentally being used persistently, which would cause subtle bugs.
 *
 * Created by dave on 14/05/16.
 */
public class RegionLayerTransient {

    public HashSet< Integer > _ffInputActive;
    public HashSet< Integer > _fbInputActive;

    public ArrayList< Integer > _regionActiveCells = new ArrayList< Integer >();

    public HashMap< Integer, Integer > _classifierActiveCells = new HashMap< Integer, Integer >();
    public HashMap< Integer, ArrayList< Integer > > _classifierActiveInput = new HashMap< Integer, ArrayList< Integer > >();

    public HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > _activeInputClassifierRanking = new HashMap< Integer, TreeMap< Float, ArrayList< Integer > > >();
    public HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > _classifierActiveInputRanking = new HashMap< Integer, TreeMap< Float, ArrayList< Integer > > >();

    public RegionLayerTransient() {

    }

    public TreeMap< Float, ArrayList< Integer > > getRankingLazy( HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > rankingMap, int i ) {
        TreeMap< Float, ArrayList< Integer > > ranking = rankingMap.get( i );
        if( ranking == null ) {
            ranking = Ranking.CreateRanking();
            rankingMap.put( i, ranking );
        }
        return ranking;
    }

    public void addClassifierActiveInput( int classifier, int activeInput ) {
        ArrayList< Integer > al = _classifierActiveInput.get( classifier );
        if( al == null ) {
            al = new ArrayList< Integer >();
            _classifierActiveInput.put( classifier, al );
        }

        al.add( activeInput );
    }
//    _ffInputActive = null;
//    _fbInputActive = null;
//    _regionActive = null;
//    _ffInputActiveClassifier.clear();

}
