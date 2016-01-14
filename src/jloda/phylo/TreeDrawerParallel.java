/**
 * TreeDrawerParallel.java 
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
package jloda.phylo;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.graphview.DefaultGraphDrawer;
import jloda.graphview.GraphView;
import jloda.graphview.IGraphDrawer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * draws a tree using parallel edges
 * Daniel Huson, 1.2007
 */
public class TreeDrawerParallel extends DefaultGraphDrawer implements IGraphDrawer {
    private final PhyloGraphView treeView;
    private final PhyloTree tree;

    final static public String DESCRIPTION = "Draw (rooted) trees using parallel lines";

    /**
     * constructor
     *
     * @param graphView
     * @param graph
     */
    public TreeDrawerParallel(PhyloGraphView graphView, PhyloTree graph) {
        super(graphView);
        this.treeView = graphView;
        this.tree = graph;
        setupGraphView(graphView);
    }

    /**
     * setd up the graphview
     *
     * @param graphView
     */
    public void setupGraphView(GraphView graphView) {
        graphView.setAllowInternalEdgePoints(false);
        graphView.setMaintainEdgeLengths(true);
        graphView.setAllowMoveNodes(true);
        graphView.setAllowMoveInternalEdgePoints(false);
        graphView.setKeepAspectRatio(false);
        graphView.setAllowRotationArbitraryAngle(false);
        graphView.trans.setAngle(0);
    }

    /**
     * paint the graph. If rect is non-null, only need to cover rect
     *
     * @param graphics
     * @param rect
     */
    public void paint(Graphics graphics, Rectangle rect) {
        super.paint(graphics, rect);
    }

    /**
     * compute an embedding of the graph
     *
     * @param toScale if true, build to-scale embedding
     * @return true, if embedding was computed
     */
    public boolean computeEmbedding(boolean toScale) {
        treeView.removeAllInternalPoints();
        Node root = tree.getRoot();
        if (root == null) {
            // compute root
            root = tree.getFirstNode();
        }
        computeEmbeddingRec(root, null, 0, 0, toScale);

        return false;
    }

    /**
     * recursively compute the embedding
     *
     * @param v
     * @param e
     * @param hDistToRoot horizontal distance from node to root
     * @param leafNumber  rank of leaf in vertical ordering
     * @param toScale
     * @return index of last leaf
     */
    private int computeEmbeddingRec(Node v, Edge e, double hDistToRoot, int leafNumber, boolean toScale) {
        if (v.getDegree() == 1 && e != null)  // hit a leaf
        {
            treeView.setLocation(v, toScale ? hDistToRoot : 0, ++leafNumber);
        } else {
            Point2D first = null;
            Point2D last = null;
            double minX = Double.MAX_VALUE;
            for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
                if (f != e) {
                    Node w = f.getOpposite(v);
                    leafNumber = computeEmbeddingRec(w, f, hDistToRoot + tree.getWeight(f), leafNumber, toScale);
                    if (first == null)
                        first = treeView.getLocation(w);
                    last = treeView.getLocation(w);
                    if (last.getX() < minX)
                        minX = last.getX();
                }
            }
            if (first != null) // always true
            {
                double x;
                if (toScale)
                    x = hDistToRoot;
                else
                    x = minX - 1;
                double y = 0.5 * (last.getY() + first.getY());
                treeView.setLocation(v, x, y);

                for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
                    if (f != e) {
                        Node w = f.getOpposite(v);
                        java.util.List<Point2D> list = new LinkedList<>();
                        Point2D p = new Point2D.Double(x, treeView.getLocation(w).getY());
                        list.add(p);
                        treeView.setInternalPoints(f, list);
                    }
                }
            }
        }
        return leafNumber;
    }

    /**
     * get all nodes hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return nodes hit
     */
    public NodeSet getHitNodes(int x, int y) {
        return super.getHitNodes(x, y);
    }

    /**
     * get all nodes hit by mouse at (x,y) with tolerance of d pixels
     *
     * @param x
     * @param y
     * @param d tolerance
     * @return nodes hit
     */
    public NodeSet getHitNodes(int x, int y, int d) {
        return super.getHitNodes(x, y, d);
    }

    public NodeSet getHitNodeLabels(int x, int y) {
        return super.getHitNodeLabels(x, y);
    }

    /**
     * get all nodes contained in rect
     *
     * @param rect
     * @return nodes contained in rect
     */
    public NodeSet getHitNodes(Rectangle rect) {
        return super.getHitNodes(rect);
    }

    /**
     * get all node labels contained in rect
     *
     * @param rect
     * @return node labels contained in rect
     */
    public NodeSet getHitNodeLabels(Rectangle rect) {
        return null;
    }

    /**
     * get all edges hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return edges hits
     */
    public EdgeSet getHitEdges(int x, int y) {
        return super.getHitEdges(x, y);

    }

    /**
     * get all edge labels hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return edge labels
     */
    public EdgeSet getHitEdgeLabels(int x, int y) {
        return super.getHitEdgeLabels(x, y);
    }

    /**
     * get all edges contained in rect
     *
     * @param rect
     * @return edges contained in rect
     */
    public EdgeSet getHitEdges(Rectangle rect) {
        return super.getHitEdges(rect);
    }

    /**
     * get all edge labels contained in rect
     *
     * @param rect
     * @return edges contained in rect
     */
    public EdgeSet getHitEdgeLabels(Rectangle rect) {
        return super.getHitEdgeLabels(rect);
    }
}
