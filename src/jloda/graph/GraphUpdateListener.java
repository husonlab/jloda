/*
 * GraphUpdateListener.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/**
 * @version $Id: GraphUpdateListener.java,v 1.4 2005-01-07 14:23:05 huson Exp $
 * @author Daniel Huson
 * 11.02
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
     * @param v
     * @param newLabel
     */
    void nodeLabelChanged(Node v, String newLabel);

    /**
     * edge label changed
     *
     * @param e
     * @param newLabel
     */
    void edgeLabelChanged(Edge e, String newLabel);

}
