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

package io.agi.core.util;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Utility functions for properties files.
 * <p/>
 * Created by dave on 27/12/15.
 */
public class PropertiesUtil {

    /**
     * A quick and easy convenience interface for getting individual properties from .properties files.
     * It hides the exceptions to avoid you having to handle them. This assumes you'll resolve these issues at debug time.
     *
     * @param file
     * @param key
     * @param defaultValue
     * @return
     */
    public static String get( String file, String key, String defaultValue ) {
        try {
            Properties p = new Properties();
            p.load( new FileInputStream( file ) );

            if( p.containsKey( key ) ) {
                String value = p.getProperty( key );
                return value;
            }
            return defaultValue;
        }
        catch( Exception e ) {
            System.err.println( "Error reading properties for Node: " );
            e.printStackTrace();
            return defaultValue;
        }
    }
}
