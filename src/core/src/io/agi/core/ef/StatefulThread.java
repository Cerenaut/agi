/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.ef;

import io.agi.core.data.Data;
import io.agi.core.orm.CallbackThread;
import io.agi.core.orm.ObjectMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class that combines the Stateful and Asynchronous interfaces so you don't
 * have to write code to implement them.
 * 
 * A lock is provided to manage concurrent access to the state of this object.
 * In particular this lock should be used for all accesses to the ObjectMap.
 * 
 * You can either do fine-grained locking via the setStateSafe and getStateSafe
 * methods, or coarse locking with explicit locking.
 * 
 * @author dave
 */
public class StatefulThread extends CallbackThread implements Stateful {

    protected final Lock _l = new ReentrantLock();
    
    protected ObjectMap _om;
    protected HashSet< String > _dataKeys = new HashSet< String >();
    

    public StatefulThread() {
        _om = new ObjectMap();
    }
    public StatefulThread( ObjectMap om ) {
        _om = om;
    }

    public Lock getLock() {
        return _l;
    }

    /**
     * Convenience method. Blocking lock if argument is null.
     * Else arg is wait timeout.
     * @param waitTime
     * @return 
     */
    public boolean lock( Integer waitTime ) {
        if( waitTime != null ) {
            try {
                return _l.tryLock( waitTime, TimeUnit.MILLISECONDS );
            }
            catch( InterruptedException ie ) {
                return false;
            }
        }
        else {
            _l.lock();
            return true;
        }
    }

    /**
     * Unlock lock if held.
     */
    public void unlock() {
        _l.unlock();
    }
    
    @Override public Collection< String > getKeys() {
        return _dataKeys;
    }

    @Override public Data getState( String key ) {
        Object o = _om.get( key ); 
        if( o != null ) {
            if( o instanceof Data ) {
                return (Data)o;
            }
        }
        return null;
    }
    
    @Override public void setState( String key, Data fa ) {
        _dataKeys.add( key );
        _om.put( key, fa );
    }
    
    @Override public Data getStateSafe( String key ) {
        if( !lock( null ) ) {
            return null;
        }
        
        Data d = getState( key );
        
        if( d != null ) {
            d = new Data( d ); // deep copy
        }
        
        unlock();
        
        return d;
    }
    
    @Override public void setStateSafe( String key, Data d ) {
        if( !lock( null ) ) {
            return;
        }
        
        setState( key, d );
        
        unlock();
    }
    
}
