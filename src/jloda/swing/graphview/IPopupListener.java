/*
 * IPopupListener.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.graph.EdgeSet;
import jloda.graph.NodeSet;

import java.awt.event.MouseEvent;

/**
 * listen for popmenu events
 */
public interface IPopupListener {
    /**
     * popup menu on node
     *
	 */
    void doNodePopup(MouseEvent me, NodeSet nodes);

    /**
     * popup menu on node label
     *
	 */
    void doNodeLabelPopup(MouseEvent me, NodeSet nodes);

    /**
     * popup menu on edge
     *
	 */
    void doEdgePopup(MouseEvent me, EdgeSet edges);

    /**
     * popup menu on edge
     *
	 */
    void doEdgeLabelPopup(MouseEvent me, EdgeSet edges);

    /**
     * popup menu not on graph
     *
	 */
    void doPanelPopup(MouseEvent me);

}
