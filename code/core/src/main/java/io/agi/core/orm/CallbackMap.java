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

        for( Entry< String, Callback > e : es ) {
            e.getValue().call();
        }
    }
}

