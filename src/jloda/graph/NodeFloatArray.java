/*
 * NodeFloatArray.java Copyright (C) 2020. Daniel H. Huson
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
 * Node float array
 * Daniel Huson, 2003
 */

public class NodeFloatArray extends GraphBase implements NodeAssociation<Float> {
    private Float[] data;
    private boolean isClear = true;

    /**
     * Construct a node array.
     *
     * @param g Graph
     */
    public NodeFloatArray(Graph g) {
        setOwner(g);
        data = new Float[g.getMaxNodeId() + 1];
        g.registerNodeAssociation(this);
    }

    /**
     * Construct a node array for the given graph and initialize all entries
     * to obj.
     *
     * @param g Graph
     * @param f Object
     */
    public NodeFloatArray(Graph g, Float f) {
        this(g);
        setAll(f);
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeFloatArray(NodeAssociation<Float> src) {
        setOwner(src.getOwner());
        src.getOwner().nodes().forEach(v -> put(v, src.getValue(v)));
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        Arrays.fill(data, 0);
        isClear = true;
    }

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return an Object the entry for node v
     */
    public Float getValue(Node v) {
        checkOwner(v);
        if (v.getId() < data.length)
            return data[v.getId()];
        else
            return null;
    }

    public float get(Node v) {
        checkOwner(v);
        if (v.getId() < data.length && data[v.getId()] != null)
            return data[v.getId()];
        else
            return 0f;
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param value Object
     */
    public void setValue(Node v, Float value) {
        checkOwner(v);

        if (value != null && isClear)
            isClear = false;

        if (v.getId() >= data.length) {
            grow(v.getId());
        }
        data[v.getId()] = value;
    }

    public void set(Node v, float value) {
        checkOwner(v);
        if (isClear)
            isClear = false;

        if (v.getId() >= data.length) {
            grow(v.getId());
        }
        data[v.getId()] = value;
    }

    @Override
    public void put(Node v, Float value) {
        setValue(v, value);
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
            Float[] newData = new Float[newSize];
            for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
                if (v.getId() < data.length)
                    newData[v.getId()] = data[v.getId()];
            data = newData;
        }
    }

    /**
     * Set the entry for all nodes to obj.
     *
     * @param value Object
     */
    public void setAll(Float value) {
        clear();
        if (value != null && getOwner().getNumberOfNodes() > 0) {
            isClear = false;
            for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext()) {
                if (v.getId() >= data.length) {
                    grow(v.getId());
                }
                data[v.getId()] = value;
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
        NodeFloatArray result = new NodeFloatArray(graph);
        result.data = new Float[data.length];
        System.arraycopy(data, 0, result.data, 0, data.length);
        result.isClear = isClear();
        return result;
    }
}

// EOF
