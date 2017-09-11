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

import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMap;
import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMapConfig;
import io.agi.core.ann.unsupervised.ParameterLessSelfOrganizingMap;
import io.agi.core.ann.unsupervised.ParameterLessSelfOrganizingMapConfig;
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
public class ParameterLessSelfOrganizingMapEntity extends Entity {

    public static final String ENTITY_TYPE = "plsom";

    public static final String IMPL_NAME = "plsom";

    public static final String INPUT = "input";

    public static final String OUTPUT_BOUNDS = "output-bounds";
    public static final String OUTPUT_WEIGHTS = "output-weights";
    public static final String OUTPUT_MASK = "output-mask";
    public static final String OUTPUT_ERROR = "output-error";
    public static final String OUTPUT_ACTIVE = "output-active";

    public ParameterLessSelfOrganizingMapEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_BOUNDS );
        attributes.add( OUTPUT_WEIGHTS );
        attributes.add( OUTPUT_MASK );
        attributes.add( OUTPUT_ERROR );
        attributes.add( OUTPUT_ACTIVE );
    }

    @Override
    public Class getConfigClass() {
        return ParameterLessSelfOrganizingMapEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Do nothing unless the input is
        Data input = getData( INPUT );

        if( input == null ) {
            return;
        }

        // Get all the parameters:
        int inputs = input.getSize();

        String implName = getName() + Keys.DELIMITER + IMPL_NAME; // the name of the object that implements

        // Create the config object:
        ParameterLessSelfOrganizingMapEntityConfig config = ( ParameterLessSelfOrganizingMapEntityConfig ) _config;

        ParameterLessSelfOrganizingMapConfig c = new ParameterLessSelfOrganizingMapConfig();
        c.setup( _om, implName, getRandom(), inputs, config.widthCells, config.heightCells, config.neighbourhoodRange );//, config.inputDiameter );

        // Create the implementing object itself, and copy data from persistence into it:
        ParameterLessSelfOrganizingMap dsom = new ParameterLessSelfOrganizingMap( implName, _om );
        dsom._c = c;
        dsom._inputValues = input;

        int boundsSize = c.getBoundsSize( inputs );

        DataSize dataSizeBounds = DataSize.create( boundsSize );
        DataSize dataSizeWeights = DataSize.create( config.widthCells, config.heightCells, inputs );
        DataSize dataSizeCells = DataSize.create( config.widthCells, config.heightCells );

        Data bounds = getDataLazyResize( OUTPUT_BOUNDS, dataSizeBounds );
        Data weights = getDataLazyResize( OUTPUT_WEIGHTS, dataSizeWeights );
        Data errors = getDataLazyResize( OUTPUT_ERROR, dataSizeCells ); // deep copies the size so they each own a copy
        Data activity = getDataLazyResize( OUTPUT_ACTIVE, dataSizeCells ); // deep copies the size so they each own a copy
        Data mask = getDataLazyResize( OUTPUT_MASK, dataSizeCells ); // deep copies the size so they each own a copy

        dsom._inputValues = input;
        dsom._inputBounds = bounds;
        dsom._cellWeights = weights;
        dsom._cellErrors = errors;
        dsom._cellActivity = activity;
        dsom._cellMask = mask;

        if( config.reset ) {
            dsom.reset();
        }

        dsom.update();

        setData( OUTPUT_BOUNDS, bounds );
        setData( OUTPUT_WEIGHTS, weights );
        setData( OUTPUT_ERROR, errors );
        setData( OUTPUT_ACTIVE, activity );
        setData( OUTPUT_MASK, mask );
    }
}