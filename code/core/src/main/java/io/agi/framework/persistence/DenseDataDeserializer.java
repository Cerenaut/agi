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

package io.agi.framework.persistence;

import io.agi.core.data.Data;
import io.agi.framework.persistence.models.ModelData;

import java.util.HashMap;

/**
 * Useful standard implementation.
 *
 * Created by dave on 25/01/17.
 */
public class DenseDataDeserializer implements DataDeserializer {

    public Data getCombinedData( String inputAttribute, HashMap< String, Data > allRefs ) {

        // case 1: No input.
        int nbrRefs = allRefs.size();
        if( nbrRefs == 0 ) {
            return null;
        }

        // case 2: Single input
        if( nbrRefs == 1 ) {
            String refKey = allRefs.keySet().iterator().next();
            Data refData = allRefs.get( refKey );
            if( refData == null ) {
                return null;
            }
            Data d = new Data( refData );
            return d;
        }

        // case 3: Multiple inputs (combine as vector)
        int sumVolume = 0;

        for( String refKey : allRefs.keySet() ) {
            Data refData = allRefs.get( refKey );
            if( refData == null ) {
                return null;
            }
            int volume = refData.getSize();
            sumVolume += volume;
        }

        Data d = new Data( sumVolume );

        int offset = 0;

        for( String refKey : allRefs.keySet() ) {
            Data refData = allRefs.get( refKey );
            int volume = refData.getSize();

            d.copyRange( refData, offset, 0, volume );

            offset += volume;
        }

        return d;
    }

    public String getEncoding( String inputAttribute ) {
        return ModelData.ENCODING_DENSE;
    }

}
