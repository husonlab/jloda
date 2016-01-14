/**
 * IteratorAdapter.java 
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * An abstract base class for iterators with single element caching. Derived
 * classes need only implement the method <code>findNext</code>.
 */
public abstract class IteratorAdapter<T> implements Iterator<T> {
    private final LinkedList<T> cache = new LinkedList<>();

    /**
     * Returns the next available element or throws an exception.
     *
     * @return the next element.
     * @throws NoSuchElementException if no more elements are available.
     */
    protected abstract T findNext() throws NoSuchElementException;

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */

    public boolean hasNext() {
        if (cache.size() == 0) {
            try {
                cache.addLast(findNext());
            } catch (NoSuchElementException ex) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */

    public T next() {
        if (cache.size() == 0) {
            return findNext();
        } else {
            return cache.removeFirst();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */

    public void remove() {
        throw new UnsupportedOperationException("not supported");
    }
}
