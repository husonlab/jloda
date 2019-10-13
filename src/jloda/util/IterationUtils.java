/*
 * IterationUtils.java Copyright (C) 2019. Daniel H. Huson
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

import java.util.*;

/**
 * iteration utilities
 * Daniel Huson, 3.2019
 */
public class IterationUtils {


    /**
     * join multiple iterables into one
     *
     * @param iterables
     * @param <T>
     * @return iterable over multiple iterables
     */
    public static <T> Iterable<T> join(final Iterable<T>... iterables) {
        return () -> new Iterator<>() {
            private int which = 0;
            private Iterator<T> iterator = null;

            {
                while (which < iterables.length) {
                    iterator = iterables[which].iterator();
                    if (iterator.hasNext())
                        break;
                    which++;
                }
                if (which == iterables.length)
                    iterator = null; // no non-empty iterator found
            }

            @Override
            public boolean hasNext() {
                return iterator != null;
            }

            @Override
            public T next() {
                final T next = iterator.next();
                while (!iterator.hasNext()) {
                    which++;
                    if (which < iterables.length)
                        iterator = iterables[which].iterator();
                    else {
                        iterator = null;
                        break;
                    }
                }
                return next;
            }
        };
    }

    public static <T> List<T> asList(Iterable<T> iteratable) {
        return asList(iteratable, new ArrayList<>());
    }

    public static <T> List<T> asList(Iterable<T> iteratable, List<T> list) {
        for (T value : iteratable) {
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
}
