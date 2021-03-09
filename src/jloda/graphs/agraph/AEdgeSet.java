/*
 *  EdgeSet.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.graphs.interfaces.IEdgeSet;
import jloda.util.BitSetUtils;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * a set of adjacentEdges
 * Daniel Huson, 3.2021
 */
public class AEdgeSet implements IEdgeSet<AGraph.Edge> {
    private final AGraph aGraph;
    private final BitSet members = new BitSet();


    public AEdgeSet(AGraph aGraph) {
        this.aGraph = aGraph;
    }

    public void clear() {
        members.clear();
    }

    public int size() {
        return members.cardinality();
    }

    public boolean add(AGraph.Edge v) {
        if (!members.get(v.getId())) {
            members.set(v.getId());
            return true;
        } else
            return false;
    }

    public boolean addAll(Collection<? extends AGraph.Edge> list) {
        var old = members.cardinality();
        for (var v : list) {
            members.set(v.getId());
        }
        return (members.cardinality() != old);
    }

    public boolean remove(Object obj) {
        if (obj instanceof AGraph.Edge && contains(obj)) {
            members.set(((AGraph.Edge) obj).getId(), false);
            return true;
        }
        return false;
    }

    public boolean removeAll(Collection<?> list) {
        var old = members.cardinality();
        for (var obj : list) {
            if (obj instanceof AGraph.Edge) {
                members.set(((AGraph.Edge) obj).getId(), false);
            }
        }
        return (members.cardinality() != old);
    }

    public boolean contains(Object obj) {
        return obj instanceof AGraph.Edge && members.get(((AGraph.Edge) obj).getId());
    }

    @Override
    public boolean isEmpty() {
        return members.isEmpty();
    }

    @Override
    public Object[] toArray() {
        var array = new Object[size()];
        int i = 0;
        for (var v : this) {
            array[i++] = v;
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        if (array.length > size()) {
            for (int i = size(); i < array.length; i++) {
                array[i] = null;
            }
        } else
            array = (T[]) new Object[size()];
        int i = 0;
        for (var v : this) {
            array[i++] = (T) v;
        }
        return array;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (var obj : collection) {
            if (!contains(obj))
                return false;
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        if (collection.size() == size() && containsAll(collection))
            return false;
        else {
            clear();
            for (var obj : collection) {
                if (obj instanceof AGraph.Edge)
                    add((AGraph.Edge) obj);
            }
            return true;
        }
    }

    public Iterator<AGraph.Edge> iterator() {
        return new Iterator<>() {
            private final Iterator<Integer> it = BitSetUtils.members(members).iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public AGraph.Edge next() {
                return aGraph.getEdge(it.next());
            }
        };
    }
}
