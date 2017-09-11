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

package io.agi.core.ann.convolutional;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dave on 11/08/17.
 */
public class ConvolutionalNetworkConfig extends NetworkConfig {

    // Method of describing the geometry of a convolutional network:
    // http://cs231n.github.io/convolutional-networks/
    // 3d input (e.g. x,y,z={rgb})
    // 3d output (w,h,d)
    // depth, stride and zero-padding
    // depth = z, a param
    // stride = stride of the filter
    // zero-padding (like an offset)
    // filters always extend through the full depth of the input volume. For example, if the input is [32x32x3] then
    // doing 1x1 convolutions would effectively be doing 3-dimensional dot products (since the input depth is 3 channels).

    public static final String KEY_LAYERS = "layers";

    public static final String KEY_LAYER_INPUT_PADDING = "layer-input-padding";
    public static final String KEY_LAYER_INPUT_STRIDE = "layer-input-stride";
    public static final String KEY_LAYER_WIDTH = "layer-width";
    public static final String KEY_LAYER_HEIGHT = "layer-height";
    public static final String KEY_LAYER_DEPTH = "layer-depth";

    public static final String KEY_LAYER_POOLING_WIDTH = "layer-pooling-width";
    public static final String KEY_LAYER_POOLING_HEIGHT = "layer-pooling-height";

    public static final String KEY_LAYER_FIELD_WIDTH = "layer-field-width";
    public static final String KEY_LAYER_FIELD_HEIGHT = "layer-field-height";
    public static final String KEY_LAYER_FIELD_DEPTH = "layer-field-depth";

    public ConvolutionalNetworkConfig() {

    }

    public void setup(
            ObjectMap om, String name, Random r,
            int nbrLayers,

            String layerInputPadding,
            String layerInputStride,
            String layerWidth,
            String layerHeight,
            String layerDepth,
            String layerfieldWidth,
            String layerfieldHeight,
            String layerfieldDepth,
            String layerPoolingWidth,
            String layerPoolingHeight
    ) {
        super.setup( om, name, r );

        setNbrLayers( nbrLayers );

        setLayerValues( KEY_LAYER_INPUT_PADDING, layerInputPadding );
        setLayerValues( KEY_LAYER_INPUT_STRIDE, layerInputStride );
        setLayerValues( KEY_LAYER_WIDTH, layerWidth );
        setLayerValues( KEY_LAYER_HEIGHT, layerHeight );
        setLayerValues( KEY_LAYER_DEPTH, layerDepth );
        setLayerValues( KEY_LAYER_FIELD_WIDTH, layerfieldWidth );
        setLayerValues( KEY_LAYER_FIELD_HEIGHT, layerfieldHeight );
        setLayerValues( KEY_LAYER_FIELD_DEPTH, layerfieldDepth );
        setLayerValues( KEY_LAYER_POOLING_WIDTH, layerPoolingWidth );
        setLayerValues( KEY_LAYER_POOLING_HEIGHT, layerPoolingHeight );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        ConvolutionalNetworkConfig c = ( ConvolutionalNetworkConfig ) nc;

        setNbrLayers( c.getNbrLayers() );

        setLayerValues( KEY_LAYER_INPUT_PADDING, c.getLayerValues( KEY_LAYER_INPUT_PADDING ) );
        setLayerValues( KEY_LAYER_INPUT_STRIDE, c.getLayerValues( KEY_LAYER_INPUT_STRIDE ) );
        setLayerValues( KEY_LAYER_WIDTH, c.getLayerValues( KEY_LAYER_WIDTH ) );
        setLayerValues( KEY_LAYER_HEIGHT, c.getLayerValues( KEY_LAYER_HEIGHT ) );
        setLayerValues( KEY_LAYER_DEPTH, c.getLayerValues( KEY_LAYER_DEPTH ) );
        setLayerValues( KEY_LAYER_FIELD_WIDTH, c.getLayerValues( KEY_LAYER_FIELD_WIDTH ) );
        setLayerValues( KEY_LAYER_FIELD_HEIGHT, c.getLayerValues( KEY_LAYER_FIELD_HEIGHT ) );
        setLayerValues( KEY_LAYER_FIELD_DEPTH, c.getLayerValues( KEY_LAYER_FIELD_DEPTH ) );
        setLayerValues( KEY_LAYER_POOLING_WIDTH, c.getLayerValues( KEY_LAYER_POOLING_WIDTH ) );
        setLayerValues( KEY_LAYER_POOLING_HEIGHT, c.getLayerValues( KEY_LAYER_POOLING_HEIGHT ) );
    }

