/*
 * IndexGif89Frame.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//******************************************************************************
// IndexGif89Frame.java
//******************************************************************************
package jloda.swing.export.gifEncode;

//==============================================================================

/**
 * Instances of this Gif89Frame subclass are constructed from bitmaps in the
 * form of color-index pixels, which accords with a GIF's native palettized
 * color model.  The class is useful when complete control over a GIF's color
 * palette is desired.  It is also much more efficient when one is using an
 * algorithmic frame generator that isn't interested in RGB values (such
 * as a cellular automaton).
 * <p/>
 * Objects of this class are normally added to a Gif89Encoder object that has
 * been provided with an explicit color table at construction.  While you may
 * also add them to "auto-map" encoders without an exception being thrown,
 * there obviously must be at least one DirectGif89Frame object in the sequence
 * so that a color table may be detected.
 *
 * @author J. M. G. Elliott (tep@jmge.net)
 * @version 0.90 beta (15-Jul-2000)
 * @see Gif89Encoder
 * @see Gif89Frame
 * @see DirectGif89Frame
 */
public class IndexGif89Frame extends Gif89Frame {

    //----------------------------------------------------------------------------

    /**
     * Construct a IndexGif89Frame from color-index pixel data.
     *
     * @param width     Width of the bitmap.
     * @param height    Height of the bitmap.
     * @param ci_pixels Array containing at least width*height color-index pixels.
     */
    public IndexGif89Frame(int width, int height, byte[] ci_pixels) {
        theWidth = width;
        theHeight = height;
        ciPixels = new byte[theWidth * theHeight];
        System.arraycopy(ci_pixels, 0, ciPixels, 0, ciPixels.length);
    }

    //----------------------------------------------------------------------------

    Object getPixelSource() {
        return ciPixels;
    }
}
