/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.graph;

import org.w3c.dom.Element;

/**
 * This factory assumes something finds the vertex and updates its value.
 * 
 * @author dave
 */
public class SimpleVertexFactory implements DataSourceVertexFactory {

    public static final String VERTEX_TYPE = "simple";
    
    public static final String ATTRIBUTE_VECTOR_NAME = "vector";
    public static final String ATTRIBUTE_VECTOR_SIZE = "size";
    
    public SimpleVertexFactory() {
        
    }
    
    public DataSourceVertex createVertex( DataSourceGraph sg, String name, String type, Element e ) {
        
        String vectorName = e.getAttribute( ATTRIBUTE_VECTOR_NAME );
        String vectorSize = e.getAttribute( ATTRIBUTE_VECTOR_SIZE );
        
        int size = Integer.valueOf( vectorSize );
        
        SimpleVertex sv = new SimpleVertex();
        sv.setup( sg, name, VERTEX_TYPE, vectorName, size );
        
        return sv;
    }
    
}
