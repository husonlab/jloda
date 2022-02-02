/*
 * GraphUpdateListener.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.graph;

/**
 * graph update listener
 * Daniel Huson, 2003
 */
public interface GraphUpdateListener {
    /**
     * A node has been created
     *
     * @param v the new node
     */
    void newNode(Node v);

    /**
     * A node is about to be deleted
     *
     * @param v the node that will be deleted
     */
    void deleteNode(Node v);

    /**
     * An edge has been created
     *
     * @param e the new edge
     */
    void newEdge(Edge e);

    /**
     * An edge is about to be deleted
     *
     * @param e the edge that will be deleted
     */
    void deleteEdge(Edge e);

    /**
     * The graph has changed.
     * This method is called after one of the above specific methods has be
     * called
     */
    void graphHasChanged();

    /**
     * node label changed
     *
	 */
    void nodeLabelChanged(Node v, String newLabel);

    /**
     * edge label changed
     *
	 */
    void edgeLabelChanged(Edge e, String newLabel);

}
