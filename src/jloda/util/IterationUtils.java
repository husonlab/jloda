/*
 * IterationUtils.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.util;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * iteration utilities
 * Daniel Huson, 3.2019
 */
public class IterationUtils {

    /**
     * join multiple iterables into one
     *
     * @param collections
     * @param <T>
     * @return iterable over multiple iterables
     */
    public static <T, L extends Collection<T>> Iterable<T> join(final Collection<L> collections) {
        return () -> new Iterator<>() {
            private Iterator<T> iterator = null;
            private final Iterator<L> metaIterator = collections.iterator();

            {
                if (metaIterator.hasNext())
                    iterator = metaIterator.next().iterator();
            }

            @Override
            public boolean hasNext() {
                return iterator != null;
            }

            @Override
            public T next() {
                if (iterator == null)
                    throw new NoSuchElementException();

                final T next = iterator.next();
                while (!iterator.hasNext()) {
                    if (metaIterator.hasNext())
                        iterator = metaIterator.next().iterator();
                    else {
                        iterator = null;
                        break;
                    }
                }
                return next;
            }
        };
    }

    public static <T> List<T> asList(Iterable<T> iterable) {
        return asList(iterable, new ArrayList<>());
    }

    public static <T> List<T> asList(Iterable<T> iterable, List<T> list) {
        for (T value : iterable) {
            list.add(value);
        }
        return list;
    }

    public static <T> Set<T> asSet(Iterable<T> iteratable) {
        final HashSet<T> set = new HashSet<>();
        return asSet(iteratable, set);
    }

    public static <T> Set<T> asSet(Iterable<T> iteratable, Set<T> set) {
        for (T value : iteratable) {
            set.add(value);
        }
        return set;
    }

    public static <T> Iterator<T> iteratorNonNullElements(Iterator<T> it) {
        return new Iterator<T>() {
            T next = getNextNonNull();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public T next() {
                var result = next;
                next = getNextNonNull();
                return result;
            }

            private T getNextNonNull() {
                while (it.hasNext()) {
                    T a = it.next();
                    if (a != null)
                        return a;
                }
                return null;

            }
        };
    }

    public static <T> int count(Iterable<T> iterable) {
        int count = 0;

        var it = iterable.iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    public static <T> Stream<T> asStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
