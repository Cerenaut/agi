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

import io.agi.core.ann.unsupervised.*;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import java.awt.*;
import java.util.*;


/**
 * TOOD add use of feedback to adjust the inference of winning cells.
 *
 * Created by dave on 22/10/16.
 */
public class QuiltedCompetitiveLearning extends NamedObject {

    // Data structures
    public Data _input1;
    public Data _input2;
    public Data _input;
    public Data _quiltCells;

    public boolean _emit2ndBest = false;
    public QuiltedCompetitiveLearningConfig _config;
    public BinaryTreeQuilt _quilt;
    public HashMap< Integer, GrowingNeuralGas > _classifiers = new HashMap< Integer, GrowingNeuralGas >(); // apart from in first layer, there will be no commonality of input distributions
//    public GrowingNeuralGas _classifier;

    // computed transient state
//    protected HashSet< Integer > _inputActive;
//    protected HashMap< Integer, ArrayList< Integer > > _classifierActiveInput = new HashMap< Integer, ArrayList< Integer > >();
//    protected HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > _activeInputClassifierRanking = new HashMap< Integer, TreeMap< Float, ArrayList< Integer > > >();

    public QuiltedCompetitiveLearning( String name, ObjectMap om ) {
        super( name, om );
    }

    public void setup( QuiltedCompetitiveLearningConfig config ) {
        _config = config;

        setupObjects();
        setupData();
        reset();
    }

    protected void setupObjects() {

        String quiltName = getKey( _config.QUILT );

        BinaryTreeQuiltConfig hqc = new BinaryTreeQuiltConfig();
        hqc.copyFrom( _config._quiltConfig, quiltName );

        BinaryTreeQuilt btq = new BinaryTreeQuilt();
        btq.setup( hqc );
        _quilt = btq;

        Point p = _config._quiltConfig.getQuiltSize();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                String name = getKey( _config.CLASSIFIER );

                GrowingNeuralGasConfig gngc = new GrowingNeuralGasConfig();
                gngc.copyFrom( _config._classifierConfig, name );

                GrowingNeuralGas classifier = new GrowingNeuralGas( gngc._name, gngc._om );
                classifier.setup( gngc );

                int quiltOffset = _config._quiltConfig.getQuiltOffset( x, y );
                _classifiers.put( quiltOffset, classifier );
            }
        }
    }

    protected void setupData() {
        int inputArea = _config._quiltConfig.getInputArea();
        Point input1Size = _config._quiltConfig.getInput1Size();
        Point input2Size = _config._quiltConfig.getInput2Size();
        Point regionSize = _config.getCellsSize();

        DataSize dataSizeInput  = DataSize.create( inputArea );
        DataSize dataSizeInput1 = DataSize.create( input1Size.x, input1Size.y );
        DataSize dataSizeInput2 = DataSize.create( input2Size.x, input2Size.y );
        DataSize dataSizeQuilt  = DataSize.create( regionSize.x, regionSize.y );

        // external inputs
        _input1     = new Data( dataSizeInput1 );
        _input2     = new Data( dataSizeInput2 );
        _input      = new Data( dataSizeInput  );
        _quiltCells = new Data( dataSizeQuilt  );
    }

    public void reset() {
//        organizerReset();
        _quilt.reset();

        Point p = _config._quiltConfig.getQuiltSize();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                int quiltOffset = _config._quiltConfig.getQuiltOffset( x, y );
                GrowingNeuralGas classifier = _classifiers.get( quiltOffset );
                classifier.reset();
            }
        }
    }

    public void update() {

        // combine inputs into one structure
        int inputOffset = 0;
        int input1Area = _config._quiltConfig.getInput1Area();
        int input2Area = _config._quiltConfig.getInput2Area();
        _input.copyRange( _input1, inputOffset, 0, input1Area );
        inputOffset = input1Area;
        _input.copyRange( _input2, inputOffset, 0, input2Area );
//        _inputActive = _input.indicesMoreThan( 0.f ); // find all the active bits.

        _quilt.update(); // train receptive fields (if not fixed)

        classifierUpdate();
    }

