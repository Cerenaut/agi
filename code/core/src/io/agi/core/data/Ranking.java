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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.data;

import java.util.*;
import java.util.Map.Entry;

/**
 * Ranking functions. A common data structure is used based on Java collections.
 * Duplicate entries per rank are allowed, as these occur frequently in actual
 * test problems.
 *
 * @author dave
 */
public class Ranking {

    public TreeMap< Float, ArrayList< Integer > > _ranking;

    public Ranking() {
        _ranking = new TreeMap< Float, ArrayList< Integer > >();
    }

    public Ranking( FloatArray2 v ) {
        _ranking = rank( v );
    }

    public static TreeMap< Float, ArrayList< Integer > > rank( FloatArray2 vq ) {

        TreeMap< Float, ArrayList< Integer > > ranking = new TreeMap< Float, ArrayList< Integer > >();

        int models = vq._values.length;

        for( int j = 0; j < models; ++j ) {

            float quality = vq._values[ j ];

            ArrayList< Integer > al = ranking.get( quality );

            if( al == null ) {
                al = new ArrayList< Integer >();
                ranking.put( quality, al );
            }

            al.add( j );
        }

        return ranking;
    }

    public boolean isSameAs( Ranking r ) {
        try {
            Set< Entry< Float, ArrayList< Integer > > > thisES = _ranking.entrySet();
            Set< Entry< Float, ArrayList< Integer > > > thatES = r._ranking.entrySet();

            int thisSize = thisES.size();
            int thatSize = thatES.size();

            if( thisSize != thatSize ) {
                return false;
            }

            Iterator iThis = thisES.iterator();
            Iterator iThat = thatES.iterator();

            while( iThis.hasNext() ) {

                Entry< Float, ArrayList< Integer > > eThis = ( Entry< Float, ArrayList< Integer > > ) iThis.next();
                Entry< Float, ArrayList< Integer > > eThat = ( Entry< Float, ArrayList< Integer > > ) iThat.next();

                ArrayList< Integer > alThis = eThis.getValue();
                ArrayList< Integer > alThat = eThat.getValue();

                for( int i = 0; i < alThis.size(); ++i ) {
                    int nThis = alThis.get( i );
                    int nThat = alThat.get( i );

                    if( nThis != nThat ) {
                        return false;
                    }
                }
            }

            return true;
        }
        catch( Exception e ) { // either or both null
            return false;
        }
    }

    public static Integer getElementWithIndex( HashSet< Integer > elements, int i ) {
        int index = 0;
        for( Integer element : elements ) {
            if( index == i ) {
                return element;
            }
            ++index;
        }
        return null;
    }

    public static HashSet< Integer > selectValuesRoulette( Random r, TreeMap< Float, ArrayList< Integer > > ranking, int selectionSetSize ) {

        HashSet< Integer > selections = new HashSet< Integer >();

        while( selections.size() < selectionSetSize ) {
            Integer selected = Ranking.roulette( r, ranking );
            if( selected == null ) {
                break;
            }
            Ranking.removeValue( ranking, selected );
            selections.add( selected );
        }
        return selections;
    }

    public static HashSet< Integer > getValues( TreeMap< Float, ArrayList< Integer > > ranking ) {
        HashSet< Integer > values = new HashSet< Integer >();

        Iterator i = ranking.keySet().iterator();

        int size = 0;

        while( i.hasNext() ) {
            Float key = ( Float ) i.next();
            ArrayList< Integer > al = ranking.get( key );

            for( Integer value : al ) {
                values.add( value );
            }
        }

        return values;
    }

    public static int getSize( TreeMap< Float, ArrayList< Integer > > ranking ) {
        Iterator i = ranking.keySet().iterator();

        int size = 0;

        while( i.hasNext() ) {
            Float key = ( Float ) i.next();
            ArrayList< Integer > al = ranking.get( key );

            size += al.size();
        }

        return size;
    }

    public static boolean containsKey( TreeMap< Float, ArrayList< Integer > > ranking, float key ) {
        ArrayList< Integer > al = ranking.get( key );
        if( al == null ) {
            return false;
        }
        return !al.isEmpty();
    }

