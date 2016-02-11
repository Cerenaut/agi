package io.agi.core.alg;

import io.agi.core.ann.supervised.ActivationFunctionFactory;
import io.agi.core.ann.supervised.FeedForwardNetworkConfig;
import io.agi.core.ann.supervised.LossFunction;
import io.agi.core.ann.unsupervised.GrowingNeuralGasConfig;
import io.agi.core.data.Data;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.ObjectMap;
import io.agi.core.orm.UnitTest;

/**
 * Created by dave on 11/01/16.
 */
public class RegionTest implements UnitTest {

    public static void main( String[] args ) {
        RegionTest ffnt = new RegionTest();
        ffnt.test( args );
    }

    public void test( String[] args ) {

        // Test parameters
        int randomSeed = 1;
        int inputWidth = 10;
        int inputHeight = 10;
        int regionSizeColumns = 50;

        // Algorithm specific parameters
        // Column Sizing
        int columnWidthCells  = 6;
        int columnHeightCells = 6;

        // Hierarchy training
        int receptiveFieldsTrainingSamples = 10;
        float receptiveFieldsElasticity = 1.0f;
        float receptiveFieldsLearningRate = 0.01f;
        float inputColumnsFrequencyLearningRate = 0.01f;
        float inputColumnsFrequencyThreshold = 0.5f;

        // Column training
        int columnInputs = 10;
        float columnLearningRate = 0.02f;
        float columnLearningRateNeighbours = 0.01f;
        float columnNoiseMagnitude = 0.0f;
        int columnEdgeMaxAge = 500;
        float columnStressLearningRate = 0.01f;
        float columnStressThreshold = 0.1f;
        int columnGrowthInterval = 100;

        // Predictor
        String predictorLossFunction = LossFunction.CROSS_ENTROPY;
        String predictorActivationFunction = ActivationFunctionFactory.LOG_SIGMOID;
        String predictorLayerSizes = String.valueOf( 36 ); // 6 * 6 * 1.something
        float predictorLearningRate = 0.1f;
        float predictorRegularization = 0.0f;

        // Build the algorithm
        RandomInstance.setSeed(randomSeed); // make the tests repeatable
        ObjectMap om = ObjectMap.GetInstance();
        RegionFactory rf = new RegionFactory();

        RegionConfig rc = new RegionConfig();
        String regionName = "region";

        rc.setup(
            om, "region", // temp name
            inputWidth, inputHeight,
            columnInputs, columnWidthCells, columnHeightCells,
            regionSizeColumns,
            receptiveFieldsTrainingSamples, receptiveFieldsElasticity, receptiveFieldsLearningRate,
            inputColumnsFrequencyLearningRate, inputColumnsFrequencyThreshold );

        int surfaceAreaCells = rc.getSurfaceAreaCells();
        int columnAreaCells = rc.getColumnAreaCells();

        GrowingNeuralGasConfig gngc = new GrowingNeuralGasConfig();
        gngc.setup(
            om, "gng", // temp name
            surfaceAreaCells, columnWidthCells, columnHeightCells,
            columnLearningRate, columnLearningRateNeighbours, columnNoiseMagnitude,
            columnEdgeMaxAge, columnStressLearningRate, columnStressThreshold, columnGrowthInterval );

        FeedForwardNetworkConfig ffnc = new FeedForwardNetworkConfig();
        int predictorInputs = surfaceAreaCells;
        int predictorOutputs = columnAreaCells;
        int predictorLayers = 2;
        ffnc.setup(
            om, "ffn",
            predictorLossFunction, predictorActivationFunction,
            predictorInputs, predictorOutputs,
            predictorLayers, predictorLayerSizes,
            predictorRegularization, predictorLearningRate );

        rf.setup( rc, gngc, ffnc );
        Region r = rf.createRegion( regionName );

        // Run
        while( true ) {
            Data d = r.getExternalInput();
            d.setRandom();
            d.thresholdLessThan( 0.5f, 1.f, 0.f );
            r.update();

            System.out.print( "." );
        }
    }

}