/*    /**
     * Returns the receptive field centroid, in pixels, of the specified classifier.
     *
     * @param xClassifier
     * @param yClassifier
     * @return
     */
/*    public float[] getClassifierReceptiveField( int xClassifier, int yClassifier ) {

        int dimensions = 2;
        int inputs = 2;
        int elements = dimensions * inputs;
        float[] rf = new float[ elements ];

        int classifierOffset = _config.getOrganizerOffset( xClassifier, yClassifier );
        int organizerOffset = classifierOffset * elements;//QuiltLayerConfig.RECEPTIVE_FIELD_DIMENSIONS;

        Point inputSize1 = Data2d.getSize( _input );
        Point inputSize2 = new Point( 1, 1 );

        float rf1_x = _organizer._cellWeights._values[ organizerOffset + 0 ];
        float rf1_y = _organizer._cellWeights._values[ organizerOffset + 1 ];
        float rf2_x = _organizer._cellWeights._values[ organizerOffset + 2 ];
        float rf2_y = _organizer._cellWeights._values[ organizerOffset + 3 ];

        rf1_x *= inputSize1.x;
        rf1_y *= inputSize1.y;

        rf2_x *= inputSize2.x;
        rf2_y *= inputSize2.y;

        rf[ 0 ] = rf1_x; // now in pixel coordinates, whereas it is trained as unit coordinates
        rf[ 1 ] = rf1_y;
        rf[ 2 ] = rf2_x; // now in pixel coordinates, whereas it is trained as unit coordinates
        rf[ 3 ] = rf2_y;

        return rf;
    }*/

    protected void classifierUpdate() {
        // update all the classifiers and thus the set of active cells in the region
        _quiltCells.set( 0.f ); // clear

        Point p = _config._quiltConfig.getQuiltSize();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                // only update active
                int classifierOffset = _config._quiltConfig.getQuiltOffset( x, y );
                float mask = _quilt._quiltMask._values[ classifierOffset ];
                if( mask != 1.f ) {
                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
                }

////                rankClassifierReceptiveFields( x, y );
//            }
//        }
//
////        updateClassifierInput();
//
//        for( int y = 0; y < p.y; ++y ) {
//            for( int x = 0; x < p.x; ++x ) {

                // only update active
//                int classifierOffset = _config.getOrganizerOffset( x, y );
//                float mask = _organizer._cellMask._values[ classifierOffset ];
//                if( mask != 1.f ) {
//                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
//                }

                Data inputMask = getClassifierMask( x, y );
                Data input = getClassifierInput( _input, inputMask );

                updateClassifier( input, x, y ); // adds to _transient._regionActiveCells and _regionActivity
            }
        }
    }

    public Data getClassifierMask( int x, int y ) {
        Data inputMask = _quilt.getInputMask( x, y );
        return inputMask;
    }

    public Data getClassifierInput( Data input, Data mask ) {
        Data maskedInput = new Data( input._dataSize );

        float maskValue = 0f; // if mask == 0, then
        float maskedValue = 0f; // input --> maskedValue

        maskedInput.mask( input, mask, maskValue, maskedValue );

        return maskedInput;
    }
/*    protected void rankClassifierReceptiveFields( int xClassifier, int yClassifier ) {

        // find the closest N cols to each active input bit
        float[] rf = getClassifierReceptiveField( xClassifier, yClassifier ); // in pixels units
        float xField1 = rf[ 0 ];
        float yField1 = rf[ 1 ];
//        float xField2 = rf[ 2 ];
//        float yField2 = rf[ 3 ];

        int inputOffset = 0;

        rankClassifierReceptiveField( xClassifier, yClassifier, _input, _inputActive, _activeInputClassifierRanking, xField1, yField1, inputOffset );
    }*/

