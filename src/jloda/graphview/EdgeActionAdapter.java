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

package jloda.graphview;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;

//import jloda.util.*;

/**
 * This provides a class that implements the EdgeAction interface, but does
 * nothing.
 */
public class EdgeActionAdapter implements EdgeActionListener {
    /**
     * Called when creating a new edge.
     *
     * @param e Edge
     */
    public void doNew(Edge e) {
    }

    /**
     * Called when deleting a new edge.
     *
     * @param e Edge
     */
    public void doDelete(Edge e) {
    }

    /**
     * Called when edges are clicked on.
     *
     * @param edges  EdgeSet
     * @param clicks int
     */
    public void doClick(EdgeSet edges, int clicks) {
    }

    /**
     * Called when edges are pressed.
     *
     * @param edges EdgeSet
     */
    public void doPress(EdgeSet edges) {
    }

    /**
     * Called when edges are released.
     *
     * @param edges EdgeSet
     */
    public void doRelease(EdgeSet edges) {
    }

    /**
     * Called when edges are selected.
     *
     * @param edges EdgeSet
     */
    public void doSelect(EdgeSet edges) {
    }

    /**
     * Called when edges are de-selected.
     *
     * @param edges EdgeSet
     */
    public void doDeselect(EdgeSet edges) {
    }

    /**
     * Called when edge labels are clicked on.
     *
     * @param edges  EdgeSet
     * @param clicks int
     */
    public void doClickLabel(EdgeSet edges, int clicks) {
    }

    /**
     * Called when edge labels were moved
     *
     * @param edges EdgeSet
     */
    public void doLabelMoved(EdgeSet edges) {
    }

}

// EOF
