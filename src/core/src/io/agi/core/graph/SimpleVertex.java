/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.graph;

/**
 * Class creates a simple vertex with a single vector of data.
 * Sutable to attach a sensor to the hierarchy.
 * 
 * Has no sources and default creates a single vector of sensed data.
 * 
 * Override update to change value on request. Or just change on your own 
 * schedule.
 * 
 * @author dave
 */
public class SimpleVertex extends DataSourceVertex {
    
    public SimpleVertex() {
        
    }
    
    public void setup( DataSourceGraph g, String vertexName, String vertexType, String vectorName, int vectorSize ) {
        super.setup( g, vertexName, vertexType );
        addVector( vectorName, vectorSize );
    }
    
}
