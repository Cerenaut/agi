package io.agi.framework.factories;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.EntityFactory;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.models.ModelEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for entity factories, that creates all the default types.
 * <p>
 * Created by dave on 12/03/16.
 */
public class CommonEntityFactory implements EntityFactory {

    private static final Logger logger = LogManager.getLogger();

    protected Node _n;

    public CommonEntityFactory() {

    }

    public void setNode( Node n ) {
        _n = n;
    }

    public Entity create( ObjectMap om, ModelEntity me ) {

        String entityName = me.name;
        String entityType = me.type;

        if ( entityType.equals( RandomVectorEntity.ENTITY_TYPE ) ) {
            return new RandomVectorEntity( om, _n, me );
        }

        if ( entityType.equals( DiscreteRandomEntity.ENTITY_TYPE ) ) {
            return new DiscreteRandomEntity( om, _n, me );
        }

        if ( entityType.equals( DynamicSelfOrganizingMapEntity.ENTITY_TYPE ) ) {
            return new DynamicSelfOrganizingMapEntity( om, _n, me );
        }

        if ( entityType.equals( GrowingNeuralGasEntity.ENTITY_TYPE ) ) {
            return new GrowingNeuralGasEntity( om, _n, me );
        }

        if ( entityType.equals( ImageSensorEntity.ENTITY_TYPE ) ) {
            return new ImageSensorEntity( om, _n, me );
        }

        if ( entityType.equals( RegionEntity.ENTITY_TYPE ) ) {
            return new RegionEntity( om, _n, me );
        }

        if ( entityType.equals( ConstantMatrixEntity.ENTITY_TYPE ) ) {
            return new ConstantMatrixEntity( om, _n, me );
        }

        if ( entityType.equals( EncoderEntity.ENTITY_TYPE ) ) {
            return new EncoderEntity( om, _n, me );
        }

        logger.error( "Could not create an entity for " + entityName + " of type " + entityType );

        return null;
    }

}
