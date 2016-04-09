package io.agi.framework.demo.light;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 20/02/16.
 */
public class LightControlEntity extends Entity {

    public static final String ENTITY_TYPE = "light-control";

    public static final String CONTROL_OUTPUT = "light-output";

    public LightControlEntity( ObjectMap om, Node n, ModelEntity me ) {
        super( om, n, me );
    }

    public void getInputAttributes( Collection< String > attributes ) {

    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        attributes.add( CONTROL_OUTPUT );
    }

    @Override
    public Class getConfigClass() {
        return LightControlEntityConfig.class;
    }

    protected void doUpdateSelf() {

        //http://localhost:8080/update?entity=mySwitch&event=update
        LightControlEntityConfig config = ( LightControlEntityConfig ) _config;

        Data output = getData( CONTROL_OUTPUT, DataSize.create( 1 ) );

        float oldOutputValue = output._values[ 0 ];
        float newOutputValue = oldOutputValue;

        float r = getRandom().nextFloat();

        if ( r < config.changeProbability ) {
            //System.out.println( "changing output" );
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
