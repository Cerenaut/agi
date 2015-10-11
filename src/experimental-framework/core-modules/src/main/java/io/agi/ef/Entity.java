package io.agi.ef;

import io.agi.core.CallbackThread;
import io.agi.core.Keys;
import io.agi.core.ObjectMap;
import io.agi.core.data.Data;
import io.agi.ef.http.node.Node;
import io.agi.ef.interprocess.coordinator.Coordinator;
import io.agi.ef.persistenceClientApi.ApiException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * An asynchronous component of the distributed system. It can run on any Node.
 * Entities share data with other entities and produce and consume events that
 * cause cascades of activity. Entities are organized into a tree-shaped hierarchy
 * of parent-child relationships.
 *
 * For efficiency, the Entity thread blocks until a "step" event occurs.
 *
 * Created by dave on 11/09/15.
 */
public class Entity extends CallbackThread {

    public static final String EVENT_CREATED = "created";

    public static final String EVENT_START = "start";
    public static final String EVENT_STOP = "stop";
    public static final String EVENT_PAUSE = "pause";
    public static final String EVENT_RESUME = "resume";
    public static final String EVENT_STEP = "step";
    public static final String EVENT_RESET = "reset";

    public static final String EVENT_STEPPED = "stepped";
    public static final String EVENT_RESETTED = "resetted";
    public static final String EVENT_STARTED = "started";
    public static final String EVENT_STOPPED = "stopped";
    public static final String EVENT_PAUSED = "paused";
    public static final String EVENT_RESUMED = "resumed";

    public static final String EVENT_CHANGED = "changed";

    public static final String PROPERTY_STEP = "step";

    protected String _name;
    protected String _parentName;
//    protected Lock _lock = new ReentrantLock();
    protected HashSet< String > _dirtyData = new HashSet<>();

    protected Logger _logger = null;

    public Entity() {
        _logger = Logger.getLogger( this.getClass().getPackage().getName() );
    }

    /**
     * Get the unique ID name of an object owned by this entity.
     * @param suffix
     * @return
     */
    public String getKey( String suffix ) {
        return GetKey(getName(), suffix);
    }

    /**
     * Get the unique ID name of an object formed from an entity-name and a object-name suffix.
     * Used for property names and data names.
     *
     * @param entityName
     * @param suffix
     * @return
     */
    public static String GetKey( String entityName, String suffix ) {
        return Keys.concatenate(entityName, suffix);
    }

    /**
     * Post (i.e. create within the distributed system) an event concerning this entity.
     * @param action
     */
    public void postSelfEvent( String action ) {
        String name = getName();
        postEvent(name, action);
    }

    /**
     * Post (i.e. create within the distributed system) an event concerning the named entity.
     * @param entity
     * @param action
     */
    public void postEvent( String entity, String action ) {
        Coordinator c = Coordinator.getInstance();
        c.postEntityEvent(entity, action);
    }

    /**
     * Get the unique name of this entity. Immutable for the lifespan of the object.
     * @return
     */
    public synchronized String getName() {
        return _name;
    }

    /**
     * Prepare the entity object using a virtual method. Also, the data may not be available during instantiation of the
     * object.
     *
     * @param name
     * @param type
     * @param parentName
     * @param config
     * @return
     */
    public synchronized boolean setup( String name, String type, String parentName, String config ) {
        _name = name;
        _parentName = parentName;

        ObjectMap.Put( name, this );

        if( parentName != null ) {
            if( parentName.equalsIgnoreCase( "null" ) ) {
                parentName = null;
            }
        }

        boolean b = Persistence.addEntity(_name, type, parentName);

        addProperty(PROPERTY_STEP, 0);

        if( b ) {
            configure(config);

            postSelfEvent( EVENT_CREATED );
//            Node n = Node.getInstance();
//            n.postEntityEvent( name, EVENT_CREATED ); // notify that
        }

        return b;
    }

    /**
     * Configure the entity with any specific parameters, specified in the parameter string. Typically, the String would
     * be a JSON object in serialized form.
     *
     * @param config
     */
    public void configure( String config ) {

    }

    public float configureProperty( String config, String property, float defaultValue ) {
        try {
            JSONObject jo = new JSONObject( config );
            double d = jo.getDouble(property);
            return (float)d;
        }
        catch (Exception e) {
            //e.printStackTrace();
            return defaultValue;
        }
    }

    public int configureProperty( String config, String property, int defaultValue ) {
        try {
            JSONObject jo = new JSONObject( config );
            int n = jo.getInt(property);
            return n;
        }
        catch (Exception e) {
            //e.printStackTrace();
            return defaultValue;
        }
    }

    public String configureProperty( String config, String property, String defaultValue ) {
        try {
            JSONObject jo = new JSONObject( config );
            String s = jo.getString(property);
            return s;
        }
        catch (Exception e) {
            //e.printStackTrace();
            return defaultValue;
        }
    }

