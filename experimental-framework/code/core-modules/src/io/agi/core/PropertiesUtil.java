package io.agi.core;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by dave on 11/09/15.
 */
public class PropertiesUtil {

    public static String get( String file, String key, String defaultValue ) {
        try {
            Properties p = new Properties();
            p.load( new FileInputStream( file ) );

            if( p.containsKey( key ) ) {
                String value = p.getProperty( key );
                return value;
            }
            return defaultValue;
        }
        catch( Exception e ) {
            System.err.println( "Error reading properties for Node: " );
            e.printStackTrace();
            return defaultValue;
        }
    }

}
