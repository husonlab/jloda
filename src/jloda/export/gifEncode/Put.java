/**
 * Put.java 
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
// Put.java
//******************************************************************************
package jloda.export.gifEncode;

import java.io.IOException;
import java.io.OutputStream;

//==============================================================================

/**
 * Just a couple of trivial output routines used by other classes in the
 * package.  Normally this kind of stuff would be in a separate IO package, but
 * I wanted the present package to be self-contained for ease of distribution
 * and use by others.
 */
final class Put {

    //----------------------------------------------------------------------------

    /**
     * Write just the low bytes of a String.  (This sucks, but the concept of an
     * encoding seems inapplicable to a binary file ID string.  I would think
     * flexibility is just what we don't want - but then again, maybe I'm slow.)
     */
    static void ascii(String s, OutputStream os) throws IOException {
        byte[] bytes = new byte[s.length()];
        for (int i = 0; i < bytes.length; ++i)
            bytes[i] = (byte) s.charAt(i);  // discard the high byte
        os.write(bytes);
    }

    //----------------------------------------------------------------------------

    /**
     * Write a 16-bit integer in little endian byte order.
     */
    static void leShort(int i16, OutputStream os) throws IOException {
        os.write(i16 & 0xff);
        os.write(i16 >> 8 & 0xff);
    }
}
