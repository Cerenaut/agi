package io.agi.core.ann.unsupervised;

import io.agi.core.math.RandomInstance;
import io.agi.core.orm.UnitTest;

/**
 * Created by dave on 11/01/16.
 */
public class GrowingNeuralGasTest implements UnitTest {

    public static void main( String[] args ) {
        GrowingNeuralGasTest ffnt = new GrowingNeuralGasTest();
        ffnt.test( args );
    }

    public void test( String[] args ) {

        int randomSeed = 1;
        float inputMin = 0.f;
        float inputMax = 1.f;
        float noiseMagnitude = 0.005f; //0.f;//0.005f;
        float learningRate = 0.02f;
        float learningRateNeighbours = 0.01f;
        float stressThreshold = 0.03f;
        float stressLearningRate = 0.1f;
        int edgeMaxAge = 400;
        int inputs = 2; // x and y.
        int widthCells = 10;
        int heightCells = 10;

        RandomInstance.setSeed(randomSeed); // make the tests repeatable

/*        GrowingNeuralGasConfig gngc = new GrowingNeuralGasConfig(
                widthCells, heightCells, inputs, inputMin, inputMax, noiseMagnitude, learningRate, learningRateNeighbours, stressThreshold, stressLearningRate, edgeMaxAge );
        GrowingNeuralGas cl = new GrowingNeuralGas();
        cl.setup( gngc );

        CompetitiveLearningDemo cld = new CompetitiveLearningDemo();
        cld.run( args, cl );

        // Functional callbacks:
        CallbackCollection cc = new CallbackCollection();
        cc.add( this );
        cc.add( _cl );
        cc.add( pc );

        // Set up the paint thread to do all the work
        pt.iterate( cc );
*/
    }

    public void call() {
        float x = getDiscreteRandom();
        float y = getDiscreteRandom();
//        _cl._inputValues._values[ 0 ] = x;
//        _cl._inputValues._values[ 1 ] = y;
    }

    public float getDiscreteRandom() {
        float gridSize = 6.f;
        float x = (float)RandomInstance.random() * ( gridSize - 0.f );
        int nx = (int)x;
        x = (float)nx;
        x /= gridSize;
        x += ((1.f / ( gridSize )) * 0.5f );
        return x;
    }

}
/*
public class CompetitiveLearningDemoPanel extends IterativelyPaintablePanel {//JPanel implements Callback, MouseListener {

    int _markSize = 16;
    int _size = 100;
    CompetitiveLearning _cl;
    CompetitiveLearningDemo _cld;

    public CompetitiveLearningDemoPanel() {

    }

    public void setup(
            int size,
            CompetitiveLearningDemo cld ) {
        _size = size;
        _cl = cld._cl;
        _cld = cld;
    }

    public Dimension getPreferredSize() {
        return new Dimension( getContentWidth(), getContentHeight() );
    }

    public int getContentHeight() {
        return _size;
    }

    public int getContentWidth() {
        return _size;
    }

    public void paintComponent( Graphics g ) {
        Graphics2D g2d = (Graphics2D)g;
        paint( g2d );
    }

    public void paint( Graphics2D g2d ) {

        // paint all the weights
        // then paint the current point
        g2d.setColor( Color.DARK_GRAY );
        int cw = getContentWidth();
        int ch = getContentHeight();
        g2d.fillRect( 0,0, cw, ch );

        g2d.setStroke( new BasicStroke( 2 ) );

        ValueRange errorRange = _cl._cellErrors.getValueRange();

        int cells = _cl._c._w * _cl._c._h;
        int inputs = _cl._c._i;
        int halfMark = _markSize / 2;

        for( int y = 0; y < _cl._c._h; ++y ) {
            for( int x = 0; x < _cl._c._w; ++x ) {

                int cell = y * _cl._c._w + x;

                float wx = _cl._cellWeights._values[ cell * inputs +0 ];
                float wy = _cl._cellWeights._values[ cell * inputs +1 ];

                float mask = _cl._cellMask._values[ cell ];
                float activity = _cl._cellActivity._values[ cell ];
                float error = (float)(( _cl._cellErrors._values[ cell ] - errorRange._min ) / errorRange.range() );

                int activityByte = Maths.realUnit2Byte( activity );
                int errorByte = Maths.realUnit2Byte( error );
                int maskByte = Maths.realUnit2Byte( mask );

                int px = (int)( wx * _size );
                int py = (int)( wy * _size );
                g2d.setColor( new Color( errorByte, maskByte, 0 ) );
                g2d.fillOval( px-halfMark, py-halfMark, _markSize, _markSize );
                g2d.setColor( new Color( activityByte, activityByte, 0 ) );
                g2d.drawOval( px-halfMark, py-halfMark, _markSize, _markSize );
            }
        }

        float wx = _cl._inputValues._values[ 0 ];
        float wy = _cl._inputValues._values[ 1 ];
        int px = (int)( wx * _size );
        int py = (int)( wy * _size );

        g2d.setColor( Color.CYAN );
        g2d.drawLine( px-halfMark, py, px+halfMark, py );
        g2d.drawLine( px, py-halfMark, px, py+halfMark );
    }
}*/