/*
 * NodeIntegerArray.java Copyright (C) 2020. Daniel H. Huson
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
 * Node integer array
 * Daniel Huson, 2003
 */

public class NodeIntegerArray extends GraphBase implements NodeAssociation<Integer> {
    private Integer[] data;
    private boolean isClear = true;
    private Integer defaultValue;

    /**
     * Construct a node array with default value null
     */
    public NodeIntegerArray(Graph g) {
        setOwner(g);
        data = new Integer[g.getMaxNodeId() + 1];
        g.registerNodeAssociation(this);
        defaultValue = null;
    }

    /**
     * Construct a node array for the given graph and set the default value
     */
    public NodeIntegerArray(Graph g, Integer value) {
        this(g);
        defaultValue = value;
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeIntegerArray(NodeAssociation<Integer> src) {
        setOwner(src.getOwner());
        src.getOwner().nodes().forEach(v -> put(v, src.getValue(v)));
        defaultValue = src.getDefaultValue();
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        isClear = true;
        Arrays.fill(data, null);
    }

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return value or null
     */
    public Integer getValue(Node v) {
        checkOwner(v);
        if (v.getId() < data.length && data[v.getId()] != null)
            return data[v.getId()];
        else
            return defaultValue;
    }

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return value or 0
     */
    public int get(Node v) {
        final Integer value = getValue(v);
        if (value != null)
            return value;
        else
            return defaultValue != null ? defaultValue : 0;
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v Node
     * @param k Object
     */
    public void setValue(Node v, Integer k) {
        checkOwner(v);

        if (k != null && isClear)
            isClear = false;

        if (v.getId() >= data.length) {
            grow(v.getId());
        }
        data[v.getId()] = k;
    }

    public void set(Node v, int value) {
        checkOwner(v);

        if (isClear)
            isClear = false;

        if (v.getId() >= data.length) {
            grow(v.getId());
        }
        data[v.getId()] = value;
    }

    @Override
    public void put(Node v, Integer k) {
        setValue(v, k);
    }

    /**
     * grows the array. Repeatedly ints the size of the array until it contains index n
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
     * Set the entry for all nodes to obj.
     *
     * @param k
     */
    public void setAll(Integer k) {
        clear();
        if (k != null && getOwner().getNumberOfNodes() > 0) {
            isClear = false;
            for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext()) {
                if (v.getId() >= data.length) {
                    grow(v.getId());
                }
                data[v.getId()] = k;
            }
        }
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
        NodeIntegerArray result = new NodeIntegerArray(graph);
        result.data = new Integer[data.length];
        System.arraycopy(data, 0, result.data, 0, data.length);
        result.isClear = isClear();
        return result;
    }

    public Iterable<Integer> values() {
        return () -> new Iterator<>() {
            Node v = getOwner().getFirstNode();

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public Integer next() {
                if (v == null)
                    throw new NoSuchElementException();
                Integer value = getValue(v);
                v = v.getNext();
                return value;
            }
        };
    }

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }

}

// EOF
