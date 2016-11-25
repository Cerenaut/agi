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

import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Output is same as input except multiplied by a real valued config property.
 * Created by dave on 5/11/16.
 */
public class BinaryErrorEntity extends Entity {

    public static final String ENTITY_TYPE = "binary-error-entity";

    public static final String INPUT_TEST = "input-test";
    public static final String INPUT_TRUTH = "input-truth";
    public static final String OUTPUT_FP = "output-fp";
    public static final String OUTPUT_FN = "output-fn";


    public BinaryErrorEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_TEST );
        attributes.add( INPUT_TRUTH );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( OUTPUT_FP );
        attributes.add( OUTPUT_FN );
    }

    @Override
    public Class getConfigClass() {
        return BinaryErrorEntityConfig.class;
    }


    @Override
    protected void doUpdateSelf() {
        BinaryErrorEntityConfig config = ( BinaryErrorEntityConfig ) _config;

        Data inputTest = getData( INPUT_TEST ); // error in classification (0,1)
        if( inputTest == null ) {
            return;
        }

        Data inputTruth = getData( INPUT_TRUTH ); // error in classification (0,1)
        if( inputTruth == null ) {
            return;
        }

        int values = inputTest.getSize();

        if( inputTruth.getSize() != values ) {
            return;
        }

        Data outputFp = new Data( inputTest._dataSize );
        Data outputFn = new Data( inputTest._dataSize );

        config.sumFalseNegative = 0;
        config.sumFalsePositive = 0;

        for( int i = 0; i < inputTest.getSize(); ++i ) {

            float truth = inputTruth._values[ i ];
            float test = inputTest._values[ i ];

            if( truth == test ) {
                continue;
            }

            float fpError = 0f;
            float fnError = 0f;

            if( truth > test ) {
                fnError = 1f; // not predicted, happened
                config.sumFalseNegative += 1;
            }
            else { // test > truth
                fpError = 1f; // predicted, didn't happen
                config.sumFalsePositive += 1;
            }

            outputFp._values[ i ] = fpError;
            outputFn._values[ i ] = fnError;
        }

        setData( OUTPUT_FP, outputFp );
        setData( OUTPUT_FN, outputFn );
    }
}