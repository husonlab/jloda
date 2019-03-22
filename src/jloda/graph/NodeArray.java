/**
 * NodeArray.java
 * Copyright (C) 2019 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.graph;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Node array
 * Daniel Huson, 2003
 */

public class NodeArray<T> extends GraphBase implements NodeAssociation<T> {
    private T[] data;
    private boolean isClear = true;

    /**
     * Construct a node array.
     *
     * @param g Graph
     */
    public NodeArray(Graph g) {
        setOwner(g);
        data = (T[]) new Object[g.getMaxNodeId() + 1];
        g.registerNodeAssociation(this);
    }

    /**
     * Construct a node array for the given graph and initialize all entries
     * to obj.
     *
     * @param g   Graph
     * @param obj Object
     */
    public NodeArray(Graph g, T obj) {
        this(g);
        setAll(obj);
        isClear = false;
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeArray(NodeAssociation<T> src) {
        this(src.getOwner());
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
            setValue(v, src.getValue(v));
        isClear = src.isClear();
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        if (getOwner().getMaxNodeId() < 0.5 * data.length)
            data = (T[]) new Object[getOwner().getMaxNodeId() + 1];
        else
            for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
                setValue(v, null);
        isClear = true;
    }

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return an Object the entry for node v
     */
    public T getValue(Node v) {
        checkOwner(v);
        if (v.getId() < data.length)
            return data[v.getId()];
        else
            return null;
    }

    public T get(Node v) {
        return getValue(v);
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param obj Object
     */
    public void setValue(Node v, T obj) {
        checkOwner(v);

        if (v.getId() >= data.length) {
            if (obj == null)
                return;
            grow(v.getId());
        }
        data[v.getId()] = obj;
        if (obj != null && isClear)
            isClear = false;
    }

    @Override
    public void put(Node v, T obj) {
        setValue(v, obj);
    }

    /**
     * grows the array. Repeatedly doubles the size of the array until it contains index n
     *
     * @param n index to be included in array
     */
    private void grow(int n) {
        int newSize = Math.max(1, 2 * data.length);
        while (newSize <= n)
            newSize *= 2;
        if (newSize > data.length) {
            T[] newData = (T[]) new Object[newSize];
            for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
                if (v.getId() < data.length)
                    newData[v.getId()] = data[v.getId()];
            data = newData;
        }
    }

    /**
     * Set the entry for all nodes to obj.
     *
     * @param obj Object
     */
    public void setAll(T obj) {
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
            setValue(v, obj);
    }

    /**
     * is array erase, that is, has nothing been set
     *
     * @return true, if erase
     */
    public boolean isClear() {
        return isClear;
    }

    /**
     * create a clone
     *
     * @return clone
     */
    public Object clone() {
        Graph graph = getOwner();
        NodeArray<T> result = new NodeArray<>(graph);
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            result.setValue(v, getValue(v));
        }
        return result;
    }

    /**
     * get an iterator over all non-null values
     *
     * @return iterator
     */
    public Iterable<T> values() {
        return () -> new Iterator<T>() {
            private Node v = getOwner().getFirstNode();

            {
                while (v != null) {
                    if (data[v.getId()] != null)
                        break;
                    v = v.getNext();
                }
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public T next() {
                if (v == null)
                    throw new NoSuchElementException();
                T result = data[v.getId()];
                v = v.getNext();
                while (v != null) {
                    if (data[v.getId()] != null)
                        break;
                    v = v.getNext();
                }
                return result;
            }
        };
    }

    /**
     * get an iterator over all non-null values
     *
     * @return iterator
     */
    public Iterable<Node> keys() {
        return () -> new Iterator<Node>() {
            private Node v = getOwner().getFirstNode();

            {
                while (v != null) {
                    if (data[v.getId()] != null)
                        break;
                    v = v.getNext();
                }
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public Node next() {
                if (v == null)
                    throw new NoSuchElementException();
                Node result = v;
                v = v.getNext();
                while (v != null) {
                    if (data[v.getId()] != null)
                        break;
                    v = v.getNext();
                }
                return result;
            }
        };
    }

}

// EOF
