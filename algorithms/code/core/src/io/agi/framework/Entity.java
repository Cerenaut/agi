package io.agi.framework;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.Persistence.Persistence;
import io.agi.framework.Persistence.PropertyConverter;
import io.agi.framework.Persistence.PropertyStringAccess;
import io.agi.framework.serialization.ModelData;

import java.util.*;

/**
 * Created by dave on 14/02/16.
 */
public abstract class Entity extends NamedObject implements EntityListener, PropertyStringAccess {

    public static final String SUFFIX_AGE = "age";
    public static final String SUFFIX_RESET = "reset"; /// used as a flag to indicate the entity should reset itself on next update.

    protected String _type;
    protected String _parent;
    protected Node _n;

    protected HashSet<String> _childrenWaiting = new HashSet<String>();

    private HashMap<String, Data> _data = new HashMap<String, Data>();
    private HashMap<String, String> _properties = new HashMap<String, String>();

    private PropertyConverter _propertyConverter = null;

    public Entity( String name, ObjectMap om, String type, Node n ) {
        super( name, om );
        _type = type;
        _n = n;

        _propertyConverter = new PropertyConverter( this );
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
        ModelData jd = p.getData( dataKey );

        if ( jd == null ) {
            jd = new ModelData( dataKey, refKeys );
        }

        jd._refKey = refKeys;
        p.setData( jd );
    }

