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

package io.agi.core.ann.supervised;

/**
 * Allows you to derive this part independently, to add extra functions.
 * <p/>
 * Created by dave on 3/01/16.
 */
public class ActivationFunctionFactory {

    public static final String LOG_SIGMOID = "log-sigmoid";
    public static final String TAN_H = "tan-h";
    public static final String SOFTMAX = "softmax";

    public TransferFunction create( String function ) {
        if( function.equals( LOG_SIGMOID ) ) {
            return TransferFunction.createLogisticSigmoid();
        }
        if( function.equals( TAN_H ) ) {
            return TransferFunction.createTanh();
        }
        if( function.equals( SOFTMAX ) ) {
            return TransferFunction.createSoftmax();
        }
        return null;
    }

}
