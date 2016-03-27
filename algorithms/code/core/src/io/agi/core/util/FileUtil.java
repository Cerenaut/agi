package io.agi.core.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File utilities
 *
 * Created by dave on 7/03/16.
 */
public class FileUtil {

    /**
     * Reads a whole text file into a String.
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static String readFile( String path ) throws IOException {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, Charset.defaultCharset() );
    }
}
