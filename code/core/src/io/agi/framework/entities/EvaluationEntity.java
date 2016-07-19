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
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by gideon on 17/07/2016.
 */
public class EvaluationEntity extends Entity {

    public static final String ENTITY_TYPE = "eval-entity";
    public static final String FEATURES = "features";
    public static final String CLASSIFICATION = "classification";

    public EvaluationEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( FEATURES );
        attributes.add( CLASSIFICATION );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {

    }

    @Override
    public Class getConfigClass() {
        return EvaluationEntityConfig.class;
    }


    @Override
    protected void doUpdateSelf() {

    }
}