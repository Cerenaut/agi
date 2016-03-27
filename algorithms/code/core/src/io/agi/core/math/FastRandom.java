package io.agi.core.math;

/**
 * http://www.javamex.com/tutorials/random_numbers/generators_overview.shtml
 * "a technique was invented by mathematician George Marsaglia that can generate medium-quality random numbers extremely
 * quickly, and using only a single variable as its state. The technique is generally known as an XORShift generator and
 * is so simple that the code to generate numbers can often be inlined where you need the numbers generated. The
 * technique is especially useful when you need to generate a large quantity of random numbers in a tight loop, or
 * simply where you need a generator that is simple to code but with a slightly better quality and period than
 * java.util.Random."
 *
 * http://www.javamex.com/tutorials/random_numbers/xorshift.shtml
 * https://docs.oracle.com/javase/7/docs/api/java/util/Random.html
 *
 * Created by dave on 27/03/16.
 */
public class FastRandom {
    // TODO. It takes a significant amount of time to generate all the random numbers needed.
}
