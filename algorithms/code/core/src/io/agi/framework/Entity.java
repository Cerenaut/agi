package io.agi.framework;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.serialization.ModelData;

import java.util.*;

/**
 * Created by dave on 14/02/16.
 */
public abstract class Entity extends NamedObject implements EntityListener {

    public static final String SUFFIX_AGE = "age";
    public static final String SUFFIX_RESET = "reset"; /// used as a flag to indicate the entity should reset itself on next update.

    protected String _type;
    protected String _parent;
    protected Node _n;

    protected HashSet<String> _childrenWaiting = new HashSet<String>();

    private HashMap<String, Data> _data = new HashMap<String, Data>();

    public Entity( String name, ObjectMap om, String type, Node n ) {
        super( name, om );
//        _name = name;
        _type = type;
//        _parent = parent;
        _n = n;
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
            String referenceSuffix) {
        String inputKey = NamedObject.GetKey( inputEntity, inputSuffix );
        String refKey = NamedObject.GetKey( referenceEntity, referenceSuffix );
        SetDataReference(p, inputKey, refKey);
    }

    public static void SetDataReference(
            Persistence p,
            String dataKey,
            String refKeys) {
        ModelData jd = p.getData(dataKey);

        if ( jd == null ) {
            jd = new ModelData( dataKey, refKeys );
        }

        jd._refKey = refKeys;
        p.setData(jd);
    }

    /**
     * Returns a random number generator that is distributed and persisted.
     * @return
     */
    public Random getRandom() {
        return RandomInstance.getInstance(); // TODO use a distributed, persisted random source
    }

    public Node getNode() {
        return _n;
    }

    public abstract void getInputKeys( Collection<String> keys );

    public abstract void getOutputKeys( Collection<String> keys );

    // Properties
    public Float getPropertyFloat( String suffix, Float defaultValue ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        return p.getPropertyFloat( key, defaultValue );
    }

    public void setPropertyFloat( String suffix, float value ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        p.setPropertyFloat( key, value );
    }

    public Double getPropertyDouble( String suffix, Double defaultValue ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        return p.getPropertyDouble( key, defaultValue );
    }

    public void setPropertyDouble( String suffix, double value ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        p.setPropertyDouble( key, value );
    }

    public Long getPropertyLong( String suffix, Long defaultValue ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        return p.getPropertyLong( key, defaultValue );
    }

    public void setPropertyLong( String suffix, long value ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        p.setPropertyLong( key, value );
    }

    public Integer getPropertyInt( String suffix, Integer defaultValue ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        return p.getPropertyInt( key, defaultValue );
    }

    public void setPropertyInt( String suffix, int value ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        p.setPropertyInt( key, value );
    }

    public Boolean getPropertyBoolean( String suffix, Boolean defaultValue ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        return p.getPropertyBoolean( key, defaultValue );
    }

    public void setPropertyBoolean( String suffix, boolean value ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        p.setPropertyBoolean( key, value );
    }

    public String getPropertyString( String suffix, String defaultValue ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        return p.getPropertyString( key, defaultValue );
    }

    public void setPropertyString( String suffix, String value ) {
        String key = getKey( suffix );
        Persistence p = _n.getPersistence();
        p.setPropertyString( key, value );
    }

    // if I cant issue another update to children until this has completed...
    // then children can't get out of sync

    public void update() {

        beforeUpdate();

        String entityName = getName();

        if ( !_n.lock( entityName ) ) {
            return;
        }

        updateSelf();

        Persistence p = _n.getPersistence();
        //        Collection< JsonEntity > children = p.getChildEntities( _name );
        Collection<String> childNames = p.getChildEntities( _name );

        if ( childNames.isEmpty() ) {
            afterUpdate();
//            _n.notifyUpdated( getName() ); // this entity, the parent, is now complete
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
    }

    protected void beforeUpdate() {
        String entityName = getName();
        int age = getPropertyInt( SUFFIX_AGE, 1 );
        System.err.println("START T: " + System.currentTimeMillis() + " Age " + age + " Thread " + Thread.currentThread().hashCode() + " Entity.update(): " + entityName);
    }

    protected void afterUpdate() {
        String entityName = getName();
        int age = getPropertyInt( SUFFIX_AGE, 1 );
        System.err.println( "END   T: "+System.currentTimeMillis() + " Age " + age + " Thread " + Thread.currentThread().hashCode() + " Entity updated: " + entityName );

        _n.notifyUpdated( entityName ); // this entity, the parent, is now complete
    }

    public void onEntityUpdated( String entityName ) {
        synchronized ( _childrenWaiting ) {
            _childrenWaiting.remove( entityName );

            //System.err.print( "Entity " + entityName + " notified about: " + entityName + " waiting for " );
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

        // 1. get inputs
        // get all the inputs and put them in the object map.
        Collection<String> inputKeys = new ArrayList<String>();
        getInputKeys( inputKeys );
        getData( inputKeys );

        // 2. get outputs
        // get all the outputs and put them in the object map.
        Collection<String> outputKeys = new ArrayList<String>();
        getOutputKeys( outputKeys );
        getData( outputKeys );

        // 3. doUpdateSelf()
        doUpdateSelf();

        // 4. set outputs
        // write all the outputs back to the persistence system
//        setData(inputKeys);
        setData( outputKeys );


        // update age:
        int age = getPropertyInt( SUFFIX_AGE, 0 );
        ++age;
        setPropertyInt(SUFFIX_AGE, age);

        //System.err.println( "Update: " + getName() + " age: " + age );
    }

    public Data getData( String keySuffix ) {
        return _data.get( getKey( keySuffix ) );
    }

    public void getData( Collection<String> keys ) {
        Persistence p = _n.getPersistence();

        for ( String keySuffix : keys ) {
            String inputKey = getKey( keySuffix );

            ModelData jd = p.getData( inputKey );

            if( jd == null ) {
                continue; // truthfully represent as null.
            }

            HashSet<String> refKeys = jd.getRefKeys();
            if ( !refKeys.isEmpty() ) {
                // Create an output matrix which is a composite of all the referenced inputs.
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

    public void setData( Collection<String> keys ) {
        Persistence p = _n.getPersistence();

        for ( String keySuffix : keys ) {
            String inputKey = getKey(keySuffix);
            Data d = _data.get(inputKey);
            p.setData( new ModelData( inputKey, d ) );
        }
    }

    /**
     * Get the data if it exists, and Create it if it doesn't.
     *
     * @param keySuffix   the key of the data
     * @param defaultSize Create data of this size, if the data does not exist
     * @return data
     */
    public Data getData( String keySuffix, DataSize defaultSize ) {
        String key = getKey(keySuffix);
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
        else if( !d._dataSize.isSameAs( defaultSize ) ){
            d.setSize( defaultSize );
        }

        return d;
    }

    protected void doUpdateSelf() {
        // default: Nothing.
    }
}
