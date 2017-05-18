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

package io.agi.framework.demo.light;

import io.agi.core.util.PropertiesUtil;
import io.agi.framework.Framework;
import io.agi.framework.Main;

import java.util.Properties;

/**
 * A simple interaction between two entities.
 * <p/>
 * Created by dave on 18/02/16.
 */
public class LightDemo {

    public static void main( String[] args ) {

        // Provide classes for entities
        LightEntityFactory ef = new LightEntityFactory();

        // Create a Node
        Main m = new Main();
        Properties p = PropertiesUtil.load( args[ 0 ] );
        m.setup( p, null, ef );

        // Create custom entities and references
        if( args.length > 1 ) {
            Framework.LoadEntities( args[ 1 ] );
        }

        if( args.length > 2 ) {
            Framework.LoadDataReferences( args[ 2 ] );
        }

        // Start the system
        m.run();
    }

}
