/*
 * DisjointIntervalSet.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.util.interval;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * a set of disjoint intervals
 * Daniel Huson, 2.2019
 */
public class DisjointIntervalSet<T> {
    private final TreeMap<Integer, Interval<T>> map = new TreeMap<>();

    /**
     * determines whether interval overlaps members of set
     *
     * @param interval
     * @return true, if overlaps
     */
    public boolean overlaps(Interval<T> interval) {
        return overlaps(interval.getStart(), interval.getEnd());
    }

    /**
     * determines whether interval [a,b] overlaps members of set
     *
     * @param start
     * @param end
     * @return true, if overlaps
     */
    public boolean overlaps(int start, int end) {
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        final Map.Entry<Integer, Interval<T>> beforeEnd = map.floorEntry(end);

        return beforeEnd != null && beforeEnd.getValue().getEnd() >= start;

    }

    /**
     * inserts interval into set
     *
     * @param interval
     * @throws IllegalArgumentException, if interval overlaps  members of set
     */
    public void insert(Interval<T> interval) throws IllegalArgumentException {
        if (overlaps(interval.getStart(), interval.getEnd()))
            throw new IllegalArgumentException("Interval overlaps");
        map.put(interval.getStart(), interval);
    }

    /**
     * inserts interval into set
     *
     * @param a
     * @param b
     * @param data
     * @throws IllegalArgumentException, if interval overlaps  members of set
     */
    public void insert(int a, int b, T data) throws IllegalArgumentException {
        insert(new Interval<>(a, b, data));
    }

    /**
     * iterates over all data objects
     *
     * @return iterator
     */
    public Iterable<T> data() {
        return () -> new Iterator<>() {
            private final Iterator<Interval<T>> it = intervals().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                return it.next().getData();
            }

            @Override
            public void remove() {
            }
        };
    }

    /**
     * iterates over all intervals
     *
     * @return iterator
     */
    public Iterable<Interval<T>> intervals() {
        return () -> new Iterator<>() {
            private final Iterator<Integer> it = map.keySet().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Interval<T> next() {
                return map.get(it.next());
            }

            @Override
            public void remove() {
            }
        };
    }

    /**
     * number of members
     *
     * @return size
     */
    public int size() {
        return map.size();
    }
}
