package io.agi.ef;

import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.ef.serialization.JsonData;
import io.agi.ef.serialization.JsonEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by dave on 14/02/16.
 */
public abstract class Entity implements EntityListener {

    protected String _name;
    protected String _type;
    protected String _parent;
    protected Node _n;

    protected ObjectMap _om = new ObjectMap(); // for data

    protected HashSet< String > _childrenWaiting = new HashSet< String >();

    protected HashMap< String, Data > _data = new HashMap< String, Data >();

    public Entity( String name, String type, String parent, Node n ) {
        _name = name;
        _type = type;
        _parent = parent;
        _n = n;
    }

    public String getName() {
        return _name;
    }

    public String getParent() {
        return _parent;
    }

    public String getType() {
        return _type;
    }

    public Node getNode() {
        return _n;
    }

    public abstract Collection< String > getInputKeys();
    public abstract Collection< String > getOutputKeys();

    public void update() {
        updateSelf();

        Persistence p = _n.getPersistence();
//        Collection< JsonEntity > children = p.getChildEntities( _name );
        Collection< String > childNames = p.getChildEntities( _name );

        synchronized( _childrenWaiting ) {
            _childrenWaiting.addAll( childNames );
        }

        // add self as listener for these children
        for( String childName : childNames ) {
            _n.addEntityListener( childName, this );
        }

        // update all the children
        for( String childName : childNames ) {
            _n.requestUpdate(childName); // schedule an update, may have already occurred
            // update to child may occur any time after this, because only 1 parent so waiting for me to call the update.
        }

        // this thread terminates now... but object persists until all children have updated.
        // now wait:
        //wait();
//        _n.wait( childNames, _name );
//
//        // notify:
//        _n.notifyUpdated( _name );
    }

    public void onEntityUpdated( String entityName ) {
        synchronized( _childrenWaiting ) {
            _childrenWaiting.remove( entityName );

            if( _childrenWaiting.isEmpty() ) {
                _n.isUpdated( _name ); // this entity, the parent, is now complete
            }
            // else: wait for other children
        }
    }

    protected void updateSelf() {
        // 1. get inputs
        // get all the inputs and put them in the object map.
        Collection< String > inputKeys = getInputKeys();
        getData(inputKeys);

        // 2. get outputs
        // get all the outputs and put them in the object map.
        Collection< String > outputKeys = getOutputKeys();
        getData(outputKeys);

        // 3. doUpdateSelf()
        doUpdateSelf();

        // 4. set outputs
        // write all the outputs back to the persistence system
        setData(outputKeys);
    }

    public void getData( Collection< String > keys ) {
        Persistence p = _n.getPersistence();

        for( String key : keys ) {
            JsonData jd = p.getData( key );
            Data d = jd.getData();
            _data.put( key, d );
        }
    }

    public void setData( Collection< String > keys ) {
        Persistence p = _n.getPersistence();

        for( String key : keys ) {
            Data d = _data.get(key);
            p.setData( new JsonData( key, d ) );
        }
    }

    protected void doUpdateSelf() {
        // default: Nothing.
    }
}
