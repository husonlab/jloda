/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import java.util.Collection;
import java.util.Iterator;

/**
 * some simple set utilities
 * Daniel HUson, 2018
 */
public class SetUtils {

    /**
     * iterator over all elements contained in the intersection of the two collections
     *
     * @param a
     * @param b
     * @return intersection
     */
    public static <T> Iterable<T> intersection(Collection<T> a, Collection<T> b) {
        return () -> new Iterator<T>() {
            final Iterator<T> it = a.iterator();
            T v = null;

            {
                while (it.hasNext()) {
                    v = it.next();
                    if (b.contains(v))
                        break;
                }
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public T next() {
                final T result = v;
                while (it.hasNext()) {
                    v = it.next();
                    if (b.contains(v))
                        break;
                }
                return result;
            }
        };
    }

    /**
     * iterator over all elements contained in the union of the given sets
     *
     * @param sets
     * @return intersection
     */
    public static Iterable union(final Collection... sets) {
        return () -> new Iterator() {
            int which = 0;
            Iterator it = null;

            {
                while (which < sets.length && !it.hasNext()) {
                    it = sets[which++].iterator();

                }
            }

            @Override
            public boolean hasNext() {
                return which < sets.length;
            }

            @Override
            public Object next() {
                final Object result = it.next();
                while (which < sets.length && !it.hasNext()) {
                    it = sets[which++].iterator();
                }
                return result;
            }
        };
    }

    /**
     * union of two sets
     *
     * @param a
     * @param b
     * @param <T>
     * @return union
     */
    public static <T> Iterable<T> union(Collection<T> a, Collection<T> b) {
        return (Iterable<T>) union(new Collection[]{a, b});
    }

    /**
     * iterator over all elements contained in the symmetric difference of two sets
     *
     * @param a
     * @param b
     * @return symmetric difference
     */
    public static <T> Iterable<T> symmetricDifference(Collection<T> a, Collection<T> b) {
        return () -> new Iterator<T>() {
            final Iterator<T> it = union(a, b).iterator();
            T v = null;

            {
                while (it.hasNext()) {
                    v = it.next();
                    if (a.contains(v) != b.contains(v))
                        break;
                }
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public T next() {
                final T result = v;
                while (it.hasNext()) {
                    v = it.next();
                    if (a.contains(v) != b.contains(v))
                        break;
                }
                return result;
            }
        };
    }

}