    /**
     * Returns a random number generator that is distributed and persisted.
     *
     * @return
     */
    public Random getRandom() {
        return RandomInstance.getInstance(); // TODO use a distributed, persisted random source
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
    public abstract void getInputKeys( Collection<String> keys );

    /**
     * Get the keys for all the data that is used as output.
     * Old value is prefetched at the start of the update, and value is written at the end of the update.
     *
     * @param keys
     */
    public abstract void getOutputKeys( Collection<String> keys );

    /**
     * Get the keys for all the properties (non input/output state used by the entity).
     */
    public abstract void getPropertyKeys( Collection<String> keys );


    // if I cant issue another update to children until this has completed...
    // then children can't get out of sync

    public void update() {

        String entityName = getName();
        if ( !_n.lock( entityName ) ) {
            return;
        }

        updateSelf();

        Persistence p = _n.getPersistence();
        //        Collection< JsonEntity > children = p.getChildEntities( _name );
        Collection<String> childNames = p.getChildEntities( _name );

        if ( childNames.isEmpty() ) {
            _n.notifyUpdated( getName() ); // this entity, the parent, is now complete
            return;
        }

        synchronized ( _childrenWaiting ) {
            _childrenWaiting.addAll( childNames );

            // add self as listener for these children
            for ( String childName : childNames ) {
                _n.addEntityListener( childName, this );
            }
        }

        // update all the children
        for ( String childName : childNames ) {
            _n.requestUpdate( childName ); // schedule an update, may have already occurred
            // update to child may occur any time after this, because only 1 parent so waiting for me to call the update.
        }

        System.err.println( "Thread " + Thread.currentThread().hashCode() + " terminating, was running: " + entityName );
    }

    public void onEntityUpdated( String entityName ) {
        synchronized ( _childrenWaiting ) {
            _childrenWaiting.remove( entityName );

            System.err.print( "Entity " + entityName + " notified about: " + entityName + " waiting for " );
            for ( String child : _childrenWaiting ) {
                System.err.print( child + ", " );
            }
            System.err.println();

            if ( _childrenWaiting.isEmpty() ) {
                _n.notifyUpdated( getName() ); // this entity, the parent, is now complete
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
        Collection<String> outputKeys = new ArrayList<String>();
        getOutputKeys( outputKeys );
        fetchData( outputKeys );

        // 3. fetch properties
        // get all the properties and put them in the properties map.
        Collection<String> propertyKeys = new ArrayList<String>();
        getPropertyKeys( propertyKeys );
        fetchProperties( propertyKeys );

        // 3. doUpdateSelf()
        doUpdateSelf();

        // update age:
        int age = _propertyConverter.getPropertyInt( SUFFIX_AGE, 0 );
        ++age;
        _propertyConverter.setPropertyInt( SUFFIX_AGE, age );

        // 4. set outputs
        // write all the outputs back to the persistence system
//        persistData(inputKeys);
        persistData( outputKeys );

        // 5. persist properties
        persistProperties( propertyKeys );


        System.err.println( "Update: " + getName() + " age: " + age );
    }

    /**
     * Populate properties map with the persisted properties.
     * @param keySuffixes
     */
    private void fetchProperties( Collection<String> keySuffixes ) {
        Persistence p = _n.getPersistence();

        for ( String keySuffix : keySuffixes ) {
            String inputKey = getKey( keySuffix );
            String value = p.getPropertyString( inputKey, "" );
            _properties.put( keySuffix, value );
        }
    }

    /**
     * Persist the properties map.
     * @param keySuffixes
     */
    private void persistProperties( Collection<String> keySuffixes ) {
        Persistence p = _n.getPersistence();
        for ( String keySuffix : keySuffixes ) {
            String value = _properties.get( keySuffix );
            String inputKey = getKey( keySuffix );
            p.setPropertyString( inputKey, value );
        }
    }

    /**
     * Populate object map with the persisted data.
     * @param keys
     */
    private void fetchData( Collection<String> keys ) {
        Persistence p = _n.getPersistence();

        for ( String keySuffix : keys ) {
            String inputKey = getKey( keySuffix );

            ModelData jd = p.getData( inputKey );

            HashSet<String> refKeys = jd.getRefKeys();
            if ( !refKeys.isEmpty() ) {
                // create an output matrix which is a composite of all the referenced inputs.
                HashMap<String, Data> allRefs = new HashMap<String, Data>();

                for ( String refKey : refKeys ) {
                    ModelData refJson = p.getData( refKey );
                    Data refData = refJson.getData();
                    allRefs.put( refKey, refData );
                }

//                String combinedKey = inputKey;//Keys.concatenate( inputKey, SUFFIX_COMBINED );

                Data combinedData = getCombinedData( keySuffix, allRefs );

                jd.setData( combinedData ); // data added to ref keys.

                p.setData( jd );

                _data.put( inputKey, combinedData );
            }
            else {
                Data d = jd.getData();
                _data.put( inputKey, d );
            }
        }
    }
    
    public void persistData( Collection<String> keys ) {
        Persistence p = _n.getPersistence();

        for ( String keySuffix : keys ) {
            String inputKey = getKey( keySuffix );
            Data d = _data.get( inputKey );
            p.setData( new ModelData( inputKey, d ) );
        }
    }

    public void setData( String keySuffix, Data output ) {
        _data.put( getKey( keySuffix ), output );
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
    protected Data getCombinedData( String inputKeySuffix, HashMap<String, Data> allRefs ) {

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
     * Get the data if it exists, and create it if it doesn't.
     *
     * @param keySuffix   the key of the data
     * @param defaultSize create data of this size, if the data does not exist
     * @return data
     */
    public Data getData( String keySuffix, DataSize defaultSize ) {
        String key = getKey( keySuffix );
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

    /**
     * If value doesn't exist, set it at defaultValue, and return defaultValue.
     */
    public String getPropertyString( String keySuffix, String defaultValue ) {
        String value = _properties.get( keySuffix );

        if ( value != null && value.length() != 0 ) {
            return value;
        }

        _properties.put( keySuffix, defaultValue );
        return defaultValue;
    }

    public void setPropertyString( String keySuffix, String value ) {
        _properties.put( keySuffix, value );
    }


    public Float getPropertyFloat( String key, Float defaultValue ) {
        return _propertyConverter.getPropertyFloat( key, defaultValue );
    }

    public void setPropertyFloat( String key, float value ) {
        _propertyConverter.setPropertyFloat( key, value );
    }

    public Double getPropertyDouble( String key, Double defaultValue ) {
        return _propertyConverter.getPropertyDouble( key, defaultValue );
    }

    public void setPropertyDouble( String key, double value ) {
        _propertyConverter.setPropertyDouble( key, value );
    }

    public Long getPropertyLong( String key, Long defaultValue ) {
        return _propertyConverter.getPropertyLong( key, defaultValue );
    }

    public void setPropertyLong( String key, long value ) {
        _propertyConverter.setPropertyLong( key, value );
    }

    public Integer getPropertyInt( String key, Integer defaultValue ) {
        return _propertyConverter.getPropertyInt( key, defaultValue );
    }

    public void setPropertyInt( String key, int value ) {
        _propertyConverter.setPropertyInt( key, value );
    }

    public Boolean getPropertyBoolean( String key, Boolean defaultValue ) {
        return _propertyConverter.getPropertyBoolean( key, defaultValue );
    }

    public void setPropertyBoolean( String key, boolean value ) {
        _propertyConverter.setPropertyBoolean( key, value );
    }

    protected void doUpdateSelf() {
        // default: Nothing.
    }

}
