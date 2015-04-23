/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
