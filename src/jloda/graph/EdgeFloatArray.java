/*
 *  Copyright (C) 2019 Daniel H. Huson
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
 * edge float array
 * Daniel Huson, 11.2017
 */
public class EdgeFloatArray extends GraphBase implements EdgeAssociation<Float> {
    private float data[];
    private boolean isClear = true;

    /**
     * Construct an edge array.
     *
     * @param g Graph
     */
    public EdgeFloatArray(Graph g) {
        setOwner(g);
        data = new float[g.getMaxEdgeId() + 1];
        g.registerEdgeAssociation(this);
    }

    /**
     * Construct an edge array for the given graph and initialize all entries
     * to obj.
     *
     * @param g   Graph
     * @param obj Object
     */
    public EdgeFloatArray(Graph g, Float obj) {
        this(g);
        setAll(obj);
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeFloatArray(EdgeAssociation<Float> src) {
        setOwner(src.getOwner());
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
            put(e, src.getValue(e));
        isClear = src.isClear();
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        for (int i = 0; i < data.length; i++)
            data[i] = 0;
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
        if (e.getId() < data.length)
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
    public void setValue(Edge e, Float obj) {
        checkOwner(e);
        if (obj == null)
            obj = 0f;
        else if (isClear)
            isClear = false;

        if (e.getId() >= data.length) {
            grow(e.getId());
        }
        data[e.getId()] = obj;
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
    public void setAll(Float obj) {
        if (obj == null)
            obj = 0f;
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext()) {
            if (e.getId() >= data.length) {
                grow(e.getId());
            }
            data[e.getId()] = obj;
        }
        isClear = (obj == 0f);
    }

    /**
     * grows the array. Repeatedly doubles the size of the array until it contains index n
     *
     * @param n index to be included in array
     */
    private void grow(float n) {
        int newSize = Math.max(1, 2 * data.length);
        while (newSize <= n)
            newSize *= 2;
        if (newSize > data.length) {
            float[] newData = new float[newSize];
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
