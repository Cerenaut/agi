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

package io.agi.core.ann.unsupervised;

import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;

import java.awt.*;

/**
 * Quilt with a 2:1 branching factor.
 *
 * Therefore, there are 2 inputs.
 *
 * We assume the inputs are 2D (or at least must project down to 2D).
 *
 * Inputs are combined by concatenating them as 1-d vectors.
 *
 * Created by dave on 22/10/16.
 */
public class BinaryTreeQuilt { //extends CompetitiveLearning {

    public BinaryTreeQuiltConfig _config;

//    public Data _cellWeights;
    public Data _quiltMask;
    public Data _quiltInputMask; // calculate once?

    public BinaryTreeQuilt() {// String name, ObjectMap om ) {
//        super( name, om );
    }

    public void setup( BinaryTreeQuiltConfig config ) {

        _config = config;

        int inputs = _config.getInputArea();
        int w = _config.getQuiltWidth();
        int h = _config.getQuiltHeight();

        _quiltInputMask = new Data( w, h, inputs );
        _quiltMask = new Data( w, h );
    }

    /**
     * Updates the per-cell input mask from config settings.
     */
    protected void updateCellInputMask() {

        int inputArea = _config.getInputArea();
        Point p = _config.getQuiltSize();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                Data inputMask = updateInputMask( x, y );

                int quiltOffset = y * p.x + x;
                int inputOffset = quiltOffset * inputArea;

                _quiltInputMask.copyRange( inputMask, inputOffset, 0, inputArea );
            }
        }
    }

    public Data getInputMask( int qx, int qy ) {

        int inputArea = _config.getInputArea();
        Point p = _config.getQuiltSize();

        Data inputMask = new Data( inputArea );

        int quiltOffset = qy * p.x + qx;
        int inputOffset = quiltOffset * inputArea;

        inputMask.copyRange( _quiltInputMask, 0, inputOffset, inputArea );

        return inputMask;
    }

    public Data updateInputMask( int qx, int qy ) {

        int inputArea = _config.getInputArea();

        Data inputMask = new Data( inputArea );

        Rectangle r1 = _config.getInputRect1( qx, qy );
        Rectangle r2 = _config.getInputRect2( qx, qy );

        Point p1 = _config.getInput1Size();
        Point p2 = _config.getInput2Size();

        int inputOffset = 0;

        addInputMask( inputMask, p1, r1, inputOffset );

        inputOffset = _config.getInput1Area();

        addInputMask( inputMask, p2, r2, inputOffset );

        return inputMask;
    }

    protected void addInputMask( Data inputMask, Point inputSize, Rectangle r, int inputOffset ) {
        for( int y = 0; y < r.height; ++y ) {
            for( int x = 0; x < r.width; ++x ) {

                int ix = r.x + x;
                int iy = r.y + y;

                if( ( ix < 0 ) || ( iy < 0 ) || ( ix >= inputSize.x ) || ( iy >= inputSize.y ) ) {
                    continue; // out of image bounds
                }

                int offset = inputOffset +  iy * inputSize.x + ix;
                inputMask._values[ offset ] = 1;
            }
        }
    }

    public void reset() {
        _quiltMask.set( 1.f );

        // define the cell input mask
        updateCellInputMask();
    }

    public void update() {
        // Nothing - currently fixed uniform distribution at specified intervals
    }

}
