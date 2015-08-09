package io.agi.ef.demos.helloworld;

import io.agi.ef.core.network.entities.Agent;
import io.agi.ef.core.actuators.MotorActuator;
import io.agi.ef.core.sensors.LightSensor;

/**
 *
 * Simple test Agent that contains a light sensor and a motor actuator.
 *
 * Behaviour:
 *  - measure light source strength
 *  - modify motor speed based on light strength
 *
 * Created by gideon on 30/07/15.
 */
public class HelloAgent extends Agent {

    private MotorActuator _motor = null;
    private LightSensor _lightSensor = null;

    public HelloAgent() {
        super();
        setup();
    }

    public HelloAgent( String agentContextPath ) throws Exception {
        super( agentContextPath );
        setup();
    }

    void setup() {
        _lightSensor = new LightSensor( 1.0f );
        addSensor( _lightSensor );

        _motor = new MotorActuator( 1.0f );
        addActuator( _motor );
    }

    @Override
    public void stepBody() {

        System.out.println( "-------------------------------" );
        System.out.println( "HelloAgent: time: " + getTime() );
        System.out.println( "HelloAgent: light brightness sensed: " + _lightSensor.getBrightness() );
        System.out.println( "HelloAgent: motor power set to: " + _motor.getPower() );
    }

    protected void setActuatorInputs() {
        float brightness = _lightSensor.getBrightness();
        _motor.setInput( brightness );
    }
}
