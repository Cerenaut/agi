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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.agi.core.util.images.BufferedImageSource;

/**
 * Constructs a BufferedImageSource from two string: First, the 'type' specifying
 * what type of source should be constructed, and a second, a configuration string.
 */
public class BufferedImageSourceFactory {

    // ImageSource Types
    public static final String TYPE_IMAGE_FILES = "images";

    public static BufferedImageSource create( String type, String configuration ) {

        // configuration = path to a directory holding image files
        if( type.equalsIgnoreCase( TYPE_IMAGE_FILES ) ) {
            BufferedImageSource bis = new BufferedImageSourceImageFile( configuration );     // a directory, in this case
            return bis;
        }

        return null;

    }

}
