/*
 * EdgeArray.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.graph;


import jloda.util.Basic;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Edge array
 * Daniel Huson 2004
 */

public class EdgeArray<T> extends GraphBase implements EdgeAssociation<T> {
    private T[] data;
    private boolean isClear = true;
    private T defaultValue;

    /**
     * Construct an edge array with default value null
     */
    public EdgeArray(Graph g) {
        setOwner(g);
        data = (T[]) new Object[g.getMaxEdgeId() + 1];
        g.registerEdgeAssociation(this);
    }

    /**
     * Construct an edge array for the given graph and set the default value
     */
    public EdgeArray(Graph g, T defaultValue) {
        this(g);
        this.defaultValue = defaultValue;
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeArray(EdgeAssociation<T> src) {
        setOwner(src.getOwner());
        getOwner().edges().forEach(e -> put(e, src.getValue(e)));
        defaultValue = src.getDefaultValue();
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return an object the entry for edge e
     */
    public T getValue(Edge e) {
        checkOwner(e);
        if (e.getId() < data.length && data[e.getId()] != null)
            return data[e.getId()];
        else
            return defaultValue;
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
        while (newSize <= n && 2L * newSize < (long) Basic.MAX_ARRAY_SIZE) {
            newSize *= 2;
        }
        if (newSize > data.length) {
            T[] newData = (T[]) new Object[newSize];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    /**
     * Set the entry for all edges.
     *
     * @param value Object
     */
    public void putAll(T value) {
        clear();
        if (value != null && getOwner().getNumberOfEdges() > 0) {
            isClear = false;
            getOwner().edges().forEach(e -> put(e, value));
        }
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        Arrays.fill(data, null);
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
        return () -> new Iterator<>() {
            private Edge e = getOwner().getFirstEdge();

            {
                while (e != null) {
                    if (e.getId() < data.length && data[e.getId()] != null)
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
                        if (e.getId() < data.length && data[e.getId()] != null)
                            break;
                        e = e.getNext();
                    }
                }
                return result;
            }
        };
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }
}


// EOF
