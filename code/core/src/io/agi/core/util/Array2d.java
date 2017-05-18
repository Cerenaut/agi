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

package io.agi.core.util;

import io.agi.core.orm.AbstractFactory;

import java.util.ArrayList;

/**
 * Created by dave on 28/12/15.
 */
public class Array2d< T > {

    protected AbstractFactory< T > _f;
    protected ArrayList< T > _objects = new ArrayList< T >();
    protected int _w;
    protected int _h;

    public Array2d() {

    }

    public void setup( AbstractFactory< T > f, int w, int h ) {
        setFactory( f );
        setSize( w, h );
    }

    public void setFactory( AbstractFactory< T > f ) {
        _f = f;
    }

    public void setSize( int w, int h ) {
        int size = w * h;
        if( _objects.size() == size ) {
            return;
        }

        _w = w;
        _h = h;

        _objects = new ArrayList< T >();

        for( int i = 0; i < size; ++i ) {
            T o = _f.create();
            _objects.add( o );
        }
    }

    public int getSize() {
        return _w * _h;
    }

    public T get( int x, int y ) {
        int i = x * _w + _h;
        if( i >= getSize() ) {
            return null;
        }
        return _objects.get( i );
    }

    public ArrayList< T > getList() {
        return _objects;
    }

}
