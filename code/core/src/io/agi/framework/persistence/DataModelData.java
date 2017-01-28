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

/**
 * An object that combines both the Object and serialized form of Data.
 *
 * Created by dave on 25/01/17.
 */
public class DataModelData {

    public String _encoding;
    public String _refKeys;
    public Data _d;
    public ModelData _md;

    public boolean hasReferences() {
        if( _refKeys != null ) {
            if( _refKeys.length() > 0 ) {
                return true;
            }
        }

        return false;
    }
}
