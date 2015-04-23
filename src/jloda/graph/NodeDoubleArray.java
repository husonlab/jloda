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
 * @version $Id: NodeDoubleArray.java,v 1.7 2007-10-23 13:10:53 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Node array
 */

public class NodeDoubleArray extends NodeArray {
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
