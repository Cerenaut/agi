package io.agi.ef.demo.helloworld;

import io.agi.core.data.Data;
import io.agi.ef.Persistence;
import io.agi.ef.entities.experiment.Agent;
import io.agi.ef.entities.experiment.Motor;
import io.agi.ef.entities.experiment.Sensor;

import java.util.HashSet;

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

    public static final String LIGHT_SENSOR_NAME = "light-sensor";
    public static final String SIMPLE_MOTOR_NAME = "simple-motor";
//    private SimpleMotor _motor = null;
//    private LightSensor _lightSensor = null;

    public HelloAgent() {
    }

    public void configure(String config) {

        // Automatically adds two entities for sensor and actuator:
        Persistence.addEntityType(SimpleMotor.ENTITY_TYPE);
        Persistence.addEntityType(LightSensor.ENTITY_TYPE);

        LightSensor ls = new LightSensor();
        SimpleMotor sm = new SimpleMotor();

        ls.setup( LIGHT_SENSOR_NAME, LightSensor.ENTITY_TYPE, getName(), null );
        sm.setup( SIMPLE_MOTOR_NAME, SimpleMotor.ENTITY_TYPE, getName(), null );

        // Add a big datastructure to test the data viewer.
        Data d = new Data( 20,20 );
        d.setRandomNormal();
        addData("random", d); // fixed size

//        _lightSensor = new LightSensor( 1.0f );
//        addSensor( _lightSensor );
//
//        _motor = new SimpleMotor( 1.0f );
//        addActuator( _motor );
    }

    /**
     * Agent makes the motor input proportional to the sensor brightness.
     * @param dirtyData
     */
    @Override
    public void doStep( HashSet< String > dirtyData ) {
        super.doStep(dirtyData);

        setMotorInputs();

        Data d1 = GetData(LIGHT_SENSOR_NAME, Sensor.DATA_SENSED);
        float brightness = d1._values[ 0 ];

        Data d2 = GetData(SIMPLE_MOTOR_NAME, Motor.DATA_OUTPUT);
        float power = d2._values[ 0 ];

        int step = getPropertyAsInt( PROPERTY_STEP );
        System.out.println( "-------------------------------" );
        System.out.println( "HelloAgent: time: " + step );
        System.out.println( "HelloAgent: light brightness sensed: " + brightness );//_lightSensor.getBrightness() );
        System.out.println( "HelloAgent: motor power produced: " + power );//_motor.getPower() );
    }

    protected void setMotorInputs() {
//        float brightness = _lightSensor.getBrightness();
//        _motor.setInput( brightness );

        Data d1 = GetData(LIGHT_SENSOR_NAME, Sensor.DATA_SENSED);
        float brightness = d1._values[ 0 ];

        Data d2 = GetData(SIMPLE_MOTOR_NAME, Motor.DATA_INPUT);
        d2._values[ 0 ] = brightness;
        SetData( SIMPLE_MOTOR_NAME, Motor.DATA_INPUT, d2 );
    }

}
