/**
 * TreeDrawerCircular.java 
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
import java.util.LinkedList;
import java.util.Stack;

/**
 * draws a tree using circle arc edges
 * Daniel Huson, 1.2007
 */
public class TreeDrawerCircular extends DefaultGraphDrawer implements IGraphDrawer {
    private final PhyloGraphView viewer;
    private final PhyloTree tree;
    private NodeSet flipNodes;

    final static public String DESCRIPTION = "Draw (rooted) tree using circle segments";

    /**
     * constructor
     *
     * @param graphView
     * @param graph
     */
    public TreeDrawerCircular(PhyloGraphView graphView, PhyloTree graph) {
        super(graphView);
        this.viewer = graphView;
        flipNodes = new NodeSet(graph);
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
        if (tree.getRoot() == null)
            return;
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
        viewer.removeAllInternalPoints();

        Node root = tree.getRoot();
        if (root == null) {
            tree.setRoot(tree.getFirstNode());
            root = tree.getRoot();
        }
        NodeSet leaves = new NodeSet(tree);

        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            if (tree.getDegree(v) == 1)
                leaves.add(v);
        }

        // recursively visit all nodes in the tree and determine the
        // angle 0-2PI of each edge. nodes are placed around the unit
        // circle at position
        // n=1,2,3,... and then an edge along which we visited nodes
        // k,k+1,...j-1,j is directed towards positions k,k+1,...,j

        EdgeDoubleArray angle = new EdgeDoubleArray(tree); // angle of edge
        setAnglesRec(0, root, null, leaves, angle);

