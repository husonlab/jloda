/**
 * NodeIntegerArray.java 
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
 * @version $Id: NodeIntegerArray.java,v 1.8 2007-11-05 16:59:44 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Node array
 * Daniel Huson, 2003
 */

public class NodeIntegerArray extends NodeArray<Integer> {
    /**
     * Construct a node int array for the given graph and initialize all
     * entries to value.
     *
     * @param g            Graph
     * @param initialValue int
     */
    public NodeIntegerArray(Graph g, int initialValue) {
        super(g, initialValue);
    }

    /**
     * Construct a node int array.
     *
     * @param g Graph
     */
    public NodeIntegerArray(Graph g) {
        super(g);
    }

    /**
     * Construct a node int map.
     *
     * @param src
     */
    public NodeIntegerArray(NodeIntegerArray src) {
        super(src);
    }

    /**
     * Construct a node int map.
     *
     * @param src
     */
    public NodeIntegerArray(NodeIntegerMap src) {
        super(src);
    }

    /**
     * Get the entry for node v.
     *
     * @param v Node
     */
    public int getValue(Node v) {
        if (super.get(v) == null)
            return 0;
        else
            return super.get(v);
    }

    /**
     * set the entry for node v to obj.
     *
     * @param v   Node
     * @param val int
     */
    public void set(Node v, int val) {
        super.set(v, val);
    }

    /**
     * Set the entry for all nodes to val.
     *
     * @param val int
     */
    public void setAll(int val) {
        super.setAll(val);
    }
}

// EOF
