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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by gideon on 1/10/14.
 */
public class BufferedImageSourceImageFile extends BufferedImageSource {

    protected ArrayList< String > _fileNames = new ArrayList< String >();
    protected String _folderName = null;
    protected BufferedImage _image = null;
    protected int _idx = 0;

    public BufferedImageSourceImageFile( String folderName ) {

        _folderName = folderName;

        addFileNames( folderName );
    }

    public void addFileNames( String folderName ) {
        File folder = new File( folderName );
        if( !folder.isFile() && folder.listFiles() != null ) {
            for( File fileEntry : folder.listFiles() ) {
                if( fileEntry.isFile() ) {
                    _fileNames.add( fileEntry.getName() );
                }
            }
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
        if( _idx >= 0 && _idx < _fileNames.size() ) {
            fullFileName = _folderName + "/" + _fileNames.get( idx );
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

            try {
                _image = ImageIO.read( new File( fullFileName ) );
            }
            catch( IOException e ) {
                e.printStackTrace();
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
