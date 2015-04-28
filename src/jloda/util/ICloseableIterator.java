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

package jloda.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * A closeable iterator, e.g. based on a file or database
 * Daniel Huson, 4.2010
 */
public interface ICloseableIterator<T> extends Iterator<T>, Closeable {
    /**
     * close associated file or database
     */
    void close() throws IOException;

    /**
     * gets the maximum progress value
     *
     * @return maximum progress value
     */
    long getMaximumProgress();

    /**
     * gets the current progress value
     *
     * @return current progress value
     */
    long getProgress();

}
