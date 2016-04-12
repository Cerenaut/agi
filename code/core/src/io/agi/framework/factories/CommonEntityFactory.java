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

    public Entity create( ObjectMap objectMap, ModelEntity modelEntity ) {

        String entityName = modelEntity.name;
        String entityType = modelEntity.type;

        if ( entityType.equals( ExperimentEntity.ENTITY_TYPE ) ) {
            return new ExperimentEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( ThresholdEntity.ENTITY_TYPE ) ) {
            return new ThresholdEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( ValueSeriesEntity.ENTITY_TYPE ) ) {
            return new ValueSeriesEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( RandomVectorEntity.ENTITY_TYPE ) ) {
            return new RandomVectorEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( DiscreteRandomEntity.ENTITY_TYPE ) ) {
            return new DiscreteRandomEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( DynamicSelfOrganizingMapEntity.ENTITY_TYPE ) ) {
            return new DynamicSelfOrganizingMapEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( GrowingNeuralGasEntity.ENTITY_TYPE ) ) {
            return new GrowingNeuralGasEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( ImageSensorEntity.ENTITY_TYPE ) ) {
            return new ImageSensorEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( RegionEntity.ENTITY_TYPE ) ) {
            return new RegionEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( ConstantMatrixEntity.ENTITY_TYPE ) ) {
            return new ConstantMatrixEntity( objectMap, _n, modelEntity );
        }

        if ( entityType.equals( EncoderEntity.ENTITY_TYPE ) ) {
            return new EncoderEntity( objectMap, _n, modelEntity );
        }

        return null;
    }

}
