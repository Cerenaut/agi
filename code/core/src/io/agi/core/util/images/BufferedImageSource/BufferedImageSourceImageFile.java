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

package io.agi.core.util.images.BufferedImageSource;

import io.agi.core.orm.AbstractPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gideon on 1/10/14.
 */
public class BufferedImageSourceImageFile extends BufferedImageSource {

    protected static final Logger logger = LogManager.getLogger();

    protected ArrayList< String > _fileNames = new ArrayList< String >();
    protected String _folderName = null;
    protected BufferedImage _image = null;
    protected int _idx = 0;

    public BufferedImageSourceImageFile( String folderName ) {
        _folderName = folderName;
        _fileNames = new ArrayList< String >();

        File folder = new File( _folderName );
        boolean isFile = folder.isFile();

        if( !isFile ) {
            DirectoryStream< Path > directoryStream = null;
            try {
                directoryStream = Files.newDirectoryStream( Paths.get( _folderName ) );
                for( Path path : directoryStream ) {
                    _fileNames.add( path.getFileName().toString() );
                }
            } catch( IOException ex ) {
                System.err.println( "IO Exception reading files in folder: " + _folderName );
            }
            finally {
                if( directoryStream != null ) {
                    try {
                        directoryStream.close();
                    }
                    catch( Exception e ) {
                        System.err.println( "Exception closing directory stream from folder: " + _folderName );
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            System.err.println( "ERROR: " + _folderName + " is a file but should be a directory." );
        }
        Collections.sort( _fileNames );
    }

    /**
     * Construct a representation of this object as the set of getImage files it
     * represents
     *
     * @return
     */
    public String toString() {
        String str = "";
        for( String fileName : _fileNames ) {
            str += fileName + ", ";
        }
        return str;
    }

    /**
     * For this class, return file name.
     *
     * @return
     */
    public String getImageFilePath() {
        return getImageFilePath( _idx );
    }

    public String getImageFilePath( int idx ) {
        String fullFileName = "undefined";
        if( idx >= 0 && idx < _fileNames.size() ) {
            fullFileName = _folderName + "/" + _fileNames.get( idx );
        }
        else {
            System.err.println( "Invalid iDX:" + idx + " of " + _fileNames.size() );
        }
        return fullFileName;
    }

    public String getImageFileName() {
        return getImageFileName( _idx );
    }
    /**
     * Get the filename of the specified image or null if not found.
     * @param idx
     * @return
     */
    public String getImageFileName( int idx ) {
        try {
            return _fileNames.get( idx );
        }
        catch( Exception e ) {
            logger.error( "Unable to get image file name.");
            logger.error( e.toString(), e );

            return null;
        }
    }

    /**
     * Return the current image. If it is not set, then cache it.
     * If it could not get an image for this index, return null.
     */
    public BufferedImage getImage() {

        if( _image == null ) {
            String fullFileName = getImageFilePath();
            InputStream stream = null;

            try {
                // Java bug:
                // https://bugs.openjdk.java.net/browse/JDK-7166379
                // http://stackoverflow.com/questions/10441276/jdk-1-7-too-many-open-files-due-to-posix-semaphores
                stream = new FileInputStream( fullFileName );
                _image = ImageIO.read( stream );
            }
            catch( IOException e ) {
                logger.error( "Unable to get image.");
                logger.error( e.toString(), e );

                _image = null;
            }
            finally {
                if( stream != null ) {
                    try {
                        stream.close();
                    }
                    catch( IOException ex ) {
                        System.err.println( "ERROR closing image input stream: " + fullFileName + ex.getMessage() );
                    }
                }
            }
        }

        return _image;
    }

    public AbstractPair< Integer, Integer > getImageSize() {

        // iterate through images till successfully get an image
        BufferedImage image = null;
        int idx = 0;
        do {
            image = getImage();
            idx = nextImage();
        } while( image == null && idx != 0 );

        int w = 0;
        int h = 0;
        if( _image != null ) {
            w = _image.getWidth();
            h = _image.getHeight();
        }

        AbstractPair< Integer, Integer > ap = new AbstractPair< Integer, Integer >( w, h );
        return ap;
    }

    public int getIdx() {
        return _idx;
    }

    @Override
    public int nextImage() {
        _idx++;

        if( _idx >= _fileNames.size() ) { // wrap around
            _idx = 0;

            _image = null;      // clear 'cached' copy
        }

        return _idx;
    }

    /**
     * If the index is out of range, it silently does nothing.
     *
     * @param index
     */
    @Override
    public boolean seek( int index ) {

        if( index >= 0 && index < _fileNames.size() ) {
            _idx = index;

            _image = null;      // clear 'cached' copy

            return true;
        }

        return false;
    }

    public int getNbrImages() {
        return _fileNames.size();
    }

    @Override
    public int bufferSize() {
        return _fileNames.size();
    }

}
