/*
 * EdgeIntegerArray.java Copyright (C) 2020. Daniel H. Huson
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
 * edge float array
 * Daniel Huson, 2003
 */
public class EdgeIntegerArray extends GraphBase implements EdgeAssociation<Integer> {
    private Integer[] data;
    private boolean isClear = true;

    /**
     * Construct an edge array.
     *
     * @param g Graph
     */
    public EdgeIntegerArray(Graph g) {
        setOwner(g);
        data = new Integer[g.getMaxEdgeId() + 1];
        g.registerEdgeAssociation(this);
    }

    /**
     * Construct an edge array for the given graph and initialize all entries
     * to obj.
     *
     * @param g     Graph
     * @param value Object
     */
    public EdgeIntegerArray(Graph g, Integer value) {
        this(g);
        setAll(value);
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeIntegerArray(EdgeAssociation<Integer> src) {
        setOwner(src.getOwner());
        src.getOwner().edges().forEach(e -> put(e, src.getValue(e)));
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        isClear = true;
        Arrays.fill(data, null);
    }


    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return integer or null
     */
    public Integer getValue(Edge e) {
        checkOwner(e);
        if (e.getId() < data.length)
            return data[e.getId()];
        else
            return null;
    }

    /**
     * get the entry for edge e
     *
     * @param e
     * @return integer or 0
     */
    public int get(Edge e) {
        checkOwner(e);
        if (e.getId() < data.length && data[e.getId()] != null)
            return data[e.getId()];
        else
            return 0;
    }

    /**
     * Set the entry for edge e to obj.
     *
     * @param e     Edge
     * @param value Object
     */
    public void setValue(Edge e, Integer value) {
        checkOwner(e);

        if (value != null && isClear)
            isClear = false;

        if (e.getId() >= data.length) {
            grow(e.getId());
        }
        data[e.getId()] = value;
    }

    public void set(Edge e, int value) {
        checkOwner(e);
        if (isClear)
            isClear = false;

        if (e.getId() >= data.length) {
            grow(e.getId());
        }
        data[e.getId()] = value;
    }

    @Override
    public void put(Edge e, Integer obj) {
        this.setValue(e, obj);
    }

    @Override
    public void setAll(Integer value) {
        clear();
        if (value != null && getOwner().getNumberOfEdges() > 0) {
            isClear = false;
            for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext()) {
                if (e.getId() >= data.length) {
                    grow(e.getId());
                }
                data[e.getId()] = value;
            }
        }
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
            Integer[] newData = new Integer[newSize];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
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
     * increase the count by one.
     *
     * @param e
     */
    public void increment(Edge e) {
        set(e, get(e) + 1);
    }

    /**
     * increase the count by the given value
     *
     * @param e
     */
    public void increment(Edge e, int value) {
        set(e, get(e) + value);
    }

    /**
     * decrease the count by one.
     *
     * @param e
     */
    public void decrement(Edge e) {
        set(e, get(e) - 1);
    }

    /**
     * decrease the count by the given value
     *
     * @param e
     */
    public void decrement(Edge e, int value) {
        set(e, get(e) - value);
    }

    public Iterable<Integer> values() {
        return () -> new Iterator<>() {
            Edge e = getOwner().getFirstEdge();

            @Override
            public boolean hasNext() {
                return e != null;
            }

            @Override
            public Integer next() {
                if (e == null)
                    throw new NoSuchElementException();
                Integer value = getValue(e);
                e = e.getNext();
                return value;
            }
        };
    }
}

// EOF
