/*
 * Copyright (c) 2017.
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

package io.agi.core.data;

/**
 * Created by dave on 1/05/17.
 */
public class Int3d {

    public int _x = 0;
    public int _y = 0;
    public int _z = 0;

    public Int3d( int x, int y, int z ) {
        _x = x;
        _y = y;
        _z = z;
    }

    public Int3d( Int3d f3 ) {
        _x = f3._x;
        _y = f3._y;
        _z = f3._z;
    }

    public int getWidth() {
        return _x;
    }

    public int getHeight() {
        return _y;
    }

    public int getDepth() {
        return _z;
    }

    public int getVolume() {
        return _x * _y * _z;
    }

    @Override
    public boolean equals( Object o ) {
        if( !( o instanceof Int3d ) ) {
            return false;
        }

        Int3d f3 = ( Int3d ) o;

        return( ( _x == f3._x ) && ( _y == f3._y ) && ( _z == f3._z ) );
    }

}
