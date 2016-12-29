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

    String sourceFilesPathTraining;
    String sourceFilesPathTesting;

    String trainingEntities = "";
    String testingEntities = "";

    int epoch = 0;
    int trainingEpochs = 1;
    int testingEpochs = 1;

    long shuffleSeed = 0;

    int imageLabel = 0;
    int imageIndex = 0;
    int imageRepeat = 0; // index of image repeat
    int imageRepeats = 1; // 1 repeat = show once only

    boolean terminate = false; // trigger to stop generating images
    String phase = PHASE_TRAINING;

    public Rectangle getReceptiveField() {
        Rectangle rectangle = new Rectangle( receptiveField.receptiveFieldX, receptiveField.receptiveFieldY, receptiveField.receptiveFieldW, receptiveField.receptiveFieldH );
        return rectangle;
    }

    public Point getResolution() {
        Point point = new Point( resolution.resolutionX, resolution.resolutionY );
        return point;
    }
}
