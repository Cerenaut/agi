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

package io.agi.framework.demo.classifier;

import io.agi.core.util.PropertiesUtil;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Main;
import io.agi.framework.Node;
import io.agi.framework.entities.*;
import io.agi.framework.factories.CommonEntityFactory;
import io.agi.framework.persistence.PersistenceUtil;
import io.agi.framework.references.DataRefUtil;

import java.util.Properties;

/**
 * Code to demonstrate a DSOM Entity on a simple test problem.
 * <p/>
 * Created by dave on 12/03/16.
 */
public class ClassifierDemo {

    public static void main( String[] args ) {

        // Provide classes for entities
        CommonEntityFactory ef = new CommonEntityFactory();

        // Create a Node
        Main m = new Main();
        Properties p = PropertiesUtil.load( args[ 0 ] );
        m.setup( p, null, ef );

        // Create custom entities and references
        if( args.length > 1 ) {
            if( args[ 1 ].equalsIgnoreCase( "create" ) ) {
                // Programmatic hook to create entities and references..
                createEntities( m._n );
            }
        }

        // Start the system
        m.run();
    }

    public static void createEntities( Node n ) {

        // Define some entities
        String modelName = "model";
        String classifierName = "classifier";

        String experimentName = "experiment";
        PersistenceUtil.CreateEntity( experimentName, ExperimentEntity.ENTITY_TYPE, n.getName(), null ); // experiment is the root entity

//        boolean discrete = false;
        boolean discrete = true;

        if( discrete ) {
            PersistenceUtil.CreateEntity( modelName, DiscreteRandomEntity.ENTITY_TYPE, n.getName(), experimentName );
        }
        else {
            PersistenceUtil.CreateEntity( modelName, RandomVectorEntity.ENTITY_TYPE, n.getName(), experimentName );
            PersistenceUtil.SetConfig( modelName, "elements", "2" );
        }
        PersistenceUtil.CreateEntity( classifierName, GrowingNeuralGasEntity.ENTITY_TYPE, n.getName(), modelName );
//        PersistenceUtil.CreateEntity( classifierName, ParameterLessSelfOrganizingMapEntity.ENTITY_TYPE, n.getName(), modelName );
//        PersistenceUtil.CreateEntity( classifierName, PlasticNeuralGasEntity.ENTITY_TYPE, n.getName(), modelName );

        DataRefUtil.SetDataReference( classifierName, ParameterLessSelfOrganizingMapEntity.INPUT, modelName, RandomVectorEntity.OUTPUT );

        boolean terminateByAge = true;
        int terminationAge = 20;

        // Experiment config
        if( !terminateByAge ) {
            PersistenceUtil.SetConfig( experimentName, "terminationEntityName", modelName );
            PersistenceUtil.SetConfig( experimentName, "terminationConfigPath", "terminate" );
            PersistenceUtil.SetConfig( experimentName, "terminationAge", "-1" ); // wait for mnist to decide
        }
        else {
            PersistenceUtil.SetConfig( experimentName, "terminationAge", String.valueOf( terminationAge ) ); // fixed steps
        }



        // Set a property:
        PersistenceUtil.SetConfig( modelName, "elements", "2" );
        PersistenceUtil.SetConfig( classifierName, Entity.SUFFIX_RESET, "true" );
    }
}
