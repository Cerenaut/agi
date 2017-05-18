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

import java.awt.image.BufferedImage;

/**
 * Created by gideon on 1/10/14.
 */
public abstract class BufferedImageSource {

    /**
     * Return the current image. If it is not set, then cache it.
     * If it could not get an image for this index, return null.
     */
    public abstract BufferedImage getImage();

    /**
     * Get ImageSize of expected image stream
     *
     * @return
     */
    public abstract AbstractPair< Integer, Integer > getImageSize();

    public String getImageName() {
        return "undefined";
    }

    /**
     * Advance the source to nextImage image
     */
    public abstract int nextImage();

    /**
     * Jump the source to the image specified by index for buffer.
     *
     * @return false if the index is out of range
     */
    public abstract boolean seek( int index );

    /**
     * Return the current buffer size. This may not be a constant depending on the implementation.
     */
    public abstract int bufferSize();
}
