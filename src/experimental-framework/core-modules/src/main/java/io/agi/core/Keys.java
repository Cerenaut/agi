package io.agi.core;

/**
 * Created by dave on 14/09/15.
 */
public class Keys {
    public static final String DELIMITER = "-";

    public static String concatenate( String prefix, String suffix ) {
        return prefix + DELIMITER + suffix;
    }

}
