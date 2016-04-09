package io.agi.framework;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.agi.core.orm.NamedObject;
import io.agi.core.util.FileUtil;
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
     * @param p
     * @param inputEntity
     * @param inputSuffix
     * @param referenceEntity
     * @param referenceSuffix
     */
    public static void SetDataReference(
            Persistence p,
            String inputEntity,
            String inputSuffix,
            String referenceEntity,
            String referenceSuffix ) {
        String inputKey = NamedObject.GetKey( inputEntity, inputSuffix );
        String refKey = NamedObject.GetKey( referenceEntity, referenceSuffix );
        SetDataReference( p, inputKey, refKey );
    }

    /**
     * Modifies the database to make the reference _entityName-suffix Data a reference input to the input _entityName-suffix.
     *
     * @param p
     * @param dataKey
     * @param refKeys
     */
    public static void SetDataReference(
            Persistence p,
            String dataKey,
            String refKeys ) {
        ModelData modelData = p.fetchData( dataKey );

        if ( modelData == null ) {
            modelData = new ModelData( dataKey, refKeys );
        }

        modelData._refKeys = refKeys;
        p.persistData( modelData );
    }

    /**
     * Allows a single config property to be obtained.
     *
     * @param entityName
     * @param configPath
     */
    public static String GetConfig( Persistence p, String entityName, String configPath ) {
        ModelEntity me = p.fetchEntity( entityName );
        JsonParser parser = new JsonParser();
        JsonObject jo = parser.parse( me.config ).getAsJsonObject();

        // navigate to the nested property
        JsonElement je = GetNestedProperty( jo, configPath );

        return je.getAsString();
    }

    /**
     * Gets the complete config object for the given entity.
     *
     * @param p
     * @param entityName
     * @return
     */
    public static String GetConfig( Persistence p, String entityName ) {
        ModelEntity me = p.fetchEntity( entityName );
        if ( me == null ) {
            return null;
        }

        return me.config;
    }

    /**
     * Allows a single config property to be modified.
     */
    public static void SetConfig( Persistence p, String entityName, String configPath, String value ) {
        ModelEntity me = p.fetchEntity( entityName );
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
        p.persistEntity( me );
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
     * @param n
     * @param name
     * @param type
     * @param node
     * @param parent
     */
    public static void CreateEntity( Node n, String name, String type, String node, String parent ) {
        String config = "";
        ModelEntity model = new ModelEntity( name, type, node, parent, config );
        CreateEntity( n, model );
    }

    /**
     * This assumes that model.config has not been set. It is overwritten.
     * @param n
     * @param model
     */
    public static void CreateEntity( Node n, ModelEntity model ) {
        Entity entity = n.getEntityFactory().create( n.getObjectMap(), model );
        EntityConfig entityConfig = entity.createConfig();
        model.config = Entity.SerializeConfig( entityConfig );
        Persistence p = n.getPersistence();
        p.persistEntity( model );
    }

    /**
     * Create entities in the persistence layer, represented in the file (see file format).
     * TODO document the file format
     * @param n
     * @param file
     */
    public static void LoadEntities( Node n, String file ) {
        Gson gson = new Gson();

        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken< List< ModelEntity > >() {
            }.getType();
            List< ModelEntity > entities = gson.fromJson( jsonEntity, listType );

            for ( ModelEntity modelEntity : entities ) {
                logger.info( "Persisting Entity of type: " + modelEntity.type + ", that is hosted at Node: " + modelEntity.node );
                CreateEntity( n, modelEntity );
            }
        }
        catch ( Exception e ) {
            logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    /**
     * Save entities to file form (see file format).
     *
     * @param entities collection of EntityModel's
     * @param file the file in which to save the entities
     */
    public static void SaveEntities( Collection< ModelEntity > entities, String file ) {

    }



    public static void LoadDataReferences( Persistence p, String file ) {
        Gson gson = new Gson();
        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken< List< ModelDataReference > >() {
            }.getType();

            List< ModelDataReference > references = gson.fromJson( jsonEntity, listType );
            for ( ModelDataReference modelDataReference : references ) {
                logger.info( "Persisting data input reference for data: " + modelDataReference.dataKey + " with input data keys: " + modelDataReference.refKeys );
                Framework.SetDataReference( p, modelDataReference.dataKey, modelDataReference.refKeys );
            }
        }
        catch ( Exception e ) {
            logger.error( e.getStackTrace() );
            System.exit( -1 );
        }
    }

    public static void LoadConfigs( Persistence p, String file ) {
        Gson gson = new Gson();
        try {
            String jsonEntity = FileUtil.readFile( file );

            Type listType = new TypeToken< List< ModelEntityConfigPath > >() {
            }.getType();
            List< ModelEntityConfigPath > modelConfigs = gson.fromJson( jsonEntity, listType );

            for ( ModelEntityConfigPath modelConfig : modelConfigs ) {

                logger.info( "Persisting entity: " + modelConfig._entityName + " config path: " + modelConfig._configPath + " value: " + modelConfig._configValue );

                Framework.SetConfig( p, modelConfig._entityName, modelConfig._configPath, modelConfig._configValue );
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
     * @param node Node current node // persistence object to the persistence layer
     * @param entityName the parent of the subtree
     * @return serialised form of subtree
     */
    public static String ExportSubtree( Node node, String entityName ) {
        String entitiesExport = null;

        //entitiesExport = exportEntitiesSubtree( node._p, entityName );
        entitiesExport = exportDataEntitiesSubtree( node, entityName );

        return entitiesExport;
    }

    protected static String exportDataEntitiesSubtree( Node node, String entityName ) {

        Gson gson = new Gson();
        Collection< ModelData > modelDatas = new ArrayList<>();

        subtreeModelDatas( node, entityName, modelDatas );

        String export = gson.toJson( modelDatas );
        return export;
    }

    /**
     * Get all the Data models for all entities in the subtree, and put in a flat collection.
     * @param entityName the parent of the subtree.
     * @param modelDatas the flat collection that will contain the data models.
     */
    protected static void subtreeModelDatas( Node node, String entityName, Collection< ModelData > modelDatas ) {

        addModelDatasForEntity( node, entityName, modelDatas );

        Collection< String > childNames = node._p.getChildEntities( entityName );
        for ( String childName : childNames ) {
            subtreeModelDatas( node, childName, modelDatas );
        }
    }

    protected static void addModelDatasForEntity( Node node, String entityName, Collection<ModelData> modelDatas ) {

        ModelEntity modelEntity = node._p.fetchEntity( entityName );

        Entity entity = node.getEntityFactory().create( node.getObjectMap(), modelEntity );
        entity._config = entity.createConfig();

        Collection< String > attributes = new ArrayList<>();
        DataFlags dataFlags = new DataFlags();
        entity.getOutputAttributes( attributes, dataFlags );

        for ( String attribute : attributes ) {
            String outputKey = entity.getKey( attribute );
            ModelData modelData = node._p.fetchData( outputKey );

            if ( modelData != null ) {
                modelDatas.add( modelData );
            }
        }
    }

    protected static String exportEntitiesSubtree( Persistence p, String entityName ) {
        Gson gson = new Gson();
        Collection< ModelEntity > modelEntities = new ArrayList<>( );
        subtreeModelEntities( p, entityName, modelEntities );
        String export = gson.toJson( modelEntities );
        return export;
    }

    /**
     * Flatten subtree of a given entity, referenced by name, into a collection of entity models.
     * Recursive method.
     * @param p
     * @param entityName
     * @param modelEntities
     */
    protected static void subtreeModelEntities( Persistence p, String entityName, Collection< ModelEntity > modelEntities ) {
        // traverse tree depth first via recursion, building the string representation
        ModelEntity modelEntity = p.fetchEntity( entityName );
        modelEntities.add( modelEntity );

        Collection< String > childNames = p.getChildEntities( entityName );
        for ( String childName : childNames ) {
            subtreeModelEntities( p, childName, modelEntities );
        }
    }


    public static boolean ImportSubtree( Persistence p, String subtree ) {
        return false;
    }
}
