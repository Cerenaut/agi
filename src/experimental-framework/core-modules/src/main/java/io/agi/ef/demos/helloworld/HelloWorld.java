package io.agi.ef.demos.helloworld;

import io.agi.ef.core.actuators.LightActuator;
import io.agi.ef.core.CommsMode;
import io.agi.ef.core.network.entities.World;
import io.agi.ef.serverapi.api.ApiResponseMessage;

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

    public HelloWorld( String contextPath ) throws Exception {
        super( contextPath );
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

        // Read the agents motors and increment the position of the Agent accordingly.
        getAgentStates();

        return null;
    }

}
