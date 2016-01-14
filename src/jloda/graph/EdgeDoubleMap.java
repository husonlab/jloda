/**
 * EdgeDoubleMap.java 
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
 * @version $Id: EdgeDoubleMap.java,v 1.2 2005-12-05 13:25:44 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Edge double map
 * Daniel Huson, 2003
 */

public class EdgeDoubleMap extends EdgeMap {
    /**
     * Construct an edge double map for the given graph.
     *
     * @param g Graph
     */
    public EdgeDoubleMap(Graph g) {
        super(g);
    }

    /**
     * Construct an edge double map for the given graph and initialize all
     * entries to value.
     *
     * @param g   Graph
     * @param val double
     */
    public EdgeDoubleMap(Graph g, double val) {
        super(g, val);
    }

    /**
     * copy constructor
     *
     * @param src
     */
    public EdgeDoubleMap(EdgeDoubleArray src) {
        super(src);
    }

    /**
     * copy constructor
     *
     * @param src
     */
    public EdgeDoubleMap(EdgeDoubleMap src) {
        super(src);
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return a double value the entry for edge e
     */
    public double getValue(Edge e) {
        if (super.get(e) == null)
            return 0;
        else
            return (Double) super.get(e);
    }

    /**
     * Set the entry for edge e to obj.
     *
     * @param e     Edge
     * @param value double
     */
    public void set(Edge e, double value) {
        super.set(e, value);
    }

    /**
     * Set the entry for all edges.
     *
     * @param val double
     */
    public void setAll(double val) {
        super.setAll(val);
    }
}

// EOF
