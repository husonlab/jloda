/*
 * MultiIterator.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.util;

import java.util.Iterator;

/**
 * iterator over iterators
 * Created by huson on 2/17/17.
 */
public class MultiIterator<T> implements Iterable<T> {
    private final Iterator<T>[] iterators;

    /**
     * constructor
     *
     * @param iterables
     */
    @SafeVarargs
    public MultiIterator(Iterable<T>... iterables) {
        iterators = new Iterator[iterables.length];
        for (int i = 0; i < iterables.length; i++)
            iterators[i] = iterables[i].iterator();
    }

    /**
     * constructor
     *
     * @param iterators
     */
    @SafeVarargs
    public MultiIterator(Iterator<T>... iterators) {
        this.iterators = iterators;
    }

    /**
     * get an iterator over all iterators
     *
     * @return iterator
     */
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int which = 0;

            @Override
            public boolean hasNext() {
                while (which < iterators.length) {
                    if (iterators[which].hasNext())
                        return true;
                    which++;
                }
                return false;
            }

            @Override
            public T next() {
                if (hasNext())
                    return iterators[which].next();
                else
                    return null;
            }

            @Override
            public void remove() {

            }
        };
    }


}
