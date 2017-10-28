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

package io.agi.framework.entities;

import io.agi.core.ann.supervised.*;
import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Currently a 2 layer network, as that's all I need; but TODO generalize to N-layers.
 *
 * Created by dave on 12/03/16.
 */
public class FeedForwardNetworkEntity extends SupervisedLearningEntity {

    public static final String ENTITY_TYPE = "feedforward-network-entity";

    public static final String WEIGHTS_1 = "weights-1";
    public static final String WEIGHTS_2 = "weights-2";
    public static final String BIASES_1 = "biases-1";
    public static final String BIASES_2 = "biases-2";
    public static final String BATCH_ERROR_GRADIENTS_1 = "batch-error-gradients-1";
    public static final String BATCH_ERROR_GRADIENTS_2 = "batch-error-gradients-2";
    public static final String BATCH_INPUTS_1 = "batch-inputs-1";
    public static final String BATCH_INPUTS_2 = "batch-inputs-2";

    // add network stuff
    public FeedForwardNetwork _ffn;

    public FeedForwardNetworkEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public Class getConfigClass() {
        return FeedForwardNetworkEntityConfig.class;
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

        super.getOutputAttributes( attributes, flags );

        // add weights for network
        attributes.add( WEIGHTS_1 );
        attributes.add( WEIGHTS_2 );
        attributes.add( BIASES_1 );
        attributes.add( BIASES_2 );
        attributes.add( BATCH_ERROR_GRADIENTS_1 );
        attributes.add( BATCH_ERROR_GRADIENTS_2 );
        attributes.add( BATCH_INPUTS_1 );
        attributes.add( BATCH_INPUTS_2 );
    }

    protected void reset( int features, int labelClasses ) {

        super.reset( features, labelClasses );

        // reset network
        createModel( features, labelClasses );

        _ffn.reset();
    }

    /**
     * Utility to create the network
     *
     * @param features
     * @param labelClasses
     */
    protected void createModel( int features, int labelClasses ) {

        if( _ffn != null ) {
            return;
        }

        FeedForwardNetworkEntityConfig config = (FeedForwardNetworkEntityConfig)_config;

        String name = getKey( "network" );
        _ffn = new FeedForwardNetwork( name, _om );

        FeedForwardNetworkConfig c = new FeedForwardNetworkConfig();

        String lossFunction = CostFunction.QUADRATIC; // must be

        int layers = 2; // fixed, for now
        int inputs = features;
        int hidden = config.hiddenLayerSize;
        int outputs = labelClasses;
        String layerSizes = hidden + "," + outputs;
        String layerActivationFns = ActivationFunctionFactory.LEAKY_RELU + "," + ActivationFunctionFactory.LEAKY_RELU; // better mutability online

        c.setup( _om, name, _r, lossFunction, inputs, layers, layerSizes, layerActivationFns, config.regularization, config.learningRate, config.batchSize );

        ActivationFunctionFactory aff = new ActivationFunctionFactory();
        aff.leak = config.leakiness; // this is how we fix the param
        _ffn.setup( c, aff );
    }

    /**
     * Initialise model data structure, and load it with parameters from persistence.
     * It should be ready for training if not already trained.
     *
     * @param features
     * @param labels
     * @param labelClasses
     */
    protected void loadModel( int features, int labels, int labelClasses ) {

        createModel( features, labels );

        NetworkLayer layer1 = _ffn._layers.get( 0 );
        NetworkLayer layer2 = _ffn._layers.get( 1 );

        layer1._weights = getDataLazyResize( WEIGHTS_1, layer1._weights._dataSize );
        layer2._weights = getDataLazyResize( WEIGHTS_2, layer2._weights._dataSize );
        layer1._biases = getDataLazyResize( BIASES_1, layer1._biases._dataSize );
        layer2._biases = getDataLazyResize( BIASES_2, layer2._biases._dataSize );
        layer1._batchErrorGradients = getDataLazyResize( BATCH_ERROR_GRADIENTS_1, layer1._batchErrorGradients._dataSize );
        layer2._batchErrorGradients = getDataLazyResize( BATCH_ERROR_GRADIENTS_2, layer2._batchErrorGradients._dataSize );
        layer1._batchInputs = getDataLazyResize( BATCH_INPUTS_1, layer1._batchInputs._dataSize );
        layer2._batchInputs = getDataLazyResize( BATCH_INPUTS_2, layer2._batchInputs._dataSize );
    }

    /**
     * Save the model to persistence.
     */
    protected void saveModel() {

        NetworkLayer layer1 = _ffn._layers.get( 0 );
        NetworkLayer layer2 = _ffn._layers.get( 1 );

        setData( WEIGHTS_1, layer1._weights );
        setData( WEIGHTS_2, layer2._weights );
        setData( BIASES_1, layer1._biases );
        setData( BIASES_2, layer2._biases );
        setData( BATCH_ERROR_GRADIENTS_1, layer1._batchErrorGradients );
        setData( BATCH_ERROR_GRADIENTS_2, layer2._batchErrorGradients );
        setData( BATCH_INPUTS_1, layer1._batchInputs );
        setData( BATCH_INPUTS_2, layer2._batchInputs );
    }

    /**
     * Train the algorithm given the entire history of training samples provided.
     *
     * @param featuresTimeMatrix History of features
     * @param labelsTimeMatrix   History of labels
     * @param features           Nbr of features in each sample
     */
    protected void trainBatch( Data featuresTimeMatrix, Data labelsTimeMatrix, int features ) {
        // Implement as needed in subclasses
        throw new java.lang.UnsupportedOperationException( "train batch is not supported by this algorithm" );
    }

    /**
     * Incrementally train the algorithm using a single sample update.
     *
     * @param features
     * @param labels
     */
    protected void trainSample( Data features, Data labels ) {
        trainOnline( features, labels );
    }

    /**
     * Train the algorithm using an online update.
     *
     * @param features
     * @param labels
     */
    protected void trainOnline( Data features, Data labels ) {

        // copy input and output
        Data input = _ffn.getInput();
        input.copy( features );

        Data ideal = _ffn.getIdeal();
        ideal.copy( labels );

        // run the network
        _ffn.feedForward();
        _ffn.feedBackward();
    }

    /**
     * Generate a prediction from the model and copy it into predictedLabels.
     *
     * @param features
     * @param predictedLabels
     */
    protected void predict( Data features, Data predictedLabels ) {

        Data input = _ffn.getInput();
        input.copy( features );
        _ffn.feedForward();
        Data output = _ffn.getOutput();

        // now convert
        predictedLabels.copy( output );
        predictedLabels.clipRange( 0f, 1f ); // as NN may go wildly beyond that
    }

}
