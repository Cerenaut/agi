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

/**
 * Created by dave on 23/05/17.
 */
public class CheatingQLearningPolicy extends EpsilonGreedyQLearningPolicy {

//    public void selectActions( Data state, Data actionQuality, Data action ) {
//        boolean random = false;
//        if( _learn ) {
//            float r = _r.nextFloat();
//            if( r < _epsilon ) {
//                random = true;
//            }
//        }
//
//        if( random ) {
//            iselectRandomActions( state, actionQuality, action );
//        }
//        else {
//            // unpack the state:
//            int currentState = state.indicesMoreThan( 0.5f ).iterator().next();
//
//            int motors = 4;
//            int observations = 10;
//            int memory = 10;
//            int states = observations + memory + memory;
//            Data combined = new Data( states );
//            Data observed = new Data( observations );
//            Data remembered = new Data( memory );
//
////            decode the state...
////            If we have multiple memory banks then we can put into the bank in first step and then retrieve later
//
//            int offsetThat = 0;
//            observed  .copyRange( combined, 0, offsetThat, observations );
//            offsetThat = observations;
//            remembered.copyRange( combined, 0, offsetThat, memory );
//
//            //00 01 02 03 04 05 06 07 08 09 | 00 01 02 03 04 05 06 07 08 09
//            // T  T  T  T  P  P  D  D  D  D | T  T  T  T  P  P  D  D  D  D
//            float t1 = observed._values[ 0 ];
//            float t2 = observed._values[ 1 ];
//            float t3 = observed._values[ 2 ];
//            float t4 = observed._values[ 3 ];
//
//            float p1 = observed._values[ 4 ];
//            float p2 = observed._values[ 5 ];
//
//            float m0 = remembered._values[ 0 ];
//            float m1 = remembered._values[ 1 ];
//            float m2 = remembered._values[ 2 ];
//            float m3 = remembered._values[ 3 ];
//
//            // if either prompt set:
//            action.set( 0f );
//            if( ( p1 > 0f ) || ( p2 > 0f ) ) {
//                // write target - how do I know which t?
//
////                action._values[ t ] = 1f;
//
//                // clear memory
//                for( int i = 0; i < memory; ++i ) {
//                    // activate clear gate for all mem slots
//                    action._values[ motors + memory + i ] = 1f;
//                }
//            }
//            else {
//                // if any target set:
//                int t = -1;
//
//                if( t1 > 0f ) {
//                    // need to bind the bit to a time.
//                    t = 0;
//                }
//                else if( t2 > 0f ) {
//                    t = 1;
//                }
//                else if( t3 > 0f ) {
//                    t = 2;
//                }
//                else if( t4 > 0f ) {
//                    t = 3;
//                }
//
//                // activate write gate
//                if( t >= 0 ) {
//                    action._values[ motors + t ] = 1f;
//                }
//            }
//
////            Data motorActions = new Data( motors );
////            motorActions.copyRange( selectedActions, 0, 0, motors );
////            Data memoryActions = new Data( gates );
////            memoryActions.copyRange( selectedActions, 0, motors, gates );
//
//        }
//    }

}
