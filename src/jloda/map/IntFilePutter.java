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

package jloda.map;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Open a file for reading and writing
 * <p/>
 * Daniel Huson, 3.2015
 */
public class IntFilePutter extends FileGetterPutterBase implements IIntPutter, IIntGetter {
    /**
     * constructor  to read and write values from an existing file
     *
     * @param file
     * @throws java.io.IOException
     */
    public IntFilePutter(File file) throws IOException {
        super(file, 0, Mode.READ_WRITE);
    }

    /**
     * constructs a int file putter using the given file and limit. (Is not in-memory)
     *
     * @param file
     * @param limit length of array
     * @throws java.io.IOException
     */
    public IntFilePutter(File file, long limit) throws IOException {
        this(file, limit, false);
    }

    /**
     * constructs a int file putter using the given file and limit
     *
     * @param file
     * @param limit    length of array
     * @param inMemory create in memory and then save on close? This uses more memory, but may be faster
     * @throws java.io.IOException
     */
    public IntFilePutter(File file, long limit, boolean inMemory) throws IOException {
        super(file, 4 * limit, inMemory ? Mode.CREATE_READ_WRITE_IN_MEMORY : Mode.CREATE_READ_WRITE);
    }

    /**
     * gets value for given index
     *
     * @param index
     * @return value or 0
     */
    public int get(long index) {
        if (index < limit()) {
            index <<= 2; // convert to file position
            final ByteBuffer buf = buffers[getWhichBuffer(index)];
            int indexBuffer = getIndexInBuffer(index);
            return ((buf.get(indexBuffer++)) << 24) + ((buf.get(indexBuffer++) & 0xFF) << 16) +
                    ((buf.get(indexBuffer++) & 0xFF) << 8) + ((buf.get(indexBuffer) & 0xFF));
        } else
            return 0;
    }

    /**
     * puts value for given index
     *
     * @param index
     * @param value
     */
    @Override
    public void put(long index, int value) {
        index <<= 2; // convert to file position
        if (index < fileLength) {
            final ByteBuffer buf = buffers[getWhichBuffer(index)];
            int indexBuffer = getIndexInBuffer(index);

            buf.put(indexBuffer++, (byte) (value >> 24));
            buf.put(indexBuffer++, (byte) (value >> 16));
            buf.put(indexBuffer++, (byte) (value >> 8));
            buf.put(indexBuffer, (byte) (value));
        } else {
            throw new ArrayIndexOutOfBoundsException("" + index);
        }
    }

    /**
     * length of array (file length / 4)
     *
     * @return array length
     * @throws java.io.IOException
     */
    @Override
    public long limit() {
        return fileLength >>> 2;
    }

    /**
     * set a new limit for a file
     *
     * @param file
     * @param newLimit
     * @throws IOException
     */
    public static void setLimit(File file, long newLimit) throws IOException {
        System.err.println("new limit: " + newLimit);

        resize(file, 4 * (newLimit + 1));
    }

}
