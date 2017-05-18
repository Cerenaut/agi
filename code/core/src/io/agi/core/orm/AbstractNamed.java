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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.orm;

/**
 * Something that has a name. It is assumed (for this class to be useful) that
 * names are unique. Therefore, object identity now rests on the name. Further,
 * the name cannot be null.
 *
 * @author dave
 */
public class AbstractNamed< T > extends NamedObject {

    protected String _name;
    public T _named;

    public AbstractNamed( String name, ObjectMap om ) {
        super( name, om );
        _named = null;
    }

    public AbstractNamed( String name, T named, ObjectMap om ) {
        super( name, om );
        _named = named;
    }

//    public String getName() {
//        return name;
//    }
//
//    public void setName( String name ) {
//        name = name;
//    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        if( !( o instanceof AbstractNamed ) ) {
            return false;
        }

        AbstractNamed an = ( AbstractNamed ) o;

        // evaluate _first:
        return _name.equals( an._name );
    }

}
