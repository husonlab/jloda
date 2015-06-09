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
 * @version $Id: EdgeIntegerMap.java,v 1.2 2005-12-05 13:25:44 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Edge integer map
 *
 *  @author Daniel Huson, 2003
 */

public class EdgeIntegerMap extends EdgeMap<Integer> {
    /**
     * Construct an edge int map for the given graph and initialize all
     * entries to value.
     *
     * @param g   Graph
     * @param val int
     */
    public EdgeIntegerMap(Graph g, int val) {
        super(g, val);
    }

    /**
     * Construct an edge int map.
     *
     * @param g Graph
     */
    public EdgeIntegerMap(Graph g) {
        super(g);
    }

    /**
     * Construct an edge int map.
     *
     * @param src
     */
    public EdgeIntegerMap(EdgeIntegerMap src) {
        super(src);
    }

    /**
     * Construct an edge int map.
     *
     * @param src
     */
    public EdgeIntegerMap(EdgeIntegerArray src) {
        super(src);
    }

    /**
     * Get the entry for edge e.
     *
     * @param e Edge
     * @return an integer value the entry for edge e
     */
    public int getValue(Edge e) {
        if (super.get(e) == null)
            return 0;
        else
            return super.get(e);
    }

    /**
     * Set the entry for edge e to obj.
     *
     * @param e     Edge
     * @param value int
     */
    public void set(Edge e, int value) {
        super.set(e, value);
    }

    /**
     * Set the entry for all edges.
     *
     * @param val int
     */
    public void setAll(int val) {
        super.setAll(val);
    }
}

// EOF
