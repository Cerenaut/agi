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

package io.agi.core.ann.supervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 10/01/16.
 */
public class FeedForwardNetworkConfig extends NetworkConfig {

    public String _keyNbrLayers = "nbr-layers";
    public String _keyNbrInputs = "nbr-inputs";
//    public String _keyNbrOutputs = "nbr-outputs";
    public String _keyLayerSizes = "layer-sizes"; // comma separated list of layer sizes
    public String _keyLayerTransferFns = "layer-transfer-functions";
    public String _keyCostFunction = "cost-function";
    public String _keyL2Regularization = "l2-regularization";
    public String _keyLearningRate = "learning-rate";

    public static final String BATCH_COUNT = "batch-age";
    public static final String BATCH_SIZE = "batch-size";

    public FeedForwardNetworkConfig() {
    }

    public void setup( ObjectMap om, String name, Random r, String costFunction, int inputs, int layers, String layerSizes, String layerActivationFunctions, float l2Regularization, float learningRate, int batchSize ) {
        super.setup( om, name, r );

        setCostFunction(costFunction);
        setLayerTransferFns(layerActivationFunctions);
        setNbrLayers(layers);
        setLayerSizes(layerSizes);
        setNbrInputs(inputs);
//        setNbrOutputs( outputs );
        setL2Regularization( l2Regularization );
        setLearningRate(learningRate);

        setBatchCount( 0 );
        setBatchSize( batchSize );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom(nc, name);

        FeedForwardNetworkConfig c = ( FeedForwardNetworkConfig ) nc;

        setCostFunction(c.getCostFunction());
        setLayerTransferFns(c.getLayerTransferFns());
        setNbrLayers(c.getNbrLayers());
        setLayerSizes(c.getLayerSizes());
        setNbrInputs(c.getNbrInputs());
//        setNbrOutputs( c.getNbrOutputs() );
        setL2Regularization(c.getL2Regularization());
        setLearningRate(c.getLearningRate());

        setBatchCount( c.getBatchCount() );
        setBatchSize( c.getBatchSize() );
    }

    public void setCostFunction(String f) {
        _om.put(getKey(_keyCostFunction), f);
    }

    public void setLayerTransferFns( String f ) {
        _om.put( getKey( _keyLayerTransferFns ), f );
    }

    public String getCostFunction() {
        return ( String ) _om.get( getKey(_keyCostFunction) );
    }

    public String getLayerTransferFn( int layer ) {
        String fns = getLayerTransferFns();
        String[] names = fns.split( "," );
        return names[ layer ];
    }

    public String getLayerTransferFns() {
        return ( String ) _om.get( getKey( _keyLayerTransferFns ) );
    }

    public void setLayerSizes( String f ) {
        _om.put( getKey( _keyLayerSizes ), f );
    }

    public int getLayerSize( int layer ) {
        String fns = getLayerSizes();
        String[] sizes = fns.split( "," );
        String size = sizes[ layer ];
        return Integer.valueOf( size );
    }

    public String getLayerSizes() {
        return ( String ) _om.get( getKey( _keyLayerSizes ) );
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( _keyLearningRate ) );
        return r.floatValue();
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( _keyLearningRate ), r );
    }

    public void setNbrLayers( int layers ) {
        _om.put( getKey( _keyNbrLayers ), layers );
    }

    public Integer getNbrLayers() {
        return ( Integer ) _om.get( getKey( _keyNbrLayers ) );
    }

    public void setNbrInputs( int layers ) {
        _om.put( getKey( _keyNbrInputs ), layers );
    }

    public Integer getNbrInputs() {
        return ( Integer ) _om.get( getKey( _keyNbrInputs ) );
    }

//    public void setNbrOutputs( int layers ) {
//        _om.put( getKey( _keyNbrOutputs ), layers );
//    }

    public Integer getNbrOutputs() {
//        return ( Integer ) _om.get( getKey( _keyNbrOutputs ) );
        int layers = getNbrLayers();
        int outputs = getLayerSize( layers -1 );
        return outputs;
    }

    public void setL2Regularization( float r ) {
        _om.put( getKey( _keyL2Regularization ), r );
    }

    public Float getL2Regularization() {
        return ( Float ) _om.get( getKey( _keyL2Regularization ) );
    }

    public void setBatchCount( int n ) {
        _om.put( getKey( BATCH_COUNT ), n );
    }

    public void setBatchSize( int n ) {
        _om.put( getKey( BATCH_SIZE ), n );
    }

    public int getBatchCount() {
        Integer n = _om.getInteger(getKey(BATCH_COUNT));
        return n.intValue();
    }

    public int getBatchSize() {
        Integer n = _om.getInteger(getKey(BATCH_SIZE));
        return n.intValue();
    }
}
