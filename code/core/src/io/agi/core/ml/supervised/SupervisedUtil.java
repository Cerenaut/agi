/*
 * Copyright (c) 2017.
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

package io.agi.core.ml.supervised;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;

/**
 *
 * FeaturesMatrices are n x m :
 *              rows (n) = data points, cols (m) = features
 *              They are stored in row major format.
 *
 * Created by gideon on 1/1/17.
 */
public class SupervisedUtil {

    // convenience method to get the specific value from featuresMatrix
    public static double getFeatureValue( Data featuresMatrix, int numFeatures, int datapointIndex, int featureIndex ) {
        float value = featuresMatrix._values[ datapointIndex * numFeatures + featureIndex ];
        return value;
    }

    // convenience method to get the truth label from classTruthVector
    public static float getClassTruth( Data classTruthVector, int datapointIndex ) {
        float value = classTruthVector._values[ datapointIndex ];
        return value;
    }

    // m = number of data points
    public static int calcMFromFeatureMatrix( Data featuresMatrix ) {
        DataSize datasetSize = featuresMatrix._dataSize;
        int m = datasetSize.getSize( DataSize.DIMENSION_Y );
        return m;
    }

    // n = feature vector size
    public static int calcNFromFeatureMatrix( Data featuresMatrix ) {
        DataSize datasetSize = featuresMatrix._dataSize;
        int n = datasetSize.getSize( DataSize.DIMENSION_X );
        return n;
    }
}
