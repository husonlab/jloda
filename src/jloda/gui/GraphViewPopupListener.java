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

package jloda.gui;

import jloda.graph.EdgeSet;
import jloda.graph.NodeSet;
import jloda.graphview.GraphView;
import jloda.graphview.IPopupListener;
import jloda.gui.commands.CommandManager;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * constructs a graph popuplistener
 * Daniel Huson, 8.2010
 */
public class GraphViewPopupListener implements IPopupListener {
    final JPopupMenu nodeMenu;
    //JPopupMenu nodeLabelMenu;
    final JPopupMenu edgeMenu;
    //JPopupMenu EdgeLabelMenu;
    final JPopupMenu panelMenu;
    private final GraphView viewer;

    /**
     * construct the popup menus
     *
     * @param viewer
     * @param nodeConfig
     * @param edgeConfig
     * @param panelConfig
     * @param commandManager
     */
    public GraphViewPopupListener(GraphView viewer, String nodeConfig, String edgeConfig, String panelConfig, CommandManager commandManager) {
        this.viewer = viewer;
        nodeMenu = new PopupMenu(nodeConfig, commandManager);
        edgeMenu = new PopupMenu(edgeConfig, commandManager);
        panelMenu = new PopupMenu(panelConfig, commandManager);
    }

    /**
     * popup menu on node
     *
     * @param me
     * @param nodes
     */
    public void doNodePopup(MouseEvent me, NodeSet nodes) {
        if (nodes.size() != 0) {
            /*
            if (me.isShiftDown() == false) {
                viewer.selectAllNodes(false);
                viewer.selectAllEdges(false);
            }
            if (!viewer.getSelected(nodes.getFirstElement()))
                viewer.setSelected(nodes.getFirstElement(), true);
                */
            nodeMenu.show(me.getComponent(), me.getX(), me.getY());
            viewer.repaint(); // stuff gets messed up
        }
    }

    /**
     * popup menu on node label
     *
     * @param me
     * @param nodes
     */
    public void doNodeLabelPopup(MouseEvent me, NodeSet nodes) {
        doNodePopup(me, nodes);
    }

    /**
     * popup menu on edge
     *
     * @param me
     * @param edges
     */
    public void doEdgePopup(MouseEvent me, EdgeSet edges) {
        if (edges.size() != 0) {
            /*
            if (me.isShiftDown() == false) {
                viewer.selectAllNodes(false);
                viewer.selectAllEdges(false);
            }
            if (!viewer.getSelected(edges.getFirstElement()))
                viewer.setSelected(edges.getFirstElement(), true);
                */
            edgeMenu.show(me.getComponent(), me.getX(), me.getY());
            viewer.repaint(); // stuff gets messed up
        }
    }

    /**
     * popup menu on edge
     *
     * @param me
     * @param edges
     */
    public void doEdgeLabelPopup(MouseEvent me, EdgeSet edges) {
        doEdgePopup(me, edges);
    }

    /**
     * popup menu not on graph
     *
     * @param me
     */
    public void doPanelPopup(MouseEvent me) {
        panelMenu.show(me.getComponent(), me.getX(), me.getY());
    }

}
