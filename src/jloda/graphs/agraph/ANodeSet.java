/*
 *  NodeSet.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.graphs.interfaces.INodeSet;
import jloda.util.BitSetUtils;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * a set of nodes
 * Daniel Huson, 3.2021
 */
public class ANodeSet implements INodeSet<AGraph.Node> {
    private final AGraph aGraph;
    private final BitSet members = new BitSet();


    public ANodeSet(AGraph aGraph) {
        this.aGraph = aGraph;
    }

    public void clear() {
        members.clear();
    }

    public int size() {
        return members.cardinality();
    }

    public boolean add(AGraph.Node v) {
        if (!members.get(v.getId())) {
            members.set(v.getId());
            return true;
        } else
            return false;
    }

    public boolean addAll(Collection<? extends AGraph.Node> list) {
        var old = members.cardinality();
        for (var v : list) {
            members.set(v.getId());
        }
        return (members.cardinality() != old);
    }

    public boolean remove(Object obj) {
        if (obj instanceof AGraph.Node && contains(obj)) {
            members.set(((AGraph.Node) obj).getId(), false);
            return true;
        }
        return false;
    }

    public boolean removeAll(Collection<?> list) {
        var old = members.cardinality();
        for (var obj : list) {
            if (obj instanceof AGraph.Node) {
                members.set(((AGraph.Node) obj).getId(), false);
            }
        }
        return (members.cardinality() != old);
    }

    public boolean contains(Object obj) {
        return obj instanceof AGraph.Node && members.get(((AGraph.Node) obj).getId());
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
                if (obj instanceof AGraph.Node)
                    add((AGraph.Node) obj);
            }
            return true;
        }
    }

    public Iterator<AGraph.Node> iterator() {
        return new Iterator<>() {
            private final Iterator<Integer> it = BitSetUtils.members(members).iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public AGraph.Node next() {
                return aGraph.getNode(it.next());
            }
        };
    }
}
