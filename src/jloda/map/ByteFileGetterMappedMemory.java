/**
 * ByteFileGetter.java 
 * Copyright (C) 2015 Daniel H. Huson
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
package jloda.map;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Open and read file of bytes. File can be arbitrarily large, uses memory mapping. Also supports reading of ints
 * <p/>
 * Daniel Huson, 4.2015
 */
public class ByteFileGetterMappedMemory extends FileGetterPutterBase implements IByteGetter {

    /**
     * constructor
     *
     * @param file
     * @throws java.io.IOException
     */
    public ByteFileGetterMappedMemory(File file) throws IOException {
        super(file); // need 4 overlap to allow reading of integers
    }

    /**
     * gets value for given index
     *
     * @return integer
     * @throws java.io.IOException
     */
    public int get(long index) {
        // note that index equals filePos and so no conversion from index to filePos necessary
        if (index < limit()) {
            return buffers[getWhichBuffer(index)].get(getIndexInBuffer(index)) & 0xFF;
        } else
            return 0;
    }

    /**
     * bulk get
     *
     * @param bytes
     * @param offset
     * @param len
     * @return number of bytes that we obtained
     * @throws IOException
     */
    @Override
    public int get(long index, byte[] bytes, int offset, int len) {
        // note that index equals filePos and so no conversion from index to filePos necessary
        int whichBuffer = getWhichBuffer(index);
        int indexInBuffer = getIndexInBuffer(index);

        for (int i = 0; i < len; i++) {
            bytes[offset++] = buffers[whichBuffer].get(indexInBuffer);
            if (++indexInBuffer == BLOCK_SIZE) {
                whichBuffer++;
                if (whichBuffer == buffers.length)
                    return i;
                indexInBuffer = 0;
            }
        }
        return len;
    }

    /**
     * gets integer represented by four bytes starting at given index
     *
     * @return integer
     * @throws IOException
     */
    @Override
    public int getInt(long index) {
        if (index < limit()) {
            // note that index equals filePos and so no conversion from index to filePos necessary

            int whichBuffer = getWhichBuffer(index);
            int indexBuffer = getIndexInBuffer(index);

            try {
                final ByteBuffer buf = buffers[whichBuffer];
                return ((buf.get(indexBuffer++)) << 24) + ((buf.get(indexBuffer++) & 0xFF) << 16) + ((buf.get(indexBuffer++) & 0xFF) << 8) + ((buf.get(indexBuffer) & 0xFF));
            } catch (Exception ex) { // exception is thrown when int goes over buffer boundary. In this case, we need to grab each byte separately
                indexBuffer = getIndexInBuffer(index);
                int result = (buffers[whichBuffer].get(indexBuffer++) << 24);
                if (indexBuffer == BLOCK_SIZE) {
                    indexBuffer = 0;
                    whichBuffer++;
                }
                result += ((buffers[whichBuffer].get(indexBuffer++) & 0xFF) << 16);
                if (indexBuffer == BLOCK_SIZE) {
                    indexBuffer = 0;
                    whichBuffer++;
                }
                result += ((buffers[whichBuffer].get(indexBuffer++) & 0xFF) << 8);
                if (indexBuffer == BLOCK_SIZE) {
                    indexBuffer = 0;
                    whichBuffer++;
                }
                result += ((buffers[whichBuffer].get(indexBuffer) & 0xFF));
                return result;
            }
        } else
            return 0;
    }


    /**
     * length of array
     *
     * @return array length
     * @throws java.io.IOException
     */
    @Override
    public long limit() {
        return fileLength;
    }
}
