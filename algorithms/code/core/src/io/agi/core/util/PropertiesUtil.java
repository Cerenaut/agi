package io.agi.core.util;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Utility functions for properties files.
 *
 * Created by dave on 27/12/15.
 */
public class PropertiesUtil {

    /**
     * A quick and easy convenience interface for getting individual properties from .properties files.
     * It hides the exceptions to avoid you having to handle them. This assumes you'll resolve these issues at debug time.
     *
     * @param file
     * @param key
     * @param defaultValue
     * @return
     */
    public static String get( String file, String key, String defaultValue ) {
        try {
            Properties p = new Properties();
            p.load( new FileInputStream( file ) );

            if ( p.containsKey( key ) ) {
                String value = p.getProperty( key );
                return value;
            }
            return defaultValue;
        }
        catch ( Exception e ) {
            System.err.println( "Error reading properties for Node: " );
            e.printStackTrace();
            return defaultValue;
        }
    }
}
