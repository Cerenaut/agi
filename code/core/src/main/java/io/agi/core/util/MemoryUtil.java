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

package io.agi.core.util;

import org.apache.logging.log4j.Logger;

/**
 * Created by gideon on 4/2/17.
 */
public class MemoryUtil {

    public static void logMemory( Logger logger ) {
        long total = Runtime.getRuntime().totalMemory() / 1000000;
        long free = Runtime.getRuntime().freeMemory() / 1000000;
        long used = total - free;

        logger.warn( "Memory (mb) (total, free, used) = (" + total + ", " +  free + ", " + used + ")" );
    }

}
