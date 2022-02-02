/*
 * EdgeActionListener.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.swing.graphview;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;

//import jloda.util.*;

/**
 * Interface for actions performed on adjacentEdges during GraphView interaction.
 */
public interface EdgeActionListener {
    /**
     * Called when creating a new edge.
     *
     * @param e Edge
     */
    void doNew(Edge e);

    /**
     * Called when deleting a new edge.
     *
     * @param e Edge
     */
    void doDelete(Edge e);

    /**
     * Called when adjacentEdges are clicked on.
     *
     * @param edges  EdgeSet
     * @param clicks int
     */
    void doClick(EdgeSet edges, int clicks);

    /**
     * Called when edge labels are clicked on.
     *
     * @param edges  EdgeSet
     * @param clicks int
     */
    void doClickLabel(EdgeSet edges, int clicks);


    /**
     * Called when adjacentEdges are pressed.
     *
     * @param edges EdgeSet
     */
    void doPress(EdgeSet edges);

    /**
     * Called when adjacentEdges are released.
     *
     * @param edges EdgeSet
     */
    void doRelease(EdgeSet edges);

    /**
     * Called when adjacentEdges are selected.
     *
     * @param edges EdgeSet
     */
    void doSelect(EdgeSet edges);

    /**
     * Called when adjacentEdges are de-selected.
     *
     * @param edges EdgeSet
     */
    void doDeselect(EdgeSet edges);

    /**
     * Called when edge labels were moved
     *
     * @param edges EdgeSet
     */
    void doLabelMoved(EdgeSet edges);

}

// EOF
