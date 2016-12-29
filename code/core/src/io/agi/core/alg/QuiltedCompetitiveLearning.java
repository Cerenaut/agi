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

import io.agi.core.ann.unsupervised.GrowingNeuralGas;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.ann.unsupervised.HierarchicalQuilt;
import io.agi.core.ann.unsupervised.HierarchicalQuiltConfig;
import io.agi.core.data.Data;
import io.agi.core.data.Data2d;
import io.agi.core.data.DataSize;
import io.agi.core.data.Ranking;
import io.agi.core.math.Geometry;
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
    public Data _input;
    public Data _quilt;

    public QuiltedCompetitiveLearningConfig _config;
    public HierarchicalQuilt _organizer;
    public HashMap< Integer, GrowingNeuralGas > _classifiers = new HashMap< Integer, GrowingNeuralGas >();

    // computed transient state
    protected HashSet< Integer > _inputActive;
    protected HashMap< Integer, ArrayList< Integer > > _classifierActiveInput = new HashMap< Integer, ArrayList< Integer > >();
    protected HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > _activeInputClassifierRanking = new HashMap< Integer, TreeMap< Float, ArrayList< Integer > > >();

    public QuiltedCompetitiveLearning(String name, ObjectMap om) {
        super(name, om);
    }

    public void setup( QuiltedCompetitiveLearningConfig config ) {
        _config = config;

        setupObjects();
        setupData();
        reset();
    }

    protected void setupObjects() {

        String organizerName = getKey(_config.ORGANIZER);
        HierarchicalQuiltConfig hqc = new HierarchicalQuiltConfig();
        hqc.copyFrom(_config._organizerConfig, organizerName);

        HierarchicalQuilt hq = new HierarchicalQuilt( hqc._name, hqc._om );
        hq.setup(hqc);
        _organizer = hq;

        Point organizerSizeCells = _config.getOrganizerSizeCells();

        for( int y = 0; y < organizerSizeCells.y; ++y ) {
            for( int x = 0; x < organizerSizeCells.x; ++x ) {

                String name = getKey( _config.CLASSIFIER );
                GrowingNeuralGasConfig gngc = new GrowingNeuralGasConfig();
                gngc.copyFrom( _config._classifierConfig, name );

                GrowingNeuralGas gng = new GrowingNeuralGas( gngc._name, gngc._om );
                gng.setup( gngc );

                int classifierOffset = _config.getOrganizerOffset( x, y );
                _classifiers.put( classifierOffset, gng );
            }
        }
    }

    protected void setupData() {
        Point inputSize = _config.getInputSize();
        Point regionSize = _config.getQuiltSizeCells();

        DataSize dataSizeInput = DataSize.create(inputSize.x, inputSize.y);
        DataSize dataSizeQuilt = DataSize.create(regionSize.x, regionSize.y);

        // external inputs
        _input = new Data( dataSizeInput );
        _quilt = new Data( dataSizeQuilt );
    }

    public void reset() {
        organizerReset();

        Point p = _config.getOrganizerSizeCells();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {
                int classifierOffset = _config.getOrganizerOffset( x, y );
                GrowingNeuralGas classifier = _classifiers.get( classifierOffset );
                classifier.reset();
            }
        }
    }

    public void update() {

        _inputActive = _input.indicesMoreThan( 0.f ); // find all the active bits.

        organizerUpdate();
        classifierUpdate();
    }

    /**
     * Returns the receptive field centroid, in pixels, of the specified classifier.
     *
     * @param xClassifier
     * @param yClassifier
     * @return
     */
    public float[] getClassifierReceptiveField( int xClassifier, int yClassifier ) {

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
    }

    protected void classifierUpdate() {
        // update all the classifiers and thus the set of active cells in the region
        _quilt.set(0.f); // clear

        Point p = _config.getOrganizerSizeCells();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                // only update active
                int classifierOffset = _config.getOrganizerOffset( x, y );
                float mask = _organizer._cellMask._values[ classifierOffset ];
                if( mask != 1.f ) {
                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
                }

                rankClassifierReceptiveFields( x, y );
            }
        }

        updateClassifierInput();

        for( int y = 0; y < p.y; ++y ) {
            for( int x = 0; x < p.x; ++x ) {

                // only update active
                int classifierOffset = _config.getOrganizerOffset( x, y );
                float mask = _organizer._cellMask._values[ classifierOffset ];
                if( mask != 1.f ) {
                    continue; // because the cell is "dead" or inactive. We already set the region output to zero, so no action required.
                }

                updateClassifier( x, y ); // adds to _transient._regionActiveCells and _regionActivity
            }
        }
    }

    protected void rankClassifierReceptiveFields( int xClassifier, int yClassifier ) {

        // find the closest N cols to each active input bit
        float[] rf = getClassifierReceptiveField( xClassifier, yClassifier ); // in pixels units
        float xField1 = rf[ 0 ];
        float yField1 = rf[ 1 ];
//        float xField2 = rf[ 2 ];
//        float yField2 = rf[ 3 ];

        int inputOffset = 0;

        rankClassifierReceptiveField( xClassifier, yClassifier, _input, _inputActive, _activeInputClassifierRanking, xField1, yField1, inputOffset );
    }

    protected void rankClassifierReceptiveField(
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

    }

    protected void updateClassifierInput() {
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
    }

    protected void updateClassifier( int xClassifier, int yClassifier ) {

        Point classifierOrigin = _config.getQuiltClassifierOrigin(xClassifier, yClassifier);
        int classifierOffset = _config.getOrganizerOffset(xClassifier, yClassifier);

        ArrayList< Integer > activeInput = getClassifierActiveInput(classifierOffset);//_classifierActiveInput.get( classifierOffset );

        Collections.sort( activeInput );

        GrowingNeuralGas classifier = _classifiers.get( classifierOffset );

        boolean learn = _config.getLearn();

        classifier._c.setLearn( learn );
        classifier.setSparseUnitInput( activeInput );
//        classifier._inputValues.copy( _input );
        classifier.update(); // trains with this sparse input.

        // map the best cell into the region/quilt
        int bestColumnCell = classifier.getBestCell();
        int bestColumnCellX = classifier._c.getCellX( bestColumnCell );
        int bestColumnCellY = classifier._c.getCellY( bestColumnCell );
        int regionX = classifierOrigin.x + bestColumnCellX;
        int regionY = classifierOrigin.y + bestColumnCellY;
        int regionOffset = _config.getQuiltOffset(regionX, regionY);

        _quilt._values[ regionOffset ] = 1.f;
    }

    protected void organizerReset() {
        // uniform quilt
        _organizer.reset();
    }

    /**
     * Trains the receptive fields of the classifiers via a specified number of samples.
     */
    protected void organizerUpdate() {
        // uniform quilt
        _organizer.update();
    }

    public static TreeMap< Float, ArrayList< Integer > > getRankingLazy( HashMap< Integer, TreeMap< Float, ArrayList< Integer > > > rankingMap, int i ) {
        TreeMap< Float, ArrayList< Integer > > ranking = rankingMap.get(i);
        if( ranking == null ) {
            ranking = Ranking.CreateRanking();
            rankingMap.put( i, ranking );
        }
        return ranking;
    }

    public ArrayList< Integer > getClassifierActiveInput( int classifier ) {
        ArrayList< Integer > activeInput = _classifierActiveInput.get( classifier );
        if( activeInput == null ) {
            return new ArrayList< Integer >();
        }
        return activeInput;
    }

    public static void addClassifierActiveInput( int classifier, int activeInput, HashMap< Integer, ArrayList< Integer > > classifierActiveInput ) {
        ArrayList< Integer > al = classifierActiveInput.get( classifier );
        if( al == null ) {
            al = new ArrayList< Integer >();
            classifierActiveInput.put( classifier, al );
        }

        al.add(activeInput);
    }

}


