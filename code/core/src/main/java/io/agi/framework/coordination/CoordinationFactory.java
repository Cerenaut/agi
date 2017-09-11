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

package io.agi.framework.coordination;

import io.agi.core.util.PropertiesUtil;
import io.agi.framework.coordination.http.HttpCoordination;
import io.agi.framework.coordination.monolithic.SingleProcessCoordination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Created by dave on 30/08/16.
 */
public class CoordinationFactory {

    public static final String PROPERTY_COORDINATION_TYPE = "coordination-type";
    public static final String COORDINATION_TYPE_HTTP = "http";
    public static final String COORDINATION_TYPE_NONE = "none";

    private static final Logger logger = LogManager.getLogger();

    public static Coordination createCoordination( Properties properties ) {
        String type = PropertiesUtil.get( properties, PROPERTY_COORDINATION_TYPE, "http" );
        Coordination c = null;
        if( type.equals( COORDINATION_TYPE_HTTP ) ) {
            logger.info( "Distributed coordination." );
            c = new HttpCoordination();
        } else {
            logger.info( "Monolithic coordination." );
            c = new SingleProcessCoordination();
        }
        return c;
    }


}
