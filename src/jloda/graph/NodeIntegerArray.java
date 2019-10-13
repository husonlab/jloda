/*
 * NodeIntegerArray.java Copyright (C) 2019. Daniel H. Huson
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


/**
 * Node integer array
 * Daniel Huson, 2003
 */

public class NodeIntegerArray extends GraphBase implements NodeAssociation<Integer> {
    private int[] data;
    private boolean isClear = true;

    /**
     * Construct a node array.
     *
     * @param g Graph
     */
    public NodeIntegerArray(Graph g) {
        setOwner(g);
        data = new int[g.getMaxNodeId() + 1];
        g.registerNodeAssociation(this);
    }

    /**
     * Construct a node array for the given graph and initialize all entries
     * to obj.
     *
     * @param g   Graph
     * @param obj Object
     */
    public NodeIntegerArray(Graph g, Integer obj) {
        this(g);
        setAll(obj);
    }

    /**
     * Copy constructor.
     *
     * @param src NodeArray
     */
    public NodeIntegerArray(NodeAssociation<Integer> src) {
        this(src.getOwner());
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
            setValue(v, src.getValue(v));
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        isClear = true;
    }

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return value or null
     */
    public Integer getValue(Node v) {
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
    public int get(Node v) {
        checkOwner(v);
        if (v.getId() < data.length)
            return data[v.getId()];
        else
            return 0;
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param obj Object
     */
    public void setValue(Node v, Integer obj) {
        checkOwner(v);

        if (obj == null)
            obj = 0;
        else if (isClear)
            isClear = false;

        if (v.getId() >= data.length) {
            grow(v.getId());
        }
        data[v.getId()] = obj;
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
    public void put(Node v, Integer obj) {
        setValue(v, obj);
    }

    /**
     * grows the array. Repeatedly ints the size of the array until it contains index n
     *
     * @param n index to be included in array
     */
    private void grow(int n) {
        int newSize = Math.max(1, 2 * data.length);
        while (newSize <= n)
            newSize *= 2;
        if (newSize > data.length) {
            int[] newData = new int[newSize];
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
    public void setAll(Integer obj) {
        if (obj == null)
            obj = 0;
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext()) {
            if (v.getId() >= data.length) {
                grow(v.getId());
            }
            data[v.getId()] = obj;
        }
        isClear = (obj == 0);
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
        result.data = new int[data.length];
        System.arraycopy(data, 0, result.data, 0, data.length);
        return result;
    }
}

// EOF