        // recursively compute node coordinates from edge angles:
        setCoords(root, angle);
        return true;
    }

    /**
     * Recursively determines the angle of every tree edge.
     *
     * @param num    int
     * @param v      Node
     * @param e      Edge
     * @param leaves NodeSet
     * @param angle  EdgeDoubleArray
     * @return b int
     */

    private int setAnglesRec(int num, Node v, Edge e, NodeSet leaves, EdgeDoubleArray angle) {
        if (leaves.contains(v))
            return num + 1;
        else {
            int a = num; // is number of nodes seen so far
            int b = 0;     // number of nodes after visiting subtree
            final boolean reverse = flipNodes.contains(v);
            for (Edge f = (reverse ? v.getLastAdjacentEdge() : v.getFirstAdjacentEdge()); f != null;
                 f = (reverse ? v.getPrevAdjacentEdge(f) : v.getNextAdjacentEdge(f))) {
                if (f != e) {
                    b = setAnglesRec(a, tree.getOpposite(v, f), f, leaves, angle);

                    // point towards the segment of the unit circle a...b:
                    angle.set(f, Math.PI * (a + 1 + b) / leaves.size());
                    a = b;
                }
            }
            if (b == 0)
                System.err.println("Warning: setAnglesRec: recursion failed");
            return b;
        }
    }

    /**
     * set the coordinates for all nodes and interior edge points
     *
     * @param root   root of tree
     * @param angles assignment of angles to edges
     */
    private void setCoords(Node root, EdgeDoubleArray angles) {
        viewer.setLocation(root, new Point(0, 0));
        for (Edge f = root.getFirstAdjacentEdge(); f != null; f = root.getNextAdjacentEdge(f)) {
            Node w = f.getOpposite(root);
            viewer.setLocation(w, Geometry.translateByAngle(viewer.getLocation(root), angles.getValue(f),
                    tree.getWeight(f)));

                setCoordsRec(viewer.getLocation(root), w, f, angles);
                addInternalPoints(angles);
        }
    }

    /**
     * recursively compute node coordinates from edge angles:
     *
     * @param origin location of origin
     * @param v      Node
     * @param e      Edge
     * @param angles EdgeDouble
     */
    private void setCoordsRec(Point2D origin, Node v, Edge e, EdgeDoubleArray angles) {
        Point2D vp = viewer.getLocation(v);
        for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
            if (f != e) {
                Node w = f.getOpposite(v);
                Point2D b = Geometry.rotateAbout(vp, angles.getValue(f) - angles.getValue(e), origin);
                Point2D c = Geometry.translateByAngle(b, angles.getValue(f), tree.getWeight(f));
                viewer.setLocation(w, c);
                setCoordsRec(origin, w, f, angles);
            }
        }
    }

    /**
     * setup arc edges
     */
    protected void addInternalPoints(EdgeDoubleArray angles) {
        final Stack<Node> stack = new Stack<>();
        stack.push(tree.getRoot());

        Point2D originPt = new Point2D.Double(0, 0);

        while (stack.size() > 0) {
            final Node v = stack.pop();
            final Point2D vPt = viewer.getLocation(v);
            // add internal points to edges
            final double vAngle = (v.getInDegree() == 1 ? angles.get(v.getFirstInEdge()) : 0);
            for (Edge f = v.getFirstOutEdge(); f != null; f = v.getNextOutEdge(f)) {
                Node w = f.getTarget();
                if (!tree.isSpecial(f) || tree.getWeight(f) == 1) {
                    viewer.getEV(f).setShape(EdgeView.ARC_LINE_EDGE);
                    double wAngle = (w.getInDegree() == 1 ? angles.get(w.getFirstInEdge()) : 0);
                    java.util.List<Point2D> list = new LinkedList<>();
                    list.add(originPt);
                    Point2D aPt = Geometry.rotate(vPt, wAngle - vAngle);
                    list.add(aPt);
                    viewer.setInternalPoints(f, list);
                } else if (tree.isSpecial(f)) {
                    viewer.getEV(f).setShape(EdgeView.QUAD_EDGE);
                    double wAngle = (w.getInDegree() == 1 ? angles.get(w.getFirstInEdge()) : 0);
                    java.util.List<Point2D> list = new LinkedList<>();
                    Point2D aPt = Geometry.rotate(vPt, wAngle - vAngle);
                    list.add(aPt);
                    viewer.setInternalPoints(f, list);
                }
                if (PhyloTreeUtils.okToDescendDownThisEdge(tree, f, v)) {
                    stack.push(f.getTarget());
                }
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
            NodeView nv = viewer.getNV(v);
            if (nv.getLabelVisible() && nv.getLabel() != null && nv.getLabel().length() > 0) {
                if (v.getDegree() == 1) {
                    final Edge e = v.getFirstAdjacentEdge();
                    final Node w = e.getOpposite(v);
                    final Point pV = trans.w2d(nv.getLocation());
                    Point2D nextToV = viewer.getNV(w).getLocation();
                    if (viewer.getInternalPoints(e) != null &&
                            viewer.getInternalPoints(e).size() != 0) {
                        if (v == e.getSource())
                            nextToV = viewer.getInternalPoints(e).get(0);
                        else
                            nextToV = viewer.getInternalPoints(e).get(
                                    viewer.getInternalPoints(e).size() - 1);
                    }
                    Point pW = trans.w2d(nextToV);

                    double angle = Geometry.moduloTwoPI(Geometry.computeAngle(Geometry.diff(pW, pV)));
                    if (angle > 1.75 * Math.PI)
                        viewer.getNV(v).setLabelLayout(NodeView.WEST);
                    else if (angle > 1.25 * Math.PI)
                        viewer.getNV(v).setLabelLayout(NodeView.SOUTH);
                    else if (angle > 0.75 * Math.PI)
                        viewer.getNV(v).setLabelLayout(NodeView.EAST);
                    else if (angle > 0.25 * Math.PI)
                        viewer.getNV(v).setLabelLayout(NodeView.NORTH);
                    else
                        viewer.getNV(v).setLabelLayout(NodeView.WEST);
                } else
                    viewer.getNV(v).setLabelLayout(NodeView.NORTHEAST);
            }
        }
        for (Edge e = tree.getFirstEdge(); e != null; e = e.getNext())
            viewer.setLabelLayout(e, EdgeView.CENTRAL);
    }

    public NodeSet getFlipNodes() {
        return flipNodes;
    }

    public void setFlipNodes(NodeSet flipNodes) {
        this.flipNodes = flipNodes;
    }
}
