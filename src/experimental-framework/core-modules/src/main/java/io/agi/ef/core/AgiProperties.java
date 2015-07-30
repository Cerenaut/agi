package io.agi.ef.core;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Fail-safe convenient but slow access to properties files.
 * 
 * @author dave
 */
public class AgiProperties {

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
            e.printStackTrace();
            return defaultValue;
        }
    }
    
}
