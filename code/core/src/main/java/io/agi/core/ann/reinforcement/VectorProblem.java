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

import java.util.Random;

/**
 * Defines a Reward based on max error in a multi-class vector matching problem. The most erroneous value determines the
 * Reward value for the whole vector.
 *
 * Created by dave on 4/09/17.
 */
public class VectorProblem implements QLearningProblem, Reward {

    public int _classes = 0;
    public float _reward = 0;

    public Data _state;
    public Data _actions;
    public Data _idealActions;

    /**
     * Generate data structures ready for use.
     * Calls reset() to generate first problem.
     *
     * @param r
     * @param actions
     */
    public void setup( Random r, int actions ) {
        _classes = actions;
        _actions = new Data( _classes );
        _idealActions = new Data( _classes );
    }

    public float getReward() {
        return _reward;
    }

    public Data getState() {
        return _state;
    }

    public void setState( Data state ) {
        _state = new Data( state );
    }

    public Data getActions() {
        return _actions;
    }

    public void setActions( Data actions ) {
        _actions.copy( actions );
    }

    public Data getIdealActions() {
        return _idealActions;
    }

    public void setIdealActions( Data actions ) {
        _idealActions.copy( actions );
    }

    protected void updateReward() {
        // e.g. len 10, 2 prompts, 10-2=8,9 are the test ones
        float maxError = 0f;

        int actions = _idealActions.getSize();

        for( int i = 0; i < actions; ++i ) {
            float ideal = _idealActions._values[ i ];
            float actual = _actions._values[ i ];
            float diff = Math.abs( ideal - actual );
            maxError = Math.max( diff, maxError );
        }

        _reward = 1f - maxError; // error = 0, reward = 1

//        if( _reward > 0.5f ) {
//            int g = 0;
//            g++;
//        }
    }

    public void update() {

        // .. externally: setActions(), given previous state
        // .. externally: setClassification()

        updateReward();
    }

}
