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

    public void doUpdateSelf() {

        EncoderConfig config = (EncoderConfig)_config;

        SparseDistributedEncoder encoder = EncoderFactory.create( config.encoderType );

        if( encoder == null ) {
            logger.error( "Could not create EncoderEntity" );
            return;
        }

        encoder.setup( config.bits, config.density );

        Data input = getData( DATA_INPUT );
        Data output = encoder.createEncodingOutput( input );
        encoder.encode( input, output );

        setData( DATA_OUTPUT, output );
    }

}