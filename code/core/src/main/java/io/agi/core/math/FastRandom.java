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

package io.agi.core.math;

import java.util.Random;

/**
 * Fast and quite pseudo-random (but not cryptographically random) number generator, based on the XorShift algorithm.
 * <p/>
 * http://www.javamex.com/tutorials/random_numbers/generators_overview.shtml
 * "a technique was invented by mathematician George Marsaglia that can generate medium-quality random numbers extremely
 * quickly, and using only a single variable as its state. The technique is generally known as an XORShift generator and
 * is so simple that the code to generate numbers can often be inlined where you need the numbers generated. The
 * technique is especially useful when you need to generate a large quantity of random numbers in a tight loop, or
 * simply where you need a generator that is simple to code but with a slightly better quality and period than
 * java.util.Random."
 * <p/>
 * http://www.javamex.com/tutorials/random_numbers/xorshift.shtml
 * https://docs.oracle.com/javase/7/docs/api/java/util/Random.html
 * <p/>
 * Created by dave on 27/03/16.
 */
public class FastRandom extends Random {

    protected long _seed;

    public FastRandom() {
    }

    public FastRandom( long seed ) {
        this._seed = seed;
    }

    public long getSeed() {
        return _seed;
    }

    public void setSeed( long seed ) {
        _seed = seed;
    }

    /**
     * Since all methods of the Random generator (nextBoolean(), nextInt(), nextLong(), nextFloat(), nextDouble()),
     * nextBytes(), nextGaussian()) depend on the next() method, this efficiently changes all number generation to the
     * Xorshift algorithm.
     * -- http://demesos.blogspot.com.au/2011/09/replacing-java-random-generator.html
     *
     * @param nbits
     * @return
     */
    protected int next( int nbits ) {
        long x = _seed;

        x ^= ( x << 21 );
        x ^= ( x >>> 35 );
        x ^= ( x << 4 );

        _seed = x;

        x &= ( ( 1L << nbits ) - 1 );

        return ( int ) x;
    }
}