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
import io.agi.core.orm.AbstractPair;
import io.agi.framework.persistence.DataJsonSerializer;
import io.agi.framework.references.DataRef;
import io.agi.framework.references.DataRefResolver;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Conversion to JSON for models.
 * <p/>
 * Created by dave on 16/02/16.
 */
public class ModelData {

    public String name; // includes node name
    public String refKeys;
    public String encoding;
    public String sizes;
    public String elements;

    public ModelData() {

    }

    public void clear() {
        name = null;
        refKeys = null;
        encoding = null;
        sizes = null;
        elements = null;
    }

    public void clearSerializedData() {
        sizes = null;
        elements = null;
    }

    public HashSet< String > getRefKeys() {
        return DataRefResolver.GetRefKeys( this.refKeys );
    }

    public void setReference( String name, String refKeys, String encoding ) {
        clearSerializedData();
        this.name = name;
        this.refKeys = refKeys;
        this.encoding = encoding;
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
     * @param dataRef
     */
    public boolean serialize( DataRef dataRef ) {//Data d, String encoding ) {
        try {
            this.name = dataRef._key;
            this.refKeys = dataRef._refKeys;
            this.encoding = dataRef._encoding;
            if( dataRef._data != null ) {
                this.sizes = DataJsonSerializer.DataSizeToString( dataRef._data._dataSize );
                this.elements = DataJsonSerializer.FloatArrayToString( dataRef._data, dataRef._encoding );
            }
            return true;
        }
        catch( Exception e ) {
            clear();
            return false;
        }
    }

    /**
     * Convert the Data object to its serialized form, so it can be stored, but only include the meta-data (not the bulk
     * data which may be very big).
     *
     * @param dataRef
     */
    public boolean serializeMeta( DataRef dataRef ) {//Data d, String encoding ) {
        try {
            this.name = dataRef._key;
            this.refKeys = dataRef._refKeys;
            this.encoding = dataRef._encoding;
            this.sizes = DataJsonSerializer.DataSizeToString( dataRef._data._dataSize );
            return true;
        }
        catch( Exception e ) {
            clear();
            return false;
        }
    }

    /**
     * Retrieves the object form of this data concept. If the data is a reference to other data, the contents cannot be
     * resolved.
     *
     * @return
     */
    public DataRef deserialize() {
        try {
            DataRef dataRef = new DataRef( this.name, null, this.refKeys, null );

            if( isReference() ) {

            }
            else {
                DataSize ds = DataJsonSerializer.StringToDataSize( sizes );
                AbstractPair< FloatArray, String > ap = DataJsonSerializer.StringToFloatArray( elements );
                FloatArray fa = ap._first;
                String encoding = ap._second;
                Data d = new Data( ds, fa );
                dataRef._data = d;
                dataRef._encoding = encoding;
            }

            return dataRef;
        }
        catch( Exception e ) {
            return null;
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



}
