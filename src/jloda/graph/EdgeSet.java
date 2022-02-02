/*
 * EdgeSet.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.graph;

import java.util.*;

/**
 * EdgeSet implements a set of edges contained in a given graph
 */
public class EdgeSet extends GraphBase implements Iterable<Edge>, Set<Edge>, AutoCloseable {
	final BitSet bits;

	/**
	 * Constructs a new empty EdgeSet for Graph G.
	 *
	 * @param graph Graph
	 */
	public EdgeSet(Graph graph) {
		setOwner(graph);
		graph.registerEdgeSet(this);
        bits = new BitSet();
    }

    /**
     * Is edge v member?
     *
     * @param e Edge
     * @return a boolean value
     */
    public boolean contains(Object e) {
        return e instanceof Edge && bits.get(((Edge) e).getId());
    }

    /**
     * Insert edge e.
     *
     * @param e Edge
     * @return true, if new
     */
    public boolean add(Edge e) {
        if (bits.get(e.getId()))
            return false;
        else {
            bits.set(e.getId(), true);
            return true;
        }
    }

    /**
     * Delete edge v from set.
     *
     * @param e Edge
     */
    public boolean remove(Object e) {
        if (e instanceof Edge && bits.get(((Edge) e).getId())) {
            bits.set(((Edge) e).getId(), false);
            return true;
        } else
            return false;

    }

    /**
     * adds all edges in the given collection
     *
     * @return true, if some element is new
     */
    public boolean addAll(Collection<? extends Edge> collection) {
        return addAll((Iterable<? extends Edge>) collection);

    }

    /**
     * adds all nodes in the given collection
     *
     * @return true, if some element is new
     */
    public boolean addAll(final Iterable<? extends Edge> collection) {
        boolean result = false;
        for (var e : collection) {
            if (add(e))
                result = true;
        }
        return result;
    }


    /**
     * returns true if all elements of collection are contained in this set
     *
     * @return all contained?
     */
    public boolean containsAll(Collection collection) {
        for (Object aCollection : collection) {
            if (!contains(aCollection))
                return false;
        }
        return true;
    }

    /**
     * removes all edges in the collection
     *
     * @return true, if something actually removed
     */
    public boolean removeAll(Collection collection) {
        boolean result = false;
        for (var obj : collection) {
            if (remove(obj))
                result = true;
        }
        return result;
    }

    /**
     * keep only those elements contained in the collection
     *
     * @return true, if set changes
     */
    public boolean retainAll(Collection collection) {
        final int old = bits.cardinality();
        final BitSet newBits = new BitSet();

        for (Object obj : collection) {
            if (obj instanceof Edge) {
                newBits.set(((Edge) obj).getId());
            }
        }
        bits.and(newBits);
        return old != bits.cardinality();
    }

    /**
     * Delete all edges from set.
     */
    public void clear() {
        bits.clear();
    }

    /**
     * is empty?
     *
     * @return true, if empty
     */
    public boolean isEmpty() {
        return bits.isEmpty();
    }

    /**
     * return all contained edges as array
     */
    public Edge[] toArray() {
        return toArray(new Edge[0]);
    }

    public <T> T[] toArray(T[] objects) {
        if (objects.length < size())
            objects = Arrays.copyOf(objects, size());
        int i = 0;
        for (Edge e : getOwner().edges()) {
            if (contains(e))
                objects[i++] = (T) e;
        }
        return objects;
    }

    /**
     * Puts all edges into set.
     */
    public void addAll() {
        for (Edge e : getOwner().edges()) {
            add(e);
        }
    }

    /**
     * Returns the size of the set.
     *
     * @return size
     */
    public int size() {
        return bits.cardinality();
    }

    /**
     * Returns an enumeration of the elements in the set.
     *
     * @return an enumeration of the elements in the set
     */
    public Iterator<Edge> iterator() {
        return successors(null).iterator();
    }

    /**
     * gets all successors
     *
     * @return all successors
     */
    public Iterable<Edge> successors(final Edge afterMe) {
        return () -> new Iterator<>() {
            Edge e = (afterMe == null ? getOwner().getFirstEdge() : afterMe.getNext());

            {
                while (e != null) {
                    if (contains(e))
                        break;
                    e = e.getNext();
                }
            }

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Edge next() {
                Edge result = e;
                {
                    e = e.getNext();
                    while (e != null) {
                        if (contains(e))
                            break;
                        e = e.getNext();
                    }
                }
                return result;
            }
        };
    }

    /**
     * returns a clone of this set
     *
     * @return a clone
     */
    public Object clone() {
        EdgeSet result = new EdgeSet(getOwner());
        result.addAll(this);
        return result;
    }

    /**
     * do the two sets have a non-empty intersection?
     *
     * @return true, if intersection is non-empty
     */
    public boolean intersects(EdgeSet aset) {
        return bits.intersects(aset.bits);
    }

    /**
     * gets the first element or null
     *
     * @return first element or null
     */
    public Edge getFirstElement() {
        final Iterator<Edge> it = iterator();
        if (it.hasNext())
            return it.next();
        else
            return null;
    }

    /**
     * gets the last edge or null. Not efficient,
	 *
	 * @return last edge or null
	 */
	public Edge getLastElement() {
		for (Edge e = getOwner().getLastEdge(); e != null; e = e.getPrev()) {
			if (contains(e))
				return null;
		}
		return null;
	}

	@Override
	public void close() {
		getOwner().close(this);
	}
}

// EOF
