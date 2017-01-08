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

package io.agi.framework.demo.mnist;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * A generic setup for an analytics layer in the hierarchy.
 * The analytics layer evaluates the performance of the core algorithm.
 * e.g. Supervised Learning algorithm to evaluate the performance of an unsupervised algorithm
 * on a dataset for which we have labels.
 *
 * We have one feature series and one label series which is segmented into a
 * - training dataset, and a
 * - testing dataset
 *
 * We perform 1 pass over the training dataset to train any subscribed 'testing entities'
 *
 * At each step, one 'feature' is emitted (as a Data structure) and one class label (as a Data structure).
 * ----> or rather tell the subscribed entity to take another one from its input (so it can get it directly from the source)
 *
 * This entity can terminate an experiment when the test dataset are processed.
 *
 * Created by gideon on 08/01/17.
 */
public class AnalyticsEntity extends Entity {

    public static final String ENTITY_TYPE = "analytics";

    public AnalyticsEntity( ObjectMap om, Node n, ModelEntity model ) {
        super(om, n, model);
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
    }

    @Override
    public Class getConfigClass() {
        return AnalyticsEntityConfig.class;
    }

    public void doUpdateSelf() {

        // Check for a reset (to start of sequence and re-train)
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig ) _config;

        if( config.reset ) {
            config.terminate = false;
            config.phase = AnalyticsEntityConfig.PHASE_TRAINING;
            config.count = 0;
        }

        increment();

        // inform subscribed testing entities, whether they are in 'learn' or 'test' mode
        try {
            Collection< String > entityNames = getEntityNames( config.testingEntities );
            for( String entityName : entityNames ) {
                Framework.SetConfig( entityName, "learn", String.valueOf( isTraining() ) );
            }
        }
        catch( Exception e ) {
        } // this is ok, the experiment is just not configured to have a learning flag

    }

    /**
     * Perform one iteration, calculate the phase we should be in, and set the correct phase.
     * At the appropriate point, transition from 'training' to 'testing', and from 'testing' to 'terminate'
     *
     * @return
     */
    public void increment() {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig )_config;

        _logger.warn( "=======> Phase: " + AnalyticsEntityConfig.PHASE_TESTING + ", index: " + config.count );

        if ( isTraining() ) {
            if ( config.count >= config.trainSetSize ) {
                config.phase = AnalyticsEntityConfig.PHASE_TESTING;
                _logger.warn( "=======> Transition to test phase. (1)" );
            }
        }
        else {
            if ( config.count >= config.testSetSize ) {
                config.phase = null;
                config.terminate = true;
                _logger.warn( "=======> Terminating on end of test set (2)" );
            }
        }

        config.count++;
    }

    /**
     * Can only be in one of two phases, so return true if training, false if testing.
     * This is a pure getter, it does not calculate the phase that we should be in.
     * @return true if training, false if testing.
     */
    public boolean isTraining() {
        AnalyticsEntityConfig config = ( AnalyticsEntityConfig )_config;
        if ( config.phase.equals( AnalyticsEntityConfig.PHASE_TRAINING ) ) {
            return true;
        }
        else {
            return false;
        }
    }

}
