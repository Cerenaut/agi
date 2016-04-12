package io.agi.framework;

import com.google.gson.Gson;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.FastRandom;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * A conceptual Entity that has an update() method, and some children Entities.
 * <p>
 * Instances are transient and all state must be persisted as Data or Properties.
 * <p>
 * Created by dave on 14/02/16.
 */
public abstract class Entity extends NamedObject implements EntityListener {

    protected static final Logger _logger = LogManager.getLogger();

    public static final String SUFFIX_FLUSH = "flush"; /// Required: Triggers all flushable data to be persisted.
    public static final String SUFFIX_RESET = "reset"; /// Required: Triggers all flushable data to be persisted.

    protected Node _n;
    protected ModelEntity _model = null;
    protected EntityConfig _config = null;
    protected FastRandom _r;
    protected HashSet< String > _childrenWaiting = new HashSet< String >();
    protected HashMap< String, Data > _data = new HashMap< String, Data >();
    protected DataFlags _dataFlags = new DataFlags();
    protected DataMap _dataMap = new DataMap(); // used to check for data changes since load.
    protected boolean _flushChildren = false;

    public Entity( ObjectMap om, Node n, ModelEntity model ) {
        super( model.name, om );
        _model = model;
        _n = n;
        _r = new FastRandom();
    }

    public String getParent() {
        return _model.parent;
    }

    public String getType() {
        return _model.type;
    }

    /**
     * Returns a random number generator that is distributed and persisted.
     *
     * @return
     */
    public Random getRandom() {
        return _r; // A distributed, persisted random source, with one copy and one seed per entity. The seeds are persisted.
    }

    public Node getNode() {
        return _n;
    }

    /**
     * Get the keys for all the data that is used as input.
     * Prefetch it at the start of the update, never write it.
     *
     * @param attributes
     */
    public abstract void getInputAttributes( Collection< String > attributes );

    /**
     * Get the keys for all the data that is used as output.
     * Old value is prefetched at the start of the update, and value is written at the end of the update.
     *
     * @param attributes
     */
    public abstract void getOutputAttributes( Collection< String > attributes, DataFlags flags );

    /**
     * Return the class of the config object for the derived entity.
     *
     * @return
     */
    public abstract Class getConfigClass();

    /**
     * Get the config object, to be fetched at the start of update and persisted at the dn
     * (non input/output state used by the entity).
     */
    public EntityConfig getConfig() {
        return _config;
    }

    public void setConfig( EntityConfig config ) {
        _config = config;
    }

    /**
     * Create appropriate Config object, populate with the parameters expressed as a string in 'model.config'.
     * @return the populated config object
     */
    public EntityConfig createConfig() {
        Class configClass = getConfigClass();
        Gson gson = new Gson();
        String configString = _model.config;
        if( ( configString == null ) || ( configString.length() == 0 ) ) {
            configString = "{}";
        }
        EntityConfig config = ( EntityConfig ) gson.fromJson( configString, configClass );
        return config;
    }

    // if I cant issue another update to children until this has completed...
    // then children can't get out of sync

    public void update() {

        _config = createConfig();

        beforeUpdate();

        updateSelf();

        Persistence p = _n.getPersistence();
        Collection< String > childNames = p.getChildEntities( _name );

        if ( childNames.isEmpty() ) {
            afterUpdate(); // will notify the parent in the afterUpdate handler
            return;
        }

        // Require all children to flush on next update.
        // They can only be updated by this class, so we know they will respect the flush.
        if ( _flushChildren ) {
            for ( String childName : childNames ) {
                Framework.SetConfig( childName, SUFFIX_FLUSH, "true" );
            }
        }

        // Now wait for all children to update
        synchronized ( _childrenWaiting ) {
            _childrenWaiting.addAll( childNames );

            // add self as listener for these children
            for ( String childName : childNames ) {
                _n.addEntityListener( childName, this );
            }
        }

        // update all the children (Note, they will update on other Nodes potentially, and definitely in another thread.
        for ( String childName : childNames ) {
            _logger.info( "Request update of child: " + childName );
            _n.requestUpdate( childName ); // schedule an update, may have already occurred
            // update to child may occur any time after this, because only 1 parent so waiting for me to call the update.
        }
    }

    protected void beforeUpdate() {
        String entityName = getName();
        int age = _config.age; // getPropertyInt( SUFFIX_AGE, 1 );
        _logger.info( "START T: " + System.currentTimeMillis() + " Age " + age + " Thread " + Thread.currentThread().hashCode() + " Entity.update(): " + entityName );
    }

