package io.agi.framework;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.FastRandom;
import io.agi.core.orm.Keys;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.entities.EntityProperties;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelData;

import java.util.*;

/**
 * A conceptual Entity that has an update() method, and some children Entities.
 *
 * Instances are transient and all state must be persisted as Data or Properties.
 *
 * Created by dave on 14/02/16.
 */
public abstract class Entity extends NamedObject implements EntityListener {

    public static final String SUFFIX_AGE = "age"; /// Optional: Number of updates of the entity, since reset
    public static final String SUFFIX_SEED = "seed"; /// Optional: Seeds the random number generator
    public static final String SUFFIX_RESET = "reset"; /// Optional: Used as a flag to indicate the entity should reset itself on next update.
    public static final String SUFFIX_FLUSH = "flush"; /// Required: Triggers all flushable data to be persisted.

    protected String _type;
    protected String _parent;
    protected Node _n;
    protected FastRandom _r;
    protected boolean _flush = false;

    protected HashSet< String > _childrenWaiting = new HashSet< String >();

    private HashMap< String, Data > _data = new HashMap< String, Data >();
    private HashMap< String, String > _properties = new HashMap< String, String >();

    private DataFlags _dataFlags = new DataFlags();
    private DataMap _dataMap = new DataMap(); // used to check for data changes since load.

    public Entity( String name, ObjectMap om, String type, Node n ) {
        super( name, om );
        _type = type;
        _n = n;
        _r = new FastRandom();
    }

    public void setParent( String parent ) {
        _parent = parent;
    }

    public String getParent() {
        return _parent;
    }

    public String getType() {
        return _type;
    }

    /**
     * Modifies the database to make the reference entity-suffix Data a reference input to the input entity-suffix.
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

    public static void SetDataReference(
            Persistence p,
            String dataKey,
            String refKeys ) {
        ModelData modelData = p.getData( dataKey );

        if ( modelData == null ) {
            modelData = new ModelData( dataKey, refKeys );
        }

        modelData._refKeys = refKeys;
        p.setData( modelData );
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
     * @param keys
     */
    public abstract void getInputKeys( Collection< String > keys );

    /**
     * Get the keys for all the data that is used as output.
     * Old value is prefetched at the start of the update, and value is written at the end of the update.
     *
     * @param keys
     */
    public abstract void getOutputKeys( Collection< String > keys, DataFlags flags );

    /**
     * Get the property objects, to be fetched at the start of update and persisted at the dn
     * (non input/output state used by the entity).
     */
    public abstract EntityProperties getProperties();

    // if I cant issue another update to children until this has completed...
    // then children can't get out of sync

    public void update() {

        beforeUpdate();

        String entityName = getName();

        if( !_n.lock( entityName ) ) {
            return;
        }

        updateSelf();

        Persistence p = _n.getPersistence();
        Collection< String > childNames = p.getChildEntities( _name );

        if ( childNames.isEmpty() ) {
            afterUpdate(); // will notify the parent in the afterUpdate handler
            return;
        }

        // Require all children to flush on next update.
        // They can only be updated by this class, so we know they will respect the flush.
        for( String childName : childNames ) {


            String childKey = GetKey( childName, SUFFIX_FLUSH );
            p.setPropertyString( childKey, "true" );

            // TODO TODO
            // get the entity, then .properties of entity can be set



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
            _n.requestUpdate( childName ); // schedule an update, may have already occurred
            // update to child may occur any time after this, because only 1 parent so waiting for me to call the update.
        }
    }

    protected void beforeUpdate() {
        String entityName = getName();
        int age = _properties.age; // getPropertyInt( SUFFIX_AGE, 1 );
        System.err.println("START T: " + System.currentTimeMillis() + " Age " + age + " Thread " + Thread.currentThread().hashCode() + " Entity.update(): " + entityName);
    }

    protected void afterUpdate() {
        String entityName = getName();
        int age = _properties.age; // getPropertyInt(SUFFIX_AGE, 1);
        System.err.println( "END   T: " + System.currentTimeMillis() + " Age " + age + " Thread " + Thread.currentThread().hashCode() + " Entity updated: " + entityName );

        _n.notifyUpdated( entityName ); // this entity, the parent, is now complete
    }

    public void onEntityUpdated( String entityName ) {
        //System.err.println( "Entity: " + getName() + " being notified about: " + entityName );
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
        Collection<String> inputKeys = new ArrayList<String>();
        getInputKeys( inputKeys );
        fetchData( inputKeys );

        // 2. fetch outputs
        // get all the outputs and put them in the object map.
        Collection< String > outputKeys = new ArrayList< String >();
        getOutputKeys( outputKeys, _dataFlags );
        fetchData( outputKeys );

        // 3. fetch properties
        // get all the properties and put them in the properties map.
        EntityProperties properties = getProperties();
        fetchProperties( properties );

        // Set the random number generator, with the current time (i.e. random), if not loaded.
        if ( properties.seed <=0 ) {
            properties.seed = System.currentTimeMillis();
        }
        _r.setSeed( properties.seed );

        // 3. doUpdateSelf()
        doUpdateSelf();

        // update age:
        properties.age++;

        // update the random seed for next time.
        if ( properties.seed <=0 ) {
            properties.seed = System.currentTimeMillis();
        }
        properties.seed = _r.getSeed();

        // 4. set outputs
        // write all the outputs back to the persistence system
        //persistData(inputKeys); These aren't persisted, by definition you're promising not to write them.
        persistData( outputKeys );

        // 5. persist properties
        persistProperties( properties );

        //System.err.println( "Update: " + getName() + " age: " + age );
    }

