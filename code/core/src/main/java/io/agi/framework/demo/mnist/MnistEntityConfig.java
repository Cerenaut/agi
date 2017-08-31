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
        public int receptiveFieldX;
        public int receptiveFieldY;
        public int receptiveFieldW;
        public int receptiveFieldH;
    }

    public class Resolution {
        public int resolutionX;
        public int resolutionY;
    }

    public ReceptiveField receptiveField = new ReceptiveField();
    public Resolution resolution = new Resolution();
    public boolean greyscale;
    public boolean invert;

    public String learningEntityName;
    public String learningConfigPath;

    public String sourceFilesPathTraining;
    public String sourceFilesPathTesting;

    public int imageIndex = 0;
    public int batch = 0;
    public int trainingBatches = 1;

    public boolean imageStep = true;
    public boolean terminate = false;

    public Rectangle getReceptiveField() {
        Rectangle rectangle = new Rectangle( receptiveField.receptiveFieldX, receptiveField.receptiveFieldY, receptiveField.receptiveFieldW, receptiveField.receptiveFieldH );
        return rectangle;
    }

    public Point getResolution() {
        Point point = new Point( resolution.resolutionX, resolution.resolutionY );
        return point;
    }

}