    protected void afterUpdate() {
        String entityName = getName();
        int age = _config.age; // getPropertyInt(SUFFIX_AGE, 1);
        _logger.info( "END   T: " + System.currentTimeMillis() + " Age " + age + " Thread " + Thread.currentThread().hashCode() + " Entity updated: " + entityName );

        _n.notifyUpdated( entityName ); // this entity, the parent, is now complete
    }

    public void onEntityUpdated( String entityName ) {
        _logger.info( "Entity: " + getName() + " being notified about: " + entityName );
        synchronized ( _childrenWaiting ) {
            _childrenWaiting.remove( entityName );

            //System.err.print( "Entity: " + getName() + " notified about: " + entityName + ", waiting for " );
            //for ( String child : _childrenWaiting ) {
            //    System.err.print( child + ", " );
            //}
            //System.err.println();

            if ( _childrenWaiting.isEmpty() ) {
                afterUpdate();
            }
            // else: wait for other children
        }
    }

    protected void updateSelf() {

        // 1. fetch inputs
        // get all the inputs and put them in the object map.
        Collection< String > inputKeys = new ArrayList< String >();
        getInputAttributes( inputKeys );
        fetchData( inputKeys );

        // 2. fetch outputs
        // get all the outputs and put them in the object map.
        Collection< String > outputKeys = new ArrayList< String >();
        getOutputAttributes( outputKeys, _dataFlags );
        fetchData( outputKeys );

        // Set the random number generator, with the current time (i.e. random), if not loaded.
        if ( _config.seed == null ) {
            _config.seed = System.currentTimeMillis();
        }

        _r.setSeed( _config.seed );

        // 3. doUpdateSelf()
        doUpdateSelf();

        if ( _config.reset ) {
            _config.age = 0;
        }
        else {
            _config.age++; // update age:
        }

        _flushChildren = _config.flush;

        _config.seed = _r.getSeed(); // update the random seed for next time.
        _config.reset = false; // cancel reset after reset.
        _config.flush = false; // clear flush after flush: if it was true, make it false.

        // 4. persist data
        // write all the outputs back to the persistence system
        persistData( outputKeys );

        // 5. persist config of this entity
        persistConfig();

        _logger.info( "Update: " + getName() + " age: " + _config.age );
    }

    public static String SerializeConfig( EntityConfig entityConfig ) {
        Gson gson = new Gson();
        String config = gson.toJson( entityConfig );
        return config;
    }

    protected void persistConfig() {
        _model.config = SerializeConfig( _config );
        Persistence p = _n.getPersistence();
        p.persistEntity( _model );
    }

