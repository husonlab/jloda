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

import java.io.IOException;

/**
 * A readonly long-indexed array of bytes
 * Daniel Huson, 4.2015
 */
public interface IByteGetter {
    /**
     * gets value for given index
     *
     * @param index
     * @return value or 0
     */
    int get(long index);

    /**
     * bulk get
     *
     * @param index
     * @param bytes
     * @param offset
     * @param len
     * @return
     */
    int get(long index, byte[] bytes, int offset, int len);

    /**
     * gets next four bytes as a single integer
     * @param index
     * @return integer
     */
    int getInt(long index);

    /**
     * length of array
     *
     * @return array length
     * @throws java.io.IOException
     */
    long limit();

    /**
     * close the file
     */
    void close() throws IOException;
}
