/**
 * NodeMap.java 
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
 * @version $Id: NodeMap.java,v 1.2 2005-12-05 13:25:45 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Node map
 * Daniel Huson, 2003
 */

public class NodeMap<T> extends GraphBase implements NodeAssociation<T> {
    private final Map<Node, T> data;
    private boolean isClear;

    /**
     * Construct a node map.
     *
     * @param g Graph
     */
    public NodeMap(Graph g) {
        setOwner(g);
        data = new HashMap<>();
        g.registerNodeAssociation(this);
        isClear = true;
    }


    /**
     * Construct a node map for the given graph and initialize all entries
     * to obj.
     *
     * @param g   Graph
     * @param obj Object
     */
    public NodeMap(Graph g, T obj) {
        this(g);
        setAll(obj);
        if (obj != null && isClear)
            isClear = false;
    }

    /**
     * Copy constructor.
     *
     * @param src NodeAssociation
     */
    public NodeMap(NodeAssociation<T> src) {
        this(src.getOwner());
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
            set(v, src.get(v));
        isClear = src.isClear();
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
            data.remove(v);
        isClear = true;
    }

    /**
     * Get the entry for node v or the default object
     *
     * @param v Node
     * @return an Object the entry for node v
     */
    public T get(Node v) {
        checkOwner(v);
        return data.get(v);
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param obj Object
     */
    public void set(Node v, T obj) {
        checkOwner(v);
        data.put(v, obj);
        if (obj != null && isClear)
            isClear = false;
    }

    /**
     * Set the entry for all nodes to obj.
     *
     * @param obj Object
     */
    public void setAll(T obj) {
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
            data.put(v, obj);
        if (obj != null && isClear)
            isClear = false;
    }

    /**
     * get the entry as an int
     *
     * @param v
     * @return int value
     */
    public int getInt(Node v) {
        Object obj = get(v);
        if (obj == null)
            return 0;
        else if (obj instanceof Double)
            return (int) ((Double) obj).doubleValue();
        else
            return (Integer) obj;

    }

    /**
     * get the entry as a double
     *
     * @param v
     * @return double value
     */
    public double getDouble(Node v) {
        Object obj = get(v);
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
