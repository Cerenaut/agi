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

/**
 * An object that is part of a hierarchy of objects, creating a systematic method for generating unique names for
 * properties and parameters. Objects can obviously be nested.
 * <p/>
 * References an object map, as this is where objects can be stored by name.
 * <p/>
 * Created by dave on 10/01/16.
 */
public class NamedObject {

    protected String _name;
    protected ObjectMap _om;

    public NamedObject( String name, ObjectMap om ) {
        _name = name;
        _om = om;
        om.put( _name, this );
    }

    public String getName() {
        return _name;
    }

    public void setName( String name ) {
        _name = name;
    }

    public String getKey( String suffix ) {
        return GetKey( _name, suffix );
    }

    public static String GetKey( String name, String suffix ) {
        return Keys.concatenate( name, suffix );
    }

    public ObjectMap getObjectMap() {
        return _om;
    }

    public void setObjectMap( ObjectMap om ) {
        _om = om;
    }

    public Object getObject( String suffix ) {
        String key = getKey( suffix );
        return _om.get( key );
    }

    public void putObject( Object o, String suffix ) {
        String key = getKey( suffix );
        _om.put( key, o );
    }

}