    /**
     * Populate member object map with the persisted data.
     *
     * @param attributes
     */
    private void fetchData( Collection< String > attributes ) {
        Persistence p = _n.getPersistence();

        for ( String attribute : attributes ) {
            String inputKey = getKey( attribute );

            ModelData modelData = null;

            // check for cache policy
            if ( _dataFlags.hasFlag( attribute, DataFlags.FLAG_NODE_CACHE ) ) {
                Data d = _n.getCachedData( inputKey );

                if ( d != null ) {
                    _logger.info( "Skipping fetch of Data: " + inputKey );
                    _data.put( inputKey, d );
                    continue;
                }
            }

            // check for no - read
            if ( _dataFlags.hasFlag( attribute, DataFlags.FLAG_PERSIST_ONLY ) ) {
                _logger.info( "Skipping fetch of Data: " + inputKey );
                continue;
            }

            modelData = p.fetchData( inputKey );

            if ( modelData == null ) { // not found
                continue; // truthfully represent as null.
            }

            HashSet< String > refKeys = modelData.getRefKeys();

            if ( refKeys.isEmpty() ) {
                Data d = modelData.getData();
                _data.put( inputKey, d );
            }
            else {
                // Create an output matrix which is a composite of all the referenced inputs.
                HashMap< String, Data > allRefs = new HashMap< String, Data >();

                for ( String refKey : refKeys ) {
                    ModelData refJson = p.fetchData( refKey );
                    Data refData = refJson.getData();
                    allRefs.put( refKey, refData );
                }

                Data combinedData = getCombinedData( attribute, allRefs );

                modelData.setData( combinedData, false ); // data added to ref keys.

                p.persistData( modelData ); // DAVE: BUG? It writes it back out.. I guess we wanna see this, but seems excessive.

                _data.put( inputKey, combinedData );
            }
        }

        // check to see whether we need a backup of these structures, to implement the lazy-persist policy.
        for ( String keySuffix : _dataFlags._dataFlags.keySet() ) {
            if ( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_LAZY_PERSIST ) ) {
                String inputKey = getKey( keySuffix );
                Data d = _data.get( inputKey );
                if( d != null ) {
                    Data copy = new Data(d); // make a deep copy
                    _dataMap.putData(inputKey, copy);
                }
            }
        }
    }

    public void persistData( Collection< String > attributes ) {
        Persistence p = _n.getPersistence();

        for ( String keySuffix : attributes ) {
            String inputKey = getKey( keySuffix );
            Data d = _data.get( inputKey );

            // check for cache policy
            if ( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_NODE_CACHE ) ) {
                _n.setCachedData( inputKey, d ); // cache this one, so we don't need to read it next time.
            }

            if ( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_LAZY_PERSIST ) ) {
                Data copy = _dataMap.getData( inputKey );

                if( copy != null ) {
                    if (copy.isSameAs(d)) {
                        //System.err.println( "Skipping persist of Data: " + inputKey + " because: Not changed." );
                        continue; // don't persist.
                    }
                }
            }

            if ( _config.flush == false ) {
                if ( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_PERSIST_ON_FLUSH ) ) {
                    //System.err.println( "Skipping persist of Data: " + inputKey + " because: Only on flush, and not a flush." );
                    continue;
                }
            }

            boolean sparse = false;
            if ( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_SPARSE_UNIT ) ) {
                sparse = true;
            }

            p.persistData( new ModelData( inputKey, d, sparse ) );
        }
    }

    /**
     * Update the local member copy of the data, which will be persisted later.
     *
     * @param attribute
     * @param output
     */
    public void setData( String attribute, Data output ) {
        _data.put( getKey( attribute ), output );
    }

    /**
     * Default implementation: If a single reference, copy its shape.
     * Otherwise, creates a 1-D vector containing all input bits.
     * This is a reasonable solution for many cases where shape is not important.
     *
     * @param inputAttribute
     * @param allRefs
     * @return
     */
    protected Data getCombinedData( String inputAttribute, HashMap< String, Data > allRefs ) {

        // case 1: No input.
        int nbrRefs = allRefs.size();
        if ( nbrRefs == 0 ) {
            return null;
        }

        // case 2: Single input
        if ( nbrRefs == 1 ) {
            String refKey = allRefs.keySet().iterator().next();
            Data refData = allRefs.get( refKey );
            if ( refData == null ) {
                return null;
            }
            Data d = new Data( refData );
            return d;
        }

        // case 3: Multiple inputs (combine as vector)
        int sumVolume = 0;

        for ( String refKey : allRefs.keySet() ) {
            Data refData = allRefs.get( refKey );
            if ( refData == null ) {
                return null;
            }
            int volume = refData.getSize();
            sumVolume += volume;
        }

        Data d = new Data( sumVolume );

        int offset = 0;

        for ( String refKey : allRefs.keySet() ) {
            Data refData = allRefs.get( refKey );
            int volume = refData.getSize();

            d.copyRange( refData, offset, 0, volume );

            offset += volume;
        }

        return d;
    }

    public Data getData( String keySuffix ) {
        return _data.get( getKey( keySuffix ) );
    }

    /**
     * Get the data if it exists, and Create it if it doesn't.
     *
     * @param attribute   the name of the data
     * @param defaultSize create data of this size, if the data does not exist
     * @return data
     */
    public Data getData( String attribute, DataSize defaultSize ) {
        String key = getKey( attribute );
        Data d = _data.get( key );

        if ( d == null ) {
            d = new Data( defaultSize );
        }
        else {
            d.setSize( defaultSize );
        }

        return d;
    }

    /**
     * Gets the specified data structure. If null, or if it does not have the same dimensions as the specified size,
     * then it will be resized. The old values are not copied on resize.
     *
     * @param attribute
     * @param defaultSize
     * @return
     */
    public Data getDataLazyResize( String attribute, DataSize defaultSize ) {
        String key = getKey( attribute );
        Data d = _data.get( key );

        if ( d == null ) {
            d = new Data( defaultSize );
        }
        else if ( !d._dataSize.isSameAs( defaultSize ) ) {
            d.setSize( defaultSize );
        }

        return d;
    }

    /**
     * The actual function of the entity, to be implemented by derived classes.
     */
    protected void doUpdateSelf() {
        // default: Nothing.
    }

}
