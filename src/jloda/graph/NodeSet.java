/*
 * NodeSet.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.util.Basic;

import java.util.*;

/**
 * NodeSet implements a set of nodes contained in a given graph
 * Daniel Huson, 2003
 */
public class NodeSet extends GraphBase implements Set<Node>, AutoCloseable {
	private final BitSet bits;

	/**
	 * Constructs a new empty NodeSet for Graph G.
	 *
	 * @param graph Graph
	 */
	public NodeSet(Graph graph) {
		setOwner(graph);
		graph.registerNodeSet(this);
        bits = new BitSet();
    }

    /**
     * copy constructor
     *
	 */
    public NodeSet(NodeSet other) {
        this(other.getOwner());
        this.bits.or(other.bits);
    }

    /**
     * Is node v member?
     *
     * @param v Node
     * @return a boolean value
     */
    public boolean contains(Object v) {
        return v instanceof Node && bits.get(((Node) v).getId());
    }

    /**
     * Insert node v.
     *
     * @param v Node
     * @return true, if new
     */
    public boolean add(Node v) {
        if (bits.get(getOwner().getId(v)))
            return false;
        else {
            bits.set(getOwner().getId(v), true);
            return true;
        }
    }

    /**
     * Delete node v from set.
     *
     * @param v Node
     */
    public boolean remove(Object v) {
        if (bits.get(getOwner().getId((Node) v))) {
            bits.set(getOwner().getId((Node) v), false);
            return true;
        } else
            return false;

    }

    /**
     * set all nodes
     *
     * @return true, if some element added
     */
    public boolean setAll(final Iterable<? extends Node> collection) {
        clear();
        return addAll(collection);
    }


    /**
     * adds all nodes in the given collection
     *
     * @return true, if some element is new
     */
    public boolean addAll(final Collection<? extends Node> collection) {
        return addAll((Iterable<? extends Node>) collection);
    }

    /**
     * adds all nodes in the given collection
     *
     * @return true, if some element is new
     */
    public boolean addAll(final Iterable<? extends Node> collection) {
        boolean result = false;
        for (var v : collection) {
            if (add(v))
                result = true;
        }
        return result;
    }

    /**
     * returns true if all elements of collection are contained in this set
     *
     * @return all contained?
     */
    public boolean containsAll(final Collection<?> collection) {
        for (Object o : collection) {
            if (!contains(o))
                return false;
        }
        return true;
    }

    /**
     * equals
     *
     * @return true, if equals
     */
    public boolean equals(Object obj) {
        if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            return size() == collection.size() && containsAll(collection) && collection.containsAll(this);
        } else
            return false;
    }

    /**
     * removes all nodes in the collection
     *
     * @return true, if something actually removed
     */
    public boolean removeAll(final Collection<?> collection) {
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
    public boolean retainAll(final Collection<?> collection) {
        final int old = bits.cardinality();
        final BitSet newBits = new BitSet();

        for (Object obj : collection) {
            if (obj instanceof Node) {
                newBits.set(((Node) obj).getId());
            }
        }
        bits.and(newBits);
        return old != bits.cardinality();
    }

    /**
     * Delete all nodes from set.
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
     * Puts all nodes into set.
     */
    public void addAll() {
        for (Node node : getOwner().nodes()) add(node);
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
    public Iterator<Node> iterator() {

		return new Iterator<>() {
			private Node v = getFirstElement();

			@Override
			public boolean hasNext() {
				return v != null;
			}

			@Override
			public Node next() {
				if (v == null)
					throw new NoSuchElementException();
				var result = v;
				v = getNextElement(v);
				return result;
			}
		};
    }

    /**
     * return all contained edges as array
     */
    public Node[] toArray() {
        return toArray(new Node[0]);
    }

    /**
     * copy to array
     *
	 */
    public <T> T[] toArray(T[] objects) {
        if (objects.length < size())
            objects = Arrays.copyOf(objects, size());
        int i = 0;
        for (Node node : this) {
            if (i == objects.length)
                break;
            objects[i++] = (T) node;
        }
        return objects;
    }

    /**
     * Returns the first element in the set.
     *
     * @return v Node
     */
    public Node getFirstElement() {
        Node v;
        for (v = getOwner().getFirstNode(); v != null; v = getOwner().getNextNode(v))
            if (contains(v))
                break;
        return v;
    }

    /**
     * Gets the successor element in the set.
     *
     * @param v Node
     * @return a Node the successor of node v
     */
    public Node getNextElement(Node v) {
        for (v = getOwner().getNextNode(v); v != null; v = getOwner().getNextNode(v))
            if (contains(v))
                break;
        return v;
    }

    /**
     * Gets the predecessor element in the set.
     *
     * @param v Node
     * @return a Node the predecessor of node v
     */
    public Node getPrevElement(Node v) {
        for (v = getOwner().getPrevNode(v); v != null; v = getOwner().getPrevNode(v))
            if (contains(v))
                break;
        return v;
    }


    /**
     * Returns the last element in the set.
     *
     * @return the Node the last element in the set
     */
    public Node getLastElement() {
        Node v = null;
        try {
            for (v = getOwner().getLastNode(); v != null; v = getOwner().getPrevNode(v))
                if (contains(v))
                    break;
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
        return v;
    }

    /**
     * returns a clone of this set
     *
     * @return a clone
     */
    public Object clone() {
        NodeSet result = new NodeSet(getOwner());
        result.addAll(this);
        return result;
    }

    /**
     * returns string rep
     *
     * @return string
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        boolean first = true;
        for (Object o : this) {
            if (first)
                first = false;
            else
                buf.append(", ");
            buf.append(o);
        }
        buf.append("]");
        return buf.toString();
    }

	/**
	 * do the two sets have a non-empty intersection?
	 *
	 * @return true, if intersection is non-empty
	 */
	public boolean intersects(NodeSet aset) {
		return bits.intersects(aset.bits);
	}

	@Override
	public void close() {
		getOwner().close(this);
	}
}

// EOF
