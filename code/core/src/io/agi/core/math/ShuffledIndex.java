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

package io.agi.core.math;

import io.agi.core.util.images.BufferedImageSource.BufferedImageSourceImageFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Lazily builds and maintains a list of shuffled indices.
 *
 * Created by dave on 28/12/16.
 */
public class ShuffledIndex {

    public ArrayList< Integer > _shuffled = null;
    public long _shuffleSeed = 0;

    /**
     * Maintains a static list (for performance) of a set of images.  The list is shuffled.
     * @param bis
     * @param shuffleSeed
     * @param shuffleIndex
     * @return
     */
    public int getShuffledIndexLazy( int size, long shuffleSeed, int shuffleIndex ) {
        if( !valid( size, shuffleSeed ) ) { //( _shuffleSeed != shuffleSeed ) || ( _shuffled == null ) ) {
            shuffle( size, shuffleSeed );
        }

        return getShuffledIndex( shuffleIndex );
    }

    public boolean valid( int size, long shuffleSeed ) {
        if( _shuffled == null ) {
            return false;
        }
        if( _shuffled.size() != size ) {
            return false;
        }
        if( _shuffleSeed != shuffleSeed ) {
            return false;
        }
        return true;
    }

    public int getShuffledIndex( int shuffleIndex ) {
        return _shuffled.get( shuffleIndex );
    }

    /**
     * Re-generate the shuffled list
     * @param size
     * @param shuffleSeed
     */
    public void shuffle( int size, long shuffleSeed ) {
        ArrayList<Integer> sorted = new ArrayList<Integer>();

        for( int i = 0; i < size; ++i ) {
            sorted.add(i);
        }

        Random r = new Random();
        r.setSeed( shuffleSeed );

        Collections.shuffle(sorted, r);

        _shuffled = sorted;
        _shuffleSeed = shuffleSeed;
    }
}
