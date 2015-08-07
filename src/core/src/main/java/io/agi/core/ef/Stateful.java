/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.ef;

import io.agi.core.data.Data;
import java.util.Collection;

/**
 * An interface to explore the state of an object, and query it.
 * The state consists of a number of FloatArray objects and the ability to 
 * enumerate them by name. Each FloatArray has a fixed spatial structure in N
 * dimensions and can have between 0 and infinity elements.
 * 
 * This interface is not thread-safe by itself.
 * 
 * @author dave
 */
public interface Stateful {
    
    public Collection< String > getKeys();
    
    public Data getState( String key );
    public void setState( String key, Data fa );

    /**
     * Thread safe accessor; handles synchronization internally.
     * @param key
     * @return 
     */
    public Data getStateSafe( String key );
    
    /**
     * Thread safe accessor; handles synchronization internally.
     * @param key
     * @param fa 
     */
    public void setStateSafe( String key, Data fa );
    
}
