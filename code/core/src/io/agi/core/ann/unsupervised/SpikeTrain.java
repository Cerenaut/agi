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

import java.util.ArrayList;

/**
 * A temporary object to hold a history of spikes
 *
 * Created by dave on 22/07/16.
 */
public class SpikeTrain {


    public SpikeTrain() {

    }

    /**
     * Return all spikes at index after delay specified.
     *
     * @param index
     * @param delay
     * @param t
     * @return
     */
    public ArrayList< Float > getSpikeTimes( int index, float delay, float t ) {
        return null;
    }

    public void addSpike( float time ) {

    }

}
