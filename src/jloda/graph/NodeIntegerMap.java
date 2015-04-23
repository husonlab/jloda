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
 * @version $Id: NodeIntegerMap.java,v 1.2 2005-12-05 13:25:44 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Node array
 */

public class NodeIntegerMap extends NodeMap<Integer> {
    /**
     * Construct a node int array for the given graph and initialize all
     * entries to value.
     *
     * @param g   Graph
     * @param val int
     */
    public NodeIntegerMap(Graph g, int val) {
        super(g, val);
    }

    /**
     * Construct a node int array.
     *
     * @param g Graph
     */
    public NodeIntegerMap(Graph g) {
        super(g);
    }

    /**
     * Construct a node int map.
     *
     * @param src
     */
    public NodeIntegerMap(NodeIntegerArray src) {
        super(src);
    }

    /**
     * Construct a node int map.
     *
     * @param src
     */
    public NodeIntegerMap(NodeIntegerMap src) {
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
