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

package io.agi.framework.references;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.agi.core.orm.AbstractPair;
import io.agi.core.orm.Keys;
import io.agi.core.util.FileUtil;
import io.agi.framework.Naming;
import io.agi.framework.Node;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelDataReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dave on 25/10/17.
 */
public class DataRefUtil {

    protected static final Logger _logger = LogManager.getLogger();

    /**
     * Modifies the database to make the reference _entityName-suffix Data a reference input to the input _entityName-suffix.
     *
     * @param inputEntity
     * @param inputSuffix
     * @param referenceEntity
     * @param referenceSuffix
     */
    public static void SetDataReference(
            String inputEntity,
            String inputSuffix,
            String referenceEntity,
            String referenceSuffix ) {
        String inputKey = GetDataKey( inputEntity, inputSuffix );
        String refKey = GetDataKey( referenceEntity, referenceSuffix );
        SetDataReference( inputKey, refKey );
    }

    public static String GetDataKey( String entityName, String suffix ) {
        return Naming.GetDataKey( entityName, suffix );
    }

    public static void SetDataReferences(
            String inputEntity,
            String inputSuffix,
            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes ) {
        String inputKey = GetDataKey( inputEntity, inputSuffix );
        String refKeys = "";

        for( AbstractPair< String, String > ap : referenceEntitySuffixes ) {
            String referenceEntity = ap._first;
            String referenceSuffix = ap._second;
            String refKey = GetDataKey( referenceEntity, referenceSuffix );
            if( refKeys.length() > 0 ) {
                refKeys = refKeys + ",";
            }
            refKeys = refKeys + refKey;
        }

        SetDataReference( inputKey, refKeys, DataJsonSerializer.ENCODING_DENSE );
    }

    public static void SetDataReference(
            String dataKey,
            String refKeys ) {
        SetDataReference( dataKey, refKeys, DataJsonSerializer.ENCODING_DENSE );
    }

    /**
     * Modifies the database to make the reference _entityName-suffix Data a reference input to the input _entityName-suffix.
     *
     * @param dataKey
     * @param refKeys
     * @param encoding May be null
     */
    public static void SetDataReference(
            String dataKey,
            String refKeys,
            String encoding ) {
        Node n = Node.NodeInstance();
        DataRefMap map = n.getDataRefMap();
//        Persistence p = n.getPersistence();
        ModelData modelData = new ModelData();
        modelData.setReference( dataKey, refKeys, encoding );
        map.setData( modelData );
//        p.persistData( modelData );
    }

    /**
     * Set the data in the model, in the persistence layer.
     * If an entry exists for this key, replace it.
     *
     * @param modelData
     */
    public static void SetData( ModelData modelData ) {
        Node n = Node.NodeInstance();
        DataRefMap map = n.getDataRefMap();
        map.setData( modelData );
//        Persistence persistence = Node.NodeInstance().getPersistence();

//        n.persistData( modelData );
//        n.setModelDataPersist(modelData);
//        Persistence persistence = Node.NodeInstance().getPersistence();
//        persistence.persistData( modelData );
    }

    /**
     * Load data references from file, which allows data to be mapped from one entity to another (input and outputs).
     * @param file
     */
    public static void LoadDataReferences( String file ) {
        Gson gson = new Gson();
        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken< List< ModelDataReference > >() {
            }.getType();

            List< ModelDataReference > references = gson.fromJson( jsonEntity, listType );
            for( ModelDataReference modelDataReference : references ) {
                _logger.debug( "Persisting data input reference for data: " + modelDataReference.dataKey + " with input data keys: " + modelDataReference.refKeys );
                DataRefUtil.SetDataReference( modelDataReference.dataKey, modelDataReference.refKeys );
            }
        }
        catch( Exception e ) {
            _logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    /**
     * Load Data objects from file into the system.
     * @param file
     */
    public static boolean ReadData( String file ) {
        boolean success = true;
        try {
            String jsonData = FileUtil.readFile( file );
            ImportData( jsonData );
        }
        catch( Exception e ) {
            success = false;
            _logger.error( e.toString(), e );
        }

        return success;
    }

    /**
     * Import Data objects to the system from serialized form as Json.
     *
     * @param jsonData
     * @throws Exception
     */
    public static void ImportData( String jsonData ) throws Exception {
        Collection< ModelData > modelDatas = ModelData.StringToModelDatas( jsonData );

        if( modelDatas == null ) {
            return;
        }

        for( ModelData modelData : modelDatas ) {
            SetData( modelData );
        }
    }

    /**
     * Imports serialized data from elsewhere, and saves it to persistence under a new key.
     * Since there may me many data in the imported JSON, we select one using its original key.
     * We then replace the old key (name) with a new one and save it to the current persistence layer.
     *
     * @param jsonData Serialized Data
     * @param oldKey Key when it was originally serialized
     * @param newKey Key for use in current application - data will be saved to this key.
     * @throws Exception
     */
    public static void ImportDataWithKey( String jsonData, String oldKey, String newKey ) throws Exception {
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken< List< ModelData > >() {
            }.getType();

            List< ModelData > modelDatas = gson.fromJson( jsonData, listType );

            for( ModelData modelData : modelDatas ) {

                if( !modelData.name.equals( oldKey ) ) {
                    continue;
                }

                modelData.name = newKey;

                SetData( modelData );
            }
        }
        catch( Exception e ) {
            throw( e );
        }
    }

}
