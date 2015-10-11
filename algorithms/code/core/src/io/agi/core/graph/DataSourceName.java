/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.graph;

/**
 * Shared data is accessed from within the hierarchy by an address. An address
 * consists of a vertex and a vector within that vertex. The vectors in each 
 * vertex all have a unique name. Each vertex also has a unique name.
 * 
 * @author dave
 */
public class DataSourceName {
       
    public String _vertex;
    public String _vector;
    
    public DataSourceName( String vertex, String vector ) {
        _vertex = vertex;
        _vector = vector;
    }
    
}
