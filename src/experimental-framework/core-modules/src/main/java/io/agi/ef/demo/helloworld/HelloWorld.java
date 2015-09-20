package io.agi.ef.demo.helloworld;

import io.agi.core.data.Data;
import io.agi.ef.Persistence;
import io.agi.ef.entities.experiment.Motor;
import io.agi.ef.entities.experiment.World;

import java.util.HashSet;

/**
 *
 * Simple test World that contains a single light source.
 * The light source fluctuates (according to a triangular wave profile).
 *
 * Created by gideon on 1/08/15.
 */
public class HelloWorld extends World {

    public static final String LIGHT_SOURCE_NAME = "light-source";

    public HelloWorld() {
    }

    public void configure(String config) {

        // Automatically adds a LightMotor to the World
        String lightSourceConfig = "{ max: 10 }";
        Persistence.addEntityType(LightSource.ENTITY_TYPE);
        LightSource ls = new LightSource();
        ls.setup( LIGHT_SOURCE_NAME, LightSource.ENTITY_TYPE, getName(), lightSourceConfig );

//        _light = new LightMotor( 0, 10 );
//        addActuator( _light );
    }

    @Override
    public void doStep( HashSet< String > dirtyData ) {
        super.doStep(dirtyData);

        int step = getPropertyAsInt( PROPERTY_STEP );
        Data d = GetData( LIGHT_SOURCE_NAME, LightSource.DATA_OUTPUT);
        float brightness = d._values[ 0 ];

        System.out.println( "-------------------------------" );
        System.out.println( "HelloWorld: time: " + step );
        System.out.println( "HelloWorld: light brightness: " + brightness );
    }

}
