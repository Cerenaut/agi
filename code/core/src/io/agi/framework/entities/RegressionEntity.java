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
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * This is a 'learning analytics' Entity
 * These Entities have two main phases, 'learn' on and off
 * <p>
 * In Learn=on (Training) phase - it simply collects the data that it needs (via a VectorSeriesEntity that is an Input)
 * In Learn=off (Testing) phase - train SVM if not already trained, and give predictions (i.e. only train once then predict)
 * <p>
 * <p>
 * <p>
 * Created by gideon on 11/07/2016.
 */
public class RegressionEntity extends SupervisedLearningEntity {

    public static final String ENTITY_TYPE = "regression-entity";

    public RegressionEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    @Override
    public void getInputAttributes( Collection< String > attributes ) {
        super.getInputAttributes( attributes );
    }

    @Override
    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
        super.getOutputAttributes( attributes, flags );
    }

    @Override
    public Class getConfigClass() {
        return RegressionEntityConfig.class;
    }



}
