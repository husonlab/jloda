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

import jloda.graphs.interfaces.IEdgeIntegerArray;

import java.util.Arrays;
import java.util.Iterator;

/**
 * an edge int array
 * Daniel Huson, 3.2021
 */
public class AEdgeIntegerArray implements IEdgeIntegerArray<AGraph.Edge> {
    private final int[] array;

    public AEdgeIntegerArray(AGraph aGraph) {
        array = new int[aGraph.getNumberOfEdges()];
    }

    public void put(AGraph.Edge a, Integer value) {
        array[a.getId()] = value;
    }

    public Integer getValue(AGraph.Edge a) {
        return array[a.getId()];
    }

    public void clear() {
        Arrays.fill(array, 0);
    }

    public void fill(int value) {
        Arrays.fill(array, value);
    }

    public Iterable<Integer> values() {
        return () -> new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < array.length;
            }

            @Override
            public Integer next() {
                int result = array[i];
                i = i + 1;
                return result;
            }
        };
    }
}
