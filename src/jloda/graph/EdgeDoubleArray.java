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
 * @version $Id: EdgeDoubleArray.java,v 1.7 2005-12-05 13:25:44 huson Exp $
 *
 * @author Daniel Huson
 *
 */
package jloda.graph;


/**
 * Edge double array
 * Daniel Huson, 2003
 */

public class EdgeDoubleArray extends EdgeArray<Double> {
    /**
     * Construct an edge double array for the given graph.
     *
     * @param g Graph
     */
    public EdgeDoubleArray(Graph g) {
        super(g);
    }

    /**
     * Construct an edge double array for the given graph and initialize all
     * entries to value.
     *
     * @param g   Graph
     * @param val double
     */
    public EdgeDoubleArray(Graph g, double val) {
        super(g, val);
    }

    /**
     * copy constructor
     *
     * @param src
     */
    public EdgeDoubleArray(EdgeDoubleArray src) {
        super(src);
    }

    /**
     * copy constructor
     *
     * @param src
     */
    public EdgeDoubleArray(EdgeDoubleMap src) {
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
            return super.get(e);
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
