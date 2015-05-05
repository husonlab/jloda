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
 * Open and read file of integers. File can be arbitrarily large, uses memory mapping.
 * <p/>
 * Daniel Huson, 4.2015
 */
public class IntFileGetter extends FileGetterPutterBase implements IIntGetter {
    /**
     * constructor
     *
     * @param file
     * @throws java.io.IOException
     */
    public IntFileGetter(File file) throws IOException {
        super(file);
    }

    /**
     * gets value for given index
     *
     * @return integer
     * @throws IOException
     */
    public int get(long index) {
        if (index < limit()) {
            index <<= 2; // convert to file position
            // the following works because we buffers overlap by 4 bytes
            int indexBuffer = getIndexInBuffer(index);
            final ByteBuffer buf = buffers[getWhichBuffer(index)];
            return ((buf.get(indexBuffer++)) << 24) + ((buf.get(indexBuffer++) & 0xFF) << 16) + ((buf.get(indexBuffer++) & 0xFF) << 8) + ((buf.get(indexBuffer) & 0xFF));
        } else
            return 0;
    }

    /**
     * length of array (file length/4)
     *
     * @return array length
     * @throws java.io.IOException
     */
    @Override
    public long limit() {
        return fileLength >>> 2;
    }
}
