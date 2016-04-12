package io.agi.framework.entities;

import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.Persistence;
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
 * <p>
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
        return ExperimentConfig.class;
    }

    protected void doUpdateSelf() {

        // Get all the parameters:
        ExperimentConfig config = ( ExperimentConfig ) _config;

        Persistence p = _n.getPersistence();

        try {
            String stringValue = Framework.GetConfig( config.terminationEntityName, config.terminationConfigPath );

            Boolean b = Boolean.valueOf( stringValue );

            if ( b != null ) {
                if ( b ) {
                    config.terminate = true;
                }
            }
        }
        catch ( Exception e ) {
            // this is ok, the experiment is just not configured to have a termination condition
        }
    }

    protected void afterUpdate() {
        super.afterUpdate();

        ExperimentConfig config = ( ExperimentConfig ) _config;

        if ( ( !config.terminate ) && ( !config.pause ) ) {
            _n.requestUpdate( getName() ); // queue another update.
        }
    }

}