package io.agi.framework;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.coordination.Coordination;
import io.agi.framework.persistence.Persistence;
import io.agi.framework.persistence.models.ModelEntity;
import io.agi.framework.persistence.models.ModelNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * A container in which the program can execute.
 * The system can be distributed by having multiple nodes, potentially on different virtual or physical machines.
 * Created by dave on 14/02/16.
 */
public class Node {

    public static final String KEY_NODE = "node";

    protected ObjectMap _om;
    protected String _name;
    protected String _host;
    protected int _port;
    protected EntityFactory _ef;
    protected Coordination _c;
    protected Persistence _p;

    protected HashMap< String, ArrayList< EntityListener > > _entityListeners = new HashMap< String, ArrayList< EntityListener > >();

    public Node() {
    }

    /**
     * Sets up the interfaces, which cannot be modified afterwards.
     */
    public void setup(
            ObjectMap om,
            String name,
            String host,
            int port,
            EntityFactory ef,
            Coordination c,
            Persistence p ) {

        _om = om;
        _om.put( KEY_NODE, this );

        _name = name;
        _host = host;
        _port = port;

        _ef = ef;
        _c = c;
        _p = p;

        ModelNode jn = new ModelNode( _name, _host, _port );
        _p.setNode( jn );
    }

    public String getName() {
        return _name;
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    /**
     * Returns the persistence layer
     *
     * @return
     */
    public Persistence getPersistence() {
        return _p;
    }

    /**
     * Returns the coordination layer
     *
     * @return
     */
    public Coordination getCoordination() {
        return _c;
    }

    /**
     * A callback that is called when an Entity has been updated, including all its children.
     *
     * @param entityName
     */
    public void notifyUpdated( String entityName ) {
//        int count = _p.getEntityAge(entityName);
//        count += 1;
//        _p.setEntityAge(entityName, count);
        unlock( entityName );

        // broadcast to any distributed listeners:
        _c.onUpdated( entityName );
    }

    /**
     * Called by the distributed system when an entity has been updated.
     *
     * @param entityName
     */
    public void onUpdated( String entityName ) {
        callEntityListeners( entityName );
    }

    /**
     * This method requests the distributed system to update the specified entity.
     * We don't know which Node hosts the Entity - it could be this, it could be another.
     * So, broadcast (or directly send) the update requet to another Node[s].
     *
     * @param entityName
     */
    public void requestUpdate( String entityName ) {
        // TODO: this should broadcast to the wider system the update request, in case it is handled by another Node
        //doUpdate(entityName); // monolithic only variant
        _c.doUpdate( entityName );
    }

    /**
     * This method is called when the distributed system has received a request for an update of an Entity.
     *
     * @param entityName
     */
    public void doUpdate( String entityName ) {

        ModelEntity modelEntity = _p.getEntity( entityName );

        if ( modelEntity == null ) {
            return; // bad entity
        }

        if ( !modelEntity.node.equals( getName() ) ) {
            return;
        }

        Entity e = _ef.create( _om, entityName, modelEntity.type );
        e.setParent( modelEntity.parent );

        forkUpdate( e ); // returns immediately
    }

    /**
     * Creates a thread to actually do the work of updating the entity
     *
     * @param e
     */
    protected void forkUpdate( final Entity e ) {
        Thread t = new Thread( new Runnable() {
            @Override
            public void run() {
                e.update();
            }
        } );
        t.start();
    }

    public boolean lock( String entityName ) {
        Semaphore s = getLock( entityName );

        //System.err.println( "Thread "+ Thread.currentThread().hashCode() + " waiting for " + entityName );
        try {
            s.acquire();
        }
        catch ( InterruptedException ie ) {
            //System.err.println("Thread " + Thread.currentThread().hashCode() + " cant get lock for " + entityName);
            return false;
        }

        //System.err.println("Thread " + Thread.currentThread().hashCode() + " has lock for " + entityName);

        return true;
    }

    public Semaphore getLock( String entityName ) {
        synchronized ( _entityNameSemaphores ) {
            Semaphore l = _entityNameSemaphores.get( entityName );
            if ( l == null ) {
                l = new Semaphore( 1 ); // binary
                _entityNameSemaphores.put( entityName, l );
            }

            return l;
        }
    }

    public void unlock( String entityName ) {
        Semaphore l = getLock( entityName );

        //System.err.println("Thread " + Thread.currentThread().hashCode() + " about to unlock " + entityName);

        l.release();
    }

    public HashMap< String, Semaphore > _entityNameSemaphores = new HashMap< String, Semaphore >();

    /**
     * Adds a listener to the specified Entity.
     * It will persist for only one call.
     *
     * @param entity
     * @param listener
     */
    public void addEntityListener( String entity, EntityListener listener ) {
        synchronized ( _entityListeners ) {
            ArrayList< EntityListener > al = _entityListeners.get( entity );
            if ( al == null ) {
                al = new ArrayList();
                _entityListeners.put( entity, al );
            }
            al.add( listener );
        }
    }

    public void removeEntityListener( String entity, EntityListener el ) {
        synchronized ( _entityListeners ) {
            ArrayList< EntityListener > al = _entityListeners.get( entity );
            if ( al != null ) {
                for ( EntityListener el2 : al ) {
                    if ( el2.equals( el ) ) {
                        al.remove( el );
                    }
                }
            }
        }
    }

    /**
     * Call any listeners associated with this Entity, and then remove them.
     *
     * @param entity
     */
    public void callEntityListeners( String entity ) {
        synchronized ( _entityListeners ) {
            ArrayList< EntityListener > al = _entityListeners.get( entity );
            if ( al == null ) {
                al = new ArrayList< EntityListener >();
                _entityListeners.put( entity, al );
            }

            for ( EntityListener listener : al ) {
                listener.onEntityUpdated( entity );
            }

            al.clear(); // remove references, it doesn't need calling twice.
        }
    }

}
