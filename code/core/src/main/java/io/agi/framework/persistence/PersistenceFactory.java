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

package io.agi.framework.persistence;

import io.agi.core.util.PropertiesUtil;
import io.agi.framework.persistence.jdbc.JdbcPersistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Created by dave on 30/08/16.
 */
public class PersistenceFactory {

    public static final String PROPERTY_PERSISTENCE_TYPE = "persistence-type";
    public static final String PERSISTENCE_TYPE_COUCHBASE = "couchbase";
    public static final String PERSISTENCE_TYPE_JDBC = "jdbc";
    public static final String PERSISTENCE_TYPE_NODE = "node";

    private static final Logger logger = LogManager.getLogger();

    public static Persistence createPersistence( Properties properties ) {

        // Now instantiate as specified
        String type = PropertiesUtil.get( properties, PROPERTY_PERSISTENCE_TYPE, "couchbase" );

        Persistence p = null;

        if( type.equals( PERSISTENCE_TYPE_JDBC ) ) {
            logger.info( "Using JDBC (SQL) for persistence." );
            p = JdbcPersistence.Create( properties );
        }
        else if( type.equals( PERSISTENCE_TYPE_NODE ) ) {
            logger.info( "Using Node memory for persistence (note: You mustn't have more than one node in this configuration)." );
            p = new NodeMemoryPersistence();
        }

        return p;
    }

}
