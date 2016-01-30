package io.agi.core.orm;

/**
 * Created by dave on 27/12/15.
 */
public class Keys {

    public static final String DELIMITER = "-";

    public static String concatenate( String prefix, String suffix ) {
        return prefix + DELIMITER + suffix;
    }
}
