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
public class FileBufferedImageSource extends BufferedImageSource {

    private ArrayList<String> _fileNames = new ArrayList<String>();
    private String _folderName = null;
    private BufferedImage _image = null;
    private int _idx = 0;

    public FileBufferedImageSource( String folderName ) {

        _folderName = folderName;

        addFileNames( folderName );
    }

    public void addFileNames( String folderName ) {
        File folder = new File( folderName );
        if ( !folder.isFile() && folder.listFiles() != null ) {
            for ( File fileEntry : folder.listFiles() ) {
                if ( fileEntry.isFile() ) {
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
        for ( String fileName : _fileNames ) {
            str += fileName + ", ";
        }
        return str;
    }

    /**
     * Return the current image. If it is not set, then read it in.
     */
    public BufferedImage getImage() {

        if (        _image == null
                &&  _idx < _fileNames.size() ) {

            String fullFileName = _folderName + "/" + _fileNames.get( _idx );

            try {
                _image = ImageIO.read( new File( fullFileName ) );
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }

        return _image;
    }

    public AbstractPair<Integer, Integer> getImageSize() {
        int w = 0;
        int h = 0;
        getImage();

        if ( _image != null ) {
            w = _image.getWidth();
            h = _image.getHeight();
        }

        AbstractPair<Integer, Integer> ap = new AbstractPair<Integer, Integer>( w, h );
        return ap;
    }

    @Override
    public int nextImage() {
        _idx++;

        if ( _idx >= _fileNames.size() ) { // wrap around
            _idx = 0;

            _image = null;      // clear 'cached' copy
        }

        return _idx;
    }

    /**
     * If the index is out of range, it silently does nothing.
     * @param index
     */
    @Override
    public boolean jumpToImage( int index ) {

        if ( index >= 0 && index < _fileNames.size() ) {
            _idx = index;

            _image = null;      // clear 'cached' copy

            return true;
        }

        return false;
    }

    @Override
    public int bufferSize() {
        return _fileNames.size();
    }

}
