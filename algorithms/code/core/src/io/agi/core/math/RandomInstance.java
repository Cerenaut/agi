/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.math;

import java.util.Random;

/**
 * Utilities for managing a centralized random number generator that isn't shared
 * with code except your own. Global access via static interface.
 * <p>
 * TODO: Consider http://docs.oracle.com/javase/tutorial/essential/concurrency/threadlocalrandom.html
 *
 * @author dave
 */
public class RandomInstance {

    private static volatile long seedUniquifier = 8682522807148012L; // copied from java.util.Random
    protected static volatile long _seed = 0;
    protected static Random _instance = null;

    public static double random() {
        return getInstance().nextDouble();
    }

    public void reset() {
        _instance = null;
        _seed = 0;
    }

    public static void setSeed( long seed ) {
        if ( _instance != null ) {
            System.err.println( "It's too late to seed the random number generator!" );
            System.exit( -1 );
        }

        instantiate( seed );
    }

    public static long getSeed() {
        return _seed;
    }

    protected static long getRandomSeed() {
        long seed = ( ++seedUniquifier + System.nanoTime() );  // method copied from java.util.Random
        return seed;
    }

    protected static void instantiate( long seed ) {
        _seed = seed;
        _instance = new Random( _seed );
        System.out.println( "Random seed = " + _seed );
    }

    public static Random getInstance() {
        if ( _instance == null ) {
            //_instance = new Random( getRandomSeed() ); // seeded with time
            instantiate( getRandomSeed() );
        }
        return _instance;
    }

    /**
     * Random value between 0 and n-1.
     *
     * @param n
     * @return
     */
    public static int randomInt( int n ) {
        double r = ( ( double ) ( n ) ) * RandomInstance.random();
        return ( int ) r;
    }

    /**
     * Random value in radians, range -pi <= n < pi
     *
     * @return
     */
    public static double randomRadians() {
        double r = ( RandomInstance.random() * Math.PI * 2.0 ) - Math.PI; // -pi : pi
        return r;
    }

    /**
     * See randomNormal( Random, int )
     *
     * @return
     */
    public static double randomNormal() {
        return randomNormal( getInstance(), 12 );
    }

    /**
     * Should return a value 0.0 <= n < 1.0, taken from a normal distribution
     * approximated by averaging n samples.
     * <p>
     * Generates Irwin Hall distribution, aka uniform sum distribution.
     * This is an approximation of a normal distribution.
     * <p>
     * n = 12 is *usually enough*
     *
     * @param o
     * @param n
     * @return
     */
    public static double randomNormal( Random o, int n ) {

        //int n = 12;
        double r = 0.0;

        for ( int i = 1; i < n; ++i ) // unroll please compiler?
        {
            r += o.nextDouble();
        }

        // truncate the interval from 0..N to 0..1
        // Mean was: n-(n/2), now: 0.5
        r /= ( double ) ( n - 1 );

        return r;
    }

    /**
     * See randomNormal( Random )
     *
     * @param o
     * @param n
     * @return
     */
    public static double randomNormal( Random o ) {
        return randomNormal( o, Constants.RANDOM_NORMAL_SAMPLES );
    }

}
