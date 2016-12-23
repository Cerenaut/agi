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
import libsvm.*;

/**
 * Created by gideon on 14/12/16.
 */
public class Svm extends NamedObject implements Callback, Supervised {

    private SvmConfig _config;
    svm_model _model = null;

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
        // svm.svm_save_model(  );

    }

    public void loadSavedModel() {

        // to implement
        // _model = svm.svm_load_model();

    }

    @Override
    public void saveModel() {
        // to implement
    }

    public int predict() {

        // to implement

        return 0;
    }

    public void train( Data featuresMatrix, Data classTruthVector ) {

        svm_parameter paramaters = setupParamaters();

        svm_problem problem = setupProblem( featuresMatrix, classTruthVector );

        _model = svm.svm_train( problem, paramaters );

        saveModel();
    }

    private svm_problem setupProblem( Data featuresMatrix, Data classTruthVector ) {

        DataSize datasetSize = featuresMatrix._dataSize;
        int m = datasetSize.getSize( DataSize.DIMENSION_Y );        // m = number of data points
        int n = datasetSize.getSize( DataSize.DIMENSION_X );        // n = feature vector size

        svm_problem prob = new svm_problem();
        prob.l = m;
        prob.y = new double[ prob.l ];
        prob.x = new svm_node[ prob.l ][ n ];

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for ( int i = 0; i < m ; ++i ) {

            // iterate dimensions of x (elements of the vector)
            for ( int j = 0 ; j < n ; j++ ) {

                int classTruth = getClassTruth( classTruthVector, i, j );

                float xij = featuresMatrix._values[ i * m + j];

                if ( xij == 0.f ) {
                    continue;
                }

                prob.x[ i ][ j ] = new svm_node();
                prob.x[ i ][ j ].index = j+1;
                prob.x[ i ][ j ].value = xij;
                prob.y[ i ] = classTruth;
            }
        }

        return prob;
    }

    private svm_parameter setupParamaters() {
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

        return param;
    }

    private int getClassTruth( Data classTruthVector, int i, int j ) {
        return 0;
    }

}
