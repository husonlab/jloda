/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

/**
 * @version $Id: EdgeMap.java,v 1.2 2005-12-05 13:25:44 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Edge map
 */

public class EdgeMap<T> extends GraphBase implements EdgeAssociation<T> {
    private Map<Edge, T> data;
    private boolean isClear;

    /**
     * Construct an edge map.
     *
     * @param g Graph
     */
    public EdgeMap(Graph g) {
        setOwner(g);
        g.registerEdgeAssociation(this);
        data = new HashMap<>();
        isClear = true;
    }

    /**
     * Construct an edge map for the given graph and initialize all entries
     * to obj.
     *
     * @param g   Graph
     * @param obj Object
     */
    public EdgeMap(Graph g, T obj) {
        this(g);
        setAll(obj);
        if (obj != null && isClear)
            isClear = false;
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeMap
     */
    public EdgeMap(EdgeAssociation<T> src) {
        Graph G = src.getOwner();
        setOwner(G);
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
        return data.get(e);
    }

    /**
     * Set the entry for edge e to obj.
     *
     * @param e   Edge
     * @param obj Object
     */
    public void set(Edge e, T obj) {
        checkOwner(e);
        data.put(e, obj);
        if (obj != null && isClear)
            isClear = false;
    }

    /**
     * Set the entry for all edges.
     *
     * @param obj Object
     */
    public void setAll(T obj) {
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
            data.put(e, obj);
        if (obj != null && isClear)
            isClear = false;

    }

    /**
     * Clear all entries.
     */
    public void clear() {
        for (Edge e = getOwner().getFirstEdge(); e != null; e = e.getNext())
            data.remove(e);
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
            return (Integer) obj;

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