    public int getNbrLayers() {
        Integer n = _om.getInteger( getKey( KEY_LAYERS ) );
        return n.intValue();
    }

    public int getLayerInputPadding( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_INPUT_PADDING, layer ) );
        return n;
    }

    public int getLayerInputStride( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_INPUT_STRIDE, layer ) );
        return n;
    }

    public int getLayerWidth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_WIDTH, layer ) );
        return n;
    }

    public int getLayerHeight( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_HEIGHT, layer ) );
        return n;
    }

    public int getLayerDepth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_DEPTH, layer ) );
        return n;
    }

    public int getLayerFieldWidth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_FIELD_WIDTH, layer ) );
        return n;
    }

    public int getLayerFieldHeight( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_FIELD_HEIGHT, layer ) );
        return n;
    }

    public int getLayerFieldDepth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_FIELD_DEPTH, layer ) );
        return n;
    }

    public int getLayerPoolingWidth( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_POOLING_WIDTH, layer ) );
        return n;
    }

    public int getLayerPoolingHeight( int layer ) {
        int n = Integer.valueOf( getLayerValue( KEY_LAYER_POOLING_HEIGHT, layer ) );
        return n;
    }

    public void setNbrLayers( int layers ) {
        _om.put( getKey( KEY_LAYERS ), layers );
    }

    public void setLayerInputPadding( int layer, int n ) {
        setLayerValue( KEY_LAYER_INPUT_PADDING, layer, String.valueOf( n ), "0" );
    }

    public void setLayerInputStride( int layer, int n ) {
        setLayerValue( KEY_LAYER_INPUT_STRIDE, layer, String.valueOf( n ), "0" );
    }

    public void setLayerWidth( int layer, int n ) {
        setLayerValue( KEY_LAYER_WIDTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerHeight( int layer, int n ) {
        setLayerValue( KEY_LAYER_HEIGHT, layer, String.valueOf( n ), "0" );
    }

    public void setLayerDepth( int layer, int n ) {
        setLayerValue( KEY_LAYER_DEPTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerFieldWidth( int layer, int n ) {
        setLayerValue( KEY_LAYER_FIELD_WIDTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerFieldHeight( int layer, int n ) {
        setLayerValue( KEY_LAYER_FIELD_HEIGHT, layer, String.valueOf( n ), "0" );
    }

    public void setLayerFieldDepth( int layer, int n ) {
        setLayerValue( KEY_LAYER_FIELD_DEPTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerPoolingWidth( int layer, int n ) {
        setLayerValue( KEY_LAYER_POOLING_WIDTH, layer, String.valueOf( n ), "0" );
    }

    public void setLayerPoolingHeight( int layer, int n ) {
        setLayerValue( KEY_LAYER_POOLING_HEIGHT, layer, String.valueOf( n ), "0" );
    }


    public String getLayerValues( String key ) {
        return ( String ) _om.get( key );
    }

    public Float getLayerValueFloat( String key, int layer ) {
        String value = getLayerValue( key, layer );
        if( value == null ) {
            return null;
        }
        Float f = Float.valueOf( value );
        return f;
    }

    public Integer getLayerValueInteger( String key, int layer ) {
        String value = getLayerValue( key, layer );
        if( value == null ) {
            return null;
        }
        Integer n = Integer.valueOf( value );
        return n;
    }

    public String getLayerValue( String key, int layer ) {
        String values = _om.getString( getKey( key ) );
        return GetLayerValue( values, layer );
    }

    public static String GetLayerValue( String values, int layer ) {

        String[] sizes = values.split( "," );
        if( layer >= sizes.length ) {
            return "";
        }
        String value = sizes[ layer ];
        return value;
    }

    public void setLayerValues( String key, String values ) {
        _om.put( getKey( key ), values );
    }

    public void setLayerValue( String key, int layer, String value, String defaultValue ) {
        int layers = getNbrLayers();
        if( layer >= layers ) {
            return;
        }

        // get old value
        String oldValues = _om.getString( getKey( key ) );
        String[] oldValueArray = oldValues.split( "," );

        ArrayList< String > newValues = new ArrayList< String >();
        while( newValues.size() < layers ) {
            newValues.add( defaultValue );
        }

        for( int i = 0; i < oldValueArray.length; ++i ) {
            String oldValue = oldValueArray[ i ];
            newValues.set( i, oldValue );
        }

        // replace new value
        newValues.set( layer, value );

        String newValuesString = String.join( ",", newValues );

        _om.put( getKey( key ), newValuesString );
    }
}
