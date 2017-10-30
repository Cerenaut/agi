/*
 * Copyright (c) 2016.
 *
 * This file is part of Project AGI. <http://agi.io>
 *
 * Project AGI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project AGI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Project AGI.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.agi.core.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File utilities
 * <p/>
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

    /**
     * This is intended to be a common util for writing very large Strings (which we assume were created with
     * StringBuilder, as this is to be encouraged) to a file. The objective is to minimize memory use.
     *
     * @param filePathName
     * @param sb
     * @return
     */
    public static boolean WriteFileMemoryEfficient( String filePathName, StringBuilder sb, boolean append ) {

        // Some performance tips for large files:
        // http://stackoverflow.com/questions/1677194/dumping-a-java-stringbuilder-to-file

        File f = null;
        FileWriter writer = null;
        boolean result = false;
//        boolean append = false;

        try {
            f = new File( filePathName );
            writer = new FileWriter( f.getAbsoluteFile(), append );
            writer.append( sb );
            writer.close();
            result = true;
        }
        catch( IOException ioe ) {
            result = false;
        }
        finally {
            if( writer != null ) {
                try {
                    writer.close();
                }
                catch( IOException ioe ) {
                    result = false;
                }
            }
        }

        return result;
    }
}
