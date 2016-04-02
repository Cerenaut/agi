package io.agi.framework.entities;

import io.agi.core.sdr.ScalarEncoder;
import io.agi.framework.EntityConfig;

/**
 * Created by dave on 2/04/16.
 */
public class EncoderConfig extends EntityConfig {

    String encoderType = ScalarEncoder.class.getSimpleName();
    int bits = 8;
    int density = 1;

}
