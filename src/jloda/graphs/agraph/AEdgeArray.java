/*
 *  AEdgeArray.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.graphs.agraph;

import jloda.graphs.interfaces.IEdgeArray;

import java.util.Arrays;
import java.util.Iterator;

/**
 * an edge array
 * Daniel Huson, 3.2021
 *
 * @param <T>
 */
public class AEdgeArray<T> implements IEdgeArray<AGraph.Edge, T> {
    private final T[] array;

    public AEdgeArray(AGraph aGraph) {
        array = (T[]) new Object[aGraph.getNumberOfEdges()];
    }

    public void put(AGraph.Edge v, T value) {
        array[v.getId()] = value;
    }

    public T getValue(AGraph.Edge v) {
        return array[v.getId()];
    }

    public void clear() {
        Arrays.fill(array, null);
    }

    public void fill(T value) {
        Arrays.fill(array, value);
    }

    public Iterable<T> values() {
        return () -> new Iterator<>() {
            private int i = nextNonNull(0);

            @Override
            public boolean hasNext() {
                return i < array.length;
            }

            @Override
            public T next() {
                T result = array[i];
                i = nextNonNull(i + 1);
                return result;
            }

            private int nextNonNull(int i) {
                while (i < array.length) {
                    if (array[i] != null)
                        break;
                    i++;
                }
                return i;
            }
        };
    }
}
