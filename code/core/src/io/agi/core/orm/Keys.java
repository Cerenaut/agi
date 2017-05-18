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

package io.agi.core.orm;

/**
 * A systematic way of forming unique object names.
 * <p/>
 * Created by dave on 27/12/15.
 */
public class Keys {

    public static final String DELIMITER = "-";

    public static String concatenate( String prefix, String suffix ) {
        return prefix + DELIMITER + suffix;
    }

    public static String concatenate( String s1, String s2, String s3 ) {
        return s1 + DELIMITER + s2 + DELIMITER + s3;
    }

    public static String concatenate( String s1, String s2, String s3, String s4 ) {
        return s1 + DELIMITER + s2 + DELIMITER + s3 + DELIMITER + s4;
    }
}
