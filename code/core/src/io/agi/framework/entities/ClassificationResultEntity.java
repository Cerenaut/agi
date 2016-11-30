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

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.DataFlags;
import io.agi.framework.Entity;
import io.agi.framework.Framework;
import io.agi.framework.Node;
import io.agi.framework.persistence.models.ModelEntity;

import java.util.Collection;

/**
 * Created by dave on 3/11/16.
 */
public class ClassificationResultEntity extends Entity {

    public static final String ENTITY_TYPE = "classification-result";

    public static final String INPUT_LABEL = "input-label";
    public static final String INPUT_CLASS = "input-class";

    public ClassificationResultEntity( ObjectMap om, Node n, ModelEntity model ) {
        super( om, n, model );
    }

    public void getInputAttributes( Collection< String > attributes ) {
        attributes.add( INPUT_LABEL );
        attributes.add( INPUT_CLASS );
    }

    public void getOutputAttributes( Collection< String > attributes, DataFlags flags ) {
    }

    public Class getConfigClass() {
        return ClassificationResultEntityConfig.class;
    }

    @Override
    protected void doUpdateSelf() {

        ClassificationResultEntityConfig config = ( ClassificationResultEntityConfig ) _config;

        Data labelData = getData( INPUT_LABEL );
        if( labelData == null ) {
            return;
        }

        Data classData = getData( INPUT_CLASS );
        if( classData == null ) {
            return;
        }

        // calculate the result
        int labelValue = (int)labelData._values[ 0 ];
        int classValue = (int)classData._values[ 0 ];
        int errorValue = 0;
        if( labelValue != classValue ) {
            errorValue = 1;
        }

        // update the config based on the result:
        config.classPredicted = classValue; // the predicted class given the input features
        config.classError = errorValue; // 1 if the prediction didn't match the input class
        config.classTruth = labelValue; // the value that was taken as input
    }

}
