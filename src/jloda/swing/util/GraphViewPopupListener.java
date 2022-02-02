/*
 * GraphViewPopupListener.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.swing.util;

import jloda.graph.EdgeSet;
import jloda.graph.NodeSet;
import jloda.swing.commands.CommandManager;
import jloda.swing.graphview.GraphView;
import jloda.swing.graphview.IPopupListener;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * constructs a graph popuplistener
 * Daniel Huson, 8.2010
 */
public class GraphViewPopupListener implements IPopupListener {
    final PopupMenu nodeMenu;
    //JPopupMenu nodeLabelMenu;
    final PopupMenu edgeMenu;
    //JPopupMenu EdgeLabelMenu;
    final PopupMenu panelMenu;
    private final GraphView viewer;

    /**
     * construct the popup menus
     *
	 */
    public GraphViewPopupListener(GraphView viewer, String nodeConfig, String edgeConfig, String panelConfig, CommandManager commandManager) {
        this.viewer = viewer;
        nodeMenu = new PopupMenu(viewer, nodeConfig, commandManager, false, true, false);
        edgeMenu = new PopupMenu(viewer, edgeConfig, commandManager, false, false, true);
        panelMenu = new PopupMenu(viewer, panelConfig, commandManager);
    }

    /**
     * popup menu on node
     *
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
	 */
    public void doNodeLabelPopup(MouseEvent me, NodeSet nodes) {
        doNodePopup(me, nodes);
    }

    /**
     * popup menu on edge
     *
	 */
    public void doEdgePopup(MouseEvent me, EdgeSet edges) {
        if (edges.size() != 0) {
            /*
            if (me.isShiftDown() == false) {
                viewer.selectAllNodes(false);
                viewer.selectAllEdges(false);
            }
            if (!viewer.getSelected(adjacentEdges.getFirstElement()))
                viewer.setSelected(adjacentEdges.getFirstElement(), true);
                */
            edgeMenu.show(me.getComponent(), me.getX(), me.getY());
            viewer.repaint(); // stuff gets messed up
        }
    }

    /**
     * popup menu on edge
     *
	 */
    public void doEdgeLabelPopup(MouseEvent me, EdgeSet edges) {
        doEdgePopup(me, edges);
    }

    /**
     * popup menu not on graph
     *
	 */
    public void doPanelPopup(MouseEvent me) {
        panelMenu.show(me.getComponent(), me.getX(), me.getY());
    }

    public JPopupMenu getNodeMenu() {
        return nodeMenu;
    }

    public JPopupMenu getEdgeMenu() {
        return edgeMenu;
    }

    public JPopupMenu getPanelMenu() {
        return panelMenu;
    }
}
