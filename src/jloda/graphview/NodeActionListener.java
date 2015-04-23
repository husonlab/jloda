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
