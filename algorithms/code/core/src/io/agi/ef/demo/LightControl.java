package io.agi.ef.demo;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.math.RandomInstance;
import io.agi.core.math.Unit;
import io.agi.core.orm.ObjectMap;
import io.agi.ef.Entity;
import io.agi.ef.Node;

import java.util.Collection;

/**
 * Created by dave on 20/02/16.
 */
public class LightControl extends Entity {

    public static final String ENTITY_TYPE = "light-control";
    public static final String CONTROL_OUTPUT = "light-output";
    public static final String CHANGE_PROBABILITY = "change-probability";

    public LightControl( String entityName, ObjectMap om, String type, String parent, Node n ) {
        super( entityName, om, type, parent, n );
    }

    public void getInputKeys( Collection< String > keys ) {

    }

    public void getOutputKeys( Collection< String > keys ) {
        keys.add( CONTROL_OUTPUT );
    }

    protected void doUpdateSelf() {

        //http://localhost:8080/update?entity=mySwitch&event=update
        float pChange = getPropertyFloat(CHANGE_PROBABILITY, 0.05f );

        Data output = getDataDefaultSize( CONTROL_OUTPUT, DataSize.create(1) );
//        Data output = _data.get( getKey( CONTROL_OUTPUT ) );
//
//        if( output == null ) {
//            output = new Data( 1 );
//        }
//        else {
//            DataSize ds = DataSize.create(1);
//            output.setSize(ds);
//        }

        float oldOutputValue = output._values[ 0 ];
        float newOutputValue = oldOutputValue;

        float r = (float)RandomInstance.random();

        if( r < pChange ) {
            if( newOutputValue < 0.5f ) {
                newOutputValue = 1.f;
            }
            else {
                newOutputValue = 0.f;
            }
        }

        output._values[ 0 ] = newOutputValue;

        _data.put( getKey( CONTROL_OUTPUT), output );
    }

}
