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
import io.agi.core.data.Data2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * https://medium.com/emergent-future/simple-reinforcement-learning-with-tensorflow-part-0-q-learning-with-tables-and-neural-networks-d195264329d0
 * The FrozenLake environment consists of a 4x4 grid of blocks, each one either being the start block, the goal block,
 * a safe frozen block, or a dangerous hole. The objective is to have an agent learn to navigate from the start to the
 * goal without moving onto a hole.
 *
 * At any given time the agent can choose to move either up, down, left, or right. The catch is that there is a wind
 * which occasionally blows the agent onto a space they didn’t choose. As such, perfect performance every time is
 * impossible, but learning to avoid the holes and reach the goal are certainly still doable. The reward at every step
 * is 0, except for entering the goal, which provides a reward of 1.
 *
 * In the case of the FrozenLake environment, we have 16 possible states (one for each block), and 4 possible actions
 * (the four directions of movement), giving us a 16x4 table of Q-values. We start by initializing the table to be
 * uniform (all zeros), and then as we observe the rewards we obtain for various actions, we update the table accordingly.
 *
 * More:
 * https://gym.openai.com/envs/FrozenLake-v0
 *
 * FrozenLake-v0 defines "solving" as getting average reward of 0.78 over 100 consecutive trials.
 *
 * However, the ice is slippery, so you won't always move in the direction you intend.
 *
 * The episode ends when you reach the goal or fall in a hole. You receive a reward of 1 if you reach the goal, and zero
 * otherwise.
 *
 * SFFF       (S: starting point, safe)
 * FHFH       (F: frozen surface, safe)
 * FFFH       (H: hole, fall to your doom)
 * HFFG       (G: goal, where the frisbee is located)
 *
 * @author dave
 */
public class FrozenLakeProblem implements QLearningProblem, Reward {

    public static final int CELL_START = 0;
    public static final int CELL_FROZEN = 1;
    public static final int CELL_HOLE = 2;
    public static final int CELL_GOAL = 3;

    public static final int ACTION_UP = 0;
    public static final int ACTION_DOWN = 1;
    public static final int ACTION_LEFT = 2;
    public static final int ACTION_RIGHT = 3;
    public static final int ACTIONS = 4;

    Random _r;
    float _reward = 0;

    int _cell;
    Data _map;
    Data _state;
    Data _actions;

    /**
     * Default parameters for the problem
     */
    public void setup( Random r, int size, ArrayList< Integer > holes ) {
        _r = r;
        _cell = 0;
        _map = new Data( size, size );
        _state = new Data( size * size );
        _actions = new Data( ACTIONS );

        createMap( holes );

        reset();
    }

    public void createMap( ArrayList< Integer > holes ) {
        int cells = getNbrCells();
        _map.set( CELL_FROZEN );
        _map._values[ 0 ] = CELL_START;
        _map._values[ cells -1 ] = CELL_GOAL;

        for( Integer hole : holes ) {
            _map._values[ hole ] = CELL_HOLE;
        }
    }

    public int getNbrStates() {
        return getNbrCells();
    }

    public int getNbrActions() {
        return ACTIONS;
    }
    public int getNbrCells() {
        return _map.getSize();
    }

    public void reset() {
        _state.set( 0f );

        int cells = getNbrCells();
        for( int i = 0; i < cells; ++i ) {
            float cell = _map._values[ i ];
            if( i == CELL_START ) {
                _state._values[ i ] = 1; // move back to start
                return;
            }
        }
    }

    public int getCell() {
        Point size = Data2d.getSize( _map );

        for( int y = 0; y < size.y; ++y ) {
            for( int x = 0; x < size.x; ++x ) {
                int cell = y * size.x + x;
                float state = _state._values[ cell ];

                if( state > 0f ) {
                    return cell;
                }
            }
        }
        return -1;
    }

    public Point getCellXY() {
        Point size = Data2d.getSize( _map );

        for( int y = 0; y < size.y; ++y ) {
            for( int x = 0; x < size.x; ++x ) {
                int cell = y * size.x + x;
                float state = _state._values[ cell ];

                if( state > 0f ) {
                    return new Point( x, y );
                }
            }
        }
        return new Point( -1, -1 );
    }

    public int getAction() {
        for( int a = 0; a < _actions.getSize(); ++a ) {
            float action = _actions._values[ a ];

            if( action > 0f ) {
                return a;
            }
        }

        return -1;
    }

    public void print() {

        Point size = Data2d.getSize( _map );

        for( int y = 0; y < size.y; ++y ) {
            for( int x = 0; x < size.x; ++x ) {
                float state = _state._values[ y * size.x + x ];
                float cell = _map._values[ y * size.x + x ];

                if( state > 0f ) {
                    System.err.print( '+' );
                    continue;
                }

                if( cell == CELL_GOAL ) {
                    System.err.print( 'G' );
                }
                if( cell == CELL_START ) {
                    System.err.print( 'S' );
                }
                if( cell == CELL_FROZEN ) {
                    System.err.print( 'F' );
                }
                if( cell == CELL_HOLE ) {
                    System.err.print( 'H' );
                }
            }
            System.err.println();
        }
    }

    public float getReward() {
        return _reward;
    }

    public Data getState() {
        return _state;
    }

    public void setActions( Data actions ) {
        _actions.copy( actions );
    }

    public Data getActions() {
        return _actions;
    }

    public void update() {

        // .. externally: setActions(), given previous state
        int action = getAction();

        Point p1 = getCellXY();
        Point p2 = new Point( p1 );
        Point size = Data2d.getSize( _map );

        // implement action
        if( action == ACTION_UP ) {
            p2.y = Math.max( 0, p2.y - 1 );
        }
        if( action == ACTION_DOWN ) {
            p2.y = Math.min( size.y - 1, p2.y + 1 );
        }
        if( action == ACTION_LEFT ) {
            p2.x = Math.max( 0, p2.x - 1 );
        }
        if( action == ACTION_RIGHT ) {
            p2.x = Math.min( size.x-1, p2.x + 1 );
        }

        // return to start if hole
        _reward = 0f;

        // record new state
        int cell2 = p2.y * size.x + p2.x;
        _state.set( 0f );
        _state._values[ cell2 ] = 1f;

        float cellType = _map._values[ p2.y * size.x + p2.x ];
        if( cellType == CELL_HOLE ) {
            reset();
        }
        else if( cellType == CELL_GOAL ) {
            reset();
            _reward = 1f;
        }

        //print();
        //System.err.println( " R=" + _reward );
    }

}