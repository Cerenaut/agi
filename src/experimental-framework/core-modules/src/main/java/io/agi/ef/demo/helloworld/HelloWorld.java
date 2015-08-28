package io.agi.ef.demo.helloworld;

import io.agi.ef.experiment.actuators.LightActuator;
import io.agi.ef.experiment.entities.World;

import javax.ws.rs.core.Response;

/**
 *
 * Simple test World that contains a single light source.
 * The light source fluctuates (according to a triangular wave profile).
 *
 * Created by gideon on 1/08/15.
 */
public class HelloWorld extends World {

    private LightActuator _light = null;

    public HelloWorld() {
        super();
        setup();
    }

    private void setup() {
        _light = new LightActuator( 0, 10 );
        addActuator( _light );
    }

    public Response step() {
        super.step();

        System.out.println( "-------------------------------" );
        System.out.println( "HelloWorld: time: " + getTime() );
        System.out.println( "HelloWorld: light brightness: " + _light.getBrightness() );

        return null;
    }

}
