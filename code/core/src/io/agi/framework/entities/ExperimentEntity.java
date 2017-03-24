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

package io.agi.framework.entities;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Features:
 * - Name: Each experiment should have a unique name
 * - Update: updates all child entities.
 * - Objective/Cost function: Defines some function of performance or quality
 * - Termination Condition: Defines some condition on which update should stop
 * - Scheduler: Defines how many updates should be run.
 * - Import/Export: Allow import and export of complete subtree to disk.
 * - Logging: Log variables of interest as they change over time.
 * - Flush: All children can be flushed from the Node Cache on demand, or on export.
 * <p/>
 * Created by gideon on 20/03/2016.
 */
public class ExperimentEntity extends Entity {

    public static final String ENTITY_TYPE = "experiment";

    public ExperimentEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {

    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

    }

    @Override
    public Class getConfigClass() {
        return ExperimentEntityConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        ExperimentEntityConfig config = ( ExperimentEntityConfig ) _config;

        _logger.debug( "Experiment: " + getName() + " age: " + _config.age + " terminationAge: " + ( (ExperimentEntityConfig) _config ).terminationAge );

        if( config.terminating ) {
            config.terminated = true; // it already updated after starting to terminate.
        }

        // reset if age == 0
        if( config.age == 0 ) {
            _logger.info( "Experiment: " + getName() + " enabling reset because age is 0." );
            config.reset = true; // will be turned off automatically
        }

        // see if external condition says terminate
        try {

            String stringValue = Framework.GetConfig( config.terminationEntityName, config.terminationConfigPath );

            Boolean b = Boolean.valueOf( stringValue );

            if( b != null ) {
                if( b ) {
                    _logger.info( "Experiment: " + getName() + " terminating due to external condition." );
                    config.terminate = true;
                }
// don't force un-terminate...
//                else {
//                    config.terminate = false;
//                }
            }
        }
        catch( Exception e ) {
            // this is ok, the experiment is just not configured to have a termination condition
        }


        // see if we should terminate based on age.
        if( config.terminationAge >= 0 ) {
            if( config.age >= config.terminationAge ) {
                _logger.info( "Experiment: " + getName() + " terminating due age (" + config.age + ") greater than threshold (" + config.terminationAge + ")." );
                config.terminate = true;
            }
        }

        // flush if we will be terminating.
        if( config.terminate ) {
            _logger.info( "Experiment: " + getName() + " enabling flush due to imminent termination." );
            config.flush = true; // ensure data is flushed. Since this is the end of the experiment, we don't need to disable flush again
        }

        // if reset, then undo any termination:
        if( config.reset ) {
            _logger.info( "Experiment: " + getName() + " undoing termination condition due to reset event." );
            config.terminate = false;
            config.terminating = false;
            config.terminated = false;
        }

        if( config.terminate ) {
            config.terminating = true;
        }
    }

    protected void afterUpdate() {
        super.afterUpdate();

        ExperimentEntityConfig config = ( ExperimentEntityConfig ) _config;

        if( ( !config.terminated ) && ( !config.pause ) ) {
            _logger.debug( "Experiment: " + getName() + " requesting another update." );
            _n.requestUpdate( getName() ); // queue another update.
        }
    }

}
