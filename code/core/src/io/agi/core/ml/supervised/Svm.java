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

package io.agi.core.ml.supervised;

import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.Callback;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;
import io.agi.framework.entities.SVMEntityConfig;
import libsvm.*;

/**
 * Created by gideon on 14/12/16.
 */
public class Svm extends NamedObject implements Callback, Supervised {

    private SvmConfig _config;

    public Svm( String name, ObjectMap om ) {
        super( name, om );
    }

    @Override
    public void call() {
        update();
    }

    public void update() {

    }

    public void setup( SvmConfig config ) {
        this._config = config;
    }

    public void reset() {

        // to implement

    }

    public void loadSavedModel() {

        // to implement

    }

    public int predict() {

        // to implement

        return 0;
    }

    public void train( Data featuresMatrix, Data classTruthVector ) {

        svm_parameter param = new svm_parameter();

        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0;
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 40;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[ 0 ];
        param.weight = new double[ 0 ];

        // values from config
        param.C = _config.getRegularisation();

        DataSize datasetSize = featuresMatrix._dataSize;

        int n = datasetSize.getSize( DataSize.DIMENSION_Y );        // n = number of data points
        int m = datasetSize.getSize( DataSize.DIMENSION_X );        // m = feature vector size

        // build problem
        svm_problem prob = new svm_problem();
        prob.l = n;
        prob.y = new double[ prob.l ];
        prob.x = new svm_node[ prob.l ][ m ];

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for ( int i = 0; i < n ; ++i ) {

            // iterate dimensions of x (elements of the vector)
            for ( int j = 0 ; j < m ; j++) {

                int classTruth = 0;
                // ****** GET IT FROM ClassTruthVector ********
                // To Implement

                float xij = featuresMatrix._values[ i * n + j];

                if ( xij == 0.f ) {
                    continue;
                }

                prob.x[ i ][ j ] = new svm_node();
                prob.x[ i ][ j ].index = j+1;
                prob.x[ i ][ j ].value = xij;
                prob.y[ i ] = classTruth;
            }
        }

        // build model
        svm_model model = svm.svm_train( prob, param );

        // save model
        // to implement
    }

}
