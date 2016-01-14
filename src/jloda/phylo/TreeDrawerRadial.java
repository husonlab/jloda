/**
 * TreeDrawerRadial.java 
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

import jloda.graph.*;
import jloda.graphview.*;
import jloda.util.Geometry;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Random;

/**
 * draws a tree using parallel edges
 * Daniel Huson, 1.2007
 */
public class TreeDrawerRadial extends DefaultGraphDrawer implements IGraphDrawer {
    private final PhyloGraphView treeView;
    private final PhyloTree tree;

    final static public String DESCRIPTION = "Draw (rooted) trees using angled lines";

    /**
     * constructor
     *
     * @param graphView
     * @param graph
     */
    public TreeDrawerRadial(PhyloGraphView graphView, PhyloTree graph) {
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
        graphView.setAllowInternalEdgePoints(true);
        graphView.setMaintainEdgeLengths(true);
        graphView.setAllowMoveNodes(true);
        graphView.setAllowMoveInternalEdgePoints(false);
        graphView.setKeepAspectRatio(true);
        graphView.setAllowRotationArbitraryAngle(true);
    }

    /**
     * paint the graph. If rect is non-null, only need to cover rect
     *
     * @param graphics
     * @param rect
     */
    public void paint(Graphics graphics, Rectangle rect) {
        Node root = tree.getFirstNode();
        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            if (tree.getDegree(v) > tree.getDegree(root))
                root = v;
        }

        super.paint(graphics, rect);
    }

    /**
     * compute an embedding of the graph
     *
     * @param toScale if true, build to-scale embedding
     * @return true, if embedding was computed
     */
    public boolean computeEmbedding(boolean toScale) {
        if (tree.getNumberOfNodes() == 0)
            return true;
        treeView.removeAllInternalPoints();

        // don't use setRoot to remember root
        Node root = tree.getFirstNode();
        NodeSet leaves = new NodeSet(tree);

        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            if (tree.getDegree(v) == 1)
                leaves.add(v);
            if (tree.getDegree(v) > tree.getDegree(root))
                root = v;
        }

        // recursively visit all nodes in the tree and determine the
        // angle 0-2PI of each edge. nodes are placed around the unit
        // circle at position
        // n=1,2,3,... and then an edge along which we visited nodes
        // k,k+1,...j-1,j is directed towards positions k,k+1,...,j

        EdgeDoubleArray angle = new EdgeDoubleArray(tree); // angle of edge
        Random rand = new Random();
        rand.setSeed(1);
        int seen = setAnglesRec(0, root, null, leaves, angle, rand);

        // rotate all edges so that taxon number 1 appears on the right:
        Node v = tree.getTaxon2Node(1);
        if (v != null) {
            Edge e = v.getFirstAdjacentEdge();
            if (e != null) {
                double alpha = angle.getValue(e);
                for (Edge f = tree.getFirstEdge(); f != null; f = f.getNext()) {
                    angle.set(f, angle.getValue(f) - alpha);
                }
            }
        }

        if (seen != leaves.size())
            System.err.println("Warning: Number of nodes seen: " + seen +
                    " != Number of leaves: " + leaves.size());

        // recursively compute node coordinates from edge angles:
        setCoordsRec(root, null, angle);
        return true;
    }

    /**
     * Recursively determines the angle of every tree edge.
     *
     * @param num    int
     * @param root   Node
     * @param entry  Edge
     * @param leaves NodeSet
     * @param angle  EdgeDoubleArray
     * @param rand   Random
     * @return b int
     */

    private int setAnglesRec(int num, Node root, Edge entry, NodeSet leaves,
                             EdgeDoubleArray angle, Random rand) {
        if (leaves.contains(root))
            return num + 1;
        else {
            Iterator edges = tree.getAdjacentEdges(root);
            // edges.permute(); // look at children in random order
            int a = num; // is number of nodes seen so far
            int b = 0;     // number of nodes after visiting subtree
            while (edges.hasNext()) {
                Edge e = (Edge) edges.next();
                if (e != entry) {
                    b = setAnglesRec(a, tree.getOpposite(root, e), e, leaves, angle, rand);

                    // point towards the segment of the unit circle a...b:
                    angle.set(e, Math.PI * (a + 1 + b) / leaves.size());
                    a = b;
                }
            }
            if (b == 0)
                System.err.println("Warning: setAnglesRec: recursion failed");
            return b;
        }
    }

    /**
     * recursively compute node coordinates from edge angles:
     *
     * @param root  Node
     * @param entry Edge
     * @param angle EdgeDouble
     */
    private void setCoordsRec(Node root, Edge entry, EdgeDoubleArray angle) {
        Iterator edges = tree.getAdjacentEdges(root);

        while (edges.hasNext()) {
            Edge e = (Edge) edges.next();

            if (e != entry) {
                Node v = tree.getOpposite(root, e);

                // translate in the computed direction by the given amount
                treeView.setLocation(v,
                        Geometry.translateByAngle(treeView.getLocation(root), angle.getValue(e),
                                tree.getWeight(e)));
                setCoordsRec(v, e, angle);
            }
        }
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
        return super.getHitNodeLabels(rect);
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

    /**
     * set the default label positions for nodes and edges
     *
     * @param resetAll
     */
    public void resetLabelPositions(boolean resetAll) {
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            NodeView nv = treeView.getNV(v);
            if (nv.getLabelVisible() && nv.getLabel() != null && nv.getLabel().length() > 0) {
                if (v.getDegree() == 1) {
                    final Edge e = v.getFirstAdjacentEdge();
                    final Node w = e.getOpposite(v);
                    final Point pV = trans.w2d(nv.getLocation());
                    Point2D nextToV = treeView.getNV(w).getLocation();
                    if (treeView.getInternalPoints(e) != null &&
                            treeView.getInternalPoints(e).size() != 0) {
                        if (v == e.getSource())
                            nextToV = treeView.getInternalPoints(e).get(0);
                        else
                            nextToV = treeView.getInternalPoints(e).get(
                                    treeView.getInternalPoints(e).size() - 1);
                    }
                    Point pW = trans.w2d(nextToV);

                    double angle = Geometry.moduloTwoPI(Geometry.computeAngle(Geometry.diff(pW, pV)));
                    if (angle > 1.75 * Math.PI)
                        treeView.getNV(v).setLabelLayout(NodeView.WEST);
                    else if (angle > 1.25 * Math.PI)
                        treeView.getNV(v).setLabelLayout(NodeView.SOUTH);
                    else if (angle > 0.75 * Math.PI)
                        treeView.getNV(v).setLabelLayout(NodeView.EAST);
                    else if (angle > 0.25 * Math.PI)
                        treeView.getNV(v).setLabelLayout(NodeView.NORTH);
                    else
                        treeView.getNV(v).setLabelLayout(NodeView.WEST);
                } else
                    treeView.getNV(v).setLabelLayout(NodeView.NORTHEAST);
            }
        }
        for (Edge e = tree.getFirstEdge(); e != null; e = e.getNext())
            treeView.setLabelLayout(e, EdgeView.CENTRAL);
    }
}
