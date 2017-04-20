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



import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray;
import io.agi.framework.Node;
import io.agi.framework.persistence.DataModelData;
import io.agi.framework.persistence.DataDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

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
            HashSet< String > refKeys = new HashSet<>();

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
     * Retrieves the object form of this data concept. If the data is a reference to other data, the contents cannot be
     * resolved.
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
     * Retrieves the object form of this data concept. If the data is a reference to other data, the contents will be
     * resolved by deserializing the referenced Data via a Node and a DataDeserializer object.
     *
     * @param n
     * @param deserializer
     * @return
     */
    public Data getData( Node n, DataDeserializer deserializer ) {
        HashSet< String > refKeys = getRefKeys();

        if( refKeys.isEmpty() ) {
            return getData();
        }
        else {
            // Create an output matrix which is a composite of all the referenced inputs.
            HashMap< String, Data > allRefs = new HashMap<>();

            for( String refKey : refKeys ) {
                ModelData refJson = n.getModelData( refKey, deserializer );
                if( refJson == null ) {
                    continue; // don't put in data store
                }
                Data refData = refJson.getData();
                allRefs.put( refKey, refData );
            }

            Data combinedData = deserializer.getCombinedData( name, allRefs );
            String combinedEncoding = deserializer.getEncoding( name );
            setData( combinedData, combinedEncoding ); // data added to ref keys.

// NOTE: If i put this in the cache, it loses the refkeys and becomes constant.
//                if( _config.cache ) {
//                    _n.setCachedData( inputKey, combinedData ); // DAVE: BUG? It writes it back out.. I guess we wanna see this, but seems excessive.
//                }
//                else {
//            n.persistData( this, combinedData ); // save the pre existing object to cache

//            DataModelData dmd = new DataModelData();
//            dmd._d = combinedData;
//            dmd._md = this;
            n.setDataCache( this.name, combinedData, combinedEncoding ); // save the pre existing object to cache
//                }

            return combinedData;
        }
    }

    /**
     * Efficient implementation to convert to a string of valid JSON objects directly.
     *
     * @param modelDatas
     * @return
     */
    public static String ModelDatasToJsonString( Collection< ModelData > modelDatas ) {
        StringBuilder sb = new StringBuilder( 100 );

        ModelDatasToJsonStringBuilder( modelDatas, sb );

        String s = sb.toString();
        return s;
    }

    /**
     * Converts a collection of ModelData to serial form inside a StringBuilder, for efficiency.
     *
     * @param modelDatas
     * @param sb
     */
    public static void ModelDatasToJsonStringBuilder( Collection< ModelData > modelDatas, StringBuilder sb ) {

        sb.append( "[ " );

        boolean first = true;

        for( ModelData m : modelDatas ) {

            if( first ) {
                first = false;
            } else {
                sb.append( "," );
            }

            m.toString( sb );
        }

        sb.append( " ]" );
    }

    /**
     * Converts this object to its serial form inside a StringBuilder, for efficiency.
     * @param sb
     */
    public void toString( StringBuilder sb ) {
        sb.append( "{ " );
        sb.append( " \"name\": \"" + name + "\"" + "," );
        sb.append( " \"refKeys\": \"" + refKeys + "\"" + ","  );
        sb.append( " \"sizes\": " + sizes + ","  );
        sb.append( " \"elements\": " );
        sb.append( elements ); // big one
        sb.append( " }" );
    }

    /**
     * Attempts to deserialize a JSON array of ModelData objects.
     *
     * @param json
     * @return
     * @throws Exception
     */
    public static Collection< ModelData > StringToModelDatas( String json ) throws Exception {
        Gson gson = new Gson();
        Type listType = new TypeToken< List< ModelData > >() {
        }.getType();

        List< ModelData > modelDatas = gson.fromJson( json, listType );

        // implement decoding?

        return modelDatas;
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
        ArrayList< String > chunks = new ArrayList< String >();
        ArrayList< String > values = new ArrayList< String >();
        int chunkSize = 100;

        if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_BINARY ) ) ) {
            for( int i = 0; i < fa._values.length; ++i ) {
                float value = fa._values[ i ];
                if( value == 0.f ) {
                    continue; // only add the nonzero value indices.
                }

                String s = String.valueOf( i ); // TODO consider serializing this as integer.
                values.add( s );
                if( values.size() >= chunkSize ) {
                    String chunk = StringUtils.join( values, "," );
                    chunks.add( chunk );
                    values.clear();
                }
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

        String elements = StringUtils.join( chunks, "," );
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

            // Chunk-stream based processing of deserialization: For memory efficiency.
            // We only hold one contiguous copy of the whole string in memory, and deserialize it into numbers in chunks.
            // This is complicated but saves a heap of RAM requirement.
            int elementsStringLength = elementsString.length();
            int chunkSize = 1024;
            int chunkOffset = 0;
            String temp = "";
            Float tempNumberOld = null;
            int tempNumberIdx = 0;

            //System.err.println( "NEW array! encidng: "+ encoding+ " sz: " + elementsStringLength  );

            while( chunkOffset < elementsStringLength ) {
                int beginIdx = chunkOffset;
                int endIdx = beginIdx + chunkSize;// exclusive
                endIdx = Math.min( elementsStringLength, endIdx );

                //System.err.println( "1 encidng: "+ encoding+ " tempNumberIdx: " + tempNumberIdx + " chunkOffset: " + chunkOffset + " i1: " + beginIdx + " i2: " + endIdx + " sz: " + elementsStringLength  );

                chunkOffset += chunkSize;

                String chunk = elementsString.substring( beginIdx, endIdx ).trim();

                // if chunk has a ',' before any digits, then we must insert a comma to replace what we removed
                String combined = null;

                // otherwise, we just combine
                if( combined == null ) {
                    combined = temp + chunk;
                }

                String[] splitString = combined.split( "," ); // last one may be incomplete

                // copy values in the combined chunk to an array of numbers
                float[] splitValues = new float[ splitString.length -1 ];

                for( int i = 0; i < splitString.length -1; ++i ) {
                    String splitStringElement = splitString[ i ];

//                    try {
                    Float value = Float.valueOf( splitStringElement );
                    splitValues[ i ] = value;
//                    }
//                    catch( NumberFormatException e ) {
//                        System.err.println( "TEMP: " + temp + " CHUNK: " + chunk + " COMB: " + combined );
//                    }
                }

                // remember the last number may be incomplete. So save that bit to do later
                // e.g. 123,456,789
                //             |     complete number (789)
                //               |   incomplete number (78)
                if( splitString.length > 0 ) {
                    temp = splitString[ splitString.length - 1 ]; // temp is the last bit
                    if( chunk.length() > 0 ) {
                        char charN = chunk.charAt( chunk.length() - 1 ); // remember, chunk is trimmed, so we don't need to check anything except the last char
                        if( charN == ',' ) {
                            temp = temp + ",";
                        }
                    }
                }

                // now process each of the numbers we do have
                for( int i = 0; i < splitValues.length; ++i ) {
                    int numberIdx = tempNumberIdx +i;
                    float value = splitValues[ i ];

//                    System.err.println( "numberIdx: " + numberIdx + " value: " + value );
                    decodeValue( encoding, fa, value, tempNumberOld, numberIdx );
                    tempNumberOld = value; // always the previously seen value, or null
                }

                tempNumberIdx += splitValues.length; // add the number of values we processed

            } // while( has more chunks )

            // handle the last string.
            try {
                int numberIdx = tempNumberIdx +0;
                float value = Float.valueOf( temp );
                decodeValue( encoding, fa, value, tempNumberOld, numberIdx );
            }
            catch( NumberFormatException e ) {
                e.printStackTrace();
                // nothing, just a bit of whitespace?
            }

//            String[] splitString = elementsString.split( "," );
//
//            if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_BINARY ) ) ) {
//                if( elementsString.length() > 0 ) { // can be empty string if all zeros.
//                    for( int i = 0; i < splitString.length; ++i ) {
//                        String valueString = splitString[ i ];
//                        Integer value = Integer.valueOf( valueString );
//                        fa._values[ value ] = 1.f;
//                    }
//                }
//            }
//            else if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_REAL ) ) ) {
//                if( elementsString.length() > 0 ) { // can be empty string if all zeros.
//                    int values = splitString.length >> 1;
//                    for( int i = 0; i < values; ++i ) {
//                        int i1 = i * 2;
//                        int i2 = i1 +1;
//                        String indexString = splitString[ i1 ];
//                        String valueString = splitString[ i2 ];
//                        Integer index = Integer.valueOf( indexString );
//                        Float value = Float.valueOf( valueString );
//                        fa._values[ index ] = value;
//                    }
//                }
//            }
//            else {
//                for( int i = 0; i < splitString.length; ++i ) {
//                    String valueString = splitString[ i ];
//                    Float value = Float.valueOf( valueString );
//                    fa._values[ i ] = value;
//                }
//            }
            return fa;
        }
        catch( Exception e ) {
         //   e.printStackTrace();
         //   System.exit( -1 );
            return null;
        }
    }

    private static void decodeValue( String encoding, FloatArray fa, float value, Float valueBefore, int numberIdx ) {
        if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_BINARY ) ) ) {
            int intValue = (int)value;
            fa._values[ intValue ] = 1.f;
        }
        else if( ( encoding != null ) && ( encoding.equals( ModelData.ENCODING_SPARSE_REAL ) ) ) {
            if( (numberIdx & 1) == 0 ) {
                // even: 0, 2 etc: do nothing
            }
            else {
                // odd: 1, 3, 5 etc
                Integer index = valueBefore.intValue();
                fa._values[ index ] = value;
            }
        }
        else { // default encoding
            fa._values[ numberIdx ] = value;
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

    public void zeroData() {
        sizes = null;
        elements = null;
    }
}
