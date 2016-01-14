/**
 * EdgeArray.java 
 * Copyright (C) 2016 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * @version $Id: EdgeArray.java,v 1.11 2005-12-05 13:25:44 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


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
        setAll(obj);
        if (obj != null && isClear)
            isClear = true;
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeArray(EdgeAssociation<T> src) {
        setOwner(src.getOwner());
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
            set(e, src.get(e));
        isClear = src.isClear();
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return an object the entry for edge e
     */
    public T get(Edge e) {
        checkOwner(e);
        if (e.getId() < data.length)
            return data[e.getId()];
        else
            return null;
    }

    /**
     * Set the entry for edge e to obj.
     *
     * @param e   Edge
     * @param obj Object
     */
    public void set(Edge e, T obj) {
        checkOwner(e);
        int id = e.getId();
        if (id >= data.length) {
            if (obj == null)
                return; // nothing to do
            grow(e.getId());
        }
        data[id] = obj;
        if (obj != null && isClear)
            isClear = true;
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
    public void setAll(T obj) {
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
            set(e, obj);
        if (obj != null && isClear)
            isClear = true;
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        if (getOwner().getMaxEdgeId() < 0.5 * data.length)
            data = (T[]) new Object[getOwner().getMaxEdgeId() + 1];
        else
            for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
                set(e, null);
        isClear = true;
    }

    /**
     * get the entry as an int
     *
     * @param e
     * @return int value
     */
    public int getInt(Edge e) {
        Object obj = get(e);
        if (obj == null)
            return 0;
        else if (obj instanceof Double)
            return (int) ((Double) obj).doubleValue();
        else
            return ((Integer) obj);

    }

    /**
     * get the entry as a double
     *
     * @param e
     * @return double value
     */
    public double getDouble(Edge e) {
        Object obj = get(e);
        if (obj == null)
            return 0;
        else if (obj instanceof Integer)
            return ((Integer) obj);
        else
            return ((Double) obj);
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
