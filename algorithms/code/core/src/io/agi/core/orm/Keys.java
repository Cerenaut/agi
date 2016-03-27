package io.agi.core.orm;

/**
 * A systematic way of forming unique object names.
 *
 * Created by dave on 27/12/15.
 */
public class Keys {

    public static final String DELIMITER = "-";

    public static String concatenate( String prefix, String suffix ) {
        return prefix + DELIMITER + suffix;
    }
    public static String concatenate( String s1, String s2, String s3 ) {
        return s1 + DELIMITER + s2 + DELIMITER + s3;
    }
    public static String concatenate( String s1, String s2, String s3, String s4 ) {
        return s1 + DELIMITER + s2 + DELIMITER + s3 + DELIMITER + s4;
    }
}
