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

package io.agi.framework.entities.convolutional;

import io.agi.core.ann.convolutional.ConvolutionalNetwork;
import io.agi.core.ann.convolutional.competitive.CompetitiveLearningConvolutionalNetworkConfig;
import io.agi.core.ann.convolutional.competitive.CompetitiveLearningConvolutionalNetworkFactory;
import io.agi.core.ann.convolutional.competitive.CompetitiveLearningConvolutionalNetworkLayer;
import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 12/08/17.
 */
public class CompetitiveLearningConvolutionalNetworkEntity extends Entity {

    public static final String ENTITY_TYPE = "competitive-learning-convolutional-network";

    // data
    public static final String DATA_INPUT = "input";
    public static final String DATA_OUTPUT = "output";
    public static final String DATA_INVERSE = "inverse";
    public static final String DATA_INVERSE_SELECTED = "inverse-selected";

    public static final String DATA_LAYER_CONV_ERROR_ = "layer-conv-error-";
    public static final String DATA_LAYER_CONV_BEST_ = "layer-conv-best-";
    public static final String DATA_LAYER_POOL_ERROR_ = "layer-pool-error-";
    public static final String DATA_LAYER_POOL_BEST_ = "layer-pool-best-";

    public static final String DATA_LAYER_WEIGHTS_ = "layer-weights";
    public static final String DATA_LAYER_MASK_ = "layer-mask";
    public static final String DATA_LAYER_ERROR_ = "layer-error";
    public static final String DATA_LAYER_ACTIVE_ = "layer-active";

    public static final String DATA_LAYER_CELL_UTILITY_ = "layer-cell-utility";
    public static final String DATA_LAYER_CELL_STRESS_ = "layer-cell-stress";
    public static final String DATA_LAYER_CELL_AGES_ = "layer-cell-ages";
    public static final String DATA_LAYER_EDGES_ = "layer-edges";
    public static final String DATA_LAYER_EDGES_AGES_ = "layer-edges-ages";
    public static final String DATA_LAYER_AGE_SINCE_GROWTH_ = "layer-age-since-growth";

    public CompetitiveLearningConvolutionalNetworkEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        attributes.add( DATA_OUTPUT );
//        flags.putFlag( DATA_OUTPUT, DataFlags.FLAG_SPARSE_BINARY );
        attributes.add( DATA_INVERSE );
        attributes.add( DATA_INVERSE_SELECTED );

        CompetitiveLearningConvolutionalNetworkConfig networkConfig = createNetworkConfig();

