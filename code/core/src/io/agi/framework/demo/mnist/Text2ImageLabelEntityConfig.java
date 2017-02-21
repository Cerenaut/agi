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

import io.agi.framework.EntityConfig;

import java.awt.*;

/**
 * Created by dave on 10/07/16.
 */
public class Text2ImageLabelEntityConfig extends ImageLabelEntityConfig {

    // Set these
    public String sourceTextFileTraining;
    public String sourceTextFileTesting;

    // defaults, automatically updated
    public int charIndex = -1;
//    public int digitIndex = 2; // 0,1,2 depending on which part of ASCII coding

    // generated - do not set manually
    public String character = "?";
    public int characterCode = 0;

}
