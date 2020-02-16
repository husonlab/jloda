/*
 * EdgeFloatArray.java Copyright (C) 2020. Daniel H. Huson
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
 * edge float array
 * Daniel Huson, 11.2017
 */
public class EdgeFloatArray extends GraphBase implements EdgeAssociation<Float> {
    private Float[] data;
    private boolean isClear = true;

    /**
     * Construct an edge array.
     *
     * @param g Graph
     */
    public EdgeFloatArray(Graph g) {
        setOwner(g);
        data = new Float[g.getMaxEdgeId() + 1];
        g.registerEdgeAssociation(this);
    }

    /**
     * Construct an edge array for the given graph and initialize all entries
     * to obj.
     *
     * @param g     Graph
     * @param value Object
     */
    public EdgeFloatArray(Graph g, Float value) {
        this(g);
        setAll(value);
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeFloatArray(EdgeAssociation<Float> src) {
        setOwner(src.getOwner());
        src.getOwner().edges().forEach(e -> put(e, src.getValue(e)));
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        Arrays.fill(data, null);
        isClear = true;
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return float or null
     */
    public Float getValue(Edge e) {
        checkOwner(e);
        if (e.getId() < data.length)
            return data[e.getId()];
        else
            return null;
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return float or 0
     */
    public float get(Edge e) {
        checkOwner(e);
        if (e.getId() < data.length && data[e.getId()] != null)
            return data[e.getId()];
        else
            return 0f;
    }


    /**
     * Set the entry for edge e to obj.
     *
     * @param e   Edge
     * @param obj Object
     */
    public void put(Edge e, Float obj) {
        setValue(e, obj);
    }

    @Override
    public void setValue(Edge e, Float f) {
        checkOwner(e);
        if (f != null && isClear)
            isClear = false;

        if (e.getId() >= data.length) {
            grow(e.getId());
        }
        data[e.getId()] = f;
    }

    public void set(Edge e, float value) {
        checkOwner(e);
        if (isClear)
            isClear = false;

        if (e.getId() >= data.length) {
            grow(e.getId());
        }
        data[e.getId()] = value;
    }

    @Override
    public void setAll(Float value) {
        clear();
        if (value != null && getOwner().getNumberOfEdges() > 0) {
            isClear = false;
            getOwner().edges().forEach(e -> put(e, value));
        }
    }

    /**
     * grows the array. Repeatedly doubles the size of the array until it contains index n
     *
     * @param n index to be included in array
     */
    private void grow(float n) {
        int newSize = Math.max(1, 2 * data.length);
        while (newSize <= n && 2L * newSize < (long) Basic.MAX_ARRAY_SIZE) {
            newSize *= 2;
        }
        if (newSize > data.length) {
            Float[] newData = new Float[newSize];
            for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext()) {
                int id = e.getId();
                if (id < data.length)
                    newData[id] = data[id];
            }
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
}

// EOF
