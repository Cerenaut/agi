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
 * Created by dave on 10/11/17.
 */
public class SparseSequenceAutoencoderConfig extends CompetitiveLearningConfig {

//    public static final String INPUTS_F = "inputs-f";
    public static final String INPUTS_B = "inputs-b";

    public static final String CELL_MAPPING_DENSITY = "cell-mapping-density";

    public static final String WIDTH_CELLS_F = "width-cells-f";
    public static final String HEIGHT_CELLS_F = "height-cells-f";
    public static final String WIDTH_CELLS_B = "width-cells-b";
    public static final String HEIGHT_CELLS_B = "height-cells-b";

    public static final String SPARSITY_TRAINING_F = "sparsity-training-f";
    public static final String SPARSITY_OUTPUT_F = "sparsity-output-f";
    public static final String SPARSITY_BATCH_F = "sparsity-batch-f";

    public static final String SPARSITY_TRAINING_B = "sparsity-training-b";
    public static final String SPARSITY_OUTPUT_B = "sparsity-output-b";
    public static final String SPARSITY_BATCH_B = "sparsity-batch-b";

    public static final String LEARNING_RATE = "learning-rate";
    public static final String MOMENTUM = "momentum";
    public static final String WEIGHTS_STD_DEV = "weights-std-dev";
    public static final String BATCH_COUNT_F = "batch-age-f";
    public static final String BATCH_COUNT_B = "batch-age-b";
    public static final String BATCH_SIZE_F = "batch-size-f";
    public static final String BATCH_SIZE_B = "batch-size-b";

    public SparseSequenceAutoencoderConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputsF,
            int inputsB,
            int wF,
            int hF,
            int wB,
            int hB,
            int cellMappingDensity,
            int sparsityTrainingF,
            int sparsityOutputF,
            int sparsityBatchF,
            int sparsityTrainingB,
            int sparsityOutputB,
            int sparsityBatchB,
            float learningRate,
            float momentum,
            float weightsStdDev,
            int batchCountF,
            int batchCountB,
            int batchSizeF,
            int batchSizeB ) {

        super.setup( om, name, r, inputsF, 0, 0 );

        setWidthCellsF( wF );
        setHeightCellsF( hF );

        setWidthCellsB( wB );
        setHeightCellsB( hB );

        setInputsB( inputsB );

        setCellMappingDensity( cellMappingDensity );

        setSparsityTrainingF( sparsityTrainingF );
        setSparsityOutputF( sparsityOutputF );
        setSparsityBatchF( sparsityBatchF );

        setSparsityTrainingB( sparsityTrainingB );
        setSparsityOutputB( sparsityOutputB );
        setSparsityBatchB( sparsityBatchB );

        setLearningRate( learningRate );
        setMomentum( momentum );
        setWeightsStdDev( weightsStdDev );
        setBatchCountF( batchCountF );
        setBatchCountB( batchCountB );
        setBatchSizeF( batchSizeF );
        setBatchSizeB( batchSizeB );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        SparseSequenceAutoencoderConfig c = ( SparseSequenceAutoencoderConfig ) nc;

        setWidthCellsF( c.getWidthCellsF() );
        setHeightCellsF( c.getHeightCellsF() );

        setWidthCellsB( c.getWidthCellsB() );
        setHeightCellsB( c.getHeightCellsB() );

        setInputsB( c.getInputsB() );

        setCellMappingDensity( c.getCellMappingDensity() );

        setSparsityTrainingF( c.getSparsityTrainingF() );
        setSparsityOutputF( c.getSparsityOutputF() );
        setSparsityBatchF( c.getSparsityBatchF() );

        setSparsityTrainingB( c.getSparsityTrainingB() );
        setSparsityOutputB( c.getSparsityOutputB() );
        setSparsityBatchB( c.getSparsityBatchB() );

        setLearningRate( c.getLearningRate() );
        setMomentum( c.getMomentum() );
        setWeightsStdDev( c.getWeightsStdDev() );
        setBatchCountF( c.getBatchCountF() );
        setBatchCountB( c.getBatchCountB() );
        setBatchSizeF( c.getBatchSizeF() );
        setBatchSizeB( c.getBatchSizeB() );
    }

    public void setLearningRate( float r ) {
        _om.put( getKey( LEARNING_RATE ), r );
    }


    public void setCellMappingDensity( int n ) {
        _om.put( getKey( CELL_MAPPING_DENSITY ), n );
    }

    public void setWidthCellsF( int n ) {
        _om.put( getKey( WIDTH_CELLS_F ), n );
    }
    public void setWidthCellsB( int n ) {
        _om.put( getKey( WIDTH_CELLS_B ), n );
    }
    public void setHeightCellsF( int n ) {
        _om.put( getKey( HEIGHT_CELLS_F ), n );
    }
    public void setHeightCellsB( int n ) {
        _om.put( getKey( HEIGHT_CELLS_B ), n );
    }

    public void setSparsityOutputF( int n ) {
        _om.put( getKey( SPARSITY_OUTPUT_F ), n );
    }

    public void setSparsityTrainingF( int n ) {
        _om.put( getKey( SPARSITY_TRAINING_F ), n );
    }

    public void setSparsityBatchF( int n ) {
        _om.put( getKey( SPARSITY_BATCH_F ), n );
    }

    public void setSparsityOutputB( int n ) {
        _om.put( getKey( SPARSITY_OUTPUT_B ), n );
    }

    public void setSparsityTrainingB( int n ) {
        _om.put( getKey( SPARSITY_TRAINING_B ), n );
    }

    public void setSparsityBatchB( int n ) {
        _om.put( getKey( SPARSITY_BATCH_B ), n );
    }

    public void setInputsB( int n ) {
        _om.put( getKey( INPUTS_B ), n );
    }

    public void setMomentum( float r ) {
        _om.put( getKey( MOMENTUM ), r );
    }

    public void setWeightsStdDev( float r ) {
        _om.put( getKey( WEIGHTS_STD_DEV ), r );
    }

    public void setBatchCountF( int n ) {
        _om.put( getKey( BATCH_COUNT_F ), n );
    }

    public void setBatchCountB( int n ) {
        _om.put( getKey( BATCH_COUNT_B ), n );
    }

    public void setBatchSizeF( int n ) {
        _om.put( getKey( BATCH_SIZE_F ), n );
    }

    public void setBatchSizeB( int n ) {
        _om.put( getKey( BATCH_SIZE_B ), n );
    }

    public float getWeightsStdDev() {
        Float r = _om.getFloat( getKey( WEIGHTS_STD_DEV ) );
        return r.floatValue();
    }

    public float getMomentum() {
        Float r = _om.getFloat( getKey( MOMENTUM ) );
        return r.floatValue();
    }

    public float getLearningRate() {
        Float r = _om.getFloat( getKey( LEARNING_RATE ) );
        return r.floatValue();
    }

    public int getCellMappingDensity() {
        Integer n = _om.getInteger( getKey( CELL_MAPPING_DENSITY ) );
        return n.intValue();
    }

    public int getWidthCellsF() {
        Integer n = _om.getInteger( getKey( WIDTH_CELLS_F ) );
        return n.intValue();
    }
    public int getWidthCellsB() {
        Integer n = _om.getInteger( getKey( WIDTH_CELLS_B ) );
        return n.intValue();
    }
    public int getHeightCellsF() {
        Integer n = _om.getInteger( getKey( HEIGHT_CELLS_F ) );
        return n.intValue();
    }
    public int getHeightCellsB() {
        Integer n = _om.getInteger( getKey( HEIGHT_CELLS_B ) );
        return n.intValue();
    }

    public int getInputsB() {
        Integer n = _om.getInteger( getKey( INPUTS_B ) );
        return n.intValue();
    }

    public int getSparsityTrainingF() {
        Integer n = _om.getInteger( getKey( SPARSITY_TRAINING_F ) );
        return n.intValue();
    }

    public int getSparsityBatchF() {
        Integer n = _om.getInteger( getKey( SPARSITY_BATCH_F ) );
        return n.intValue();
    }

    public int getSparsityOutputF() {
        Integer n = _om.getInteger( getKey( SPARSITY_OUTPUT_F ) );
        return n.intValue();
    }

    public int getSparsityTrainingB() {
        Integer n = _om.getInteger( getKey( SPARSITY_TRAINING_B ) );
        return n.intValue();
    }

    public int getSparsityBatchB() {
        Integer n = _om.getInteger( getKey( SPARSITY_BATCH_B ) );
        return n.intValue();
    }

    public int getSparsityOutputB() {
        Integer n = _om.getInteger( getKey( SPARSITY_OUTPUT_B ) );
        return n.intValue();
    }

    public int getBatchCountF() {
        Integer n = _om.getInteger( getKey( BATCH_COUNT_F ) );
        return n.intValue();
    }

    public int getBatchCountB() {
        Integer n = _om.getInteger( getKey( BATCH_COUNT_B ) );
        return n.intValue();
    }

    public int getBatchSizeF() {
        Integer n = _om.getInteger( getKey( BATCH_SIZE_F ) );
        return n.intValue();
    }

    public int getBatchSizeB() {
        Integer n = _om.getInteger( getKey( BATCH_SIZE_B ) );
        return n.intValue();
    }

}