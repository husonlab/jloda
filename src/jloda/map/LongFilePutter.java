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
public class LongFilePutter extends FileGetterPutterBase implements ILongPutter, ILongGetter {

    /**
     * constructor  to read and write values from an existing file
     *
     * @param file
     * @throws IOException
     */
    public LongFilePutter(File file) throws IOException {
        super(file, 0, Mode.READ_WRITE);
    }

    /**
     * constructs a long file putter using the given file and limit
     *
     * @param file
     * @param limit length of array
     * @throws java.io.IOException
     */
    public LongFilePutter(File file, long limit) throws IOException {
        this(file, limit, false);
    }

    /**
     * constructs a long file putter using the given file and limit
     *
     * @param file
     * @param limit length of array
     * @throws java.io.IOException
     */
    public LongFilePutter(File file, long limit, boolean inMemory) throws IOException {
        super(file, 8 * limit, (inMemory ? Mode.CREATE_READ_WRITE_IN_MEMORY : Mode.CREATE_READ_WRITE));
    }

    /**
     * gets value for given index
     *
     * @param index
     * @return value or 0
     */
    public long get(long index) {
        if (index < limit()) {
            index <<= 3; // convert to file position
            final ByteBuffer buf = buffers[getWhichBuffer(index)];
            int indexBuffer = getIndexInBuffer(index);
            return (((long) buf.get(indexBuffer++)) << 56) + ((long) (buf.get(indexBuffer++) & 0xFF) << 48) + ((long) (buf.get(indexBuffer++) & 0xFF) << 40) +
                    ((long) (buf.get(indexBuffer++) & 0xFF) << 32) + ((long) (buf.get(indexBuffer++) & 0xFF) << 24) + ((long) (buf.get(indexBuffer++) & 0xFF) << 16) +
                    ((long) (buf.get(indexBuffer++) & 0xFF) << 8) + (((long) buf.get(indexBuffer) & 0xFF));
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
    public ILongPutter put(long index, long value) {
        if (index < limit()) {
            index <<= 3; // convert to file position
            final ByteBuffer buf = buffers[getWhichBuffer(index)];
            int indexBuffer = getIndexInBuffer(index);

            buf.put(indexBuffer++, (byte) (value >> 56));
            buf.put(indexBuffer++, (byte) (value >> 48));
            buf.put(indexBuffer++, (byte) (value >> 40));
            buf.put(indexBuffer++, (byte) (value >> 32));
            buf.put(indexBuffer++, (byte) (value >> 24));
            buf.put(indexBuffer++, (byte) (value >> 16));
            buf.put(indexBuffer++, (byte) (value >> 8));
            buf.put(indexBuffer, (byte) (value));
        } else
            throw new ArrayIndexOutOfBoundsException("" + index);
        return this;
    }

    /**
     * length of array (file length / 8)
     *
     * @return array length
     * @throws java.io.IOException
     */
    @Override
    public long limit() {
        return fileLength >>> 3;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/huson/tmp/xxx.x");
        long limit = 1000000;
        LongFilePutter putter = new LongFilePutter(file, limit);
        putter.erase();
        System.err.println("limit: " + putter.limit());

        for (long i = 0; i < limit; i += limit / 10) {
            long value = i * i;
            putter.put(i, value);
            System.err.println("put(" + i + "," + value + ") - get(" + i + ")=" + putter.get(i));
        }
        putter.close();

        LongFileGetter getter = new LongFileGetter(file);
        for (long i = 0; i < limit; i += limit / 10) {
            long value = i * i;
            System.err.println("Expected=" + value + ", get(" + i + ")=" + getter.get(i));
        }

    }

    /**
     * set a new limit for a file
     *
     * @param file
     * @param newLimit
     * @throws IOException
     */
    public static void setLimit(File file, long newLimit) throws IOException {
        resize(file, 8 * (newLimit + 1));
    }
}
