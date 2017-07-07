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
public class ImageLabelEntityConfig extends EntityConfig {

    public static final String PHASE_TRAINING = "training";
    public static final String PHASE_TESTING = "testing";

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

    public String sourceFilesPathTraining;
    public String sourceFilesPathTesting;
    public String sourceFilesLabelDelimiter = "_";
    public int sourceFilesLabelIndex = 2;

    public String trainingEntities = "";
    public String testingEntities = "";

    public int epoch = 0;
    public int trainingEpochs = 1;
    public int testingEpochs = 1;

    public boolean shuffle = true;
    public boolean shuffleTraining = true;
    public boolean shuffleTesting = false;
    public long shuffleSeed = 0;

    public int imageLabel = 0;
    public int imageIndex = 0;
    public int imageRepeat = 0; // index of image repeat
    public int imageRepeats = 1; // 1 repeat = show once only
    public boolean imageChanged = false; // flag goes 1 on image change event

    public boolean terminate = false; // trigger to stop generating images
    public String phase = PHASE_TRAINING;

    public Rectangle getReceptiveField() {
        Rectangle rectangle = new Rectangle( receptiveField.receptiveFieldX, receptiveField.receptiveFieldY, receptiveField.receptiveFieldW, receptiveField.receptiveFieldH );
        return rectangle;
    }

    public Point getResolution() {
        Point point = new Point( resolution.resolutionX, resolution.resolutionY );
        return point;
    }
}
