/*
 * Copyright (c) 2017.
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

package io.agi.framework.persistence;

import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray;
import io.agi.core.orm.AbstractPair;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Functions to convert Data, DataSize, and FloatArray objects to a JSON serial form (and back).
 *
 * Created by dave on 24/10/17.
 */
public class DataJsonSerializer {

    public static final String ENCODING_DENSE = "dense";
    public static final String ENCODING_SPARSE_BINARY = "sparse-binary";
    public static final String ENCODING_SPARSE_REAL = "sparse-real";

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
        ArrayList< String > chunks = new ArrayList< String >();
        ArrayList< String > values = new ArrayList< String >();
        int chunkSize = 100;

        if( ( encoding != null ) && ( encoding.equals( ENCODING_SPARSE_BINARY ) ) ) {
            for( int i = 0; i < fa._values.length; ++i ) {
                float value = fa._values[ i ];
                if( value == 0.f ) {
                    continue; // only add the nonzero value indices.
                }

                String s = String.valueOf( i ); // Important! Serializing this as integer.
                values.add( s );
                if( values.size() >= chunkSize ) {
                    String chunk = StringUtils.join( values, "," );
                    chunks.add( chunk );
                    values.clear();
                }
            }
        }
        else if( ( encoding != null ) && ( encoding.equals( ENCODING_SPARSE_REAL ) ) ) {
            for( int i = 0; i < fa._values.length; ++i ) {
                float value = fa._values[ i ];
                if( value == 0.f ) {
                    continue; // only add the nonzero value indices.
                }

                String s = String.valueOf( i ) + "," + String.valueOf( value ); // index,value
                values.add( s );
                if( values.size() >= chunkSize ) {
                    String chunk = StringUtils.join( values, "," );
                    chunks.add( chunk );
                    values.clear();
                }
            }
            //System.err.println( " Sparse real encoding: Original size: " + fa._values.length + " encoded size: " + values.size() );
        }
        else {
            values.ensureCapacity( fa._values.length );
            for( int i = 0; i < fa._values.length; ++i ) {
                float value = fa._values[ i ];
                String s = String.valueOf( value );
                values.add( s );
                if( values.size() >= chunkSize ) {
                    String chunk = StringUtils.join( values, "," );
                    chunks.add( chunk );
                    values.clear();
                }
            }
        }

        // clear last values into chunks
        if( values.size() > 0 ) {
            String chunk = StringUtils.join( values, "," );
            chunks.add( chunk );
            values.clear();
        }

        String elements = StringUtils.join( chunks, "," );
        String result = s1 + length + s2 + elements + s3;
        return result;
    }

    public static AbstractPair< FloatArray, String > StringToFloatArray( String s ) {
//        try {
        AbstractPair< FloatArray, String > ap = new AbstractPair< FloatArray, String >();

        String lengthString = GetJsonProperty( s, "length" );
        Integer length = Integer.valueOf( lengthString );

        String encoding = GetJsonProperty( s, "encoding" );
        if( encoding == null ) {
            encoding = ENCODING_DENSE; // assume if not mentioned
        }

        FloatArray fa = new FloatArray( length ); // default to zeroes

        ap._first = fa;
        ap._second = encoding;

        String elementsString = GetJsonArrayProperty( s, "elements" );

        // Chunk-stream based processing of deserialization: For memory efficiency.
        // We only hold one contiguous copy of the whole string in memory, and deserialize it into numbers in chunks.
        // This is complicated but saves a heap of RAM requirement.
        int elementsStringLength = elementsString.length();
        int chunkSize = 1024;
        int chunkOffset = 0;

        String oldPrefix = null;
        String newPrefix = null;

//            String temp = "";
        String previousElementValue = null;
        int globalElementIdx = 0;

        // example: "123.0, 1,0,23.001 ,22"
        // chunk  : "123.0, 1,0,2
        // chunk  : "123.0, 1,0     <-- remove last value up to last comma

        //System.err.println( "Decoding encoding: "+ encoding+ " string length: " + elementsStringLength  );

        while( chunkOffset < elementsStringLength ) {
            int beginIdx = chunkOffset;
            int endIdx = beginIdx + chunkSize;// exclusive
            endIdx = Math.min( elementsStringLength, endIdx );

            //System.err.println( "Decoding encoding: "+ encoding+ " chunkOffset: " + chunkOffset + " i1: " + beginIdx + " i2: " + endIdx + " string length: " + elementsStringLength  );

            chunkOffset += chunkSize;

            String chunk = elementsString.substring( beginIdx, endIdx ).trim();

            //System.err.println( chunk );

            // remove last potentially incomplete value
            int lastComma = chunk.lastIndexOf( ',' );

            String truncatedChunk = null;
            newPrefix = null;

            // if a comma found:
            if( lastComma >= 0 ) {
                truncatedChunk = chunk.substring( 0, lastComma ); // doesn't include comma
                newPrefix = chunk.substring( lastComma +1 ); // doesn't include comma
            }
            else {
                truncatedChunk = chunk;
            }

            // add old prefix string if any
            String prefixTruncatedChunk = truncatedChunk;

            if( oldPrefix != null ) {
                prefixTruncatedChunk = oldPrefix + truncatedChunk;
            }

            oldPrefix = newPrefix;

            // Now we have a chunk of complete values to split
            String[] splitString = prefixTruncatedChunk.split( "," ); // last one may be incomplete

            for( int i = 0; i < splitString.length; ++i ) {
                //int elementIdx = globalElementIdx +i;
                String splitStringElement = splitString[ i ];
                //Float value = Float.valueOf( splitStringElement );

//                    System.err.println( "numberIdx: " + numberIdx + " value: " + value );
                decodeValue( encoding, fa, splitStringElement, previousElementValue, globalElementIdx );
                previousElementValue = splitStringElement; // always the previously seen value, or null
                globalElementIdx++;
            }

//                globalElementIdx += splitValues.length; // add the number of values we processed

        } // while( has more chunks )


        // handle the last string.
        //System.err.println( "Last string: " + oldPrefix );
        try {
//                int numberIdx = tempNumberIdx +0;
            //float value = Float.valueOf( oldPrefix );
            if( oldPrefix != null ) {
                decodeValue(encoding, fa, oldPrefix, previousElementValue, globalElementIdx);
                previousElementValue = oldPrefix; // always the previously seen value, or null
                globalElementIdx++;
            }
        }
        catch( NumberFormatException e ) {
            //e.printStackTrace();
            //System.err.println( "Error decoding last string: " + oldPrefix );
            // Conclusion: Is nothing, just a bit of whitespace?
        }

        return ap;
//        }
//        catch( Exception e ) {
//            e.printStackTrace();
//            System.exit( -1 );
//            return null;
//        }
    }

    private static void decodeValue( String encoding, FloatArray fa, String value, String valueBefore, int numberIdx ) {
        if( ( encoding != null ) && ( encoding.equals( ENCODING_SPARSE_BINARY ) ) ) {
            int intValue = Integer.valueOf( value );
            fa._values[ intValue ] = 1.f;
        }
        else if( ( encoding != null ) && ( encoding.equals( ENCODING_SPARSE_REAL ) ) ) {
            if( (numberIdx & 1) == 0 ) {
                // even: 0, 2 etc: do nothing
            }
            else {
                // odd: 1, 3, 5 etc
                Integer elementIndex = Integer.valueOf( valueBefore );
                Float elementValue = Float.valueOf( value );
                fa._values[ elementIndex ] = elementValue;
            }
        }
        else { // default encoding
            Float elementValue = Float.valueOf( value );
            fa._values[ numberIdx ] = elementValue;
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
