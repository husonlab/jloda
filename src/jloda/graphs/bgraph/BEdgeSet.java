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

package jloda.graphs.bgraph;

import jloda.graphs.interfaces.IEdgeSet;
import jloda.util.BitSetUtils;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * a set of adjacentEdges
 * Daniel Huson, 3.2021
 */
public class BEdgeSet implements IEdgeSet<BGraph.Edge> {
    private final BGraph bGraph;
    private final BitSet members = new BitSet();
    private final Object sync = new Object();
    private long latestRemoval;

    public BEdgeSet(BGraph bGraph) {
        this.bGraph = bGraph;
        latestRemoval = bGraph.getLatestEdgeRemoval();
    }

    public void clear() {
        members.clear();
    }

    public int size() {
        update();
        return members.cardinality();
    }

    public boolean add(BGraph.Edge v) {
        v.checkOwner(bGraph);
        update();
        if (!members.get(v.getId())) {
            members.set(v.getId());
            return true;
        } else
            return false;
    }

    @Override
    public boolean addAll(Collection<? extends BGraph.Edge> list) {
        update();
        var old = members.cardinality();
        for (var v : list) {
            v.checkOwner(bGraph);
            members.set(v.getId());
        }
        return (members.cardinality() != old);
    }

    @Override
    public boolean remove(Object obj) {
        update();
        if (obj instanceof BGraph.Edge && contains(obj)) {
            ((BGraph.Edge) obj).checkOwner(bGraph);
            members.set(((BGraph.Edge) obj).getId(), false);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> list) {
        update();
        var old = members.cardinality();
        for (var obj : list) {
            if (obj instanceof BGraph.Edge) {
                ((BGraph.Edge) obj).checkOwner(bGraph);
                members.set(((BGraph.Edge) obj).getId(), false);
            }
        }
        return (members.cardinality() != old);
    }

    @Override
    public boolean contains(Object obj) {
        update();
        if (obj instanceof BGraph.Edge) {
            var v = (BGraph.Edge) obj;
            v.checkOwner(bGraph);
            return members.get(v.getId());
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        update();
        return members.isEmpty();
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        update();
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
        update();
        if (collection.size() == size() && containsAll(collection))
            return false;
        else {
            clear();
            for (var obj : collection) {
                if (obj instanceof BGraph.Edge)
                    add((BGraph.Edge) obj);
            }
            return true;
        }
    }

    @Override
    public Iterator<BGraph.Edge> iterator() {
        return new Iterator<>() {
            private final Iterator<Integer> it = BitSetUtils.members(members).iterator();

            {
                update();
            }

            @Override
            public boolean hasNext() {
                update();
                return it.hasNext();
            }

            @Override
            public BGraph.Edge next() {
                update();
                return bGraph.getEdge(it.next());
            }
        };
    }

    private void update() {
        var latest = bGraph.getLatestEdgeRemoval();
        if (latestRemoval < latest) {
            synchronized (sync) {
                if (latestRemoval < latest) {
                    latestRemoval = latest;
                    for (var i : BitSetUtils.members(members)) {
                        if (bGraph.getEdge(i) == null)
                            members.set(i, false);
                    }
                }
            }
        }
    }
}
