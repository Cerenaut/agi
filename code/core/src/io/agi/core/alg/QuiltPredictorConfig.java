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

package io.agi.core.alg;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.ann.unsupervised.OnlineKSparseAutoencoderConfig;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by dave on 4/07/16.
 */
public class QuiltPredictorConfig extends NetworkConfig {

    public static final String INPUT_C_WIDTH = "input-c-width";
    public static final String INPUT_C_HEIGHT = "input-c-height";
    public static final String INPUT_C_COLUMN_WIDTH = "input-c-column-width";
    public static final String INPUT_C_COLUMN_HEIGHT = "input-c-column-height";
    public static final String INPUT_P_SIZE = "input-p-size";

    public static final String SUFFIX_PREDICTOR = "predictor";

    public QuiltPredictorConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            int inputCWidth,
            int inputCHeight,
            int inputCColumnWidth,
            int inputCColumnHeight,
            int inputPSize ) {

        super.setup( om, name, r );

        setInputCSize( inputCWidth, inputCHeight );
        setInputCColumnSize( inputCColumnWidth, inputCColumnHeight );
        setInputPSize( inputPSize );
    }

    public Random getRandom() {
        return _r;
    }

    public int getPredictorInputs() {
        Point inputCSize = getInputCSize();
        int inputPSize = getInputPSize();

        DataSize dataSizeInputC = DataSize.create( inputCSize.x, inputCSize.y );
        DataSize dataSizeInputP = DataSize.create( inputPSize );

        // Predictor
        int cells = dataSizeInputC.getVolume(); //_rc._classifierConfig.getNbrCells();
        int predictorInputs = dataSizeInputP.getVolume() + cells;
        return predictorInputs;
    }

    public int getPredictorOutputs() {
        Point inputCSize = getInputCSize();
        DataSize dataSizeInputC = DataSize.create( inputCSize.x, inputCSize.y );
        int cells = dataSizeInputC.getVolume();
        int predictorOutputs = cells;
        return predictorOutputs;
    }

    public int getInputCArea() {
        Point p = getInputCSize();

//        int area = p1.x * p1.y + p2.x * p2.y;
        int area = p.x * p.y;
        return area;
    }

    public Point getInputCSize() {
        int inputWidth = _om.getInteger( getKey( INPUT_C_WIDTH ) );
        int inputHeight = _om.getInteger( getKey( INPUT_C_HEIGHT ) );
        return new Point( inputWidth, inputHeight );
    }

    public void setInputCSize( int ffInputWidth, int ffInputHeight ) {
        _om.put( getKey( INPUT_C_WIDTH ), ffInputWidth );
        _om.put( getKey( INPUT_C_HEIGHT ), ffInputHeight );
    }

    public void setInputCColumnSize( int inputCColumnWidth, int inputCColumnHeight ) {
        _om.put( getKey( INPUT_C_COLUMN_WIDTH ), inputCColumnWidth );
        _om.put( getKey( INPUT_C_COLUMN_HEIGHT ), inputCColumnHeight );
    }

    public Point getInputCColumnSize() {
        int inputColumnWidth = _om.getInteger( getKey( INPUT_C_COLUMN_WIDTH ) );
        int inputColumnHeight = _om.getInteger( getKey( INPUT_C_COLUMN_HEIGHT ) );
        return new Point( inputColumnWidth, inputColumnHeight );
    }

    public int getInputPSize() {
        int size = _om.getInteger( getKey( INPUT_P_SIZE ) );
        return size;
    }

    public void setInputPSize( int size ) {
        _om.put( getKey( INPUT_P_SIZE ), size );
    }

}
