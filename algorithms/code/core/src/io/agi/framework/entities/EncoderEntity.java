package io.agi.framework.entities;

import io.agi.core.ann.unsupervised.*;
import io.agi.core.data.Data;
import io.agi.core.orm.ObjectMap;
import io.agi.core.sdr.EncoderFactory;
import io.agi.core.sdr.ScalarEncoder;
import io.agi.core.sdr.SparseDistributedEncoder;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by gideon on 26/03/2016.
 */
public class EncoderEntity extends Entity {

    public static final String ENTITY_TYPE = "encoder";

    // config
    private static final String ENCODER_TYPE = "encoder-type";
    private static final String BITS = "bits";
    private static final String DENSITY = "density";

    // data
    private static final String DATA_INPUT = "data-input";
    private static final String DATA_OUTPUT = "data-output";

    public EncoderEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputKeys( Collection< String > keys ) {
        keys.add(DATA_INPUT);
    }

    @Override
    public void getOutputKeys( Collection< String > keys, DataFlags flags ) {
        keys.add( DATA_OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return EncoderConfig.class;
    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {
        keys.add( SUFFIX_AGE );

        keys.add( ENCODER_TYPE );
        keys.add( BITS );
        keys.add( DENSITY );
    }

    public void doUpdateSelf() {

        String encoderType = getPropertyString( ENCODER_TYPE, ScalarEncoder.class.getSimpleName() );
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