package io.agi.core.alg;

import io.agi.core.ann.unsupervised.DynamicSelfOrganizingMapConfig;
import io.agi.core.data.Data;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

import java.awt.*;

/**
 * Created by dave on 11/01/16.
 */
public class RegionTest implements UnitTest {

    public static final String REGION = "region";

    public static void main( String[] args ) {
        RegionTest ffnt = new RegionTest();
        ffnt.test( args );
    }

    public void test( String[] args ) {
        // Test parameters
        int randomSeed = 1;
        int inputWidth = 10;
        int inputHeight = 10;
        int columns = 50;

        // Algorithm specific parameters
        // Column Sizing
        int columnWidth  = 6;
        int columnHeight = 6;

        // Hierarchy training
        int receptiveFieldsTrainingSamples = 10;
        float receptiveFieldsElasticity = 1.0f;
        float receptiveFieldsLearningRate = 0.01f;

        // Column training
        float columnLearningRate = 0.02f;
        float columnLearningRateNeighbours = 0.01f;
        float columnNoiseMagnitude = 0.0f;
        int columnEdgeMaxAge = 500;
        float columnStressLearningRate = 0.01f;
        float columnStressThreshold = 0.1f;
        int columnGrowthInterval = 100;

        RegionConfig rc = new RegionConfig( inputWidth, inputHeight, columnWidth, columnHeight, columns );

//        Point surfaceSize = Region.GetSurfaceSize( internalWidthColumns, internalHeightColumns, externalHeightColumns, columnWidth, columnHeight );
            int surfaceArea = Region.GetSurfaceArea( rc._internalWidthColumns, rc._internalHeightColumns, rc._externalHeightColumns, columnWidth, columnHeight );
        int columnInputs = surfaceArea;

        // Algorithm setup
        RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();
        ColumnFactory cf = new ColumnFactory();

        Region r = new Region( REGION, om );
        r.setup(
            cf,
            columnWidth, columnHeight, columnInputs,
            rc._externalHeightColumns, rc._internalWidthColumns, rc._internalHeightColumns,
            receptiveFieldsTrainingSamples, receptiveFieldsElasticity, receptiveFieldsLearningRate,
            columnLearningRate, columnLearningRateNeighbours, columnNoiseMagnitude, columnEdgeMaxAge, columnStressLearningRate, columnStressThreshold, columnGrowthInterval
        );

        // Run
        while( true ) {
            Data d = r.getExternalInput();
            d.setRandom();
            d.thresholdLessThan( 0.5f, 1.f, 0.f );
            r.update();
        }
    }

}