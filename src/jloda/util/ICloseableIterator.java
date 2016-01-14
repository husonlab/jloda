/**
 * ICloseableIterator.java 
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
