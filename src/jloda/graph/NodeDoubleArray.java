/**
 * NodeDoubleArray.java 
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
 * @version $Id: NodeDoubleArray.java,v 1.7 2007-10-23 13:10:53 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Node array
 * Daniel Huson, 2003
 */

public class NodeDoubleArray extends NodeArray<Double> {
    /**
     * Construct a node double array for the given graph and initialize all
     * entries to value.
     *
     * @param g   Graph
     * @param val double
     */
    public NodeDoubleArray(Graph g, double val) {
        super(g, val);
    }

    /**
     * Construct a node double array for the given graph.
     *
     * @param g Graph
     */
    public NodeDoubleArray(Graph g) {
        super(g);
    }

    /**
     * Construct a node double array.
     *
     * @param src NodeDoubleArray
     */
    public NodeDoubleArray(NodeDoubleArray src) {
        super(src);
    }

    /**
     * Construct a node double array.
     *
     * @param src NodeDoubleArray
     */
    public NodeDoubleArray(NodeDoubleMap src) {
        super(src);
    }


    /**
     * Get the entry for node v.
     *
     * @param v Node
     * @return a double value the entry for node v
     */
    public double getValue(Node v) {
        if (super.get(v) == null)
            return 0;
        else
            return (Double) super.get(v);
    }

    /**
     * Set the entry for node v to obj.
     *
     * @param v   Node
     * @param val double
     */
    public void set(Node v, double val) {
        super.set(v, val);
    }


    /**
     * set the entry to the given int value
     *
     * @param v
     * @param value
     */
    public void set(Node v, int value) {
        set(v, (double) value);
    }


    /**
     * Set the entry for all nodes to val.
     *
     * @param val double
     */
    public void setAll(double val) {
        for (Node v = getOwner().getFirstNode(); v != null; v = v.getNext())
            set(v, val);    }
}

// EOF
