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

package io.agi.framework;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.agi.core.orm.AbstractPair;
import io.agi.core.orm.NamedObject;
import io.agi.core.util.FileUtil;
import io.agi.core.util.MemoryUtil;
import io.agi.framework.coordination.http.HttpExportHandler;
import io.agi.framework.persistence.DenseDataDeserializer;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelDataReference;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelEntityConfigPath;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Functions used throughout the experimental framework, i.e. not specific to Entity or Node.
 * Created by dave on 2/04/16.
 */
public class Framework {

    protected static final Logger _logger = LogManager.getLogger();

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
        if( Framework.entityNamePrefix == null ) {
            return "";
        }

        return Framework.entityNamePrefix;
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
        Framework.entityNamePrefix = entityNamePrefix;
        return Framework.entityNamePrefix;
    }

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
        String inputKey = NamedObject.GetKey( inputEntity, inputSuffix );
        String refKey = NamedObject.GetKey( referenceEntity, referenceSuffix );
        SetDataReference( inputKey, refKey );
    }

    public static void SetDataReferences(
            String inputEntity,
            String inputSuffix,
            ArrayList< AbstractPair< String, String > > referenceEntitySuffixes ) {
        String inputKey = NamedObject.GetKey( inputEntity, inputSuffix );
        String refKeys = "";

        for( AbstractPair< String, String > ap : referenceEntitySuffixes ) {
            String referenceEntity = ap._first;
            String referenceSuffix = ap._second;
            String refKey = NamedObject.GetKey( referenceEntity, referenceSuffix );
            if( refKeys.length() > 0 ) {
                refKeys = refKeys + ",";
            }
            refKeys = refKeys + refKey;
        }

        SetDataReference( inputKey, refKeys );
    }

    /**
     * Modifies the database to make the reference _entityName-suffix Data a reference input to the input _entityName-suffix.
     *
     * @param dataKey
     * @param refKeys
     */
    public static void SetDataReference(
            String dataKey,
            String refKeys ) {
        Node n = Node.NodeInstance();
//        ModelData modelData = n.fetchData( dataKey );
        ModelData modelData = n.getModelData( dataKey, new DenseDataDeserializer() );

        if( modelData == null ) {
            modelData = new ModelData( dataKey, refKeys );
        }

        modelData.refKeys = refKeys;
//        Persistence persistence = n.getPersistence();
//        persistence.persistData(modelData);
//        n.persistData( modelData ); // ensure cache is cleared
        n.setModelDataPersist( modelData );
    }

    /**
     * Set the data in the model, in the persistence layer.
     * If an entry exists for this key, replace it.
     *
     * @param modelData
     */
    public static void SetData( ModelData modelData ) {
        Node n = Node.NodeInstance();
//        n.persistData( modelData );
        n.setModelDataPersist(modelData);
//        Persistence persistence = Node.NodeInstance().getPersistence();
//        persistence.persistData( modelData );
    }

    /**
     * Allows a single config property to be obtained.
     *
     * @param entityName
     * @param configPath
     */
    public static String GetConfig( String entityName, String configPath ) {
        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity me = persistence.fetchEntity( entityName );
        JsonParser parser = new JsonParser();
        JsonObject jo = parser.parse( me.config ).getAsJsonObject();

        // navigate to the nested property
        JsonElement je = GetNestedProperty( jo, configPath );

        return je.getAsString();
    }

    /**
     * Gets the complete config object for the given entity.
     *
     * @param entityName
     * @return
     */
    public static String GetConfig( String entityName ) {
        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity me = persistence.fetchEntity( entityName );
        if( me == null ) {
            return null;
        }

        return me.config;
    }

    /**
     * Allows the config properties to be set with a config object.
     * WARNING: if the wrong type of config is used (i.e. it has properties not intended for this entity) then there
     * will be downstream issues.
     * @param config
     */
    public static void SetConfig( String entityName, EntityConfig config ) {

        _logger.debug( "Set config of: " + entityName + " to the following: " +
                new GsonBuilder().setPrettyPrinting().create().toJson( config ) );

        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.fetchEntity( entityName );
        String configAsString = new Gson().toJson( config );
        modelEntity.config = configAsString;
    }

    /**
     * Allows a single config property to be modified.
     */
    public static void SetConfig( String entityName, String configPath, String value ) {

        _logger.debug( "Set config of: " + entityName + " path: " + configPath + " value: " + value );

        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.fetchEntity( entityName );
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse( modelEntity.config ).getAsJsonObject();

        // navigate to the nested property
        // N.B. String.split : http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        JsonObject parent = root;
        String[] pathParts = configPath.split( "[.]" );
        int index = 0;
        int maxIndex = pathParts.length - 1; // NOTE: one before the one we're looking for
        String part = null;
//        if( pathParts.length < 2 ) { // i.e. 0 or 1
//            part = configPath;
//        }

        while( index < maxIndex ) {
            part = pathParts[ index ];
            JsonElement child = parent.get( part );

            ++index;

            parent = ( JsonObject ) child;
        }

        part = pathParts[ index ];

        // replace the property:
        parent.remove( part );
        parent.addProperty( part, value );

        // re-serialize the whole thing
        modelEntity.config = root.toString();//getAsString();
        persistence.persistEntity( modelEntity );
    }

    public static void SetConfigBoolean( String entityName, String configPath, Boolean value ) {

        _logger.debug( "Set config of: " + entityName + " path: " + configPath + " value: " + value );

        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.fetchEntity( entityName );
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse( modelEntity.config ).getAsJsonObject();

        // navigate to the nested property
        // N.B. String.split : http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        JsonObject parent = root;
        String[] pathParts = configPath.split( "[.]" );
        int index = 0;
        int maxIndex = pathParts.length - 1; // NOTE: one before the one we're looking for
        String part = null;
//        if( pathParts.length < 2 ) { // i.e. 0 or 1
//            part = configPath;
//        }

        while( index < maxIndex ) {
            part = pathParts[ index ];
            JsonElement child = parent.get( part );

            ++index;

            parent = ( JsonObject ) child;
        }

        part = pathParts[ index ];

        // replace the property:
        parent.remove( part );
        parent.addProperty( part, value );

        // re-serialize the whole thing
        modelEntity.config = root.toString();//getAsString();
        persistence.persistEntity( modelEntity );
    }

    public static void SetConfigInteger( String entityName, String configPath, Integer value ) {

        _logger.debug( "Set config of: " + entityName + " path: " + configPath + " value: " + value );

        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.fetchEntity( entityName );
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse( modelEntity.config ).getAsJsonObject();

        // navigate to the nested property
        // N.B. String.split : http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        JsonObject parent = root;
        String[] pathParts = configPath.split( "[.]" );
        int index = 0;
        int maxIndex = pathParts.length - 1; // NOTE: one before the one we're looking for
        String part = null;
//        if( pathParts.length < 2 ) { // i.e. 0 or 1
//            part = configPath;
//        }

        while( index < maxIndex ) {
            part = pathParts[ index ];
            JsonElement child = parent.get( part );

            ++index;

            parent = ( JsonObject ) child;
        }

        part = pathParts[ index ];

        // replace the property:
        parent.remove( part );
        parent.addProperty( part, value );

        // re-serialize the whole thing
        modelEntity.config = root.toString();//getAsString();
        persistence.persistEntity( modelEntity );
    }

    public static void SetConfigFloat( String entityName, String configPath, Float value ) {

        _logger.debug( "Set config of: " + entityName + " path: " + configPath + " value: " + value );

        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.fetchEntity( entityName );
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse( modelEntity.config ).getAsJsonObject();

        // navigate to the nested property
        // N.B. String.split : http://stackoverflow.com/questions/3481828/how-to-split-a-string-in-java
        JsonObject parent = root;
        String[] pathParts = configPath.split( "[.]" );
        int index = 0;
        int maxIndex = pathParts.length - 1; // NOTE: one before the one we're looking for
        String part = null;
//        if( pathParts.length < 2 ) { // i.e. 0 or 1
//            part = configPath;
//        }

        while( index < maxIndex ) {
            part = pathParts[ index ];
            JsonElement child = parent.get( part );

            ++index;

            parent = ( JsonObject ) child;
        }

        part = pathParts[ index ];

        // replace the property:
        parent.remove( part );
        parent.addProperty( part, value );

        // re-serialize the whole thing
        modelEntity.config = root.toString();//getAsString();
        persistence.persistEntity( modelEntity );
    }

    public static JsonElement GetNestedProperty( JsonObject root, String path ) {
        // navigate to the nested property
        JsonElement je = root;
        String[] pathParts = path.split( "[.]" );
        String part = null;
        int index = 0;
        int maxIndex = pathParts.length;

        while( index < maxIndex ) {
            part = pathParts[ index ];

            JsonObject joParent = ( JsonObject ) je; // there is more to find
            JsonElement jeChild = joParent.get( part );

            ++index;

            je = jeChild;
        }

        return je;
    }

    /**
     * Create an entity as specified, generate its config, and persist to disk.
     *
     * @param name
     * @param type
     * @param node
     * @param parent
     */
    public static void CreateEntity( String name, String type, String node, String parent ) {
        String config = "";
        ModelEntity model = new ModelEntity( name, type, node, parent, config );
        CreateEntity( model );
    }

    /**
     * Create an entity in the persistence layer using the model.
     * Create config object from data model, convert to a string, set back to model and persist.
     * The effect is that any undefined fields will still be present (with value of null) in the persistence layer.
     *
     * @param model
     */
    public static void CreateEntity( ModelEntity model ) {
        Node node = Node.NodeInstance();
        Entity entity = node.getEntityFactory().create( node.getObjectMap(), model );
        EntityConfig entityConfig = entity.createConfig();
        model.config = Entity.SerializeConfig( entityConfig );
        node.getPersistence().persistEntity( model );
    }

    /**
     * Create entities in the persistence layer, represented in the file (see file format).
     * TODO document the file format
     *
     * @param file
     */
    public static void LoadEntities( String file ) {
        try {
            String jsonEntities = FileUtil.readFile( file );
            ImportEntities( jsonEntities );
        }
        catch( Exception e ) {
            _logger.error( e.toString(), e );
        }
    }

    /**
     * Import Entities to the system from serialized form as Json.
     *
     * @param jsonEntities
     * @throws Exception
     */
    public static void ImportEntities( String jsonEntities ) throws Exception {
        Gson gson = new Gson();

        try {
            Type listType = new TypeToken< List< ModelEntity > >() {
            }.getType();
            List< ModelEntity > entities = gson.fromJson( jsonEntities, listType );

            for( ModelEntity modelEntity : entities ) {
                _logger.debug( "Creating Entity of type: " + modelEntity.type + ", that is hosted at Node: " + modelEntity.node );
                CreateEntity( modelEntity );
            }
        }
        catch( Exception e ) {
            throw( e );
        }
    }

    /**
     * Load Data objects from file into the system.
     * @param file
     */
    public static void LoadData( String file ) {
        try {
            String jsonData = FileUtil.readFile( file );
            ImportData( jsonData );
        }
        catch( Exception e ) {
            _logger.error( e.toString(), e );
        }
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
            Framework.SetData( modelData );
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

                Framework.SetData( modelData );
            }
        }
        catch( Exception e ) {
            throw( e );
        }
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
                Framework.SetDataReference( modelDataReference.dataKey, modelDataReference.refKeys );
            }
        }
        catch( Exception e ) {
            _logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    public static void LoadConfigs( String file ) {
        Gson gson = new Gson();
        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken< List< ModelEntityConfigPath > >() {
            }.getType();
            List< ModelEntityConfigPath > modelConfigs = gson.fromJson( jsonEntity, listType );

            for( ModelEntityConfigPath modelConfig : modelConfigs ) {

                _logger.debug( "Persisting entity: " + modelConfig.entityName + " config path: " + modelConfig.configPath + " value: " + modelConfig.configValue );

                Framework.SetConfig( modelConfig.entityName, modelConfig.configPath, modelConfig.configValue );
            }
        }
        catch( Exception e ) {
            _logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    protected static String GetEntityDataSubtree( String entityName, boolean onlyDataRefs ) {

        Collection< ModelData > modelDatas = new ArrayList<>();

        if ( !onlyDataRefs ) {
            GetEntityDataSubtree( entityName, modelDatas );
        }
        else {
            Collection< ModelData > modelDatasFiltered = new ArrayList<>( );
            for ( ModelData modelData : modelDatas ) {
                if (modelData.isReference()) {
                    modelData.zeroData();
                    modelDatasFiltered.add( modelData );
                }
            }

            modelDatas = modelDatasFiltered;
        }

        Gson gson;
        if ( onlyDataRefs ) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        else {
            gson = new Gson();
        }

        String export = gson.toJson( modelDatas );
        return export;
    }

    /**
     * Get all the Data models for all entities in the subtree, and put in a flat collection.
     *
     * @param entityName the parent of the subtree.
     * @param modelDatas the flat collection that will contain the data models.
     */
    protected static void GetEntityDataSubtree( String entityName, Collection< ModelData > modelDatas ) {
        Node node = Node.NodeInstance();

        _logger.debug( "Get subtree for entity: " + entityName );
        MemoryUtil.logMemory( _logger );

        AddEntityData( entityName, modelDatas );
        Collection< String > childNames = node.getPersistence().getChildEntities( entityName );
        for( String childName : childNames ) {
            GetEntityDataSubtree( childName, modelDatas );
        }
    }

    protected static void AddEntityData( String entityName, Collection< ModelData > modelDatas ) {

        Node node = Node.NodeInstance();

        ModelEntity modelEntity = node.getPersistence().fetchEntity( entityName );

        Entity entity = node.getEntityFactory().create( node.getObjectMap(), modelEntity );
        entity._config = entity.createConfig();

        Collection< String > attributesOut = new ArrayList<>();
        Collection< String > attributesIn = new ArrayList<>();
        DataFlags dataFlags = new DataFlags();
        entity.getOutputAttributes( attributesOut, dataFlags );
        entity.getInputAttributes( attributesIn );

        Collection< String > attributes = new ArrayList<>(  );
        attributes.addAll( attributesIn );
        attributes.addAll( attributesOut );
        for( String attribute : attributes ) {
            String outputKey = entity.getKey( attribute );
            ModelData modelData = node.getModelData( outputKey, new DenseDataDeserializer() );

            if( modelData != null ) {
                modelDatas.add( modelData );
            }
        }
    }

    protected static String GetEntitySubtree( String entityName ) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Collection< ModelEntity > modelEntities = new ArrayList<>();
        AddEntitySubtree( entityName, modelEntities );
        String export = gson.toJson( modelEntities );
        return export;
    }

    /**
     * Flatten subtree of a given entity, referenced by name, into a collection of entity models.
     * Recursive method.
     *
     * @param entityName
     * @param modelEntities
     */
    protected static void AddEntitySubtree( String entityName, Collection< ModelEntity > modelEntities ) {
        // traverse tree depth first via recursion, building the string representation
        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.fetchEntity( entityName );
        modelEntities.add( modelEntity );

        Collection< String > childNames = persistence.getChildEntities( entityName );
        for( String childName : childNames ) {
            AddEntitySubtree( childName, modelEntities );
        }
    }

    /**
     * Export a subtree of entities and data, in the form of a serialised representation that allows full re-import,
     * to view or resume.
     *
     * @param entityName the parent of the subtree
     * @param type the type of export, it can be 'data' or 'entity'
     * @return serialised form of subtree
     */
    public static String ExportSubtree( String entityName, String type ) {
        String entitiesExport = null;

        if( type.equalsIgnoreCase( HttpExportHandler.TYPE_ENTITY ) ) {
            entitiesExport = GetEntitySubtree( entityName );
        } else if( type.equalsIgnoreCase( HttpExportHandler.TYPE_DATA ) ) {
            entitiesExport = GetEntityDataSubtree( entityName, false );
        } else if( type.equalsIgnoreCase( HttpExportHandler.TYPE_DATA_REFS ) ) {
            entitiesExport = GetEntityDataSubtree( entityName, true );
        }
        return entitiesExport;
    }

    /**
     * Import a subtree of entities and data.
     *
     * @param jsonEntities
     * @param jsonData
     * @return
     */
    public static boolean ImportSubtree( String jsonEntities, String jsonData ) {
        try {
            Framework.ImportEntities( jsonEntities );
            Framework.ImportData( jsonData );
            return true;
        }
        catch( Exception e ) {
            _logger.error( e.getStackTrace() );
            return false;
        }
    }

    public static boolean SaveSubtree( String entityName, String type, String path ) {
        boolean success = false;
        String subtree = Framework.ExportSubtree( entityName, type );

        File file = new File( path );
        try {
            FileUtils.writeStringToFile( file, subtree );
            file.setWritable(true, false);
            success = true;
        }
        catch( IOException e ) {
            _logger.error( "Unable to save subtree for entity: " + entityName );
            _logger.error( e.toString(), e );
        }

        return success;
    }

    public static boolean containsEntity( String entityName ) {

        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.fetchEntity( entityName );

        if (modelEntity == null) {
            return false;
        }
        else {
            return true;
        }
    }
}
