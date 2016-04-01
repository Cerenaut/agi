package io.agi.framework.entities;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.EntityFactory;
import io.agi.framework.Node;

/**
 * Base class for entity factories, that creates all the default types.
 *
 * Created by dave on 12/03/16.
 */
public class CommonEntityFactory implements EntityFactory {

    protected Node _n;

    public CommonEntityFactory() {

    }

    public void setNode( Node n ) {
        _n = n;
    }

    public Entity create( ObjectMap om, String entityName, String entityType ) {

        if ( entityType.equals( RandomVectorEntity.ENTITY_TYPE ) ) {
            return new RandomVectorEntity( entityName, om, RandomVectorEntity.ENTITY_TYPE, _n );
        }

        if ( entityType.equals( DiscreteRandomEntity.ENTITY_TYPE ) ) {
            return new DiscreteRandomEntity( entityName, om, DiscreteRandomEntity.ENTITY_TYPE, _n );
        }

        if ( entityType.equals( DynamicSelfOrganizingMapEntity.ENTITY_TYPE ) ) {
            return new DynamicSelfOrganizingMapEntity( entityName, om, DynamicSelfOrganizingMapEntity.ENTITY_TYPE, _n );
        }

        if ( entityType.equals( GrowingNeuralGasEntity.ENTITY_TYPE ) ) {
            return new GrowingNeuralGasEntity( entityName, om, GrowingNeuralGasEntity.ENTITY_TYPE, _n );
        }

        if ( entityType.equals( ImageSensorEntity.ENTITY_TYPE ) ) {
            return new ImageSensorEntity( entityName, om, ImageSensorEntity.ENTITY_TYPE, _n );
        }

        if( entityType.equals( RegionEntity.ENTITY_TYPE ) ) {
            return new RegionEntity( entityName, om, RegionEntity.ENTITY_TYPE, _n );
        }

        if( entityType.equals( ConstantMatrixEntity.ENTITY_TYPE ) ) {
            return new ConstantMatrixEntity( entityName, om, ConstantMatrixEntity.ENTITY_TYPE, _n );
        }

        if ( entityType.equals( EncoderEntity.ENTITY_TYPE ) ) {
            return new EncoderEntity( entityName, om, EncoderEntity.ENTITY_TYPE, _n );
        }

        System.out.println( "ERROR: CommonEntityFactory.create() - could not create an entity for " + entityName + " of type " + entityType );

        return null;
    }

}
