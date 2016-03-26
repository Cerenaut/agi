package io.agi.framework.entities;

import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.core.sdr.EncoderFactory;
import io.agi.core.sdr.SparseDistributedEncoder;
import io.agi.framework.Entity;
import io.agi.framework.Node;

import java.util.Collection;

/**
 * Created by gideon on 26/03/2016.
 */
public class EncoderEntity extends Entity {

    public static final String ENTITY_TYPE = "encoder";

    // properties
    private static final String ENCODER_TYPE = "encoder-type";
    private static final String BITS = "bits";
    private static final String DENSITY = "density";

    // data
    private static final String DATA_INPUT = "data-input";
    private static final String DATA_OUTPUT = "data-output";


    public EncoderEntity( String name, ObjectMap om, String type, Node n ) {
        super( name, om, type, n );
    }

    @Override
    public void getInputKeys( Collection< String > keys ) {
        keys.add( DATA_INPUT );
    }

    @Override
    public void getOutputKeys( Collection< String > keys ) {
        keys.add( DATA_OUTPUT );
    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {
        keys.add( ENCODER_TYPE );
        keys.add( BITS );
        keys.add( DENSITY );
    }

    public void doUpdateSelf() {

        String encoderType = getPropertyString( ENCODER_TYPE, "ScalarEncoder" );
        int bits = getPropertyInt( BITS, 1 );
        int density = getPropertyInt( DENSITY, 1 );

        SparseDistributedEncoder encoder = EncoderFactory.create( encoderType );

        if ( encoder == null ) {
            System.err.println( "Could not create EncoderEntity" );
            return;
        }

        encoder.setup( bits, density );

        Data input = getData( DATA_INPUT );
        Data output = encoder.createEncodingOutput( input );
        encoder.encode( input, output );

        setData( DATA_OUTPUT, output );
    }

}