    public Data configureData( String config, String dataName ) {
        try {
            JSONObject jo = new JSONObject( config );
            JSONObject joDataSize = jo.getJSONObject( dataName );
            Data d = Serialization.DataFromJson(joDataSize);
            return d;
        }
        catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * Create an entity as a child of this entity.
     * @param nodeName
     * @param entityName
     * @param entityType
     * @param entityConfig
     */
    public void addChildEntity( String nodeName, String entityName, String entityType, String entityConfig ) {

        String parentEntityName = getName();

        Coordinator c = Coordinator.getInstance();
        try {
            c.postCreateEvent( nodeName, entityName, entityType, parentEntityName, entityConfig );
        }
        catch ( ApiException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Get all children of this entity and their types.
     * @return
     */
    public HashMap< String, String > getChildEntityTypes() {
        return Persistence.getChildEntityTypes( getName() );
    }

    /**
     * Get all children entities of this entity of a particular type.
     * @param entityType
     * @return
     */
    public HashSet< String > getChildEntitiesOfType( String entityType ) {
        HashSet< String > hs = new HashSet<>();

        HashMap< String, String > hm = getChildEntityTypes();

        for( String childEntityName : hm.keySet() ) {
            String childEntityType = hm.get( childEntityName );

            if( childEntityType.equals( entityType ) ) {
                hs.add( childEntityName );
            }
        }

        return hs;
    }

    /**
     * Entities can be reset. They define what this means individually. But should come back to a fixed start state.
     */
    public void reset() {
        setProperty(PROPERTY_STEP, 0);
    }

    /**
     * Block the thread until a step event has occurred.
     * Then step, and issue an event AFTER the step has completed.
     * This allows synchronization of other entities that are waiting for this entity to step.
     */
    public synchronized void onStep() {
        // block until an event happens: entity=this, action=step
        // Entities always run step by step to allow synchronization to an external clock.
        // Of course, the clock can notify faster than old_entities can run.
        try {
            wait();
        }
        catch( InterruptedException ie ) {
            // normal
        }

        HashSet< String > dirtyData = getClearDirtyData(); // these matrices are marked as dirty prior to start of step.

//        try {
            doStep(dirtyData);
//        }
//        catch( Exception e ) {
//            e.printStackTrace(); // in the spirit of self-healing code, deal with errors gracefully.
//        }

        postSelfEvent(EVENT_STEPPED);
    }

    /**
     * The default implementation of a step - it simply increments a count.
     */
    public void doStep( HashSet< String > dirtyData ) {
        int step = getPropertyAsInt(PROPERTY_STEP);
        ++step;
        setProperty(PROPERTY_STEP, step);

        System.out.println(getName() + "::doStep( " + step + ")");
    }

    /**
     * Get a list of data matrices that were changed until now.
     * Clear the list (it's assumed we'll handle them).
     * @return
     */
    public synchronized HashSet< String > getClearDirtyData() {
        HashSet< String> hs = new HashSet<>();
        hs.addAll(_dirtyData);
        _dirtyData.clear();
        return hs;
    }

    /**
     * Default behaviour: Log all data matrices that have changed since last step.
     *
     * @param data
     * @param action
     */
    public synchronized void onDataEvent( String data, String action ) {
        if( action.equals( EVENT_CHANGED ) ) {
            _dirtyData.add(data);
        }
    }

    /**
     * Handle events relating to entities.
     * @param entity
     * @param action
     */
    public void onEvent( String entity, String action ) {
        // potentially triggers the entity to unblock.
        if( !isSelf( entity ) ) {
            onSelfEvent(action);
        }
        else if( isParent( entity ) ) {
            onParentEvent( action );
        }

        // TODO allow wait on arbitrary events of other entities using generic framework
    }

    /**
     * This method is called when the subject of the event is this entity.
     * @param action
     */
    public void onSelfEvent( String action ) {

        if( action.equals( EVENT_STEP ) ) {
            onSelfStep();
        }
        else if( action.equals(EVENT_RESET) ) {
            reset();
            postSelfEvent(EVENT_RESETTED);
        }
        else if( action.equals( EVENT_START ) ) {
            start();
            postSelfEvent(EVENT_STARTED);
        }
        else if( action.equals( EVENT_STOP ) ) {
            stop();
            postSelfEvent(EVENT_STOPPED);
        }
        else if( action.equals( EVENT_PAUSE ) ) {
            pause();
            postSelfEvent(EVENT_PAUSED);
        }
        else if( action.equals( EVENT_RESUME ) ) {
            resume();
            postSelfEvent(EVENT_RESUMED);
        }
    }

    /**
     * Allow override of a notify on self
     */
    protected synchronized void onSelfStep() {
        notifyAll();
    }

    /**
     * The default behaviour of old_entities is to cascade the parent's actions.
     * So if the parent does a "step", the child will step also.
     *
     * This behaviour can easily be overridden.
     *
     * Note we wait on the completing events (past-tense) to ensure the children don't start BEFORE the parents.
     *
     * @param action
     */
    public void onParentEvent( String action ) {

        if( action.equals( EVENT_STEPPED ) ) {
            postSelfEvent(EVENT_STEP);
        }
        else if( action.equals( EVENT_RESETTED ) ) {
            postSelfEvent(EVENT_RESET);
        }
        else if( action.equals( EVENT_STARTED ) ) {
            postSelfEvent(EVENT_START);
        }
        else if( action.equals( EVENT_STOPPED ) ) {
            postSelfEvent(EVENT_STOP);
        }
        else if( action.equals( EVENT_PAUSED ) ) {
            postSelfEvent(EVENT_PAUSE);
        }
        else if( action.equals( EVENT_RESUMED ) ) {
            postSelfEvent(EVENT_RESUME);
        }
    }

    /**
     * returns true if the argument is the name of this entity.
     * @param entity
     * @return
     */
    public synchronized boolean isSelf( String entity ) {
        return _name.equals( entity );
    }

    /**
     * returns true if the argument is the name of the parent entity of this entity.
     * @param entityName
     * @return
     */
    public synchronized boolean isParent( String entityName ) {
        //String parentEntityName =  // this should be cached.
        if( _parentName == null ) {
            return false;
        }

        String parentEntityName = _parentName; // TODO not sure if this is correct, should it be checked in DB and cached?
        if( parentEntityName.equals( entityName ) ) {
            return true;
        }

        return false;
    }

//    public synchronized void lock() {
//        _lock.lock();
//    }
//
//    public synchronized void unlock() {
//        _lock.unlock();
//    }

    /**
     * Add data to the system, owned by this entity.
     * @param suffix
     * @param data
     * @return
     */
    public boolean addData( String suffix, Data data ) {
        String name = getKey( suffix );
        return Persistence.addData(getName(), name, data );
    }

    /**
     * Modify the content of a data object owned by any entity.
     * @param suffix
     * @param data
     * @return
     */
    public boolean setData( String suffix, Data data ) {
        String name = getKey( suffix );
        return Persistence.setData(name, data);
    }

    /**
     * Set data belonging to another entity.
     * @param entityName
     * @param suffix
     * @param data
     * @return
     */
    public static boolean SetData( String entityName, String suffix, Data data ) {
        String name = Entity.GetKey(entityName, suffix);
        return Persistence.setData(name, data);
    }

    /**
     * Get data belonging to another entity.
     * @param entityName
     * @param suffix
     * @return
     */
    public static Data GetData( String entityName, String suffix) {
        String name = GetKey(entityName, suffix);
        return Persistence.getData(name);
    }

    /**
     * Retrieve data owned by any entity.
     * @param suffix
     * @return
     */
    public Data getData( String suffix ) {
        String name = getKey( suffix );
        return Persistence.getData( name );
    }

    public void addProperty( String suffix, int defaultValue ) {
        addProperty( suffix, String.valueOf( defaultValue ) );
    }
    public void addProperty( String suffix, long defaultValue ) {
        addProperty( suffix, String.valueOf( defaultValue ) );
    }
    public void addProperty( String suffix, float defaultValue ) {
        addProperty( suffix, String.valueOf( defaultValue ) );
    }
    public void addProperty( String suffix, double defaultValue ) {
        addProperty( suffix, String.valueOf( defaultValue ) );
    }
    public void addProperty( String suffix, String defaultValue ) {
        String key = getKey( suffix );
        Persistence.addProperty(key, defaultValue);
    }

    public int getPropertyAsInt( String suffix ) {
        String value = getProperty( suffix );
        return Integer.valueOf( value );
    }
    public long getPropertyAsLong( String suffix ) {
        String value = getProperty( suffix );
        return Long.valueOf( value );
    }
    public float getPropertyAsFloat( String suffix ) {
        String value = getProperty( suffix );
        return Float.valueOf( value );
    }
    public double getPropertyAsDouble( String suffix ) {
        String value = getProperty( suffix );
        return Double.valueOf( value );
    }
    public String getProperty( String suffix ) {
        String key = getKey( suffix );
        return Persistence.getProperty( key );
    }

    public void setProperty( String suffix, int defaultValue ) {
        setProperty(suffix, String.valueOf(defaultValue));
    }
    public void setProperty( String suffix, long defaultValue ) {
        setProperty(suffix, String.valueOf(defaultValue));
    }
    public void setProperty( String suffix, float defaultValue ) {
        setProperty(suffix, String.valueOf(defaultValue));
    }
    public void setProperty( String suffix, double defaultValue ) {
        setProperty(suffix, String.valueOf(defaultValue));
    }
    public boolean setProperty( String suffix, String value ) {
        String key = getKey( suffix );
        return Persistence.setProperty(key, value);
    }

}
