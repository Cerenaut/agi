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

import com.google.gson.Gson;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.FastRandom;
import io.agi.core.orm.AbstractPair;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.persistence.DataDeserializer;
import io.agi.framework.persistence.DenseDataDeserializer;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;
import io.agi.framework.persistence.models.ModelEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * A conceptual Entity that has an update() method, and some children Entities.
 * <p/>
 * Instances are transient and all state must be persisted as Data or Properties.
 * <p/>
 * Created by dave on 14/02/16.
 */
public abstract class Entity extends NamedObject implements EntityListener, DataDeserializer {

    protected static final Logger _logger = LogManager.getLogger();

    public static final String SUFFIX_FLUSH = "flush"; /// Required: Triggers all flushable data to be persisted.
    public static final String SUFFIX_RESET = "reset"; /// Required: Triggers all flushable data to be persisted.

    protected Node _n;
    protected ModelEntity _model = null;
    protected EntityConfig _config = null;
    protected FastRandom _r;
    protected HashSet< String > _childrenWaiting = new HashSet<>();
    protected HashMap< String, Data > _data = new HashMap<>();
    protected DataFlags _dataFlags = new DataFlags();
//    protected DataMap _dataCopy = new DataMap(); // used to check for data changes since load.
    protected boolean _flushChildren = false;
    protected boolean _resetChildren = false;

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
     * Override to add data refs at run time, to connect entities.
     * @param input2refs
     * @param flags
     */
    public void getInputRefs( HashMap< String, AbstractPair< String, String> > input2refs, DataFlags flags ) {
    }

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
     *
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

        if( childNames.isEmpty() ) {
            afterUpdate(); // will notify the parent in the afterUpdate handler
            return;
        }

        // Propagate reset and flush behaviour to all children.
        // They can only be updated by this class, so we know they will respect the flush.
        for( String childName : childNames ) {
            Framework.SetConfig( childName, SUFFIX_FLUSH, String.valueOf( _flushChildren ) );
        }
        for( String childName : childNames ) {
            Framework.SetConfig( childName, SUFFIX_RESET, String.valueOf( _resetChildren ) );
        }

        // Now wait for all children to update
        synchronized( _childrenWaiting ) {
            _childrenWaiting.addAll( childNames );

            // add self as listener for these children
            for( String childName : childNames ) {
                _n.addEntityListener( childName, this );
            }
        }

