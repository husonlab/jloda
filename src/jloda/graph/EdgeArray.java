/*
 * EdgeArray.java
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
 * Edge array
 * Daniel Huson 2004
 */

public class EdgeArray<T> extends GraphBase implements EdgeAssociation<T> {
    private T data[];
    private boolean isClear = true;

    /**
     * Construct an edge array.
     *
     * @param g Graph
     */
    public EdgeArray(Graph g) {
        setOwner(g);
        data = (T[]) new Object[g.getMaxEdgeId() + 1];
        g.registerEdgeAssociation(this);
    }

    /**
     * Construct an edge array for the given graph and initialize all entries
     * to obj.
     *
     * @param g   Graph
     * @param obj Object
     */
    public EdgeArray(Graph g, T obj) {
        this(g);
        putAll(obj);
        if (obj != null && isClear)
            isClear = false;
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeArray(EdgeAssociation<T> src) {
        setOwner(src.getOwner());
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
            put(e, src.getValue(e));
        isClear = src.isClear();
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return an object the entry for edge e
     */
    public T getValue(Edge e) {
        checkOwner(e);
        if (e.getId() < data.length)
            return data[e.getId()];
        else
            return null;
    }

    public T get(Edge e) {
        return getValue(e);
    }

    /**
     * Set the entry for edge e to obj.
     *
     * @param e   Edge
     * @param obj Object
     */
    public void put(Edge e, T obj) {
        checkOwner(e);
        int id = e.getId();
        if (id >= data.length) {
            if (obj == null)
                return; // nothing to do
            grow(e.getId());
        }
        data[id] = obj;
        if (obj != null && isClear)
            isClear = false;
    }

    @Override
    public void setValue(Edge e, T obj) {
        this.put(e, obj);
    }

    @Override
    public void setAll(T obj) {
        this.putAll(obj);
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
            for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext()) {
                int id = e.getId();
                if (id < data.length)
                    newData[id] = data[id];
            }
            data = newData;
        }
    }

    /**
     * Set the entry for all edges.
     *
     * @param obj Object
     */
    public void putAll(T obj) {
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
            put(e, obj);
        if (obj != null && isClear)
            isClear = false;
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        if (getOwner().getMaxEdgeId() < 0.5 * data.length)
            data = (T[]) new Object[getOwner().getMaxEdgeId() + 1];
        else
            for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
                put(e, null);
        isClear = true;
    }

    /**
     * is clean, that is, has never been set since last erase
     *
     * @return true, if erase
     */
    public boolean isClear() {
        return isClear;
    }

    /**
     * get an iterator over all non-null values
     *
     * @return iterator
     */
    public Iterable<T> values() {
        return () -> new Iterator<T>() {
            private Edge e = getOwner().getFirstEdge();

            {
                while (e != null) {
                    if (data[e.getId()] != null)
                        break;
                    e = e.getNext();
                }
            }

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public T next() {
                if (e == null)
                    throw new NoSuchElementException();
                T result = data[e.getId()];
                e = e.getNext();
                {
                    while (e != null) {
                        if (data[e.getId()] != null)
                            break;
                        e = e.getNext();
                    }
                }
                return result;
            }
        };
    }
}


// EOF
