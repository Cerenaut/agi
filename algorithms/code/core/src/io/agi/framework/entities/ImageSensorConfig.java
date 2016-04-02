package io.agi.framework.entities;

import io.agi.framework.EntityConfig;

import java.awt.*;

/**
 *
 * Created by gideon on 27/03/2016.
 */
public class ImageSensorConfig extends EntityConfig {

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

    ReceptiveField receptiveField;
    Resolution resolution;
    boolean greyscale;

    String sourceFilesPath;
    String sourceType;
    int imageIndex;

    public Rectangle getReceptiveField() {
        Rectangle rectangle = new Rectangle( receptiveField.receptiveFieldX, receptiveField.receptiveFieldY, receptiveField.receptiveFieldW, receptiveField.receptiveFieldH );
        return rectangle;
    }

    public Point getResolution() {
        Point point = new Point( resolution.resolutionX, resolution.resolutionY );
        return point;
    }
}
