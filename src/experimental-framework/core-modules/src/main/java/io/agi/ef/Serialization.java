package io.agi.ef;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray2;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utilities for JSON serialization of data structures.
 *
 * Created by dave on 19/09/15.
 */
public class Serialization {

    /**
     * Convert a DataSize object to the serialized form.
     * @param ds
     * @return
     */
    public static String DataSizeToString(DataSize ds) {
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
            labels = labels + "\"" + String.valueOf( label ) + "\"";
        }
        String result = s1 + sizes + s2 + labels + s3;
        return result;
    }

    /**
     * Convert a JSON object into a DataSize object.
     * @param jo
     * @return
     */
    public static DataSize DataSizeFromJson(JSONObject jo) {
        try {
            JSONArray jaSizes = jo.getJSONArray( "sizes" );
            JSONArray jaLabels = jo.getJSONArray( "labels" );
            int length = jaSizes.length();

            DataSize ds = new DataSize( length );

            for( int d = 0; d < length; ++d ) {
                int size = jaSizes.getInt( d );
                ds._sizes[ d ] = size;
                String label = jaLabels.getString( d );
                ds.setLabel( d, label );
            }

            return ds;
        }
        catch( Exception e ) {
            return null;
        }
    }

    /**
     * Convert a String into a DataSize object, assuming it's JSON.
     * @param s
     * @return
     */
    public static DataSize DataSizeFromString(String s) {
        // Example
        //        {
        //          "sizes" : [ 10, 10 ],
        //          "labels" : [ "x", "y" ]
        //        }
        try {
            JSONObject jo = new JSONObject(s);
            return DataSizeFromJson(jo);
        }
        catch( Exception e ) {
            return null;
        }
    }

    /**
     * Converts a Data object into a JSON serialized String format.
     * @param d
     * @return
     */
    public static String DataToString(Data d) {
        // Example
        //        {
        //         "elements" : [ 1,2,3, ... ],
        //         "size" : {
        //          "sizes" : [ 10, 10 ],
        //          "labels" : [ "x", "y" ]
        //         }
        //        }
        String s1 = "{\"elements\":[";
        String s2 = "],\"size\":";
        String s3 = "}";

        String size = DataSizeToString(d._d);

        String elements = "";
        for( int i = 0; i < d._values.length; ++i ) {
            if( i > 0 ) {
                elements = elements + ","; // add comma for preceding item
            }
            float value = d._values[ i ];
            elements = elements + String.valueOf( value );
        }
        String result = s1 + elements + s2 + size + s3;
        return result;
    }

    /**
     * Convert a JSON representation of a Data object into a Data object!
     * @param jo
     * @return
     */
    public static Data DataFromJson(JSONObject jo) {
        // Example
        //        {
        //         "elements" : [ 1,2,3, ... ],
        //         "size" : {
        //          "sizes" : [ 10, 10 ],
        //          "labels" : [ "x", "y" ]
        //         }
        //        }
        try {
            // first get the size object:
            JSONObject joDataSize = jo.getJSONObject( "size" );
            DataSize ds = DataSizeFromJson(joDataSize);

            // work out the length
            JSONArray jaElements = jo.getJSONArray( "elements" );
            int length = jaElements.length();

            if( length != ds.getVolume() ) {
                return null;
            }

            Data d = new Data( ds );

            for( int i = 0; i < length; ++i ) {
                double value = jaElements.getDouble(i);
                d._values[ i ] = (float)value;
            }

            return d;
        }
        catch( Exception e ) {
            return null;
        }
    }

    /**
     * Converts a String serialization of a JSON object into a Data object.
     * @param s
     * @return
     */
    public static Data DataFromString(String s) {
        // Example
        //        {
        //         "elements" : [ 1,2,3, ... ],
        //         "size" : {
        //          "sizes" : [ 10, 10 ],
        //          "labels" : [ "x", "y" ]
        //         }
        //        }
        try {
            JSONObject jo = new JSONObject(s);
            return DataFromJson(jo);
        }
        catch( Exception e ) {
            return null;
        }
    }

    /**
     * Convert FloatArray to String
     * @param d
     * @return
     */
    public static String FloatArrayToString(FloatArray2 d) {
        String s1 = "{\"elements\":[";
        String s2 = "],\"length\":";
        String s3 = "}";

        String length = String.valueOf(d._values.length);

        String elements = "";
        for( int i = 0; i < d._values.length; ++i ) {
            if( i > 0 ) {
                elements = elements + ","; // add comma for preceding item
            }
            float value = d._values[ i ];
            elements = elements + String.valueOf( value );
        }
        String result = s1 + elements + s2 + length + s3;
        return result;
    }

    /**
     * Convert JSON to FloatArray
     * @param jo
     * @return
     */
    public static FloatArray2 FloatArrayFromJson(JSONObject jo) {
        // Example
        //        {
        //         "elements" : [ 1,2,3, ... ],
        //         "length" : 10
        //        }
        try {
            // first get the length:
            int length = jo.getInt( "length" );
//            JSONObject joDataSize = jo.getJSONObject( "size" );
//            DataSize ds = DataSizeFromJson(joDataSize);

            // work out the length
            JSONArray jaElements = jo.getJSONArray("elements");

            FloatArray2 d = new FloatArray2( length );

            for( int i = 0; i < length; ++i ) {
                double value = jaElements.getDouble( i );
                d._values[ i ] = (float)value;
            }

            return d;
        }
        catch( Exception e ) {
            return null;
        }
    }

    /**
     * Convert String to FloatArray, assuming String is JSON.
     * @param s
     * @return
     */
    public static FloatArray2 FloatArrayFromString(String s) {
        // Example
        //        {
        //         "elements" : [ 1,2,3, ... ],
        //         "size" : {
        //          "sizes" : [ 10, 10 ],
        //          "labels" : [ "x", "y" ]
        //         }
        //        }
        try {
            JSONObject jo = new JSONObject(s);
            return FloatArrayFromJson(jo);
        }
        catch( Exception e ) {
            return null;
        }
    }

}
