/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.framework.persistence.models;



import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Conversion to JSON for models.
 * <p/>
 * Created by dave on 16/02/16.
 */
public class ModelData {

    public static final String ENCODING_DENSE = "dense";
    public static final String ENCODING_SPARSE_BINARY = "sparse-binary";
    public static final String ENCODING_SPARSE_REAL = "sparse-real";

    public String name;
    public String refKeys;
    public String sizes;
    public String elements;

    public ModelData() {

    }

    public ModelData( String key, Data d, String encoding ) {
        name = key;
        refKeys = null;
        if( d != null ) {
            sizes = DataSizeToString( d._dataSize );
            elements = FloatArrayToString( d, encoding );
        }
    }

    public ModelData( String key, String refKeys ) {
        name = key;
        this.refKeys = refKeys;
        sizes = null;
        elements = null;
    }

    public ModelData( String key, String refKeys, String sizes, String elements ) {
        name = key;
        this.refKeys = refKeys;
        this.sizes = sizes;
        this.elements = elements;
    }

    public HashSet< String > getRefKeys() {
        try {
            HashSet< String > refKeys = new HashSet< String >();

            if( this.refKeys != null ) {
                String[] splitKeys = this.refKeys.split( "," );

                for( int i = 0; i < splitKeys.length; ++i ) {
                    String key = splitKeys[ i ].trim();
                    refKeys.add( key );
                }
            }

            return refKeys;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public boolean isReference() {
        if( refKeys != null ) {
            return true;
        }
        return false;
    }

    /**
     * Convert the Data object to its serialized form, so it can be stored.
     *
     * @param d
     */
    public void setData( Data d, String encoding ) {
        try {
            sizes = DataSizeToString( d._dataSize );
            elements = FloatArrayToString( d, encoding );
        }
        catch( Exception e ) {

        }
    }

    /**
     * Retrieves the object form of this data concept. Since the data may be a reference to one or more other
     *
     * @return
     */
    public Data getData() {
        // convert into data.
//        if( refKeys != null ) {
//            return null;
//        }

        try {
            DataSize ds = StringToDataSize( sizes );
            FloatArray fa = StringToFloatArray( elements );
            Data d = new Data( ds, fa );
            return d;
        }
        catch( Exception e ) {
            return null;
        }
    }

    /**
     * Convert a DataSize object to the serialized form.
     *
     * @param ds
     * @return
     */
    public static String DataSizeToString( DataSize ds ) {
        // Example
        //        {
        //          "sizes" : [ 10, 10 ],
        //          "labels" : [ "x", "y" ]
        //        }
        String s1 = "{\"sizes\":[";
        String s2 = "],\"labels\":[";
        String s3 = "]}";

        String sizes = "";
        String labels = "";
        for( int d = 0; d < ds._sizes.length; ++d ) {
            int size = ds._sizes[ d ];
            String label = ds.getLabel( d );
            if( d > 0 ) {
                sizes = sizes + ","; // add comma for preceding item
                labels = labels + ","; // add comma for preceding item
            }
            sizes = sizes + String.valueOf( size );
            labels = labels + "\"" + label + "\"";
        }
        String result = s1 + sizes + s2 + labels + s3;
        return result;
    }

    public static DataSize StringToDataSize( String s ) {
        try {
            String sizesString = GetJsonArrayProperty( s, "sizes" );
            String labelsString = GetJsonArrayProperty( s, "labels" );
            String[] splitSizes = sizesString.split( "," );
            String[] splitLabels = labelsString.split( "," );

            DataSize ds = new DataSize( splitSizes.length );

            for( int i = 0; i < splitSizes.length; ++i ) {
                String sizeString = splitSizes[ i ];
                String labelValue = splitLabels[ i ];
                labelValue = labelValue.replace( "\"", "" );
                Integer sizeValue = Integer.valueOf( sizeString );
                ds.set( i, sizeValue, labelValue );
            }

            return ds;
        }
        catch( Exception e ) {
            return null;
        }
    }

    public static String FloatArrayToString( FloatArray fa, String encoding ) {
        String s1 = "{ \"encoding\":\"" + encoding + "\",\"length\":";
        String s2 = ",\"elements\":["; // put elements last
        String s3 = "]}";

        String length = String.valueOf( fa._values.length );
        ArrayList< String > values = new ArrayList< String >();

        if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_BINARY ) ) ) {
            for( int i = 0; i < fa._values.length; ++i ) {
                float value = fa._values[ i ];
                if( value == 0.f ) {
                    continue; // only add the nonzero value indices.
                }

                String s = String.valueOf( i ); // TODO consider serializing this as integer.
                values.add( s );
            }
        }
        else if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_REAL ) ) ) {
            for( int i = 0; i < fa._values.length; ++i ) {
                float value = fa._values[ i ];
                if( value == 0.f ) {
                    continue; // only add the nonzero value indices.
                }

                String s = String.valueOf( i ) + "," + String.valueOf( value ); // index,value
                values.add( s );
            }
            //System.err.println( " Sparse real encoding: Original size: " + fa._values.length + " encoded size: " + values.size() );
        }
        else {
            values.ensureCapacity( fa._values.length );
            for( int i = 0; i < fa._values.length; ++i ) {
                float value = fa._values[ i ];
                String s = String.valueOf( value );
                values.add( s );
            }
        }

        String elements = StringUtils.join( values, "," );

        String result = s1 + length + s2 + elements + s3;
        return result;
    }

    public static FloatArray StringToFloatArray( String s ) {
        try {
            String lengthString = GetJsonProperty( s, "length" );
            Integer length = Integer.valueOf( lengthString );

            String encoding = GetJsonProperty( s, "encoding" );
            if( encoding == null ) {
                encoding = ModelData.ENCODING_DENSE; // assume if not mentioned
            }

            FloatArray fa = new FloatArray( length ); // default to zeroes

            String elementsString = GetJsonArrayProperty( s, "elements" );
            String[] splitString = elementsString.split( "," );

            if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_BINARY ) ) ) {
                if( elementsString.length() > 0 ) { // can be empty string if all zeros.
                    for( int i = 0; i < splitString.length; ++i ) {
                        String valueString = splitString[ i ];
                        Integer value = Integer.valueOf( valueString );
                        fa._values[ value ] = 1.f;
                    }
                }
            }
            else if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_REAL ) ) ) {
                if( elementsString.length() > 0 ) { // can be empty string if all zeros.
                    int values = splitString.length >> 1;
                    for( int i = 0; i < values; ++i ) {
                        int i1 = i * 2;
                        int i2 = i1 +1;
                        String indexString = splitString[ i1 ];
                        String valueString = splitString[ i2 ];
                        Integer index = Integer.valueOf( indexString );
                        Float value = Float.valueOf( valueString );
                        fa._values[ index ] = value;
                    }
                }
            }
            else {
                for( int i = 0; i < splitString.length; ++i ) {
                    String valueString = splitString[ i ];
                    Float value = Float.valueOf( valueString );
                    fa._values[ i ] = value;
                }
            }
            return fa;
        }
        catch( Exception e ) {
            return null;
        }
    }

    private static String GetJsonProperty( String json, String property ) {
        int i1 = json.indexOf( "\"" + property + "\"" );
        int i2 = json.indexOf( ":", i1 ); // this is the start of the property value
        int i3a = json.indexOf( "}", i2 ); // it ends either with } or , depending on whether it is the last property.
        int i3b = json.indexOf( ",", i2 );
        if( i3a < 0 ) {
            i3a = i3b;
        }
        if( i3b < 0 ) {
            i3b = i3a;
        }
        int i3 = Math.min( i3a, i3b );
        String propertyValue = json.substring( i2 + 1, i3 );
        propertyValue = propertyValue.replaceAll( "^\"|\"$", "" ); // remove quotes if present at start and end. http://stackoverflow.com/questions/2608665/how-can-i-trim-beginning-and-ending-double-quotes-from-a-string
        return propertyValue;
    }

    private static String GetJsonArrayProperty( String json, String property ) {
        int i1 = json.indexOf( "\"" + property + "\"" );
        int i2 = json.indexOf( "[", i1 ); // this is the start of the property value
        int i3 = json.indexOf( "]", i2 ); // it ends either with } or ,

        String propertyValue = json.substring( i2 + 1, i3 );
        return propertyValue;
    }
}
