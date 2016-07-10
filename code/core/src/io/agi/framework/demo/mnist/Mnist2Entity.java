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

package io.agi.framework.demo.mnist;

/**
 * Created by dave on 8/07/16.
 */
public class Mnist2Entity {

    public static final String INPUT_CLASSIFICATION = "input-class";
    public static final String OUTPUT_IMAGE = "output-image";
    public static final String OUTPUT_CLASS = "output-class";
    public static final String OUTPUT_IMAGE_LABEL = "output-image-label";
    public static final String OUTPUT_ERROR = "output-error";
    public static final String OUTPUT_ERROR_SERIES = "output-error-series";
    public static final String OUTPUT_TRUTH_SERIES = "output-truth-series";
    public static final String OUTPUT_CLASS_SERIES = "output-class-series";

//               ---> image   --> fn --> class/feat ---> error
//     mnist     ---> class   --------->            \-->
//               --->
//
//    mnist: produces image (data) and class (config)
//    fn: takes image produces features (data)
//    class x feat: takes [features and class] and produces configs: [predicted, error?, truth]
//    vec entity takes scalar configs, produces: pred-series, error-series,truth-series

}
