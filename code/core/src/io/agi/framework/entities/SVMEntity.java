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
import libsvm.*;

import java.util.Collection;
import java.util.Vector;

/**
 * Created by gideon on 11/07/2016.
 */
public class SVMEntity extends Entity {

    public static final String ENTITY_TYPE = "svm-entity";

    public SVMEntity( ObjectMap om, Node n, ModelEntity model ) {
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
        return SVMEntityConfig.class;
    }

    class point {
        point(double x, double y, byte value)
        {
            this.x = x;
            this.y = y;
            this.value = value;
        }
        double x, y;
        byte value;
    }


    @Override
    protected void doUpdateSelf() {

        Vector<point> point_list = new Vector<point>();

        svm_parameter param = new svm_parameter();

        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0;
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 40;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];

        // build problem
        svm_problem prob = new svm_problem();
        prob.l = point_list.size();
        prob.y = new double[prob.l];

        if(param.kernel_type == svm_parameter.PRECOMPUTED)
        {
        }
        else if(param.svm_type == svm_parameter.EPSILON_SVR ||
                param.svm_type == svm_parameter.NU_SVR)
        {
            if(param.gamma == 0)
                param.gamma = 1;
            prob.x = new svm_node[prob.l][1];
            for(int i=0;i<prob.l;i++)
            {
                point p = point_list.elementAt(i);
                prob.x[i][0] = new svm_node();
                prob.x[i][0].index = 1;
                prob.x[i][0].value = p.x;
                prob.y[i] = p.y;
            }

            // build model & classify
            svm_model model = svm.svm_train(prob, param);
            svm_node[] x = new svm_node[1];
            x[0] = new svm_node();
            x[0].index = 1;


        }
        else
        {
            if(param.gamma == 0) param.gamma = 0.5;
            prob.x = new svm_node [prob.l][2];
            for(int i=0;i<prob.l;i++)
            {
                point p = point_list.elementAt(i);
                prob.x[i][0] = new svm_node();
                prob.x[i][0].index = 1;
                prob.x[i][0].value = p.x;
                prob.x[i][1] = new svm_node();
                prob.x[i][1].index = 2;
                prob.x[i][1].value = p.y;
                prob.y[i] = p.value;
            }

            // build model & classify
            svm_model model = svm.svm_train(prob, param);
            svm_node[] x = new svm_node[2];
            x[0] = new svm_node();
            x[1] = new svm_node();
            x[0].index = 1;
            x[1].index = 2;

        }




    }
}
