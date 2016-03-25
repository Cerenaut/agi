/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.orm;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Has a bunch of things that are called when this is called
 * Used to run a distributed system with no ordering dependencies
 *
 * @author dave
 */
public class CallbackMap implements Callback {

    public HashMap< String, Callback > _vertices = new HashMap< String, Callback >();

    public CallbackMap() {

    }

    public void addCallback( String name, Callback c ) {
        _vertices.put( name, c );
    }

    public Callback getCallback( String name ) {
        return _vertices.get( name );
    }

    @Override
    public void call() {
        Set< Entry< String, Callback > > es = _vertices.entrySet();

        for ( Entry< String, Callback > e : es ) {
            e.getValue().call();
        }
    }
}

