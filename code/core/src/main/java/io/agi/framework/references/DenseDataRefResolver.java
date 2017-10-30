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
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.models.ModelData;

import java.util.HashMap;

/**
 * Created by dave on 24/10/17.
 */
public class DenseDataRefResolver  extends DataRefResolver {

    public Data getCombinedData( DataRef dataRef, HashMap< String, DataRef > referred ) {

        // case 1: No input.
        int nbrRefs = referred.size();
        if( nbrRefs == 0 ) {
            return null;
        }

        // case 2: Single input
        if( nbrRefs == 1 ) {
            String refKey = referred.keySet().iterator().next();
            DataRef dataRef2 = referred.get( refKey );
            if( dataRef2._data == null ) {
                return null;
            }
            Data d = new Data( dataRef2._data ); // deep binary copy
            return d;
        }

        // case 3: Multiple inputs (combine as vector)
        int sumVolume = 0;

        for( String refKey : referred.keySet() ) {
            DataRef dataRef2 = referred.get( refKey );
            if( dataRef2._data == null ) {
                return null; // missing data
            }
            int volume = dataRef2._data.getSize();
            sumVolume += volume;
        }

        // Create new structure of sufficint volume and copy content
        Data d = new Data( sumVolume );

        int offset = 0;

        for( String refKey : referred.keySet() ) {
            DataRef dataRef2 = referred.get( refKey );
            int volume = dataRef2._data.getSize();
            d.copyRange( dataRef2._data, offset, 0, volume );
            offset += volume;
        }

        return d;
    }

    public String getCombinedEncoding( String key ) {
        return DataJsonSerializer.ENCODING_DENSE;
    }

}