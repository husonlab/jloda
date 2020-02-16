/*
 * NodeDoubleArray.java Copyright (C) 2020. Daniel H. Huson
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

/**
 * Node array
 * Daniel Huson, 2003
 */

public class NodeDoubleArray extends GraphBase implements NodeAssociation<Double> {
    private Double[] data;
    private boolean isClear = true;

    /**
     * Construct a node array.
     *
     * @param g Graph
     */
    public NodeDoubleArray(Graph g) {
        setOwner(g);
        data = new Double[g.getMaxNodeId() + 1];
        g.registerNodeAssociation(this);
    }

    /**
     * Construct a node array for the given graph and initialize all entries
     * to obj.
     *
     * @param g     Graph
     * @param value Object
     */
    public NodeDoubleArray(Graph g, Double value) {
        this(g);
        setAll(value);
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeDoubleArray(NodeAssociation<Double> src) {
        this(src.getOwner());
        getOwner().nodeStream().forEach(e -> put(e, src.getValue(e)));
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        Arrays.fill(data, null);
        isClear = true;
    }

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return value or null
     */
    public Double getValue(Node v) {
        checkOwner(v);
        if (v.getId() < data.length)
            return data[v.getId()];
        else
            return null;
    }


    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return value or 0
     */
    public double get(Node v) {
        checkOwner(v);
        if (v.getId() < data.length && data[v.getId()] != null)
            return data[v.getId()];
        else
            return 0.0;
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v Node
     * @param d Object
     */
    public void setValue(Node v, Double d) {
        checkOwner(v);

        if (d != null && isClear)
            isClear = false;

        if (v.getId() >= data.length) {
            grow(v.getId());
        }
        data[v.getId()] = d;
    }

    public void set(Node v, double d) {
        if (isClear)
            isClear = false;
        if (v.getId() >= data.length) {
            grow(v.getId());
        }
        data[v.getId()] = d;
    }


    @Override
    public void put(Node v, Double obj) {
        setValue(v, obj);
    }

    /**
     * Set the entry for all nodes to obj.
     *
     * @param d Object
     */
    public void setAll(Double d) {
        clear();
        if (d != null && getOwner().getNumberOfNodes() > 0) {
            isClear = false;
            for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext()) {
                if (v.getId() >= data.length) {
                    grow(v.getId());
                }
                data[v.getId()] = d;
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
            Double[] newData = new Double[newSize];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
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
        NodeDoubleArray result = new NodeDoubleArray(graph);
        result.data = new Double[data.length];
        System.arraycopy(data, 0, result.data, 0, data.length);
        return result;
    }
}

// EOF
