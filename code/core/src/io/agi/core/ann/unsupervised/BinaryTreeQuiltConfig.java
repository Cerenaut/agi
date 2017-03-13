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

import io.agi.core.ann.NetworkConfig;
import io.agi.core.data.Data2d;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.Random;

/**
 * input size = 20
 * field size = 10
 * field stride = 5
 * quilt size = 3
 *      00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19
 *   F1 -- -- -- -- -- -- -- -- -- --
 *   F2                -- -- -- -- -- -- -- -- -- --
 *   F3                               -- -- -- -- -- -- -- -- -- --
 *
 * Created by dave on 29/12/15.
 */
public class BinaryTreeQuiltConfig extends NetworkConfig {//CompetitiveLearningConfig {

    public static final String QUILT_WIDTH = "quilt-width";
    public static final String QUILT_HEIGHT = "quilt-height";

    public static final String INPUT_1_WIDTH = "input-1-width";
    public static final String INPUT_1_HEIGHT = "input-1-height";

    public static final String INPUT_2_WIDTH = "input-2-width";
    public static final String INPUT_2_HEIGHT = "input-2-height";

    public static final String FIELD_1_STRIDE_X = "field-1-stride-x";
    public static final String FIELD_1_STRIDE_Y = "field-1-stride-y";

    public static final String FIELD_2_STRIDE_X = "field-2-stride-x";
    public static final String FIELD_2_STRIDE_Y = "field-2-stride-y";

    public static final String FIELD_1_SIZE_X = "field-1-size-x";
    public static final String FIELD_1_SIZE_Y = "field-1-size-y";

    public static final String FIELD_2_SIZE_X = "field-2-size-x";
    public static final String FIELD_2_SIZE_Y = "field-2-size-y";

    public BinaryTreeQuiltConfig() {
    }

    public void setup(
            ObjectMap om, String name, Random r,
            int quiltW, int quiltH,
            int input1W, int input1H,
            int input2W, int input2H,
            int field1StrideX, int field1StrideY,
            int field2StrideX, int field2StrideY,
            int field1SizeX, int field1SizeY,
            int field2SizeX, int field2SizeY ) {
        super.setup( om, name, r );

        setQuiltWidth( quiltW );
        setQuiltHeight( quiltH );

        setInput1Width( input1W );
        setInput1Height( input1H );
        setInput2Width( input2W );
        setInput2Height( input2H );

        setField1StrideX( field1StrideX );
        setField1StrideY( field1StrideY );
        setField2StrideX( field2StrideX );
        setField2StrideY( field2StrideY );

        setField1SizeX( field1SizeX );
        setField1SizeY( field1SizeY );
        setField2SizeX( field2SizeX );
        setField2SizeY( field2SizeY );
    }

    public void copyFrom( NetworkConfig nc, String name ) {
        super.copyFrom( nc, name );

        BinaryTreeQuiltConfig c = ( BinaryTreeQuiltConfig ) nc;

        setQuiltWidth( c.getQuiltWidth() );
        setQuiltHeight( c.getQuiltHeight() );

        setInput1Width( c.getInput1Width() );
        setInput1Height( c.getInput1Height() );
        setInput2Width( c.getInput2Width() );
        setInput2Height( c.getInput2Height() );

        setField1StrideX( c.getField1StrideX() );
        setField1StrideY( c.getField1StrideY() );
        setField2StrideX( c.getField2StrideX() );
        setField2StrideY( c.getField2StrideY() );

        setField1SizeX( c.getField1SizeX() );
        setField1SizeY( c.getField1SizeY() );
        setField2SizeX( c.getField2SizeX() );
        setField2SizeY( c.getField2SizeY() );
    }

    public int getInputArea() {
        return getInput1Area() + getInput2Area();
    }

    public Rectangle getInputRect1( int qx, int qy ) {
        Rectangle r = new Rectangle();
        Point fieldSize = getField1Size();
        Point fieldStride = getField1Stride();
        r.width  = fieldSize.x;
        r.height = fieldSize.y;
        r.x = qx * fieldStride.x;
        r.y = qy * fieldStride.y;
        return r;
    }
    public Rectangle getInputRect2( int qx, int qy ) {
        Rectangle r = new Rectangle();
        Point fieldSize = getField2Size();
        Point fieldStride = getField2Stride();
        r.width  = fieldSize.x;
        r.height = fieldSize.y;
        r.x = qx * fieldStride.x;
        r.y = qy * fieldStride.y;
        return r;
    }

    public Point getField1Stride() {
        Integer w = _om.getInteger( getKey( FIELD_1_STRIDE_X ) );
        Integer h = _om.getInteger( getKey( FIELD_1_STRIDE_Y ) );
        Point p = new Point( w, h );
        return p;
    }

    public Point getField2Stride() {
        Integer w = _om.getInteger( getKey( FIELD_2_STRIDE_X ) );
        Integer h = _om.getInteger( getKey( FIELD_2_STRIDE_Y ) );
        Point p = new Point( w, h );
        return p;
    }

    public Point getField1Size() {
        Integer w = _om.getInteger( getKey( FIELD_1_SIZE_X ) );
        Integer h = _om.getInteger( getKey( FIELD_1_SIZE_Y ) );
        Point p = new Point( w, h );
        return p;
    }

    public Point getField2Size() {
        Integer w = _om.getInteger( getKey( FIELD_2_SIZE_X ) );
        Integer h = _om.getInteger( getKey( FIELD_2_SIZE_Y ) );
        Point p = new Point( w, h );
        return p;
    }

