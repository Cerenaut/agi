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

package io.agi.framework.references;

import io.agi.core.data.Data;
import io.agi.framework.Node;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Handles the concept of a Data being the combination of 1 or more other Data objects.
 *
 * Created by dave on 24/10/17.
 */
public abstract class DataRefResolver {

    public static HashSet< String > GetRefKeys( String refKeys ) {
        HashSet< String > refKeysSet = new HashSet<>();

        try {
            if( refKeys != null ) {
                String[] splitKeys = refKeys.split( "," );
                for( int i = 0; i < splitKeys.length; ++i ) {
                    String key = splitKeys[ i ].trim();
                    refKeysSet.add( key );
                }
            }

            return refKeysSet;
        }
        catch( Exception e ) {
            return refKeysSet;
        }
    }

    public Data getData( Node n, DataRef dataRef ) {
        if( !dataRef.isReference() ) {
            return dataRef._data;
        }

        // Create an output matrix which is a composite of all the referenced inputs.
        HashMap< String, DataRef > allRefs = new HashMap<>();
        HashSet< String > refKeys = GetRefKeys( dataRef._refKeys );

        DataRefMap dc = n.getDataRefMap();
        for( String refKey : refKeys ) {
            DataRef dataRef2 = dc.getData( refKey );
            if( dataRef2 == null ) {
                System.out.println( "Warning: Data reference '" + refKey + "' not found." );
                //continue; // don't include this? Or? return null if some unsatisfied?
                return null;
            }

            allRefs.put( refKey, dataRef2 );
        }

        Data combinedData = getCombinedData( dataRef, allRefs );
        return combinedData;
    }

    public abstract Data getCombinedData( DataRef dataRef, HashMap< String, DataRef > referred );
    public abstract String getCombinedEncoding( String key );

}
