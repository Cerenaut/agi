/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.framework.factories;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.Entity;
import io.agi.framework.EntityFactory;
import io.agi.framework.Node;
import io.agi.framework.demo.mnist.Mnist2Entity;
import io.agi.framework.demo.mnist.MnistEntity;
import io.agi.framework.entities.*;
import io.agi.framework.persistence.models.ModelEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for entity factories, that creates all the default types.
 * <p/>
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

        if( entityType.equals( ExperimentEntity.ENTITY_TYPE ) ) {
            return new ExperimentEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ThresholdEntity.ENTITY_TYPE ) ) {
            return new ThresholdEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ValueSeriesEntity.ENTITY_TYPE ) ) {
            return new ValueSeriesEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( RandomVectorEntity.ENTITY_TYPE ) ) {
            return new RandomVectorEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( DiscreteRandomEntity.ENTITY_TYPE ) ) {
            return new DiscreteRandomEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ClassFeaturesEntity.ENTITY_TYPE ) ) {
            return new ClassFeaturesEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ParameterLessSelfOrganizingMapEntity.ENTITY_TYPE ) ) {
            return new ParameterLessSelfOrganizingMapEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( GrowingNeuralGasEntity.ENTITY_TYPE ) ) {
            return new GrowingNeuralGasEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( PlasticNeuralGasEntity.ENTITY_TYPE ) ) {
            return new PlasticNeuralGasEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ImageSensorEntity.ENTITY_TYPE ) ) {
            return new ImageSensorEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( RegionEntity.ENTITY_TYPE ) ) {
            return new RegionEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( RegionLayerEntity.ENTITY_TYPE ) ) {
            return new RegionLayerEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( AutoRegionLayerEntity.ENTITY_TYPE ) ) {
            return new AutoRegionLayerEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ConstantMatrixEntity.ENTITY_TYPE ) ) {
            return new ConstantMatrixEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( EncoderEntity.ENTITY_TYPE ) ) {
            return new EncoderEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( DecoderEntity.ENTITY_TYPE ) ) {
            return new DecoderEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( MnistEntity.ENTITY_TYPE ) ) {
            return new MnistEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( Mnist2Entity.ENTITY_TYPE ) ) {
            return new Mnist2Entity( objectMap, _n, modelEntity );
        }

        return null;
    }

}