    private String uniqueNameForObject( Object object ) {
        return Keys.concatenate( getName(), object.getClass().getSimpleName() );
    }

    /**
     * Populate properties map with the persisted properties.
     */
    private void fetchProperties( EntityProperties properties ) {
        Persistence p = _n.getPersistence();
        p.getProperties( uniqueNameForObject( properties ), properties );      // persistence hydrates the model from storage
    }

    /**
     * Persist the properties map.
     */
    private void persistProperties( EntityProperties properties ) {
        Persistence p = _n.getPersistence();
        p.setProperties( uniqueNameForObject( properties ), properties );
    }

    /**
     * Populate object map with the persisted data.
     *
     * @param keys
     */
    private void fetchData( Collection< String > keys ) {
        Persistence p = _n.getPersistence();

        for ( String keySuffix : keys ) {
            String inputKey = getKey( keySuffix );

            ModelData jd = null;

            // check for cache policy
            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_NODE_CACHE ) ) {
                Data d = _n.getCachedData(inputKey);

                if( d != null ) {
                    //System.err.println( "Skipping fetch of Data: " + inputKey );
                    _data.put( inputKey, d );
                    continue;
                }
            }

            // check for no - read
            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_PERSIST_ONLY ) ) {
                //System.err.println( "Skipping fetch of Data: " + inputKey );
                continue;
            }

            jd = p.getData( inputKey );

            if( jd == null ) { // not found
                continue; // truthfully represent as null.
            }

            HashSet< String > refKeys = jd.getRefKeys();

            if( refKeys.isEmpty() ) {
                Data d = jd.getData();
                _data.put( inputKey, d );
            }
            else {
                // Create an output matrix which is a composite of all the referenced inputs.
                HashMap< String, Data > allRefs = new HashMap< String, Data >();

                for ( String refKey : refKeys ) {
                    ModelData refJson = p.getData( refKey );
                    Data refData = refJson.getData();
                    allRefs.put( refKey, refData );
                }

//                String combinedKey = inputKey;//Keys.concatenate( inputKey, SUFFIX_COMBINED );

                Data combinedData = getCombinedData( keySuffix, allRefs );

                jd.setData( combinedData, false ); // data added to ref keys.

                p.setData( jd ); // DAVE: BUG? It writes it back out.. I guess we wanna see this, but seems excessive.

                _data.put( inputKey, combinedData );
            }
        }

        // check to see whether we need a backup of these structures, to implement the lazy-persist policy.
        for( String keySuffix : _dataFlags._dataFlags.keySet() ) {
            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_LAZY_PERSIST ) ) {
                String inputKey = getKey( keySuffix );
                Data d = _data.get(inputKey);
                Data copy = new Data( d ); // make a deep copy
                _dataMap.putData( inputKey, copy );
            }
        }
    }

    public void persistData( Collection< String > keys ) {
        Persistence p = _n.getPersistence();

        _flush = _properties.flush;

        for ( String keySuffix : keys ) {
            String inputKey = getKey( keySuffix );
            Data d = _data.get(inputKey);

            // check for cache policy
            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_NODE_CACHE ) ) {
                _n.setCachedData( inputKey, d ); // cache this one, so we don't need to read it next time.
            }

            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_LAZY_PERSIST ) ) {
                Data copy = _dataMap.getData( inputKey );

                if( copy.isSameAs( d ) ) {
                    //System.err.println( "Skipping persist of Data: " + inputKey + " because: Not changed." );
                    continue; // don't persist.
                }
            }

            if( _flush == false ) {
                if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_PERSIST_ON_FLUSH ) ) {
                    //System.err.println( "Skipping persist of Data: " + inputKey + " because: Only on flush, and not a flush." );
                    continue;
                }
            }

            boolean sparse = false;
            if( _dataFlags.hasFlag( keySuffix, DataFlags.FLAG_SPARSE_UNIT ) ) {
                sparse = true;
            }

            p.setData( new ModelData( inputKey, d, sparse ) );
        }

        // clear flush after flush.
        _properties.flush = _flush;
    }

    public void setData( String keySuffix, Data output ) {
        _data.put(getKey(keySuffix), output );
    }

    /**
     * Default implementation: If a single reference, copy its shape.
     * Otherwise, creates a 1-D vector containing all input bits.
     * This is a reasonable solution for many cases where shape is not important.
     *
     * @param inputKeySuffix
     * @param allRefs
     * @return
     */
    protected Data getCombinedData( String inputKeySuffix, HashMap< String, Data > allRefs ) {

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
     * @param keySuffix   the name of the data
     * @param defaultSize create data of this size, if the data does not exist
     * @return data
     */
    public Data getData( String keySuffix, DataSize defaultSize ) {
        String key = getKey( keySuffix );
        Data d = _data.get(key );

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
     * @param keySuffix
     * @param defaultSize
     * @return
     */
    public Data getDataLazyResize( String keySuffix, DataSize defaultSize ) {
        String key = getKey( keySuffix );
        Data d = _data.get( key );

        if ( d == null ) {
            d = new Data( defaultSize );
        }
        else if ( !d._dataSize.isSameAs( defaultSize ) ) {
            d.setSize( defaultSize );
        }

        return d;
    }

    protected void doUpdateSelf() {
        // default: Nothing.
    }

}
