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
 * Created by dave on 5/05/16.
 */
public class MnistEntityConfig extends EntityConfig {

    public class ReceptiveField {
        int receptiveFieldX;
        int receptiveFieldY;
        int receptiveFieldW;
        int receptiveFieldH;
    }

    public class Resolution {
        int resolutionX;
        int resolutionY;
    }

    ReceptiveField receptiveField = new ReceptiveField();
    Resolution resolution = new Resolution();
    boolean greyscale;
    boolean invert;

    public String learningEntityName;
    public String learningConfigPath;

    String sourceFilesPathTraining;
    String sourceFilesPathTesting;
    int imageIndex = 0;
    boolean imageStep = true;
    boolean terminate = false;

    public Rectangle getReceptiveField() {
        Rectangle rectangle = new Rectangle( receptiveField.receptiveFieldX, receptiveField.receptiveFieldY, receptiveField.receptiveFieldW, receptiveField.receptiveFieldH );
        return rectangle;
    }

    public Point getResolution() {
        Point point = new Point( resolution.resolutionX, resolution.resolutionY );
        return point;
    }

}
