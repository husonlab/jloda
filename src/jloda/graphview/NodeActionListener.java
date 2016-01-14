/**
 * NodeActionListener.java 
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

/**
 * @version $Id: NodeActionListener.java,v 1.8 2007-09-11 12:33:14 kloepper Exp $
 *
 * Actions performed during interaction.
 *
 * @author Daniel Huson
 * 6.2001
 */

import jloda.graph.Node;
import jloda.graph.NodeSet;

/**
 * Listener for actions performed on nodes during GraphView interaction.
 */
public interface NodeActionListener {
    /**
     * Called when creating a new node.
     *
     * @param v newly created node
     */
    void doNew(Node v);

    /**
     * Called when creating a new node in conjunction with a new edge.
     *
     * @param v the source node
     * @param w the newly created node
     */
    void doNew(Node v, Node w);

    /**
     * Called when deleting a node.
     *
     * @param v node that is about to be deleted
     */
    void doDelete(Node v);

    /**
     * Called when nodes are clicked on.
     *
     * @param nodes  set of nodes that have been clicked
     * @param clicks number of clicks
     */
    void doClick(NodeSet nodes, int clicks);

    /**
     * Called when nodes are pressed.
     *
     * @param nodes set of nodes that have been pressed
     */
    void doPress(NodeSet nodes);

    /**
     * Called when nodes are released.
     *
     * @param nodes set of nodes that have been released
     */
    void doRelease(NodeSet nodes);

    /**
     * Called when nodes are selected.
     *
     * @param nodes set of nodes that have become selected
     */
    void doSelect(NodeSet nodes);

    /**
     * Called when nodes are de-selected.
     *
     * @param nodes set of nodes that have become de-selected
     */
    void doDeselect(NodeSet nodes);

    /**
     * called when node label is clicked on
     *
     * @param nodes
     * @param clicks
     */
    void doClickLabel(NodeSet nodes, int clicks);

    /**
     * called when node label was moved
     *
     * @param nodes
     */
    void doMoveLabel(NodeSet nodes);

    /**
     * called when nodes moved
     */
    void doNodesMoved();
}

// EOF
