/*
 *  Copyright (C) 2015 Daniel H. Huson
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
/**
 * @version $Id: EdgeFloatMap.java,v 1.2 2005-12-05 13:25:44 huson Exp $
 * @author Daniel Huson
 */
package jloda.graph;


/**
 * Edge float map
 * Daniel Huson, 2003
 */

public class EdgeFloatMap extends EdgeMap {
    /**
     * Construct an edge float map for the given graph.
     *
     * @param g Graph
     */
    public EdgeFloatMap(Graph g) {
        super(g);
    }

    /**
     * Construct an edge float map for the given graph and initialize all
     * entries to value.
     *
     * @param g   Graph
     * @param val float
     */
    public EdgeFloatMap(Graph g, float val) {
        super(g, val);
    }

    /**
     * copy constructor
     *
     * @param src
     */
    public EdgeFloatMap(EdgeFloatArray src) {
        super(src);
    }

    /**
     * copy constructor
     *
     * @param src
     */
    public EdgeFloatMap(EdgeFloatMap src) {
        super(src);
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return a float value the entry for edge e
     */
    public float getValue(Edge e) {
        if (super.get(e) == null)
            return 0;
        else
            return (Float) super.get(e);
    }

    /**
     * Set the entry for edge e to obj.
     *
     * @param e     Edge
     * @param value float
     */
    public void set(Edge e, float value) {
        super.set(e, value);
    }

    /**
     * Set the entry for all edges.
     *
     * @param val float
     */
    public void setAll(float val) {
        super.setAll(val);
    }
}

// EOF
