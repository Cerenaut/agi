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

package io.agi.framework.entities.reinforcement_learning;

import io.agi.framework.EntityConfig;

import java.util.ArrayList;

/**
 * Created by dave on 2/06/17.
 */
public class EpsilonGreedyEntityConfig extends EntityConfig {

    public float epsilon = 0f;
//    public int simultaneousActions = 0;
    public String selectionSetSizes = "0";

//    public boolean selectNone = false;

    public int getNbrSelectionSets() {
        String[] sizes = selectionSetSizes.split( "," );
        return sizes.length;
    }

    public Integer getSelectionSetSizes( int set ) {
        String[] sizes = selectionSetSizes.split( "," );
        String size = sizes[ set ];
        return Integer.valueOf( size );
    }

    public void setSelectionSetSizes( ArrayList< Integer > selectionSetSizes ) {
        String s = "";

        for( int i = 0; i < selectionSetSizes.size(); ++i ) {
            if( i > 0 ) {
                s = s + ",";
            }
            int size = selectionSetSizes.get( i );
            s = s + size;
        }

        this.selectionSetSizes = s;
    }

    public ArrayList< Integer > getSelectionSetSizes() {

        ArrayList< Integer > setSizes = new ArrayList<>();

        String[] sizes = selectionSetSizes.split( "," );

        for( String size : sizes ) {
            Integer n = Integer.valueOf( size );
            setSizes.add( n );
        }

        return setSizes;
    }
}

