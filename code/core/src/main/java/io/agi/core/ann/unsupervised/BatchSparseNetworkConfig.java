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

package io.agi.core.ann.unsupervised;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.orm.ObjectMap;

import java.util.Random;

/**
 * Created by dave on 16/10/17.
 */
public class BatchSparseNetworkConfig extends LifetimeSparseAutoencoderConfig {

    public static final String OUTPUTS = "outputs";

    public BatchSparseNetworkConfig() {

    }

    /**
     *
     * @param om
     * @param name
     * @param r
     * @param inputs
     * @param outputs
     * @param w
     * @param h
     * @param learningRate
     * @param sparsity
     * @param weightsStdDev
     * @param batchCount
     * @param batchSize
     */
    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputs,
            int outputs,
            int w,
            int h,
            float learningRate,
            float momentum,
            int sparsity,
            int sparsityLifetime,
            float weightsStdDev,
            int batchCount,
            int batchSize ){

        super.setup( om, name, r, inputs, w, h, learningRate, momentum, sparsity, sparsityLifetime, weightsStdDev, batchCount, batchSize );

        setOutputs( outputs );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        BatchSparseNetworkConfig c = ( BatchSparseNetworkConfig ) nc;

        setOutputs( c.getOutputs() );
    }

    public void setOutputs( int n ) {
        _om.put( getKey( OUTPUTS ), n );
    }

    public int getOutputs() {
        Integer n = _om.getInteger( getKey( OUTPUTS ) );
        return n.intValue();
    }
}
