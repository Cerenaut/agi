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
