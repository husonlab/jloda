/*
 *  ANodeArray.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.graphs.bgraph;

import jloda.graphs.interfaces.INodeArray;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * a node array
 * Daniel Huson, 3.2021
 *
 * @param <T>
 */
public class BNodeArray<T> implements INodeArray<BGraph.Node, T> {
    private final ArrayList<T> array;
    private final BGraph bGraph;

    public BNodeArray(BGraph bGraph) {
        array = new ArrayList<>();
        this.bGraph = bGraph;
    }

    public void put(BGraph.Node a, T value) {
        a.checkOwner(bGraph);
        var index = a.getId();
        if (value == null && index >= array.size())
            return;
        while (array.size() < index)
            array.add(null);
        array.add(value);
    }

    public T getValue(BGraph.Node a) {
        a.checkOwner(bGraph);
        return get(a.getId());
    }

    private T get(int index) {
        if (index >= array.size())
            return null;
        else {
            if (bGraph.getNode(index) == null)
                array.set(index, null);
            return array.get(index);
        }
    }

    public void clear() {
        array.clear();
    }

    public Iterable<T> values() {
        return () -> new Iterator<>() {
            private int i = nextNonNull(0);

            @Override
            public boolean hasNext() {
                return i < array.size();
            }

            @Override
            public T next() {
                T result = array.get(i);
                i = nextNonNull(i + 1);
                return result;
            }

            private int nextNonNull(int i) {
                while (i < array.size()) {
                    if (get(i) != null)
                        break;
                    i++;
                }
                return i;
            }
        };
    }
}
