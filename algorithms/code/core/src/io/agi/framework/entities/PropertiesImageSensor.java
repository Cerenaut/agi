package io.agi.framework.entities;

/**
 * Created by gideon on 27/03/2016.
 */
public class PropertiesImageSensor {

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
}
