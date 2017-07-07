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

package io.agi.framework.entities.stdp;

import io.agi.core.data.*;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.awt.*;
import java.util.Collection;

/**
 * Created by dave on 6/05/17.
 */
public class DifferenceOfGaussiansEntity extends Entity {

    public static final String ENTITY_TYPE = "difference-of-gaussians";

    // data
    public static final String DATA_INPUT = "input";
    public static final String DATA_KERNEL = "kernel";
    public static final String DATA_OUTPUT = "output";

    public DifferenceOfGaussiansEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( DATA_INPUT );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( DATA_KERNEL );
        attributes.add( DATA_OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return DifferenceOfGaussiansEntityConfig.class;
    }

    public void doUpdateSelf() {

        // https://grey.colorado.edu/mediawiki/sites/CompCogNeuro/images/thumb/5/56/fig_on_off_rfields_edge.png/400px-fig_on_off_rfields_edge.png
        DifferenceOfGaussiansEntityConfig config = ( DifferenceOfGaussiansEntityConfig ) _config;

        Data input = getData( DATA_INPUT );

        Point inputSize = Data2d.getSize( input );

        int w = inputSize.x;
        int h = inputSize.y;

        Data kernel = getData( DATA_KERNEL );
        Data output = new Data( DataSize.create( w, h ) );

        // check for kernel param changes
        String kernelDesc = getKernelDesc( config );
        if( !kernelDesc.equals( config.kernelDescCached ) ) {
            config.kernelDescCached = kernelDesc;
            kernel = getKernel( config );
            setData( DATA_KERNEL, kernel );
        }

        Convolution2d.convolve( kernel, input, output );

        output.mul( config.scaling );
        output.clipRange( config.min, config.max );

        setData( DATA_OUTPUT, output );
    }

    /**
     * Kernel calculation confirmed from DoG.m in paper code.
     * @param config
     * @return
     */
    protected Data getKernel( DifferenceOfGaussiansEntityConfig config ) {
        Data kernel = Kernels2d.DifferenceOfGaussians( config.kernelWidth, config.kernelHeight, config.stdDev1, config.stdDev2 );
        float mean = (float)kernel.mean();
        kernel.sub( mean ); // now mean is zero
        float max = kernel.max();
        float min = Math.abs( kernel.min() );
        float absMax = Math.max( max, min );
        kernel.div( absMax ); // max is now 1
        return kernel;
    }

    protected String getKernelDesc( DifferenceOfGaussiansEntityConfig config ) {
        String s = config.kernelWidth + "," + config.kernelHeight + "," + config.stdDev1 + "," + config.stdDev2;// + "," + config.scaling;
        return s;
    }
}
