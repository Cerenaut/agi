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

package io.agi.framework.entities;

import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;
import io.agi.core.util.FileUtil;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Allows Data to be written to and read from local filesystem
 * Created by dave on 2/04/16.
 */
public class DataFileEntity extends Entity {

    public static final String ENTITY_TYPE = "vector-data-file";

    public static final String INPUT_WRITE = "input-write";
    public static final String OUTPUT_READ = "output-read";

    public DataFileEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_WRITE );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        DataFileEntityConfig config = ( DataFileEntityConfig ) _config;

        attributes.add( OUTPUT_READ );

        if( config.encoding.equals( ModelData.ENCODING_SPARSE_BINARY ) ) {
            flags.putFlag( OUTPUT_READ, DataFlags.FLAG_SPARSE_BINARY );
        }
        else if( config.encoding.equals( ModelData.ENCODING_SPARSE_REAL ) ) {
            flags.putFlag( OUTPUT_READ, DataFlags.FLAG_SPARSE_REAL );
        }
        else { // remove existing flag, if any
            flags.removeFlag( OUTPUT_READ, DataFlags.FLAG_SPARSE_BINARY );
            flags.removeFlag( OUTPUT_READ, DataFlags.FLAG_SPARSE_REAL );
        }
    }

    public Class getConfigClass() {
        return DataFileEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        DataFileEntityConfig config = ( DataFileEntityConfig ) _config;

        // reset flag acts to clear the file
        if( config.reset ) {
            if( config.write ) {
                write( null, config.fileNameWrite, false, config.ioCapacity, config.newLine );
            }
        }

        // only append or write to file if learn flag enabled
        if( config.learn ) {
            if( config.write ) {
                Data input = getData( INPUT_WRITE );
                if( input != null ) {
                    write( input, config.fileNameWrite, config.append, config.ioCapacity, config.newLine );
                }
            }
        }

        // optionally read a file
        if( config.read ) {
            Data output = Data2d.readCsvFile( config.fileNameRead );
            setData( OUTPUT_READ, output );
            config.read = false;
        }
    }

    protected static void write( Data data, String filePathName, boolean append, int ioCapacity, String newLine ) {

        StringBuilder sb = new StringBuilder( ioCapacity );
        if( data != null ) {
            data.toCsv( sb, newLine ); // serialize
        }

        boolean b = FileUtil.WriteFileMemoryEfficient( filePathName, sb, append ); // write the file efficiently
        if( !b ) {
            _logger.error( "Couldn't write data to file: " + filePathName );
        }
    }


}
