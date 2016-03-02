/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.graph;

import io.agi.core.data.Data;
import java.util.HashMap;

/**
 * A vertex in the latest version of a hierarchy. Vertices get data from Sources
 which are objects that collate data from a set of other vertices in the 
 hierarchy.
 
 Each vertex has a set of named vectors, that contain data exposed to other
 vertices (used by Sources).
 
 Each DataSource adds a named vector with the same name as the source which contains
 the data obtained from the sources.
 
 Note that you can't have sources and vectors with the same name.
 * 
 * @author dave
 */
public abstract class DataSourceVertex {
    
    protected DataSourceGraph _g;
    protected String _name;
    protected String _type;
    
    public HashMap< String, DataSource > _sources = new HashMap< String, DataSource >(); // local copies of concatenated vectors from other
    protected HashMap< String, Data > _vectors = new HashMap< String, Data >();
    
    public DataSourceVertex() {
        
    }

    public void setup( DataSourceGraph g, String name, String type ) {
        _g = g;
        _name = name;
        _type = type;
        _g.addVertex( this );
    }
    
    public String getName() {
        return _name;
    }
    public String getType() {
        return _type;
    }
    
    public void call() {
        update();
    }
    
    public void update() {
        updateSources();
    }
    
    public void updateSources() {
        for( DataSource s : _sources.values() ) {
            s.clear();
            s.create();
        }
    }

    public void addSourceInput( String name, DataSourceName ha ) {
        DataSource s = addSource( name );
        s.addVector( ha );
    }
    public DataSource addSource( String name ) {
        DataSource s = getSource( name );
        if( s != null ) {
            return s;
        }

        if( _vectors.get( name ) != null ) {
            return null;
        }
        
        s = new DataSource();
        s.setup( name, this );
        _sources.put( name, s );
        return s;
    }
    public DataSource getSourceLazy( String name ) {
        return addSource( name );
    }
    public DataSource getSource( String name ) {
        return _sources.get( name );
    }

    public void addVector( String name ) {
        Data fa = null;
        addVector( name, fa );
    }
    public void addVector( String name, int size ) {
        Data fa = new Data( size );
        addVector( name, fa );
    }
    public void addVector( String name, int w, int h ) {
        Data fa = new Data( w, h );
        addVector( name, fa );
    }
    public void addVector( String name, int w, int h, int d ) {
        Data fa = new Data( w, h, d );
        addVector( name, fa );
    }
    public void addVector( String name, Data fa ) {
        setVector( name, fa );
    }
    public void setVector( String name, Data fa ) {
//        if( _sources.get( name ) != null ) {
//            return;
//        }
        _vectors.put( name, fa ); 
    }
    
    public Data getVector( DataSourceName ha ) {
        return _g.getVector( ha );
    }
    public Data getVector( String name ) {
        // Query both sources and simple vectors
        // Sources set these vectors when updated.
        // So we only need to use the vectors interface.
//        DataSource s = _sources.get( name );
//        if( s != null ) {
//            Data fa = s.getVector();
//            return fa; // may be null.
//        }
        
        return _vectors.get( name );
    }
}
