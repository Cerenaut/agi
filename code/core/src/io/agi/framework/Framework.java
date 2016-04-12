package io.agi.framework;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.agi.core.orm.NamedObject;
import io.agi.core.util.FileUtil;
import io.agi.framework.coordination.http.HttpExportHandler;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelDataReference;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelEntityConfigPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Functions used throughout the experimental framework, i.e. not specific to Entity or Node.
 * Created by dave on 2/04/16.
 */
public class Framework {

    protected static final Logger logger = LogManager.getLogger();

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

    /**
     * Modifies the database to make the reference _entityName-suffix Data a reference input to the input _entityName-suffix.
     *
     * @param dataKey
     * @param refKeys
     */
    public static void SetDataReference(
            String dataKey,
            String refKeys ) {
        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelData modelData = persistence.fetchData( dataKey );

        if ( modelData == null ) {
            modelData = new ModelData( dataKey, refKeys );
        }

        modelData.refKeys = refKeys;
        persistence.persistData( modelData );
    }

    /**
     * Set the data in the model, in the persistence layer.
     * If an entry exists for this key, replace it.
     * @param modelData
     */
    public static void SetData( ModelData modelData ) {
        Persistence persistence = Node.NodeInstance().getPersistence();
        persistence.persistData( modelData );
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
        if ( me == null ) {
            return null;
        }

        return me.config;
    }

    /**
     * Allows a single config property to be modified.
     */
    public static void SetConfig( String entityName, String configPath, String value ) {
        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity me = persistence.fetchEntity( entityName );
        JsonParser parser = new JsonParser();
        JsonObject root = parser.parse( me.config ).getAsJsonObject();

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

        while ( index < maxIndex ) {
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
        me.config = root.toString();//getAsString();
        persistence.persistEntity( me );
    }

    public static JsonElement GetNestedProperty( JsonObject root, String path ) {
        // navigate to the nested property
        JsonElement je = root;
        String[] pathParts = path.split( "[.]" );
        String part = null;
        int index = 0;
        int maxIndex = pathParts.length - 1;

        while ( index < maxIndex ) {
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
     * @param file
     */
    public static void LoadEntities( String file ) {
        Gson gson = new Gson();

        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken< List< ModelEntity > >() {
            }.getType();
            List< ModelEntity > entities = gson.fromJson( jsonEntity, listType );

            for ( ModelEntity modelEntity : entities ) {
                logger.info( "Persisting Entity of type: " + modelEntity.type + ", that is hosted at Node: " + modelEntity.node );
                CreateEntity( modelEntity );
            }
        }
        catch ( Exception e ) {
            logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    public static void LoadData( String file ) {
        Gson gson = new Gson();
        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken< List< ModelData > >() {
            }.getType();

            List< ModelData > modelDatas = gson.fromJson( jsonEntity, listType );
            for ( ModelData modelData : modelDatas ) {
                Framework.SetData( modelData );
            }
        }
        catch ( Exception e ) {
            logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    public static void LoadDataReferences( String file ) {
        Gson gson = new Gson();
        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken< List< ModelDataReference > >() {
            }.getType();

            List< ModelDataReference > references = gson.fromJson( jsonEntity, listType );
            for ( ModelDataReference modelDataReference : references ) {
                logger.info( "Persisting data input reference for data: " + modelDataReference.dataKey + " with input data keys: " + modelDataReference.refKeys );
                Framework.SetDataReference( modelDataReference.dataKey, modelDataReference.refKeys );
            }
        }
        catch ( Exception e ) {
            logger.error( e.getStackTrace() );
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

            for ( ModelEntityConfigPath modelConfig : modelConfigs ) {

                logger.info( "Persisting entity: " + modelConfig._entityName + " config path: " + modelConfig._configPath + " value: " + modelConfig._configValue );

                Framework.SetConfig( modelConfig._entityName, modelConfig._configPath, modelConfig._configValue );
            }
        }
        catch ( Exception e ) {
            logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    /**
     * Export the subtree, in the form of a serialised representation that allows full re-import, to view or resume.
     *
     * @param entityName the parent of the subtree
     * @return serialised form of subtree
     */
    public static String ExportSubtree( String entityName, String type ) {
        String entitiesExport = null;

        if ( type.equalsIgnoreCase( HttpExportHandler.TYPE_ENTITY ) ) {
            entitiesExport = exportEntitiesSubtree( entityName );
        }
        else if ( type.equalsIgnoreCase( HttpExportHandler.TYPE_DATA ) ) {
            entitiesExport = exportDataEntitiesSubtree( entityName );
        }

        return entitiesExport;
    }

    protected static String exportDataEntitiesSubtree( String entityName ) {
        Gson gson = new Gson();
        Collection< ModelData > modelDatas = new ArrayList<>();

        subtreeModelDatas( entityName, modelDatas );

        String export = gson.toJson( modelDatas );
        return export;
    }

    /**
     * Get all the Data models for all entities in the subtree, and put in a flat collection.
     * @param entityName the parent of the subtree.
     * @param modelDatas the flat collection that will contain the data models.
     */
    protected static void subtreeModelDatas( String entityName, Collection< ModelData > modelDatas ) {
        Node node = Node.NodeInstance();
        addModelDatasForEntity( entityName, modelDatas );

        Collection< String > childNames = node.getPersistence().getChildEntities( entityName );
        for ( String childName : childNames ) {
            subtreeModelDatas( childName, modelDatas );
        }
    }

    protected static void addModelDatasForEntity( String entityName, Collection<ModelData> modelDatas ) {

        Node node = Node.NodeInstance();

        ModelEntity modelEntity = node.getPersistence().fetchEntity( entityName );

        Entity entity = node.getEntityFactory().create( node.getObjectMap(), modelEntity );
        entity._config = entity.createConfig();

        Collection< String > attributes = new ArrayList<>();
        DataFlags dataFlags = new DataFlags();
        entity.getOutputAttributes( attributes, dataFlags );

        for ( String attribute : attributes ) {
            String outputKey = entity.getKey( attribute );
            ModelData modelData = node.getPersistence().fetchData( outputKey );

            if ( modelData != null ) {
                modelDatas.add( modelData );
            }
        }
    }

    protected static String exportEntitiesSubtree( String entityName ) {
        Persistence persistence = Node.NodeInstance().getPersistence();
        Gson gson = new Gson();
        Collection< ModelEntity > modelEntities = new ArrayList<>( );
        subtreeModelEntities( entityName, modelEntities );
        String export = gson.toJson( modelEntities );
        return export;
    }

    /**
     * Flatten subtree of a given entity, referenced by name, into a collection of entity models.
     * Recursive method.
     * @param entityName
     * @param modelEntities
     */
    protected static void subtreeModelEntities( String entityName, Collection< ModelEntity > modelEntities ) {
        // traverse tree depth first via recursion, building the string representation
        Persistence persistence = Node.NodeInstance().getPersistence();
        ModelEntity modelEntity = persistence.fetchEntity( entityName );
        modelEntities.add( modelEntity );

        Collection< String > childNames = persistence.getChildEntities( entityName );
        for ( String childName : childNames ) {
            subtreeModelEntities( childName, modelEntities );
        }
    }


    public static boolean ImportSubtree( String subtree ) {
        return false;
    }

    public static boolean ImportSubtree( String entityFilename, String dataFilename ) {

        Framework.LoadEntities( entityFilename );
        Framework.LoadData( dataFilename );

        return true;
    }


}
