/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.graph;

import io.agi.core.data.Data;
import io.agi.core.orm.Callback;
import java.util.HashMap;

/**
 * A hierarchy of vertices. Vertices share data by exposing named vectors.
 * Sources collate named vectors from other vertices and present it as a local
 * concatenated copy.
 * 
 * A tree-like graph. 
 * 
 * @author dave
 */
public class DataSourceGraph implements Callback {

    public static final String OBJECT_KEY = "data-source-graph"; // default, recommended ObjectDirectory key
    
    public HashMap< String, DataSourceVertex > _vertices = new HashMap< String, DataSourceVertex >();
    
    public DataSourceGraph() {
        
        
    }
    
    public void setup() {
        
    }
    
    public void call() {
        update();
    }
    
    public void update() {
        for( DataSourceVertex sv : _vertices.values() ) {
            sv.update();
        }
    }
    
    public void addVertex( DataSourceVertex sv ) {
        String name = sv._name;
        _vertices.put( name, sv );
    }
    public DataSourceVertex getVertex( String name ) {
        return _vertices.get( name );
    }
    
    public Data getVector( DataSourceName ha ) {
        DataSourceVertex sv = getVertex( ha._vertex );
        Data fa = sv.getVector( ha._vector );
        return fa;
    }
    
}