        // update all the children (Note, they will update on other Nodes potentially, and definitely in another thread.
        for( String childName : childNames ) {
            _logger.debug( "Request update of child: " + childName );
            _n.requestUpdate( childName ); // schedule an update, may have already occurred
            // update to child may occur any time after this, because only 1 parent so waiting for me to call the update.
        }
    }

    protected void beforeUpdate() {
        String entityName = getName();
        int age = _config.age; // getPropertyInt( SUFFIX_AGE, 1 );
        _logger.info("START T: " + System.currentTimeMillis() + " Age " + age + " Thread " + Thread.currentThread().hashCode() + " Entity.update(): " + entityName);
    }

    protected void afterUpdate() {
        String entityName = getName();
        int age = _config.age; // getPropertyInt(SUFFIX_AGE, 1);
        _logger.info( "END   T: " + System.currentTimeMillis() + " Age " + age + " Thread " + Thread.currentThread().hashCode() + " Entity updated: " + entityName );

        _n.notifyUpdated(entityName); // this entity, the parent, is now complete
    }

    public void onEntityUpdated( String entityName ) {
        _logger.debug("Entity: " + getName() + " being notified about: " + entityName);
        synchronized( _childrenWaiting ) {
            _childrenWaiting.remove( entityName );

            //System.err.print( "Entity: " + getName() + " notified about: " + entityName + ", waiting for " );
            //for ( String child : _childrenWaiting ) {
            //    System.err.print( child + ", " );
            //}
            //System.err.println();

            if( _childrenWaiting.isEmpty() ) {
                afterUpdate();
            }
            // else: wait for other children
        }
    }

    /**
     * For dynamically connecting entities
     */
    public void connectEntities( HashMap< String, AbstractPair< String, String> > input2refs ) {

        for ( String input : input2refs.keySet() ) {
            AbstractPair< String, String > ref = input2refs.get( input );
            Framework.SetDataReference( _name, input, ref._first, ref._second );
        }
    }

    protected void updateSelf() {

        // get references to create entity connections at run time
        HashMap< String, AbstractPair< String, String> > input2refs = new HashMap<>(  );
        getInputRefs( input2refs, _dataFlags );
        connectEntities( input2refs );

        // 1. fetch inputs
        // get all the inputs and put them in the object map.
        Collection< String > inputAttributes = new ArrayList<>();
        getInputAttributes( inputAttributes );
        fetchData( inputAttributes );

        // 2. fetch outputs
        // get all the outputs and put them in the object map.
        Collection< String > outputAttributes = new ArrayList<>();
        getOutputAttributes( outputAttributes, _dataFlags );
        fetchData( outputAttributes );


        // Set the random number generator, with the current time (i.e. random), if not loaded.
        if( _config.seed == null ) {
            _config.seed = System.currentTimeMillis();
        }

        _r.setSeed( _config.seed );

        // 3. doUpdateSelf()
        doUpdateSelf();

        if( _config.reset ) {
            _logger.info( getName() + ": Reset enabled." );
            _config.age = 1; // i.e. 1 step after the reset.
        }
        else {
            _config.age++; // update age:
        }

        if( _config.flush ) {
            _logger.info( getName() + ": Flush enabled." );
        }

        _flushChildren = _config.flush;
        _resetChildren = _config.reset;

        _config.seed = _r.getSeed(); // update the random seed for next time.
        _config.reset = false; // cancel reset after reset.
        //_config.flush = false; // clear flush after flush: if it was true, make it false. EDIT: [dave] I've disabled this, it's more useful to leave it under manual control

        // 4. persist data
        // write all the outputs back to the persistence system
        persistData( outputAttributes );

        // 5. persist config of this entity
        persistConfig();

        _logger.debug( "Updated: " + getName() + " age after update: " + _config.age );
    }

    public static String SerializeConfig( EntityConfig entityConfig ) {
        Gson gson = new Gson();
        String config = gson.toJson(entityConfig);
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
     * @param attributes to be used as a suffix with entity name to produce the key
     */
    private void fetchData( Collection< String > attributes ) {
        Persistence p = _n.getPersistence();

        for( String attribute : attributes ) {

            String inputKey;
            inputKey = getKey( attribute );

//            ModelData modelData;

//            // check for cache policy
//            // Change: just try to read anything available from the data cache.
////            if( _dataFlags.hasFlag( attribute, DataFlags.FLAG_NODE_CACHE ) ) {
//                Data cached = _n.getCachedData( inputKey );
//
//                if( cached != null ) {
//                    _logger.info( "Skipping fetch of Data: " + inputKey );
//                    _data.put( inputKey, cached );
//                    continue;
//                }
//                // else: allow read
////            }

            // check for no - read
            if( _dataFlags.hasFlag( attribute, DataFlags.FLAG_PERSIST_ONLY ) ) {
                _logger.debug( "Skipping fetch of Data: " + inputKey );
                continue;
            }

//            Data d = _n.getCachedDataLazy( inputKey ); // gets and caches the data.
            Data d = _n.getData( inputKey, this ); // gets data, from cache if available

            if( d == null ) {
                continue;
            }
//            modelData = p.fetchData( inputKey );
//
//            if( modelData == null ) { // not found
//                continue; // truthfully represent as null.
//            }
//
//            Data d = modelData.getDataNames( _n, this );
            _data.put( inputKey, d );

/*            HashSet< String > refKeys = modelData.getRefKeys();

            if( refKeys.isEmpty() ) {
                Data d = modelData.getDataNames();
                _data.put( inputKey, d );
            } else {
                // Create an output matrix which is a composite of all the referenced inputs.
                HashMap< String, Data > allRefs = new HashMap<>();

                for( String refKey : refKeys ) {
                    ModelData refJson = _n.fetchData( refKey );
                    if( refJson == null ) {
                        continue; // don't put in data store
                    }
                    Data refData = refJson.getDataNames();
                    allRefs.put( refKey, refData );
                }

                Data combinedData = getCombinedData( attribute, allRefs );

                modelData.setData( combinedData, ModelData.ENCODING_DENSE ); // data added to ref keys.

// If i put this in the cache, it loses the refkeys and becomes constant.
//                if( _config.cache ) {
//                    _n.setCachedData( inputKey, combinedData ); // DAVE: BUG? It writes it back out.. I guess we wanna see this, but seems excessive.
//                }
//                else {
                    p.persistData( modelData );
//                }

                _data.put( inputKey, combinedData );
            }*/
        }

        // check to see whether we need a backup of these structures, to implement the lazy-persist policy.
//        for( String keySuffix : _dataFlags._dataFlags.keySet() ) {
//            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_LAZY_PERSIST ) ) {
//                String inputKey = getKey( keySuffix );
//                Data d = _data.get( inputKey );
//                if( d != null ) {
//                    Data copy = new Data( d ); // make a deep copy
//                    _dataCopy.putData( inputKey, copy );
//                }
//            }
//        }
    }

    public void persistData( Collection< String > attributes ) {
        Persistence p = _n.getPersistence();

        for( String keySuffix : attributes ) {
            String inputKey = getKey( keySuffix );
            Data d = _data.get( inputKey );

            String encoding = ModelData.ENCODING_DENSE;

            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_SPARSE_BINARY ) ) {
                encoding = ModelData.ENCODING_SPARSE_BINARY;
            }
            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_SPARSE_REAL ) ) {
                encoding = ModelData.ENCODING_SPARSE_REAL;
            }

            boolean cached = false;

            if( _config.cache ) {
                // even if I'm gonna flush it, cache it so next time (when flush maybe false) I read non-stale data from the cache.
                _n.setDataCache( inputKey, d, encoding ); // cache this one, so we don't need to read it next time.

                cached = true;

                if( _config.flush == false ) {
                    continue;
                }
                // else: continue to persist
            }

            // check for cache policy
            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_NODE_CACHE ) ) {
                if( !cached ) {
//                    _n.setCachedData( inputKey, d ); // cache this one, so we don't need to read it next time.
                    _n.setDataCache( inputKey, d, encoding ); // cache this one, so we don't need to read it next time.
                }
            }

