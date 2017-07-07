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
import io.agi.framework.demo.mnist.AnalyticsEntity;
import io.agi.framework.demo.mnist.ClassificationAnalysisEntity;
import io.agi.framework.demo.mnist.ImageLabelEntity;
import io.agi.framework.demo.mnist.MnistEntity;
import io.agi.framework.demo.sd19.Text2ImageLabelEntity;
import io.agi.framework.demo.sequence.DistractedSequenceRecallEntity;
import io.agi.framework.entities.*;
import io.agi.framework.entities.reinforcement_learning.EpsilonGreedyEntity;
import io.agi.framework.entities.reinforcement_learning.GatedRecurrentMemoryEntity;
import io.agi.framework.entities.reinforcement_learning.QLearningEntity;
import io.agi.framework.entities.reinforcement_learning.TrainingScheduleEntity;
import io.agi.framework.entities.stdp.ConvolutionalSpikeEncoderEntity;
import io.agi.framework.entities.stdp.DifferenceOfGaussiansEntity;
import io.agi.framework.entities.stdp.SpikingConvolutionalNetworkEntity;
import io.agi.framework.persistence.models.ModelEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for entity factories, that creates all the default types.
 * <p/>
 * Created by dave on 12/03/16.
 */
public class CommonEntityFactory implements EntityFactory {

    private static final Logger _logger = LogManager.getLogger();

    protected Node _n;

    public CommonEntityFactory() {

    }

    public void setNode( Node n ) {
        _n = n;
    }

    public Entity create( ObjectMap objectMap, ModelEntity modelEntity ) {

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

        if( entityType.equals( VectorSeriesEntity.ENTITY_TYPE ) ) {
            return new VectorSeriesEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( RandomVectorEntity.ENTITY_TYPE ) ) {
            return new RandomVectorEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( DiscreteRandomEntity.ENTITY_TYPE ) ) {
            return new DiscreteRandomEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ClassificationResultEntity.ENTITY_TYPE ) ) {
            return new ClassificationResultEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( FeatureLabelsCorrelationEntity.ENTITY_TYPE ) ) {
            return new FeatureLabelsCorrelationEntity( objectMap, _n, modelEntity );
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

//        if( entityType.equals( ConsensusRegionLayerEntity.ENTITY_TYPE ) ) {
//            return new ConsensusRegionLayerEntity( objectMap, _n, modelEntity );
//        }
//
//        if( entityType.equals( SpikingRegionLayerEntity.ENTITY_TYPE ) ) {
//            return new SpikingRegionLayerEntity( objectMap, _n, modelEntity );
//        }

//        if( entityType.equals( HqClRegionLayerEntity.ENTITY_TYPE ) ) {
//            return new HqClRegionLayerEntity( objectMap, _n, modelEntity );
//        }

        if( entityType.equals( ConstantMatrixEntity.ENTITY_TYPE ) ) {
            return new ConstantMatrixEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ConfigProductEntity.ENTITY_TYPE ) ) {
            return new ConfigProductEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( SpikeEncoderEntity.ENTITY_TYPE ) ) {
            return new SpikeEncoderEntity( objectMap, _n, modelEntity );
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

        if( entityType.equals( ImageLabelEntity.ENTITY_TYPE ) ) {
            return new ImageLabelEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( Text2ImageLabelEntity.ENTITY_TYPE ) ) {
            return new Text2ImageLabelEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( KSparseAutoencoderEntity.ENTITY_TYPE ) ) {
            return new KSparseAutoencoderEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( OnlineKSparseAutoencoderEntity.ENTITY_TYPE ) ) {
            return new OnlineKSparseAutoencoderEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( CompetitiveKSparseAutoencoderEntity.ENTITY_TYPE ) ) {
            return new CompetitiveKSparseAutoencoderEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( QuiltedCompetitiveLearningEntity.ENTITY_TYPE ) ) {
            return new QuiltedCompetitiveLearningEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( PyramidRegionLayerEntity.ENTITY_TYPE ) ) {
            return new PyramidRegionLayerEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( PredictiveCodingEntity.ENTITY_TYPE ) ) {
            return new PredictiveCodingEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( FeedForwardNetworkQuiltPredictorEntity.ENTITY_TYPE ) ) {
            return new FeedForwardNetworkQuiltPredictorEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( HebbianQuiltPredictorEntity.ENTITY_TYPE ) ) {
            return new HebbianQuiltPredictorEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( AnalyticsEntity.ENTITY_TYPE ) ) {
            return new AnalyticsEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( SupervisedBatchTrainingEntity.ENTITY_TYPE ) ) {
            return new SupervisedBatchTrainingEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ClassificationAnalysisEntity.ENTITY_TYPE ) ) {
            return new ClassificationAnalysisEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( FeedForwardNetworkEntity.ENTITY_TYPE ) ) {
            return new FeedForwardNetworkEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( DataQueueEntity.ENTITY_TYPE ) ) {
            return new DataQueueEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( ConvolutionalSpikeEncoderEntity.ENTITY_TYPE ) ) {
            return new ConvolutionalSpikeEncoderEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( QLearningEntity.ENTITY_TYPE ) ) {
            return new QLearningEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( VectorCopyRangeEntity.ENTITY_TYPE ) ) {
            return new VectorCopyRangeEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( DifferenceOfGaussiansEntity.ENTITY_TYPE ) ) {
            return new DifferenceOfGaussiansEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( SpikingConvolutionalNetworkEntity.ENTITY_TYPE ) ) {
            return new SpikingConvolutionalNetworkEntity( objectMap, _n, modelEntity );
        }

        if( entityType.equals( TrainingScheduleEntity.ENTITY_TYPE ) ) {
            return new TrainingScheduleEntity( objectMap, _n, modelEntity );
        }
        if( entityType.equals( DistractedSequenceRecallEntity.ENTITY_TYPE ) ) {
            return new DistractedSequenceRecallEntity( objectMap, _n, modelEntity );
        }
        if( entityType.equals( EpsilonGreedyEntity.ENTITY_TYPE ) ) {
            return new EpsilonGreedyEntity( objectMap, _n, modelEntity );
        }
        if( entityType.equals( GatedRecurrentMemoryEntity.ENTITY_TYPE ) ) {
            return new GatedRecurrentMemoryEntity( objectMap, _n, modelEntity );
        }

        return null;
    }

}
