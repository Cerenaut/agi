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

package io.agi.framework;

import io.agi.core.orm.Keys;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Functions for convenient and compatible naming of entities and other properties.
 *
 * Created by dave on 25/10/17.
 */
public class Naming {

    public static final String ENTITY_NAME_PREFIX_DELIMITER = "--";

    protected static String entityNamePrefix;
    protected static SecureRandom entityNameRandom;

    public static String GetEntityNameWithPrefix( String entityPrefix, String entityNameSuffix ) {
        String prefix = GetEntityNamePrefix();
        String name = entityNameSuffix;

        if( prefix != null ) {
            if( prefix.length() > 0 ) {
                name = prefix + ENTITY_NAME_PREFIX_DELIMITER + name;
            }
        }

        return name;
    }

    public static String GetEntityName( String entityNameSuffix ) {
        String prefix = GetEntityNamePrefix();
        return GetEntityNameWithPrefix( prefix, entityNameSuffix );
    }

    public static String GetEntityNamePrefix() {
        if( Naming.entityNamePrefix == null ) {
            return "";
        }

        return Naming.entityNamePrefix;
    }

    public static String SetEntityNamePrefixRandom() {
        // http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
        if( entityNameRandom == null ) {
            entityNameRandom = new SecureRandom();
        }

        int bits = 130;
        int base = 32;
        String prefix = new BigInteger( bits, entityNameRandom ).toString( base ) + ENTITY_NAME_PREFIX_DELIMITER;

        return SetEntityNamePrefix( prefix );
    }

    public static String SetEntityNamePrefixDateTime() {
        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd-hh:mm:ss" );
        String prefix = formatter.format( now );
        return SetEntityNamePrefix( prefix );
    }

    public static String SetEntityNamePrefix( String entityNamePrefix ) {
        Naming.entityNamePrefix = entityNamePrefix;
        return Naming.entityNamePrefix;
    }

    public static String GetDataKey( String entityName, String dataSuffix ) {
        return Keys.concatenate( entityName, dataSuffix );
    }

}
