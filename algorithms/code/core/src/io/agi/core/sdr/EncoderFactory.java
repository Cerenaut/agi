package io.agi.core.sdr;

import io.agi.framework.entities.RandomVectorEntity;

/**
 * Created by gideon on 26/03/2016.
 */
public class EncoderFactory {

    public static SparseDistributedEncoder create( String encoderType ) {

        if ( encoderType.equals( ScalarEncoder.class.getSimpleName() ) ) {
            return new ScalarEncoder();
        }

        System.err.println( "ERROR: EncoderFactory.create() - could not create an encoder for " + encoderType );

        return null;
    }
}