//            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_LAZY_PERSIST ) ) {
//                Data copy = _dataCopy.getDataNames( inputKey );
//
//                if( copy != null ) {
//                    if( copy.isSameAs( d ) ) {
//                        //System.err.println( "Skipping persist of Data: " + inputKey + " because: Not changed." );
//                        if( _config.cache == false ) { // if cache = true, we only get here if flush is true.
//                            continue; // don't persist.
//                        }
//                    }
//                }
//            }

            if( _config.flush == false ) {
                if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_PERSIST_ON_FLUSH ) ) {
                    //System.err.println( "Skipping persist of Data: " + inputKey + " because: Only on flush, and not a flush." );
                    continue;
                }
            }

            _n.setDataPersist(inputKey, d, encoding);
//            ModelData modelData = new ModelData( inputKey, d, encoding ); // converts to json
//            p.persistData( modelData );
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
    public Data getCombinedData( String inputAttribute, HashMap< String, Data > allRefs ) {
        DataDeserializer dds = new DenseDataDeserializer();
        return dds.getCombinedData( inputAttribute, allRefs );
    }

    public String getEncoding( String inputAttribute ) {
        return ModelData.ENCODING_DENSE;
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

        if( d == null ) {
            d = new Data( defaultSize );
        } else {
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

        if( d == null ) {
            d = new Data( defaultSize );
        } else if( !d._dataSize.isSameAs( defaultSize ) ) {
            d.setSize( defaultSize );
        }

        return d;
    }

    public static Collection< String > getEntityNames( String configValue ) {

        Collection< String > c = new ArrayList<>();

        if ( configValue.length() == 0 ) {
            return c;
        }

        String[] names = configValue.split(",");
        for( String s : names ) {

            if ( s.length() == 0 ) {
                continue;
            }

            c.add( s.trim() );
        }
        return c;
    }

    /**
     * The actual function of the entity, to be implemented by derived classes.
     */
    protected void doUpdateSelf() {
        // default: Nothing.
    }

}
