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

import io.agi.core.orm.ObjectMap;
import io.agi.core.util.FileUtil;
import io.agi.framework.DataFlags;
import io.agi.framework.references.DataRef;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.references.DataRefMap;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Creates a circular queue of Data items copied from some other Data source.
 *
 * Created by Dave on 26/03/2016.
 */
public class DataWriterEntity extends Entity {

    public static final String ENTITY_TYPE = "data-writer";

    public DataWriterEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

    }

    @Override
    public Class getConfigClass() {
        return DataWriterEntityConfig.class;
    }

    public void doUpdateSelf() {

        DataWriterEntityConfig config = (DataWriterEntityConfig)_config;

        if( ( config.age % config.ageInterval ) != 0 ) {
            // don't log anything
            return;
        }

        try {
            String[] dataNames = config.dataNames.split( "," );

            ArrayList< ModelData > modelDatas = new ArrayList< ModelData >();

            DataRefMap map = _n.getDataRefMap();

            for( String dataName : dataNames ) {
                String dataNameTrim = dataName.trim();
                DataRef dataRef = map.getData( dataNameTrim );
                ModelData md = new ModelData();
                if( md.serialize( dataRef ) ) {
                    modelDatas.add( md );
                }
                //ModelData m = _n.getModelData( dataNameTrim, new DataRef.DenseDataRefResolver() ); // benefit from existing serialization if any
                //modelDatas.add( m );
            }

            // filename for output looks like this:
            // PATH+SEPARATOR+PREFIX+"_"+AGE+".json"
            String filePathName = config.writeFilePath + File.separator + config.writeFilePrefix + "_" + config.age + "." + config.writeFileExtension;

            _logger.info( "Writing Datas: " + config.dataNames + " to file: " + filePathName );

            StringBuilder sb = new StringBuilder( 100 );
            ModelData.ModelDatasToJsonStringBuilder( modelDatas, sb ); // build the string efficiently
            boolean append = false;
            boolean b = FileUtil.WriteFileMemoryEfficient( filePathName, sb, append ); // write the file efficiently
            if( !b ) {
                _logger.error( "Unable to serialize some Data objects to file." );
            }
        }
        catch( Exception e ) {
            _logger.error( "Exception caught while trying to serialize some Data objects to file." );
            _logger.error( e.toString(), e );
        }
    }

}