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
import io.agi.core.util.FileUtil;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;

import java.io.File;
import java.util.Collection;

/**
 * Creates a rolling window of values from a property, captured over time. The window is updated every update() with a
 * new value from the property. The oldest value is discarded.
 * <p/>
 * Created by dave on 2/04/16.
 */
public class ValueSeriesEntity extends Entity {

    public static final String ENTITY_TYPE = "value-series";

    public static final String OUTPUT = "output";

    public ValueSeriesEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT );
    }

    public Class getConfigClass() {
        return ValueSeriesEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        ValueSeriesEntityConfig config = ( ValueSeriesEntityConfig ) _config;

        if( !_config.learn ) {
            return; // don't append or update the output data except when "learning" ie accumulating
        }

        String stringValue = Framework.GetConfig( config.entityName, config.configPath );
        Float newValue = Float.valueOf( stringValue );

        // default missing values to 0
        if( newValue == null ) {
            newValue = 0.f;
        }

        Data output;

        if( config.period < 0 ) { // keep infinite history
            Data input = getData( OUTPUT ); // error in classification (0,1)
            int oldLength = 0;
            if( input == null ) {
                output = new Data( DataSize.create( 1 ) );
            }
            else {
                // infinite length or accumulate and flush
                oldLength = input.getSize();
                int newLength = oldLength +1;

                if( ( config.flushPeriod >= 0 ) && ( oldLength >= config.flushPeriod ) ) { // truncate and flush?
                    String key = getKey( OUTPUT );
                    write( key, input, config );
                    newLength = 1;
                    oldLength = 0; // will cause nothing to be retained
                }

                output = new Data( DataSize.create( newLength ) );

                for( int i = 0; i < oldLength; ++i ) {
                    output._values[ i ] = input._values[ i ];
                }
            }

            output._values[ oldLength ] = newValue;
        }
        else {
            // finite history rolling window
            output = getDataLazyResize( OUTPUT, DataSize.create( config.period ) );

            // shift all the old values 1 place
            for( int i1 = config.period -2; i1 >= 0; --i1 ) {

                int i2 = i1 + 1;
                if( i2 >= config.period ) {
                    continue;
                }

                float x1 = output._values[ i1 ];
                output._values[ i2 ] = x1;
            }

            output._values[ 0 ] = newValue; // most recent = 0
        }

        setData( OUTPUT, output );
    }

    protected static void write( String key, Data accumulated, ValueSeriesEntityConfig config ) {

        String filePathName = config.writeFilePath + File.separator + config.writeFilePrefix + "_" + config.age + "." + config.writeFileExtension;

        _logger.info( "Writing Datas: " + key + " to file: " + filePathName );

        StringBuilder sb = new StringBuilder( 100 );
        ModelData md = new ModelData( key, accumulated, config.writeFileEncoding );
        md.toString( sb );
        boolean b = FileUtil.WriteFileMemoryEfficient( filePathName, sb ); // write the file efficiently
        if( !b ) {
            _logger.error( "Unable to serialize some Data objects to file." );
        }
    }
}
