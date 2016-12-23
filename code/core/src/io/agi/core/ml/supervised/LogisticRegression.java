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

import com.sun.org.apache.bcel.internal.generic.RET;
import io.agi.core.data.Data;
import io.agi.core.data.DataSize;
import io.agi.core.orm.Callback;
import io.agi.core.orm.NamedObject;
import io.agi.core.orm.ObjectMap;

import de.bwaldvogel.liblinear.*;

/**
 * Created by gideon on 23/12/16.
 */
public class LogisticRegression extends NamedObject implements Callback, Supervised {

    private LogisticRegressionConfig _config;
    private Model _model = null;

    public LogisticRegression( String name, ObjectMap om ) {
        super( name, om );
    }

    @Override
    public void call() {
        update();
    }

    public void update() {

    }

    public void setup( LogisticRegressionConfig config ) {
        this._config = config;
    }

    @Override
    public void reset() {
        _model = null;
    }

    @Override
    public int predict() {

        // sample below
        Feature[] instance = { new FeatureNode( 1, 4 ), new FeatureNode( 2, 2 ) };
        double prediction = Linear.predict( _model, instance );

        // convert input to Feature instance, then predict

        return 0;
    }

    @Override
    public void loadSavedModel() {
        // load model or use it directly
        // _model = Model.load( _model );
    }

    @Override
    public void saveModel() {

        if( _model != null ) {
//        File modelFile = new File("model");
//        _model.save(modelFile);
        } else {
            // throw exception
        }
    }

    @Override
    public void train( Data featuresMatrix, Data classTruthVector ) {

        Parameter paramaters = setupParamaters();

        Problem problem = setupProblem( featuresMatrix, classTruthVector );

        _model = Linear.train( problem, paramaters );

        saveModel();
    }

    private Problem setupProblem( Data featuresMatrix, Data classTruthVector ) {

        DataSize datasetSize = featuresMatrix._dataSize;
        int m = datasetSize.getSize( DataSize.DIMENSION_Y );        // m = number of data points
        int n = datasetSize.getSize( DataSize.DIMENSION_X );        // n = feature vector size

        Problem problem = new Problem();
        problem.l = m; // number of training examples
        problem.n = n; // number of features

        // iterate data points (vectors in the VectorSeries - each vector is a data point)
        for( int i = 0; i < n; ++i ) {

            // iterate dimensions of x (elements of the vector)
            for( int j = 0; j < m; j++ ) {

                int classTruth = getClassTruth( classTruthVector, i, j );

                double xij = featuresMatrix._values[ i * n + j ];

                if( xij == 0.f ) {
                    continue;
                }

                problem.x[ i ][ j ] = new FeatureNode( j + 1, xij );
                problem.y[ i ] = classTruth;
            }
        }

        return problem;
    }

    private Parameter setupParamaters() {
        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0;    // cost of constraints violation
        double eps = 0.01; // stopping criteria

        Parameter parameter = new Parameter( solver, C, eps );
        return parameter;
    }

    private int getClassTruth( Data classTruthVector, int i, int j ) {
        return 0;
    }

}
