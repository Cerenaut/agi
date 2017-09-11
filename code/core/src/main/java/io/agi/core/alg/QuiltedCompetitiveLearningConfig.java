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

package io.agi.core.alg;

import io.agi.core.ann.NetworkConfig;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.ann.unsupervised.BinaryTreeQuiltConfig;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * Created by dave on 22/10/16.
 */
public class QuiltedCompetitiveLearningConfig extends NetworkConfig {

//    public static final String INPUT_WIDTH = "input-width";
//    public static final String INPUT_HEIGHT = "input-height";

    public static final String QUILT = "organizer";
    public static final String CLASSIFIER = "classifier";

//    public static final String CLASSIFIERS_PER_BIT = "classifiers-per-bit";

    public BinaryTreeQuiltConfig _quiltConfig;
    public GrowingNeuralGasConfig _classifierConfig;

    public QuiltedCompetitiveLearningConfig() {
    }

    public void setup(
            ObjectMap om,
            String name,
            Random r,
            BinaryTreeQuiltConfig organizerConfig,
            GrowingNeuralGasConfig classifierConfig ) {//
//            int inputWidth,
//            int inputHeight,
//            int classifiersPerBit ) {
        super.setup( om, name, r );

        _quiltConfig = organizerConfig;
        _classifierConfig = classifierConfig;

//        setClassifiersPerBit( classifiersPerBit );
//        setInputSize( inputWidth, inputHeight );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        QuiltedCompetitiveLearningConfig c = (QuiltedCompetitiveLearningConfig) nc;

        _quiltConfig.copyFrom( c._quiltConfig, c._name );
        _classifierConfig.copyFrom( c._classifierConfig, c._name );

//        setClassifiersPerBit( c.getClassifiersPerBit() );
//        setInputSize( c.getInputSize().x, c.getInputSize().y );
    }

    public Random getRandom() {
        return _r;
    }

//    public Point getInputSize() {
//        int inputWidth = _om.getInteger( getKey( INPUT_WIDTH ) );
//        int inputHeight = _om.getInteger( getKey( INPUT_HEIGHT ) );
//        return new Point( inputWidth, inputHeight );
//    }
//
//    public void setInputSize( int ffInputWidth, int ffInputHeight ) {
//        _om.put( getKey( INPUT_WIDTH ), ffInputWidth );
//        _om.put( getKey( INPUT_HEIGHT ), ffInputHeight );
//    }
//
//    public int getInputArea() {
//        Point p = getInputSize();
//        int inputArea = p.x * p.y;
//        return inputArea;
//    }

//    public Point getQuiltSize() {
//        int width = _quiltConfig.getQuiltWidth();
//        int height = _quiltConfig.getQuiltHeight();
//        return new Point( width, height );
//    }
//
//    public int getQuiltArea() {
//        int width = _quiltConfig.getQuiltWidth();
//        int height = _quiltConfig.getQuiltHeight();
//        return ( width * height );
//    }

//    public int getClassifiersPerBit() {
//        int n = _om.getInteger( getKey( CLASSIFIERS_PER_BIT ) );
//        return n;
//    }
//
//    public void setClassifiersPerBit( int n ) {
//        _om.put( getKey( CLASSIFIERS_PER_BIT ), n );
//    }
//
//    public int getQuiltOffsetCells( int xQuilt, int yQuilt ) {
//        Point p = getQuiltSizeCells();
//        int stride = p.x;
//        return Data2d.getOffset( stride, xQuilt, yQuilt );
//    }
//
//    public Point getQuiltGivenOffset( int regionOffset ) {
//        Point regionSize = getQuiltSizeCells();
//        int yQuilt = regionOffset / regionSize.x;
//        int xQuilt = regionOffset % regionSize.x;
//        return new Point( xQuilt, yQuilt );
//    }
//
//    /**
//     * Calculates the coordinates of the classifier that owns the given region cell.
//     *
//     * @param xQuilt
//     * @param yQuilt
//     * @return
//     */
//    public Point getQuiltCoordinateGivenQuiltCoordinate( int xQuilt, int yQuilt ) {
//        Point columnSize = getClassifierSize();
//        int xClassifier = xQuilt / columnSize.x;
//        int yClassifier = yQuilt / columnSize.y;
//        return new Point( xClassifier, yClassifier ); // the
//    }
//
//    /**
//     * Calculates the coordinates of the cell within a classifier that represents the given region cell.
//     *
//     * @param xQuilt
//     * @param yQuilt
//     * @return
//     */
//    public Point getColumnCoordinateGivenQuiltCoordinate( int xQuilt, int yQuilt ) {
//        Point columnSize = getClassifierSize();
//
//        int xClassifier = ( xQuilt / columnSize.x ) * columnSize.x;
//        int yClassifier = ( yQuilt / columnSize.y ) * columnSize.y;
//
//        int xCell = xQuilt - xClassifier;
//        int yCell = yQuilt - yClassifier; // note: may include depth
//
//        return new Point( xCell, yCell ); // the
//    }
//
//    public Point getClassifierCellGivenColumnCell( int xColumnCell, int yColumnCell ) {
//        Point classifierSizeCells = getClassifierSize();
//        int yOrigin = ( yColumnCell / classifierSizeCells.y ) * classifierSizeCells.y; // excludes any fractional part
//
//        int xClassifierCell = xColumnCell;
//        int yClassifierCell = yColumnCell - yOrigin;
//
//        return new Point( xClassifierCell, yClassifierCell ); // the
//    }
//
//    public Point getColumnCellGivenClassifierCell( int xClassifierCell, int yClassifierCell, int zDepth ) {
//        Point classifierSize = _classifierConfig.getSizeCells();
//        int yOrigin = zDepth * classifierSizeCells.y;
//
//        int xColumnCell = xClassifierCell;
//        int yColumnCell = yClassifierCell + yOrigin;
//
//        return new Point( xColumnCell, yColumnCell ); // the
//    }

    public Point getCellsOriginOfQuilt( int xq, int yq ) {
        Point classifierSize = _classifierConfig.getSizeCells();
        int xQuilt = xq * classifierSize.x;
        int yQuilt = yq * classifierSize.y;

        Point cellsOrigin = new Point( xQuilt, yQuilt );
        return cellsOrigin;
    }

    public int getCellsOffset( int cx, int cy ) {
        Point p = getCellsSize();
        int stride = p.x;
        return Data2d.getOffset( stride, cx, cy );
    }

    public int getCellsArea() {
        Point cellsSize = getCellsSize();
        int cellsArea = cellsSize.x * cellsSize.y;
        return cellsArea;
    }

    public Point getCellsSize() {
        Point classifierSize = _classifierConfig.getSizeCells();
        Point quiltSize = _quiltConfig.getQuiltSize();

        int width = classifierSize.x * quiltSize.x;
        int height = classifierSize.y * quiltSize.y;

        Point regionSize = new Point( width, height );
        return regionSize;
    }

}