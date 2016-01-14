/**
 * EdgeActionAdapter.java 
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
