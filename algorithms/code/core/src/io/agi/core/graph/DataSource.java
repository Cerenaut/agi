/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.graph;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import java.util.ArrayList;

/**
 * A source describes how to get and collate (concatenate, specifically) a set
 * of vectors from vertices in the hierarchy. 
 *
 * The source wraps this data by adding it as a named vector in the local 
 * vertex, to which it has a reference.
 * 
 * @author dave
 */
public class DataSource {

    public String _name;
    
    public DataSourceVertex _v;  
    // a bunch of data sources anywhere in the hierarchy, typically but not 
    // always other vertices.
    public ArrayList< DataSize > _dimensions = new ArrayList< DataSize >();
    public ArrayList< DataSourceName > _addresses = new ArrayList< DataSourceName >();

    protected boolean _inputValid = false;
    //Data _input; // input data from the inputs, concatenated

    public void setup( String name, DataSourceVertex v ) {
        _name = name;
        _v = v;
    }

    public int getNbrSources() {
        return _addresses.size();
    }
    
    public ArrayList< DataSize > getDataSize() {
        return _dimensions;
    }
    
    public boolean isValid() {
        return _inputValid;
    }
    
    public void addVector( DataSourceName ha ) {
        _addresses.add( ha );
    }

    public Integer getVolume() {
        Data fa = getVector();
        if( fa != null ) {
            return fa.getSize();
        }
        return null;
    }
    
    public Data getVector() {
        return _v.getVector( _name );
    }
    
    public void clear() {
        _inputValid = false;
    }
    
    public void create() {
        
        ArrayList< Data > inputs = new ArrayList< Data >();

        boolean valid = true;
        
        int length = 0;
        int sourceAddresses = _addresses.size();

        // try to reassemble the list of source dimensions, if the list changes
        if( _dimensions.size() != sourceAddresses ) {
            _dimensions.clear();
            for( int s = 0; s < sourceAddresses; ++s ) {
                DataSourceName sa = _addresses.get( s );
                Data fa = _v.getVector( sa );
                
                if( fa == null ) {
                    continue;
                }
                
                _dimensions.add( fa._d ); // list may be incomplete if not all sources are defined
            }
        }
        
        for( DataSourceName ha : _addresses ) {
            Data fa = _v.getVector( ha );

            if( fa == null ) {
                valid = false;
                break;
            }
            
            inputs.add( fa );
            
            length += fa.getSize();
        }

        if( valid == false ) {
            _v.setVector( _name, null ); // indicate that it's not valid yet
            return;
        }
        
        // detect uninitialized or resized inputs:
        boolean reallocate = false;

        Data fa = _v.getVector( _name );

        // detect whether resize needed:
        if( fa == null ) {
            reallocate = true;
        }
        else if( fa.getSize() != length ) {
            reallocate = true;
        }     
        
        if( reallocate ) {
            fa = new Data( length );
        }

        // now copy content
        if( !_inputValid ) {
            int offset = 0;
            for( int i = 0; i < inputs.size(); ++i ) {
                Data input = inputs.get( i );
                int volume = input.getSize();
                fa.copyRange( input, offset, 0, volume );
                offset += volume;
            }

            // don't do this every time unless dirty
            _inputValid = true;
        }

        _v.setVector( _name, fa ); // make it available
    }
    
    /**
     * Work out which of the sources a 1-d input is from.
     * @param sourceDataSize
     * @param i
     * @return 
     */
    public Integer getSourceIndexGivenVectorIndex( int i ) {
        if( !isValid() ) {
            return null;
        }
        int sources = _dimensions.size();
        int offset = 0;
        for( int s = 0; s < sources; ++s ) {
            DataSize d = _dimensions.get( s );
            int volume = d.getVolume();
            offset += volume;
            if( i < offset ) {
                return s;
            }
        }
        return -1;
    }

    /**
     * Work out the offset within a source for a particular global index.
     * @param sourceDataSize
     * @param i
     * @return 
     */
    public Integer getSourceOffsetGivenVectorIndex( int i ) {
        if( !isValid() ) {
            return null;
        }
        
        int sources = _dimensions.size();
        int offset = 0;
        for( int s = 0; s < sources; ++s ) {
            DataSize d = _dimensions.get( s );
            int volume = d.getVolume();
            int offset2 = offset + volume;
            if( i < offset2 ) {
                return i - offset;
            }
            offset = offset2;
        }
        return -1;
    }
}
