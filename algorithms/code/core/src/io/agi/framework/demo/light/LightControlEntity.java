package io.agi.framework.demo.light;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.RandomInstance;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.Node;

import java.util.Collection;

/**
 * Created by dave on 20/02/16.
 */
public class LightControlEntity extends Entity {

    public static final String ENTITY_TYPE = "light-control";
    public static final String CONTROL_OUTPUT = "light-output";
    public static final String CHANGE_PROBABILITY = "change-probability";

    public LightControlEntity( String entityName, ObjectMap om, String type, Node n ) {
        super( entityName, om, type, n );
    }

    public void getInputKeys( Collection< String > keys ) {

    }

    public void getOutputKeys( Collection< String > keys ) {
        keys.add( CONTROL_OUTPUT );
    }

    @Override
    public void getPropertyKeys( Collection< String > keys ) {

    }

    protected void doUpdateSelf() {

        //http://localhost:8080/update?entity=mySwitch&event=update
        float pChange = getPropertyFloat( CHANGE_PROBABILITY, 0.05f );

        Data output = getData( CONTROL_OUTPUT, DataSize.create( 1 ) );

        float oldOutputValue = output._values[ 0 ];
        float newOutputValue = oldOutputValue;

        float r = ( float ) RandomInstance.random();

        if ( r < pChange ) {
            System.out.println( "changing output" );
            if ( newOutputValue < 0.5f ) {
                newOutputValue = 1.f;
            }
            else {
                newOutputValue = 0.f;
            }
        }

        output._values[ 0 ] = newOutputValue;

        setData( CONTROL_OUTPUT, output );
    }

}