        int layers = networkConfig.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {

            attributes.add( DATA_LAYER_CONV_ERROR_ + layer );
            attributes.add( DATA_LAYER_CONV_BEST_ + layer );
            attributes.add( DATA_LAYER_POOL_ERROR_ + layer );
            attributes.add( DATA_LAYER_POOL_BEST_ + layer );

            attributes.add( DATA_LAYER_WEIGHTS_ + layer );
            attributes.add( DATA_LAYER_MASK_ + layer );
            attributes.add( DATA_LAYER_ERROR_ + layer );
            attributes.add( DATA_LAYER_ACTIVE_ + layer );

            attributes.add( DATA_LAYER_CELL_UTILITY_ + layer );
            attributes.add( DATA_LAYER_CELL_STRESS_ + layer );
            attributes.add( DATA_LAYER_CELL_AGES_ + layer );
            attributes.add( DATA_LAYER_EDGES_ + layer );
            attributes.add( DATA_LAYER_EDGES_AGES_ + layer );
            attributes.add( DATA_LAYER_AGE_SINCE_GROWTH_ + layer );
        }
    }

    @Override
    public Class getConfigClass() {
        return CompetitiveLearningConvolutionalNetworkEntityConfig.class;
    }

    protected CompetitiveLearningConvolutionalNetworkConfig createNetworkConfig() {
        CompetitiveLearningConvolutionalNetworkEntityConfig config = ( CompetitiveLearningConvolutionalNetworkEntityConfig ) _config;
        CompetitiveLearningConvolutionalNetworkFactory cnf = new CompetitiveLearningConvolutionalNetworkFactory();
        CompetitiveLearningConvolutionalNetworkConfig cnc = (CompetitiveLearningConvolutionalNetworkConfig)cnf.createConfig();
        cnc.setup(
                _om, _name, _r,
                config.learningRate,
                config.learningRateNeighbours,
                config.edgeMaxAge,
                config.stressLearningRate,
                config.stressSplitLearningRate,
                config.stressThreshold,
                config.utilityLearningRate,
                config.utilityThreshold,
                config.growthInterval,
                config.nbrLayers,
                config.layerInputPadding,
                config.layerInputStride,
                config.layerWidth,
                config.layerHeight,
                config.layerDepth,
                config.layerfieldWidth,
                config.layerfieldHeight,
                config.layerfieldDepth,
                config.layerPoolingWidth,
                config.layerPoolingHeight );

        return cnc;
    }

    public void doUpdateSelf() {

        Data input = getData( DATA_INPUT );
        if( input == null ) {
            return; // can't update yet.
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Algorithm specific parameters
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        CompetitiveLearningConvolutionalNetworkEntityConfig config = ( CompetitiveLearningConvolutionalNetworkEntityConfig ) _config;
        CompetitiveLearningConvolutionalNetworkFactory cnf = new CompetitiveLearningConvolutionalNetworkFactory();
        CompetitiveLearningConvolutionalNetworkConfig cnc = createNetworkConfig();
        ConvolutionalNetwork cn = cnf.create();
        cn.setup( cnf, cnc );
        cn.setInput( input );
        cn.resize();

        copyDataFromPersistence( cn );

        // Update the region-layer, including optional reset and learning on/off switch
        if( config.reset ) {
            cn.reset();
        }

        cn._config.setLearn( config.learn );
        cn.update();

//        Data output = scn.getOutput(); // the potential max-pooling
        CompetitiveLearningConvolutionalNetworkLayer cnl = (CompetitiveLearningConvolutionalNetworkLayer)cn._layers.get( cn._layers.size() -1 );
//        Data output = cnl._poolBest;
        Data output = cnl._poolError;
        Data inverted = cn.invert( output );

        setData( DATA_OUTPUT, output );
        setData( DATA_INVERSE, inverted );

        // Invert a selection of final-layer cells:
        Data outputSelection = new Data( output._dataSize );

        if( config.invertSelection.length() > 0 ) {
            try {
                String[] splitSelection = config.invertSelection.split( "," );
                for( String selection : splitSelection ) {
                    int n = Integer.valueOf( selection );
                    outputSelection._values[ n ] = 1f;
                }
            }
            catch( Exception e ) {
                outputSelection._values[ 0 ] = 1f;
            }

        }

        Data invertedSelection = cn.invert( outputSelection );
        setData( DATA_INVERSE_SELECTED, invertedSelection );

        // Save computed config properties
        copyDataToPersistence( cn );
    }

    protected void copyDataFromPersistence( ConvolutionalNetwork cn ) {

        int layers = cn._config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            CompetitiveLearningConvolutionalNetworkLayer nl = (CompetitiveLearningConvolutionalNetworkLayer)cn._layers.get( layer );

            nl._convError = getDataLazyResize( DATA_LAYER_CONV_ERROR_ + layer, nl._convError._dataSize );
            nl._convBest = getDataLazyResize( DATA_LAYER_CONV_BEST_ + layer, nl._convBest._dataSize );
            nl._poolError = getDataLazyResize( DATA_LAYER_POOL_ERROR_ + layer, nl._poolError._dataSize );
            nl._poolBest = getDataLazyResize( DATA_LAYER_POOL_BEST_ + layer, nl._poolBest._dataSize );

            nl._classifier._cellWeights = getDataLazyResize( DATA_LAYER_WEIGHTS_ + layer, nl._classifier._cellWeights._dataSize );
            nl._classifier._cellMask = getDataLazyResize( DATA_LAYER_MASK_ + layer, nl._classifier._cellMask._dataSize );
            nl._classifier._cellErrors = getDataLazyResize( DATA_LAYER_ERROR_ + layer, nl._classifier._cellErrors._dataSize );
            nl._classifier._cellActivity = getDataLazyResize( DATA_LAYER_ACTIVE_ + layer, nl._classifier._cellActivity._dataSize );

            nl._classifier._cellUtility = getDataLazyResize( DATA_LAYER_CELL_UTILITY_ + layer, nl._classifier._cellUtility._dataSize );
            nl._classifier._cellStress = getDataLazyResize( DATA_LAYER_CELL_STRESS_ + layer, nl._classifier._cellStress._dataSize );
            nl._classifier._cellAges = getDataLazyResize( DATA_LAYER_CELL_AGES_ + layer, nl._classifier._cellAges._dataSize );
            nl._classifier._edges = getDataLazyResize( DATA_LAYER_EDGES_ + layer, nl._classifier._edges._dataSize );
            nl._classifier._edgesAges = getDataLazyResize( DATA_LAYER_EDGES_AGES_ + layer, nl._classifier._edgesAges._dataSize );
            nl._classifier._ageSinceGrowth = getDataLazyResize( DATA_LAYER_AGE_SINCE_GROWTH_ + layer, nl._classifier._ageSinceGrowth._dataSize );
        }
    }

    protected void copyDataToPersistence( ConvolutionalNetwork cn ) {

        int layers = cn._config.getNbrLayers();

        for( int layer = 0; layer < layers; ++layer ) {
            CompetitiveLearningConvolutionalNetworkLayer nl = (CompetitiveLearningConvolutionalNetworkLayer)cn._layers.get( layer );

            setData( DATA_LAYER_CONV_ERROR_ + layer, nl._convError );
            setData( DATA_LAYER_CONV_BEST_ + layer, nl._convBest );
            setData( DATA_LAYER_POOL_ERROR_ + layer, nl._poolError );
            setData( DATA_LAYER_POOL_BEST_ + layer, nl._poolBest );

            setData( DATA_LAYER_WEIGHTS_ + layer, nl._classifier._cellWeights );
            setData( DATA_LAYER_MASK_ + layer, nl._classifier._cellMask );
            setData( DATA_LAYER_ERROR_ + layer, nl._classifier._cellErrors );
            setData( DATA_LAYER_ACTIVE_ + layer, nl._classifier._cellActivity );

            setData( DATA_LAYER_CELL_UTILITY_ + layer, nl._classifier._cellUtility );
            setData( DATA_LAYER_CELL_STRESS_ + layer, nl._classifier._cellStress );
            setData( DATA_LAYER_CELL_AGES_ + layer, nl._classifier._cellAges );
            setData( DATA_LAYER_EDGES_ + layer, nl._classifier._edges );
            setData( DATA_LAYER_EDGES_AGES_ + layer, nl._classifier._edgesAges );
            setData( DATA_LAYER_AGE_SINCE_GROWTH_ + layer, nl._classifier._ageSinceGrowth );
        }
    }
}