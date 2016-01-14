/**
 * DirectGif89Frame.java 
 * Copyright (C) 2016 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
//******************************************************************************
// DirectGif89Frame.java
//******************************************************************************
package jloda.export.gifEncode;

import java.awt.*;
import java.awt.image.PixelGrabber;
import java.io.IOException;

//==============================================================================

/**
 * Instances of this Gif89Frame subclass are constructed from RGB image info,
 * either in the form of an Image object or a pixel array.
 * <p/>
 * There is an important restriction to note.  It is only permissible to add
 * DirectGif89Frame objects to a Gif89Encoder constructed without an explicit
 * color map.  The GIF color table will be automatically generated from pixel
 * information.
 *
 * @author J. M. G. Elliott (tep@jmge.net)
 * @version 0.90 beta (15-Jul-2000)
 * @see Gif89Encoder
 * @see Gif89Frame
 * @see IndexGif89Frame
 */
public class DirectGif89Frame extends Gif89Frame {

    private int[] argbPixels;

    //----------------------------------------------------------------------------

    /**
     * Construct an DirectGif89Frame from a Java image.
     *
     * @param img A java.awt.Image object that supports pixel-grabbing.
     * @throws IOException If the image is unencodable due to failure of pixel-grabbing.
     */
    public DirectGif89Frame(Image img) throws IOException {
        PixelGrabber pg = new PixelGrabber(img, 0, 0, -1, -1, true);

        String errmsg = null;
        try {
            if (!pg.grabPixels())
                errmsg = "can't grab pixels from image";
        } catch (InterruptedException e) {
            errmsg = "interrupted grabbing pixels from image";
        }

        if (errmsg != null)
            throw new IOException(errmsg + " (" + getClass().getName() + ")");

        theWidth = pg.getWidth();
        theHeight = pg.getHeight();
        argbPixels = (int[]) pg.getPixels();
        ciPixels = new byte[argbPixels.length];
    }

    //----------------------------------------------------------------------------

    /**
     * Construct an DirectGif89Frame from ARGB pixel data.
     *
     * @param width       Width of the bitmap.
     * @param height      Height of the bitmap.
     * @param argb_pixels Array containing at least width*height pixels in the format returned by
     *                    java.awt.Color.getRGB().
     */
    public DirectGif89Frame(int width, int height, int argb_pixels[]) {
        theWidth = width;
        theHeight = height;
        argbPixels = new int[theWidth * theHeight];
        System.arraycopy(argb_pixels, 0, argbPixels, 0, argbPixels.length);
        ciPixels = new byte[argbPixels.length];
    }

    //----------------------------------------------------------------------------

    Object getPixelSource() {
        return argbPixels;
    }
}