    public static boolean containsValue( TreeMap< Float, ArrayList< Integer > > ranking, int label ) {
        Iterator i = ranking.keySet().iterator();

        while( i.hasNext() ) {
            Float key = ( Float ) i.next();
            ArrayList< Integer > al = ranking.get( key );

            for( Integer n : al ) {
                if( n.equals( label ) ) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void truncate( TreeMap< Float, ArrayList< Integer > > ranking, int maxSize, boolean max ) {
        int size = getSize( ranking );
        int excess = Math.max( 0, size - maxSize );

        for( int n = 0; n < excess; ++n ) {
            Iterator i = null;
            if( max ) {
                i = ranking.keySet().iterator();
            } else { // keep minima by pruning from other end:
                i = ranking.descendingKeySet().iterator();
            }

            Float key = ( Float ) i.next();

            ArrayList< Integer > al = ranking.get( key );

            al.remove( 0 );

            if( al.isEmpty() ) {
                ranking.remove( key );
            }
        }
    }

    public static Integer getBestValue( TreeMap< Float, ArrayList< Integer > > ranking, boolean max ) {
        Iterator i = null;
        if( max ) { // rank max first
            i = ranking.descendingKeySet().iterator(); // maxima first
        } else { // rank min first
            i = ranking.keySet().iterator(); // ascending values
        }

        while( i.hasNext() ) {
            Float key = ( Float ) i.next();
            ArrayList< Integer > al = ranking.get( key );

            for( Integer n : al ) {
                return n;
            }
        }

        return null;
    }

    public static ArrayList< Integer > getBestValues( TreeMap< Float, ArrayList< Integer > > ranking, boolean max, int maxRank ) {
        Iterator i = null;
        if( max ) { // rank max first
            i = ranking.descendingKeySet().iterator(); // maxima first
        } else { // rank min first
            i = ranking.keySet().iterator(); // ascending values
        }

        ArrayList< Integer > bestValues = new ArrayList< Integer >();

        while( i.hasNext() ) {

            Float key = ( Float ) i.next();
            ArrayList< Integer > al = ranking.get( key );

            for( Integer n : al ) {
                bestValues.add( n );

                if( bestValues.size() >= maxRank ) {
                    return bestValues;
                }
            }
        }

        return bestValues;
    }

    public static Integer getRank( TreeMap< Float, ArrayList< Integer > > ranking, boolean max, int value ) {

        Iterator i = null;
        if( max ) { // rank max first
            i = ranking.descendingKeySet().iterator(); // maxima first
        } else { // rank min first
            i = ranking.keySet().iterator(); // ascending values
        }

        int rank = 0;

        while( i.hasNext() ) {
            Float key = ( Float ) i.next();

            ArrayList< Integer > al = ranking.get( key );

            for( Integer n : al ) {

                if( n.equals( value ) ) {
                    return rank;
                }

                ++rank;
            }
        }

        return rank;
    }

//    public static Integer roulette( TreeMap< Float, ArrayList< Integer > > ranking ) {
//        return roulette( Rando mInstance.NodeInstance(), ranking );
//    }

    public static Integer roulette( Random o, TreeMap< Float, ArrayList< Integer > > ranking ) {
        float sum = Ranking.sum( ranking );
        float random = ( float ) o.nextFloat() * sum;

        Set< Float > keys = ranking.keySet();
        Iterator i = keys.iterator(); // first value = highest likelihood

        float total = 0.f;

        while( i.hasNext() ) {
            Float r = ( Float ) i.next();

            ArrayList< Integer > al = ranking.get( r );

            for( Integer n : al ) {
                total += r;

                if( total >= random ) {
                    return n;
                }
            }
        }

        return null;
    }

    public static void removeValue( TreeMap< Float, ArrayList< Integer > > ranking, int value ) {
        Set< Float > keys = ranking.keySet();
        Iterator i = keys.iterator(); // first value = highest likelihood

        Integer index = null;

        while( i.hasNext() ) {
            Float r = ( Float ) i.next();

            ArrayList< Integer > al = ranking.get( r );


            for( int n = 0; n < al.size(); ++n ) {
                if( al.get( n ).equals( value ) ) {
                    index = n;
                    break;
                }
            }

            if( index != null ) {
                al.remove( ( int ) index );
                break;
            }
        }
    }

    public static float sum( TreeMap< Float, ArrayList< Integer > > ranking ) {
        Set< Float > keys = ranking.keySet();

        Iterator i = keys.iterator(); // first value = highest likelihood

        float sum = 0.f;

        while( i.hasNext() ) {
            Float r = ( Float ) i.next();

            ArrayList< Integer > al = ranking.get( r );

            sum += ( r * ( float ) al.size() );
        }

        return sum;
    }

    public static void add( TreeMap< Float, ArrayList< Integer > > ranking, float key, int label ) {

        ArrayList< Integer > al = ranking.get( key );

        if( al == null ) {
            al = new ArrayList< Integer >();
            ranking.put( key, al );
        }

        al.add( label );

    }

    public static void toArray( HashMap< Integer, Float > ranking, FloatArray2 vr ) {

        vr.set( 0.0f );

        float weight = 1.0f / ( float ) vr._values.length;

        Set< Integer > keys = ranking.keySet();

        Iterator i = keys.iterator(); // first value = highest likelihood

        while( i.hasNext() ) {
            Integer n = ( Integer ) i.next();
            float k = ranking.get( n );

            vr._values[ n ] = 1.0f - ( k * weight );
        }
    }

    public static void toArray( TreeMap< Float, ArrayList< Integer > > ranking, FloatArray2 vr ) {

        vr.set( 0.0f );

        int k = 0;
        float weight = 1.0f / ( float ) vr._values.length;

        Set< Float > descending = ranking.descendingKeySet();

        Iterator i = descending.iterator(); // first value = highest likelihood

        while( i.hasNext() ) {
            Float r = ( Float ) i.next();

            ArrayList< Integer > al = ranking.get( r );

            for( Integer n : al ) {
                if( n > 0 ) {
                    vr._values[ n ] = 1.0f - ( k * weight );
                }
            }

            k += al.size();
        }
    }

}
