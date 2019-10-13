/*
 * NodeSet.java Copyright (C) 2019. Daniel H. Huson
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
package jloda.graph;

import jloda.util.Basic;
import jloda.util.IteratorAdapter;

import java.util.*;

/**
 * NodeSet implements a set of nodes contained in a given graph
 * Daniel Huson, 2003
 */
public class NodeSet extends GraphBase implements Set<Node> {
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
     * Is node v member?
     *
     * @param v Node
     * @return a boolean value
     */
    public boolean contains(Object v) {
        boolean result = false;
        try {
            result = bits.get(getOwner().getId((Node) v));
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
        return result;
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
     * adds all nodes in the given collection
     *
     * @param collection
     * @return true, if some element is new
     */
    public boolean addAll(final Collection<? extends Node> collection) {
        Iterator it = collection.iterator();

        boolean result = false;
        while (it.hasNext()) {
            if (add((Node) it.next()))
                result = true;
        }
        return result;
    }

    /**
     * returns true if all elements of collection are contained in this set
     *
     * @param collection
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
     * @param obj
     * @return true, if equal
     */
    public boolean equals(Object obj) {
        if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            return size() == collection.size() && containsAll(collection);
        } else
            return false;
    }

    /**
     * removes all nodes in the collection
     *
     * @param collection
     * @return true, if something actually removed
     */
    public boolean removeAll(final Collection<?> collection) {
        Iterator it = collection.iterator();

        boolean result = false;
        while (it.hasNext()) {
            if (remove(it.next()))
                result = true;
        }
        return result;
    }

    /**
     * keep only those elements contained in the collection
     *
     * @param collection
     * @return true, if set changes
     */
    public boolean retainAll(final Collection<?> collection) {
        if (collection == null)
            return false;
        boolean changed = (collection.size() != size() || !containsAll(collection));
        NodeSet was = (NodeSet) this.clone();

        clear();
        for (Object v : collection) {
            if (v instanceof Node && was.contains(v))
                add((Node) v);
        }
        return changed;
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
     * return all contained nodes as objects
     *
     * @return contained nodes
     */
    public Node[] toArray() {
        Node[] result = new Node[bits.cardinality()];
        int i = 0;
        Iterator<Node> it = getOwner().nodeIterator();
        while (it.hasNext()) {
            Node v = it.next();
            if (contains(v))
                result[i++] = v;
        }
        return result;
    }


    /**
     * Puts all nodes into set.
     */
    public void addAll() {
        Iterator it = getOwner().nodeIterator();
        while (it.hasNext())
            add((Node) it.next());
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
        return new IteratorAdapter<>() {
            private Node v = getFirstElement();

            protected Node findNext() throws NoSuchElementException {
                if (v != null) {
                    final Node result = v;
                    v = getNextElement(v);
                    return result;
                } else {
                    throw new NoSuchElementException("at end");
                }
            }
        };
    }

    /**
     * returns the set as many objects as fit into the given array
     *
     * @param objects
     * @return nodes in this set
     */
    public Node[] toArray(Node[] objects) {
        if (objects == null)
            throw new NullPointerException();
        int i = 0;
        for (Node node : this) {
            if (i == objects.length)
                break;
            objects[i++] = node;
        }
        return objects;
    }

    /**
     * todo: is this correct???
     *
     * @param objects
     * @param <T>
     * @return
     */
    public <T> T[] toArray(T[] objects) {
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
     * @param aset
     * @return true, if intersection is non-empty
     */
    public boolean intersects(NodeSet aset) {
        return bits.intersects(aset.bits);
    }
}

// EOF
