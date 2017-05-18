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

package io.agi.core.orm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Calls a set of callbacks when called.
 *
 * @author davidjr
 */
public class CallbackCollection implements Callback {

    public ArrayList< Callback > _cbs = new ArrayList< Callback >();

    public boolean _enabled = true;

    public CallbackCollection() {

    }

    public void add( int index, Callback cb ) {
        _cbs.add( index, cb );
    }

    public void add( Callback cb ) {
        _cbs.add( cb );
    }

    /**
     * Won't add more than once, based on object reference
     *
     * @param cb
     */
    public void addLazy( Callback cb ) {
        if( _cbs.contains( cb ) ) {
            return;
        }
        _cbs.add( cb );
    }

    public Collection< Callback > get() {
        return _cbs;
    }

    @Override
    public void call() {

        if( !_enabled ) {
            return;
        }

        for( Callback cb : _cbs ) {
            cb.call();
        }
    }
}
