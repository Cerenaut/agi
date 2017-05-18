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

package io.agi.framework;

import java.util.ArrayList;
import java.util.Collection;

/**
 * These 'Property' models are bags of primitives, and can be nested objects.
 * THIS IS STANDARD practice for models.
 * <p/>
 * Created by gideon on 1/04/2016.
 */
public class EntityConfig {

    public int age = 0;            // default = 'not set'      optional
    public Long seed = null;       // default = 'not set'      optional
    public boolean reset = false;  // default                  optional
    public boolean flush = false;  // default
    public boolean learn = true;   // default
    public boolean cache = false;  // default false. If true, will cache all data in the Node avoiding serialization.

    // TODO add last update date/time?
    public static Collection< String > GetEntityNames( String configValue ) {
        String[] names = configValue.split( "," );
        Collection< String > c = new ArrayList< String >();
        for( String s : names ) {
            c.add( s );
        }
        return c;
    }

}
