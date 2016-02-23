package io.agi.core.orm;

/**
 * An object that is part of a hierarchy of objects, creating a systematic method for generating unique names for
 * properties and parameters. Objects can obviously be nested.
 *
 * References an object map, as this is where objects can be stored by name.
 *
 * Created by dave on 10/01/16.
 */
public class NamedObject {

    protected String _name;
    protected ObjectMap _om;

    public NamedObject( String name, ObjectMap om ) {
        _name  = name;
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
        _om.put(key, o);
    }

}
