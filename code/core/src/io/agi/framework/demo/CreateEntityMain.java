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

package io.agi.framework.demo;

import io.agi.core.util.PropertiesUtil;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.factories.CommonEntityFactory;

import java.util.Properties;

/**
 * Created by dave on 7/07/17.
 */
public abstract class CreateEntityMain {

    /**
     * Usage: Expects some arguments. These are:
     * 0: node.properties file
     * 1 to n: 'create' flag and/or 'prefix' flag
     * @param args
     */
    public void mainImpl( String[] args ) {

        // Create a Node

        Main m = new Main();
        Properties p = PropertiesUtil.load(args[0]);
        m.setup( p, null, new CommonEntityFactory() );

        // Optionally set a global prefix for entities
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "prefix" ) ) {
                String prefix = args[ i+1 ];
                Framework.SetEntityNamePrefix(prefix);
//                Framework.SetEntityNamePrefixDateTime();
            }
        }

        // Optionally create custom entities and references
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "create" ) ) {
                createEntities( m._n );
            }
        }

        boolean update = false;
        for( int i = 1; i < args.length; ++i ) {
            String arg = args[ i ];
            if( arg.equalsIgnoreCase( "update" ) ) {
                update = true;
            }
        }

        if( update ) {
            int delay = 500;
            Thread t = new Thread() {
                public void run() {
                    try {
                        Thread.sleep( delay );
                    }
                    catch( InterruptedException e ) {}
                    //System.err.println( "main(): Requesting Update... " );
                    m._n.doUpdate( "experiment" );
                    //System.err.println( "main(): Requested Update. " );
                }
            };
            t.start();
        }

        // Start the system
        m.run();
    }

    public abstract void createEntities( Node n );

}
