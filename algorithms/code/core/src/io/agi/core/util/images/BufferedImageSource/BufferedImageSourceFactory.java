/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.util.images.BufferedImageSource;

/**
 * Constructs a BufferedImageSource from two string: First, the 'type' specifying
 * what type of source should be constructed, and a second, a configuration string.
 *
 */
public class BufferedImageSourceFactory {
        
    // ImageSource Types
    public static final String TYPE_IMAGE_FILES = "images";
    public static final String TYPE_SHAPES = "shapes";

    public static BufferedImageSource create( String type, String configuration ) {

        // configuration = path to a directory holding image files
        if( type.equalsIgnoreCase( TYPE_IMAGE_FILES ) ) {
            BufferedImageSource bis = new BufferedImageSourceImageFile( configuration );     // a directory, in this case
            return bis;
        }

// Dave: I can't init this one, it always needs a derived class to implement.                
//                else if( type.equalsIgnoreCase( TYPE_SHAPES ) ) {
//                    BufferedImageSource bis = new StubGeneratedVideoImageSource();
//                }
// Gideon: This is useful, but not implementing it here from now. Required classes are in ampf
//            else if( imageSourceType.equalsIgnoreCase( TYPE_SHAPES ) ) {
//                BufferedImageSource bis = MarkovChainImageSource.read( imageSourceFile );
//                return bis;
//            }

        return null;
        
    }
    
}
