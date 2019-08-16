/*
 * IGraphDrawer.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package jloda.swing.graphview;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Node;
import jloda.graph.NodeSet;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * implementation of a graph dendroscope
 * Daniel Huson, 12.2006
 */
public interface IGraphDrawer {
    String DESCRIPTION = "Graph Drawer";

    /**
     * setup the graph tree
     *
     * @param graphView
     */
    void setupGraphView(GraphView graphView);

    /**
     * paint the graph. If rect is non-null, need only cover the rect
     *
     * @param graphics
     * @param rect     rectangle in device coordinates
     */
    void paint(Graphics graphics, Rectangle rect);


    /**
     * compute an embedding of the graph.
     *
     * @param toScale if true, embedding should be to-scale
     * @return true, if embedding was computed
     */
    boolean computeEmbedding(boolean toScale);

    /**
     * get all nodes hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return nodes hit
     */
    NodeSet getHitNodes(int x, int y);

    /**
     * get all nodes hit by mouse at (x,y) at a tolerance of d pixels
     *
     * @param x
     * @param y
     * @param d
     * @return nodes hit
     */
    NodeSet getHitNodes(int x, int y, int d);

    /**
     * get all node labels hit by mouse at (x,y)
     * @param x
     * @param y
     * @return node labels
     */

    /**
     * get all node labels hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return nodes hit
     */
    NodeSet getHitNodeLabels(int x, int y);

    /**
     * get all nodes contained in rect
     *
     * @param rect
     * @return nodes contained in rect
     */
    NodeSet getHitNodes(Rectangle rect);

    /**
     * get all node labels contained in rect
     *
     * @param rect
     * @return node labels contained in rect
     */
    NodeSet getHitNodeLabels(Rectangle rect);

    /**
     * get all edges hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return edges hits
     */
    EdgeSet getHitEdges(int x, int y);

    /**
     * get all edge labels hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return edge labels
     */
    EdgeSet getHitEdgeLabels(int x, int y);

    /**
     * get all edges contained in rect
     *
     * @param rect
     * @return edges contained in rect
     */
    EdgeSet getHitEdges(Rectangle rect);

    /**
     * get all edge labels contained in rect
     *
     * @param rect
     * @return edges contained in rect
     */
    EdgeSet getHitEdgeLabels(Rectangle rect);

    /**
     * get the set of collapsed nodes
     *
     * @return collapsed nodes
     */
    NodeSet getCollapsedNodes();

    /**
     * set the set of collapsed nodes
     */
    void setCollapsedNodes(NodeSet collapsedNodes);

    /**
     * to support bounding-box oriented drawers, report any node whose label has been interavtively moved
     *
     * @param v
     */
    void setNodeHasMovedLabel(Node v);

    /**
     * to support bounding-box oriented drawers, report any edge whose label has been interavtively moved
     *
     * @param e
     */
    void setEdgesHasMovedLabel(Edge e);

    /**
     * to support bounding-box oriented drawers, report any edge whose internal points have been interavtively moved
     *
     * @param e
     */
    void setEdgesHasMovedInternalPoints(Edge e);

    /**
     * set the default label positions for nodes and edges
     *
     * @param resetAll if true, reset positions for user-placed labels, too
     */
    void resetLabelPositions(boolean resetAll);

    /**
     * gets the label overlap avoider
     *
     * @return label overlap avoider
     */
    LabelOverlapAvoider getLabelOverlapAvoider();

    /**
     * gets the bounding box of the graph in world coordinates
     *
     * @return bounding box
     */
    Rectangle2D getBBox();

    /**
     * rotate node labels to match edge directions?
     *
     * @param rotateLabels
     */
    void setRadialLabels(boolean rotateLabels);

    /**
     * set the auxilary parameter
     *
     * @param parameter
     */
    void setAuxilaryParameter(int parameter);

    /**
     * get the auxilary parameter
     *
     * @return auxilary parameter
     */
    int getAuxilaryParameter();

    INodeDrawer getNodeDrawer();

    void setNodeDrawer(INodeDrawer nodeDrawer);

}
