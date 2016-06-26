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

import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.ann.unsupervised.NeuralGas;
import io.agi.core.ann.unsupervised.NeuralGasConfig;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.Keys;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 12/03/16.
 */
public class NeuralGasEntity extends Entity {

    public static final String ENTITY_TYPE = "neural-gas";

    public static final String IMPL_NAME = "neural-gas";

    public static final String INPUT = "input";

    public static final String OUTPUT_WEIGHTS = "output-weights";
    public static final String OUTPUT_MASK = "output-mask";
    public static final String OUTPUT_ERROR = "output-error";
    public static final String OUTPUT_ACTIVE = "output-active";
    public static final String OUTPUT_AGES = "output-ages";
    public static final String OUTPUT_STRESS = "output-stress";

    public NeuralGasEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_WEIGHTS );
        attributes.add( OUTPUT_MASK );
        attributes.add( OUTPUT_ERROR );
        attributes.add( OUTPUT_ACTIVE );
        attributes.add( OUTPUT_AGES );
        attributes.add( OUTPUT_STRESS );
    }

    @Override
    public Class getConfigClass() {
        return NeuralGasEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is
        Data input = getData( INPUT );

        if( input == null ) {
            return;
        }

        // Get all the parameters:
        NeuralGasEntityConfig config = ( NeuralGasEntityConfig ) _config;

        int inputs = input.getSize();

        String implName = getName() + Keys.DELIMITER + IMPL_NAME; // the name of the object that implements

        // Create the config object:
        NeuralGasConfig c = new NeuralGasConfig();
        c.setup( _om, implName, getRandom(), inputs, config.widthCells, config.heightCells, config.learningRate, config.noiseMagnitude, config.neighbourhoodRange, config.minDistance, config.maxAge );

        // Create the implementing object itself, and copy data from persistence into it:
        NeuralGas ng = new NeuralGas( implName, _om );
        ng._c = c;
        ng._inputValues = input;

        DataSize dataSizeWeights = DataSize.create( config.widthCells, config.heightCells, inputs );
        DataSize dataSizeCells = DataSize.create( config.widthCells, config.heightCells );

        Data weights = getDataLazyResize( OUTPUT_WEIGHTS, dataSizeWeights );
        Data errors = getDataLazyResize( OUTPUT_ERROR, dataSizeCells ); // deep copies the size so they each own a copy
        Data activity = getDataLazyResize( OUTPUT_ACTIVE, dataSizeCells ); // deep copies the size so they each own a copy
        Data mask = getDataLazyResize( OUTPUT_MASK, dataSizeCells ); // deep copies the size so they each own a copy
        Data ages = getDataLazyResize( OUTPUT_AGES, dataSizeCells ); // deep copies the size so they each own a copy
        Data stress = getDataLazyResize( OUTPUT_STRESS, dataSizeCells ); // deep copies the size so they each own a copy

        ng._inputValues = input;
        ng._cellWeights = weights;
        ng._cellErrors = errors;
        ng._cellActivity = activity;
        ng._cellMask = mask;
        ng._cellAges = ages;
        ng._cellStress = stress;

        if( config.reset ) {
            ng.reset();
        }

        ng.update();

        setData( OUTPUT_WEIGHTS, weights );
        setData( OUTPUT_ERROR, errors );
        setData( OUTPUT_ACTIVE, activity );
        setData( OUTPUT_MASK, mask );
        setData( OUTPUT_AGES, ages );
        setData( OUTPUT_STRESS, stress );
    }
}