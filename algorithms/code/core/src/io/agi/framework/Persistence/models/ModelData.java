package io.agi.framework.persistence.models;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.data.FloatArray2;

import java.util.HashSet;

/**
 * Conversion to JSON for models.
 * <p>
 * Created by dave on 16/02/16.
 */
public class ModelData {

    public String _key;
    public String _refKeys;
    public String _sizes;
    public String _elements;

    public ModelData( String key, Data d ) {
        _key = key;
        _refKeys = null;
        if ( d != null ) {
            _sizes = DataSizeToString( d._dataSize ); //d._dataSize.toString();
            _elements = FloatArrayToString( d );//d._values.toString();
        }
    }

    public ModelData( String key, String refKeys ) {
        _key = key;
        _refKeys = refKeys;
        _sizes = null;
        _elements = null;
    }

    public ModelData( String key, String refKeys, String sizes, String elements ) {
        _key = key;
        _refKeys = refKeys;
        _sizes = sizes;
        _elements = elements;
    }

    public HashSet< String > getRefKeys() {
        try {
            HashSet< String > refKeys = new HashSet< String >();

            if ( _refKeys != null ) {
                String[] splitKeys = _refKeys.split( "," );

                for ( int i = 0; i < splitKeys.length; ++i ) {
                    String key = splitKeys[ i ];
                    refKeys.add( key );
                }
            }

            return refKeys;
        }
        catch ( Exception e ) {
            return null;
        }
    }

    public boolean isReference() {
        if ( _refKeys != null ) {
            return true;
        }
        return false;
    }

    /**
     * Conver the Data object to its serialized form, so it can be stored.
     *
     * @param d
     */
    public void setData( Data d ) {
        try {
            _sizes = DataSizeToString( d._dataSize );
            _elements = FloatArrayToString( d );
        }
        catch ( Exception e ) {

        }
    }

    /**
     * Retrieves the object form of this data concept. Since the data may be a reference to one or more other
     *
     * @return
     */
    public Data getData() {
        // convert into data.
//        if( _refKeys != null ) {
//            return null;
//        }

        try {
            DataSize ds = StringToDataSize( _sizes );
            FloatArray2 fa = StringToFloatArray( _elements );
            Data d = new Data( ds, fa );
            return d;
        }
        catch ( Exception e ) {
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
        for ( int d = 0; d < ds._sizes.length; ++d ) {
            int size = ds._sizes[ d ];
            String label = ds.getLabel( d );
            if ( d > 0 ) {
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

            for ( int i = 0; i < splitSizes.length; ++i ) {
                String sizeString = splitSizes[ i ];
                String labelValue = splitLabels[ i ];
                labelValue = labelValue.replace( "\"", "" );
                Integer sizeValue = Integer.valueOf( sizeString );
                ds.set( i, sizeValue, labelValue );
            }

            return ds;
        }
        catch ( Exception e ) {
            return null;
        }
    }

    public static String FloatArrayToString( FloatArray2 fa ) {
        String s1 = "{\"elements\":[";
        String s2 = "],\"length\":";
        String s3 = "}";

        String length = String.valueOf( fa._values.length );

        String elements = "";
        for ( int i = 0; i < fa._values.length; ++i ) {
            if ( i > 0 ) {
                elements = elements + ","; // add comma for preceding item
            }
            float value = fa._values[ i ];
            elements = elements + String.valueOf( value );
        }

        String result = s1 + elements + s2 + length + s3;
        return result;
    }

    public static FloatArray2 StringToFloatArray( String s ) {
        try {
            String lengthString = GetJsonProperty( s, "length" );
            Integer length = Integer.valueOf( lengthString );

            FloatArray2 fa = new FloatArray2( length );

            String elementsString = GetJsonArrayProperty( s, "elements" );
            String[] splitString = elementsString.split( "," );
            for ( int i = 0; i < splitString.length; ++i ) {
                String valueString = splitString[ i ];
                Float value = Float.valueOf( valueString );
                fa._values[ i ] = value;
            }

            return fa;
        }
        catch ( Exception e ) {
            return null;
        }
    }

    private static String GetJsonProperty( String json, String property ) {
        int i1 = json.indexOf( "\"" + property + "\"" );
        int i2 = json.indexOf( ":", i1 ); // this is the start of the property value
        int i3a = json.indexOf( "}", i2 ); // it ends either with } or , depending on whether it is the last property.
        int i3b = json.indexOf( ",", i2 );
        if ( i3a < 0 ) {
            i3a = i3b;
        }
        if ( i3b < 0 ) {
            i3b = i3a;
        }
        int i3 = Math.min( i3a, i3b );
        String propertyValue = json.substring( i2 + 1, i3 );
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
