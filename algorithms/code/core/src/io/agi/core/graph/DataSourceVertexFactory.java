/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.graph;

import org.w3c.dom.Element;

/**
 *
 * @author dave
 */
public interface DataSourceVertexFactory {
    
    public DataSourceVertex createVertex( DataSourceGraph sg, String name, String type, Element e );
    
}
