package io.agi.core.math;

/**
 * Created by dave on 26/01/16.
 */
public class Useful {

    /**
     * A quick handy function to work out how many groups you need if you know the size of each group, and the number
     * of individual elements.
     *
     * @param total
     * @param groupSize
     * @return
     */
    public static int GetNbrGroups( int total, int groupSize ) {
        int sets = total / groupSize;
        if ( total > ( groupSize * sets ) ) {
            ++sets;
        }
        return sets;
    }
}
