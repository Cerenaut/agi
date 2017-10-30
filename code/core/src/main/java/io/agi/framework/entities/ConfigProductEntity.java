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

package io.agi.framework.entities;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Output is same as input except multiplied by a real valued config property.
 * Created by dave on 5/11/16.
 */
public class ConfigProductEntity extends Entity {

    public static final String ENTITY_TYPE = "config-product-entity";

    public static final String INPUT = "input";
    public static final String OUTPUT = "output";


    public ConfigProductEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return ConfigProductEntityConfig.class;
    }


    @Override
    protected void doUpdateSelf() {
        ConfigProductEntityConfig config = ( ConfigProductEntityConfig ) _config;

        Float factor = null;

        try {
            String stringValue = PersistenceUtil.GetConfig( config.entityName, config.configPath );
            factor = Float.valueOf( stringValue );
        }
        catch( Exception e ) {
        }

        if( factor == null ) {
            try {
                String stringValue = PersistenceUtil.GetConfig( config.entityName, config.configPath );
                factor = (float)Integer.valueOf( stringValue );
            }
            catch( Exception e ) {
            }
        }

        if( factor == null ) {
            try {
                String stringValue = PersistenceUtil.GetConfig( config.entityName, config.configPath );
                boolean b = Boolean.valueOf( stringValue );
                if( b ) {
                    factor = 1f;
                } else {
                    factor = 0f;
                }
            }
            catch( Exception e ) {
            }
        }

        // default missing values to 0
        if( factor == null ) {
            factor = 1.f;
        }

        Data input = getData( INPUT ); // error in classification (0,1)

        if( input == null ) {
            return;
        }

        Data output = new Data( input );

        for( int i = 0; i < output.getSize(); ++i ) {

            float x = input._values[ i ];
            x *= factor;
            output._values[ i ] = x;
        }

        setData( OUTPUT, output );
    }
}