/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.graph;

import java.io.File;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author dave
 */
public class DataSourceGraphFactory {
    
    public static final String ELEMENT_VERTEX = "vertex";
    public static final String ELEMENT_EDGE = "edge"; // edges

    public static final String ATTRIBUTE_VERTEX_1 = "v1";
    public static final String ATTRIBUTE_VERTEX_2 = "v2";
    public static final String ATTRIBUTE_VECTOR_1 = "x1";
    public static final String ATTRIBUTE_VECTOR_2 = "x2";
    
    public HashMap< String, DataSourceVertexFactory > _vertexTypeFactories = new HashMap< String, DataSourceVertexFactory >();

    public DataSourceGraphFactory() {
        
    }
    
    public void addFactory( String vertexType, DataSourceVertexFactory svf ) {
        _vertexTypeFactories.put( vertexType, svf );
    }
    
    public DataSourceGraph read( String xmlFileName ) {
        DataSourceGraph sg = new DataSourceGraph();
        sg.setup();
        
        try {
            File xmlInputFile = new File( xmlFileName );
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.parse( xmlInputFile );
            NodeList nl = d.getElementsByTagName( ELEMENT_VERTEX );

            // First parse through all the vertices:
            for( int index = 0; index < nl.getLength(); ++index ) {
                
                Node node = nl.item( index );
                
                if( node.getNodeType() != Node.ELEMENT_NODE ) continue;

                Element element = (Element) node;

                if( !element.getTagName().equalsIgnoreCase( ELEMENT_VERTEX ) ) continue;
                
                String name = element.getAttribute( "name" );
                String type = element.getAttribute( "type" );

                assert( name != null );
                assert( type != null );
                
                DataSourceVertexFactory svf = _vertexTypeFactories.get( type );
                if( svf == null ) {
                    System.err.println( "ERROR: Couldn't find factory for vertex of type: " + type );
                    continue;
                }
                /*DataSourceVertex sv =*/ svf.createVertex( sg, name, type, element ); // DataSourceVertex setup() auto adds to graph
            }
            
            // Second parse through all the edges:
            nl = d.getElementsByTagName( ELEMENT_EDGE );

            // First parse through all the vertices:
            for( int index = 0; index < nl.getLength(); ++index ) {
                
                Node node = nl.item( index );
                
                if( node.getNodeType() != Node.ELEMENT_NODE ) continue;

                Element element = (Element) node;

                if( !element.getTagName().equalsIgnoreCase( ELEMENT_EDGE ) ) continue;
                
                addEdge( sg, element );
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        
        return sg;
    }

    public void addEdge( DataSourceGraph sg, Element e ) {
        String vertex1 = e.getAttribute( ATTRIBUTE_VERTEX_1 );
        String vector1 = e.getAttribute( ATTRIBUTE_VECTOR_1 );
        String vertex2 = e.getAttribute( ATTRIBUTE_VERTEX_2 );
        String vector2 = e.getAttribute( ATTRIBUTE_VECTOR_2 );

        DataSourceVertex sv2 = sg.getVertex( vertex2 );
        DataSourceName sa = new DataSourceName( vertex1, vector1 );
        sv2.addSourceInput( vector2, sa );
    }
}