/*    protected void rankClassifierReceptiveField(
            int xClassifier,
            int yClassifier,
            Data ffInput,
            HashSet< Integer > ffInputActive,
            HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > ranking,
            float xField,
            float yField,
            int inputOffset ) {
        int classifierOffset = _config.getOrganizerOffset(xClassifier, yClassifier);

        for( Integer i : ffInputActive ) {
            Point p = Data2d.getXY( ffInput._dataSize, i );

            float d = Geometry.distanceEuclidean2d( ( float ) p.getX(), ( float ) p.getY(), xField, yField );
            int inputBit = i + inputOffset;

            TreeMap< Float, ArrayList< Integer > > activeInputRanking = getRankingLazy(ranking, inputBit);

            // Rank by classifier:
            Ranking.add( activeInputRanking, d, classifierOffset ); // add classifier with quality d (distance) to i.
        }

    }*/

/*    protected void updateClassifierInput() {
        updateClassifierInput( _activeInputClassifierRanking, _classifierActiveInput );
//        updateClassifierInput( _transient._activeInputClassifierRankingOld, _transient._classifierActiveInputOld );
    }

    protected void updateClassifierInput(
            HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > activeInputClassifierRanking,
            HashMap< Integer, ArrayList< Integer > > classifierActiveInput ) {

        int classifiersPerBit = _config.getClassifiersPerBit();
        boolean max = false; // ie min [distance]

        //int inputOffset1 = 0; conceptually
        //int inputOffset2 = _input.getSize();

        Set< Integer > activeInputBits = activeInputClassifierRanking.keySet();
        for( Integer inputBit : activeInputBits ) {

            // pick the right spread of input through the region depending on which input it is from
            int maxRank = classifiersPerBit;
//            if( inputBit >= inputOffset2 ) {
//                maxRank = classifiersPerBit2;
//            }

            TreeMap< Float, ArrayList< Integer > > activeInputRanking = getRankingLazy( activeInputClassifierRanking, inputBit );

            ArrayList< Integer > activeInputClassifiers = Ranking.getBestValues( activeInputRanking, max, maxRank ); // ok now we got the current set of inputs for the column

            for( Integer classifierOffset : activeInputClassifiers ) {
                addClassifierActiveInput( classifierOffset, inputBit, classifierActiveInput );
            }
        }
    }*/

    protected void updateClassifier( Data input, int xClassifier, int yClassifier ) {

        Point cellsOrigin = _config.getCellsOriginOfQuilt( xClassifier, yClassifier );
        int quiltOffset = _config._quiltConfig.getQuiltOffset( xClassifier, yClassifier );

//        ArrayList< Integer > activeInput = getClassifierActiveInput(classifierOffset);//_classifierActiveInput.get( classifierOffset );
//
//        Collections.sort( activeInput );

        GrowingNeuralGas classifier = _classifiers.get( quiltOffset );
//        GrowingNeuralGas classifier = _classifier;

        boolean learn = _config.getLearn();

        classifier._c.setLearn( learn );
//        classifier.setSparseUnitInput( activeInput );
        classifier._inputValues.copy( input );
        classifier.update(); // trains with this sparse input.

        // map the best cell into the region/quilt
        int bestColumnCell = classifier.getBestCell();
        int bestColumnCellX = classifier._c.getCellX( bestColumnCell );
        int bestColumnCellY = classifier._c.getCellY( bestColumnCell );
        int cellX = cellsOrigin.x + bestColumnCellX;
        int cellY = cellsOrigin.y + bestColumnCellY;
        int cellOffset = _config.getCellsOffset( cellX, cellY );

        _quiltCells._values[ cellOffset ] = 1.f;

        // optionally emit the 2nd best cell into the quilt as well.
        if( !_emit2ndBest ) {
            return;
        }

        int bestColumnCell2 = classifier.get2ndBestCell();
        int bestColumnCell2X = classifier._c.getCellX( bestColumnCell2 );
        int bestColumnCell2Y = classifier._c.getCellY( bestColumnCell2 );
        int cell2X = cellsOrigin.x + bestColumnCell2X;
        int cell2Y = cellsOrigin.y + bestColumnCell2Y;
        int cell2Offset = _config.getCellsOffset( cell2X, cell2Y );

        _quiltCells._values[ cell2Offset ] = 1.f;
    }

    public void invert( Data quiltCells, Data input1, Data input2 ) {
        Data input      = new Data( _input._dataSize );
        Data inputCount = new Data( _input._dataSize );

        Point quiltSize = _config._quiltConfig.getQuiltSize();
        Point classifierSize = _config._classifierConfig.getSizeCells();

        int inputArea = _input.getSize();

        for( int qy = 0; qy < quiltSize.y; ++qy ) {
            for( int qx = 0; qx < quiltSize.x; ++qx ) {

                // only update active
                int classifierOffset = _config._quiltConfig.getQuiltOffset( qx, qy );
                float m = _quilt._quiltMask._values[ classifierOffset ];
                if( m != 1.f ) {
                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
                }

                // apply inversion for this classifier
                int quiltOffset = _config._quiltConfig.getQuiltOffset( qx, qy );
                GrowingNeuralGas classifier = _classifiers.get( quiltOffset );

                // find the max prediction for this classifier. That will be the cell that's inverted.
                float cellMaxValue = 0f;
                int cellMaxX = 0;
                int cellMaxY = 0;

                Point cellsOrigin = _config.getCellsOriginOfQuilt( qx, qy );

                for( int cy = 0; cy < classifierSize.y; ++cy ) {
                    for( int cx = 0; cx < classifierSize.x; ++cx ) {
                        int cellX = cellsOrigin.x + cx;
                        int cellY = cellsOrigin.y + cy;
                        int cellOffset = _config.getCellsOffset( cellX, cellY );

                        float quiltValue = quiltCells._values[ cellOffset ];

                        if( quiltValue >= cellMaxValue ) {
                            cellMaxValue = quiltValue;
                            cellMaxX = cx;
                            cellMaxY = cy;
                        }
                    }
                }

                int cellMaxOffset = classifier._c.getCell( cellMaxX, cellMaxY );
                Data inputOfCell = CompetitiveLearning.invert( cellMaxOffset, _input._dataSize, classifier._cellMask, classifier._cellWeights );
                Data inputMask = getClassifierMask( qx, qy );

                for( int i = 0; i < inputArea; ++i ) {
                    float im = inputMask._values[ i ];
                    if( im > 0f ) {
                        float temp = inputOfCell._values[ i ];
                        input._values[ i ] += temp;
                        inputCount._values[ i ] += 1f;
                    }
                }

            }
        }

        // normalize the input due to overlapping receptive fields
        for( int i = 0; i < inputArea; ++i ) {
            float count = inputCount._values[ i ];

            if( count == 0f ) {
                continue;
            }

            float inverted = input._values[ i ];
            float normalized = inverted / count; // mean

            input._values[ i ] = normalized;
        }

        // now split input into the two parts
        int inputOffset = 0;
        int input1Area = _config._quiltConfig.getInput1Area();
        int input2Area = _config._quiltConfig.getInput2Area();
        input1.copyRange( input, 0, inputOffset, input1Area );
        inputOffset = input1Area;
        input2.copyRange( input, 0, inputOffset, input2Area );
    }


//    protected void organizerReset() {
//        // uniform quilt
//        _organizer.reset();
//    }
//
//    protected void organizerUpdate() {
//        // uniform quilt
//        _organizer.update();
//    }

//    public static TreeMap< Float, ArrayList< Integer > > getRankingLazy( HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > rankingMap, int i ) {
//        TreeMap< Float, ArrayList< Integer > > ranking = rankingMap.get(i);
//        if( ranking == null ) {
//            ranking = Ranking.CreateRanking();
//            rankingMap.put( i, ranking );
//        }
//        return ranking;
//    }
//
//    public ArrayList< Integer > getClassifierActiveInput( int classifier ) {
//        ArrayList< Integer > activeInput = _classifierActiveInput.get( classifier );
//        if( activeInput == null ) {
//            return new ArrayList< Integer >();
//        }
//        return activeInput;
//    }
//
//    public static void addClassifierActiveInput( int classifier, int activeInput, HashMap< Integer, ArrayList< Integer > > classifierActiveInput ) {
//        ArrayList< Integer > al = classifierActiveInput.get( classifier );
//        if( al == null ) {
//            al = new ArrayList< Integer >();
//            classifierActiveInput.put( classifier, al );
//        }
//
//        al.add(activeInput);
//    }

}


