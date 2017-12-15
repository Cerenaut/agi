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

package io.agi.framework.persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.agi.core.orm.AbstractPair;
import io.agi.core.orm.Keys;
import io.agi.core.util.FileUtil;
import io.agi.core.util.MemoryUtil;
import io.agi.framework.*;
import io.agi.framework.coordination.http.HttpExportHandler;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelEntityConfigPath;
import io.agi.framework.references.DataRef;
import io.agi.framework.references.DataRefMap;
import io.agi.framework.references.DataRefUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility functions to manipulate the serialized form and persistence layer directly.
 *
 * Node state isn't updated.
 *
 * Created by dave on 24/10/17.
 */
public class PersistenceUtil {

    protected static final Logger _logger = LogManager.getLogger();

    /**
     * Gets the complete config object for the given entity.
     *
     * @param entityName
     * @return
     */
    public static String GetConfig( String entityName ) {
        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity me = persistence.getEntity( entityName );
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
        ModelEntity modelEntity = persistence.getEntity( entityName );
        String configAsString = new Gson().toJson( config );
        modelEntity.config = configAsString;
    }

    /**
     * Allows a single config property to be modified.
     */
    private static void SetConfigProperty( String entityName, String configPath, Object value ) {

        _logger.debug( "Set config of: " + entityName + " path: " + configPath + " value: " + value );

        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.getEntity( entityName );
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

        if( value instanceof Boolean ) {
            parent.addProperty( part, ( Boolean ) value );
        } else if( value instanceof Integer ) {
            parent.addProperty( part, ( Integer ) value );
        } else if( value instanceof Float ) {
            parent.addProperty( part, ( Float ) value );
        } else if ( value instanceof Long ) {
            parent.addProperty( part, ( Long ) value );
        } else {
            parent.addProperty( part, ( String ) value );
        }

        // re-serialize the whole thing
        modelEntity.config = root.toString();//getAsString();
        persistence.persistEntity( modelEntity );
    }

    /**
     * Allows a single config property to be modified.
     */
    public static void SetConfig( String entityName, String configPath, String value ) {

        SetConfigProperty( entityName, configPath, value );
    }

    public static void SetConfigBoolean( String entityName, String configPath, Boolean value ) {

        SetConfigProperty( entityName, configPath, value );
    }

    public static void SetConfigInteger( String entityName, String configPath, Integer value ) {

        SetConfigProperty( entityName, configPath, value );
    }

    public static void SetConfigFloat( String entityName, String configPath, Float value ) {

        SetConfigProperty( entityName, configPath, value );
    }

    public static void SetConfigLong( String entityName, String configPath, Long value ) {

        SetConfigProperty( entityName, configPath, value );
    }

    /**
     * Allows a single config property to be obtained.
     *
     * @param entityName
     * @param configPath
     */
    public static String GetConfig( String entityName, String configPath ) {
        try {
            Persistence persistence = Node.NodeInstance().getPersistence();
            ModelEntity me = persistence.getEntity( entityName );
            JsonParser parser = new JsonParser();
            JsonObject jo = parser.parse( me.config ).getAsJsonObject();

            // navigate to the nested property
            JsonElement je = GetNestedProperty( jo, configPath );

            return je.getAsString();
        }
        catch( Exception e ) {
            _logger.error( e.toString(), e );
            return null;
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

                PersistenceUtil.SetConfig( modelConfig.entityName, modelConfig.configPath, modelConfig.configValue );
            }
        }
        catch( Exception e ) {
            _logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
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

    public static String GetEntityName( String entityNameSuffix ) {
        return Naming.GetEntityName( entityNameSuffix );
    }

        /**
         * Create an entity as specified, generate its config, and persist to disk.
         *
         * @param name
         * @param type
         * @param node
         * @param parent
         * @return name The name of the entity created, which might be the next parent (makes for neat code)
         */
    public static String CreateEntity( String name, String type, String node, String parent ) {
        String config = "";
        ModelEntity model = new ModelEntity( name, type, node, parent, config );
        CreateEntity( model );
        return name;
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
        Persistence p = node.getPersistence();
        p.persistEntity( model );
    }

    /**
     * Create entities in the persistence layer, represented in the file (see file format).
     * TODO document the file format
     *
     * @param file
     */
    public static boolean ReadEntities( String file ) {
        boolean success = true;
        try {
            String jsonEntities = FileUtil.readFile( file );
            ImportEntities( jsonEntities );
        }
        catch( Exception e ) {
            success = false;
            _logger.error( e.toString(), e );
        }

        return success;
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
        ModelEntity modelEntity = persistence.getEntity( entityName );
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
            PersistenceUtil.ImportEntities( jsonEntities );
            DataRefUtil.ImportData( jsonData );
            return true;
        }
        catch( Exception e ) {
            _logger.error( e.getStackTrace() );
            return false;
        }
    }

    public static boolean SaveSubtree( String entityName, String type, String path ) {
        boolean success = false;
        String subtree = ExportSubtree( entityName, type );

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

    public static boolean EntityExists( String entityName ) {

        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.getEntity( entityName );

        if( modelEntity == null ) {
            return false;
        }
        else {
            return true;
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
                    modelData.clearSerializedData();
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
        DataRefMap map = node.getDataRefMap();

        ModelEntity modelEntity = node.getPersistence().getEntity( entityName );

        Entity entity = node.getEntityFactory().create( node.getObjectMap(), modelEntity );
        entity.setConfig( entity.createConfig() ); // create a blank config object

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
            //ModelData modelData = node.getModelData( outputKey, new DataRef.DenseDataRefResolver() );
            DataRef dataRef = map.getData( outputKey );
            ModelData modelData = new ModelData();
            boolean b = modelData.serialize( dataRef );
            if( b ) {
                modelDatas.add( modelData );
            }
        }
    }
}