    public Point getInput1Size() {
        Integer w = _om.getInteger( getKey( INPUT_1_WIDTH  ) );
        Integer h = _om.getInteger( getKey( INPUT_1_HEIGHT ) );
        Point p = new Point( w, h );
        return p;
    }

    public Point getInput2Size() {
        Integer w = _om.getInteger( getKey( INPUT_2_WIDTH  ) );
        Integer h = _om.getInteger( getKey( INPUT_2_HEIGHT ) );
        Point p = new Point( w, h );
        return p;
    }

    public int getInput1Area() {
        Point p = getInput1Size();
        return p.x * p.y;
    }

    public int getInput2Area() {
        Point p = getInput2Size();
        return p.x * p.y;
    }

    public void setInput1Width( int input1W ) {
        _om.put( getKey( INPUT_1_WIDTH ), input1W );
    }
    public void setInput1Height( int input1H ) {
        _om.put( getKey( INPUT_1_HEIGHT ), input1H );
    }
    public void setInput2Width( int input2W ) {
        _om.put( getKey( INPUT_2_WIDTH ), input2W );
    }
    public void setInput2Height( int input2H ) {
        _om.put( getKey( INPUT_2_HEIGHT ), input2H );
    }

    public void setField1StrideX( int field1StrideX ) {
        _om.put( getKey( FIELD_1_STRIDE_X ), field1StrideX );
    }
    public void setField1StrideY( int field1StrideY ) {
        _om.put( getKey( FIELD_1_STRIDE_Y ), field1StrideY );
    }
    public void setField2StrideX( int field2StrideX ) {
        _om.put( getKey( FIELD_2_STRIDE_X ), field2StrideX );
    }
    public void setField2StrideY( int field2StrideY ) {
        _om.put( getKey( FIELD_2_STRIDE_Y ), field2StrideY );
    }

    public void setField1SizeX( int field1SizeX ) {
        _om.put( getKey( FIELD_1_SIZE_X ), field1SizeX );
    }
    public void setField1SizeY( int field1SizeY ) {
        _om.put( getKey( FIELD_1_SIZE_Y ), field1SizeY );
    }
    public void setField2SizeX( int field2SizeX ) {
        _om.put( getKey( FIELD_2_SIZE_X ), field2SizeX );
    }
    public void setField2SizeY( int field2SizeY ) {
        _om.put( getKey( FIELD_2_SIZE_Y ), field2SizeY );
    }

    public void setQuiltWidth( int quiltW ) {
        _om.put( getKey( QUILT_WIDTH ), quiltW );
    }
    public void setQuiltHeight( int quiltH ) {
        _om.put( getKey( QUILT_HEIGHT ), quiltH );
    }

    public int getQuiltWidth() {
        Integer n = _om.getInteger( getKey( QUILT_WIDTH ) );
        return n.intValue();
    }
    public int getQuiltHeight() {
        Integer n = _om.getInteger( getKey( QUILT_HEIGHT ) );
        return n.intValue();
    }
    public Point getQuiltSize() {
        Integer w = _om.getInteger( getKey( QUILT_WIDTH  ) );
        Integer h = _om.getInteger( getKey( QUILT_HEIGHT ) );
        Point p = new Point( w, h );
        return p;
    }
    public int getQuiltOffset( int qx, int qy ) {
        Point p = getQuiltSize();
        int stride = p.x;
        return Data2d.getOffset( stride, qx, qy );
    }
    public int getQuiltArea() {
        Integer w = _om.getInteger( getKey( QUILT_WIDTH  ) );
        Integer h = _om.getInteger( getKey( QUILT_HEIGHT ) );
        return w * h;
    }

    public int getInput1Width() {
        Integer n = _om.getInteger( getKey( INPUT_1_WIDTH ) );
        return n.intValue();
    }
    public int getInput1Height() {
        Integer n = _om.getInteger( getKey( INPUT_1_HEIGHT ) );
        return n.intValue();
    }
    public int getInput2Width() {
        Integer n = _om.getInteger( getKey( INPUT_2_WIDTH ) );
        return n.intValue();
    }
    public int getInput2Height() {
        Integer n = _om.getInteger( getKey( INPUT_2_HEIGHT ) );
        return n.intValue();
    }

    public int getField1StrideX() {
        Integer n = _om.getInteger( getKey( FIELD_1_STRIDE_X ) );
        return n.intValue();
    }
    public int getField1StrideY() {
        Integer n = _om.getInteger( getKey( FIELD_1_STRIDE_Y ) );
        return n.intValue();
    }
    public int getField2StrideX() {
        Integer n = _om.getInteger( getKey( FIELD_2_STRIDE_X ) );
        return n.intValue();
    }
    public int getField2StrideY() {
        Integer n = _om.getInteger( getKey( FIELD_2_STRIDE_Y ) );
        return n.intValue();
    }

    public int getField1SizeX() {
        Integer n = _om.getInteger( getKey( FIELD_1_SIZE_X ) );
        return n.intValue();
    }
    public int getField1SizeY() {
        Integer n = _om.getInteger( getKey( FIELD_1_SIZE_Y ) );
        return n.intValue();
    }
    public int getField2SizeX() {
        Integer n = _om.getInteger( getKey( FIELD_2_SIZE_X ) );
        return n.intValue();
    }
    public int getField2SizeY() {
        Integer n = _om.getInteger( getKey( FIELD_2_SIZE_Y ) );
        return n.intValue();
    }

}
