/*
 * DefaultGraphDrawer.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.graph.*;
import jloda.swing.util.BasicSwing;
import jloda.swing.util.Geometry;
import jloda.util.ProgramProperties;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * default graph drawer
 * Daniel Huson, 12.2006
 */
public class DefaultGraphDrawer implements IGraphDrawer {
    public static String DESCRIPTION = "Default graph drawer";

    protected final GraphView graphView;
    protected final Graph graph;
    protected final Transform trans;

    protected final NodeSet hitNodes;
    protected final NodeSet hitNodeLabels;
    protected final EdgeSet hitEdges;
    protected final EdgeSet hitEdgeLabels;

    private final LabelOverlapAvoider labelOverlapAvoider;

    private NodeArray<Color> subtreeColors;

    private INodeDrawer nodeDrawer;

    private boolean radialLabels = false;

    private Node foundNode = null;

    private int auxilaryParameter = 0; // not used

    /**
     * constructor. Call only after graph and trans have been set for GraphView
     *
     * @param graphView
     */
    public DefaultGraphDrawer(GraphView graphView) {
        this.graphView = graphView;
        this.graph = graphView.getGraph();
        trans = graphView.trans;
        hitNodes = new NodeSet(graph);
        hitNodeLabels = new NodeSet(graph);
        hitEdges = new EdgeSet(graph);
        hitEdgeLabels = new EdgeSet(graph);
        labelOverlapAvoider = new LabelOverlapAvoider(graphView, 100);

        setupGraphView(graphView);
        nodeDrawer = new DefaultNodeDrawer(graphView);
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
        graphView.setKeepAspectRatio(true);
        graphView.setAllowRotationArbitraryAngle(true);
    }

    /**
     * paint the graph
     *
     * @param gc0
     * @param rect
     */
    public void paint(Graphics gc0, Rectangle rect) {
        final Graphics2D gc = (Graphics2D) gc0;
        labelOverlapAvoider.resetHasNoOverlapToPreviouslyDrawnLabels();

        Color tmpColor = gc0.getColor();
        gc0.setColor(tmpColor);

        gc.setFont(graphView.getFont());

        BasicStroke stroke = new BasicStroke(1);
        gc.setStroke(stroke);

        MagnifierUtil magnifierUtil = new MagnifierUtil(graphView);

        nodeDrawer.setup(graphView, gc);

        try {
// ensure that all nodes with labels have valid label rects
            for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                final NodeView nv = graphView.getNV(v);
                if (nv.isLabelVisible() && nv.getLabel() != null)
                    nv.setLabelSize(BasicSwing.getStringSize(gc, graphView.getLabel(v), graphView.getFont(v))); // ensure label rect is set
            }

            // edge label rects get computed during drawing!

            if (graphView.getAutoLayoutLabels()) {
                if (ProgramProperties.get("use-rtree-label-layouter", false)) {
                    LabelLayoutRTree labelLayoutRTRee = new LabelLayoutRTree();
                    labelLayoutRTRee.layout(graphView, gc);
                } else {
                    LabelLayouter layouter = new LabelLayouter(gc);
                    layouter.initialLayout(trans, graphView);
                    layouter.nonOverlappingLayout(trans, graphView);
                }
            }

            // first we draw the unselected items:

            // draw unselected edges
            for (Edge e = graph.getFirstEdge(); e != null; e = graph.getNextEdge(e)) {
                if (!graphView.selectedEdges.contains(e)) {
                    final EdgeView ev = graphView.getEV(e);
                    final Node v = graph.getSource(e);
                    final Node w = graph.getTarget(e);
                    final NodeView nv = graphView.getNV(v);
                    final NodeView nw = graphView.getNV(w);

                    Point2D nextToV = nw.getLocation();
                    Point2D nextToW = nv.getLocation();

                    if (nextToV == null || nextToW == null)
                        continue;

                    if (graphView.getInternalPoints(e) != null) {
                        if (graphView.getInternalPoints(e).size() != 0) {
                            nextToV = graphView.getInternalPoints(e).get(0);
                            nextToW = graphView.getInternalPoints(e).get(
                                    graphView.getInternalPoints(e).size() - 1);
                        }
                    }
                    // if we are in magnifier mode and the edge does not contain any internal points,
                    // add some
                    magnifierUtil.addInternalPoints(e);

                    boolean arcEdge = (ev.getShape() == EdgeView.ARC_LINE_EDGE || ev.getShape() == EdgeView.QUAD_EDGE);

                    final Point pv = arcEdge ? trans.w2d(nv.getLocation()) : nv.computeConnectPoint(nextToV, trans);
                    final Point pw = arcEdge ? trans.w2d(nw.getLocation()) : nw.computeConnectPoint(nextToW, trans);

                    if (graphView.getEV(e).getLineWidth() != stroke.getLineWidth()) {
                        stroke = new BasicStroke(graphView.getEV(e).getLineWidth());
                        gc.setStroke(stroke);
                    }

                    if (graph.findDirectedEdge(w, v) != null)
                        graphView.adjustBiEdge(pv, pw); // want parallel bi-edges

                    ev.draw(gc, pv, pw, trans, false);

                    if (ev.getLabel() != null && ev.getLabelVisible()) {
                        ev.setLabelReferenceLocation(nextToV, nextToW, trans);
                        //ev.setLabelSize(gc);
                        ev.drawLabel(gc, trans, false);
                    }
                    magnifierUtil.removeAddedInternalPoints(e);
                }
            }

            for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                if (!graphView.selectedNodes.contains(v)) {
                    final NodeView nv = graphView.getNV(v);
                    if (nv.getLineWidth() != stroke.getLineWidth()) {
                        stroke = new BasicStroke(nv.getLineWidth());
                        gc.setStroke(stroke);
                    }
                    nodeDrawer.draw(v, false);
                }
            }

            for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
                if (!graphView.selectedNodes.contains(v)) {
                    final NodeView nv = graphView.getNV(v);
                    if (nv.getLineWidth() != stroke.getLineWidth()) {
                        stroke = new BasicStroke(graphView.getNV(v).getLineWidth());
                        gc.setStroke(stroke);
                    }
                    if (nv.isLabelVisible() && labelOverlapAvoider.hasNoOverlapToPreviouslyDrawnLabels(v, nv)) {
                        nodeDrawer.drawLabel(v, false);
                    }
                }
            }

            //draw selected edges
            for (Edge e : graphView.selectedEdges) {
                final EdgeView ev = graphView.getEV(e);
                final Node v = graph.getSource(e);
                final NodeView nv = graphView.getNV(v);
                final Node w = graph.getTarget(e);
                final NodeView nw = graphView.getNV(w);

                Point2D nextToV = nw.getLocation();
                Point2D nextToW = nv.getLocation();

                if (nextToV == null || nextToW == null)
                    continue;

                if (graphView.getInternalPoints(e) != null) {
                    if (graphView.getInternalPoints(e).size() != 0) {
                        nextToV = graphView.getInternalPoints(e).get(0);
                        nextToW = graphView.getInternalPoints(e).get(graphView.getInternalPoints(e).size() - 1);
                    }
                }
                // if we are in magnifier mode and the edge does not contain any internal points,
                // add some
                magnifierUtil.addInternalPoints(e);
                final Point pv = nv.computeConnectPoint(nextToV, trans);
                final Point pw = nw.computeConnectPoint(nextToW, trans);

                if (graphView.getEV(e).getLineWidth() != stroke.getLineWidth()) {
                    stroke = new BasicStroke(graphView.getEV(e).getLineWidth());
                    gc.setStroke(stroke);
                }

                if (graph.findDirectedEdge(w, v) != null)
                    graphView.adjustBiEdge(pv, pw); // want parallel bi-edges

                ev.draw(gc, pv, pw, trans, true);

                if (graphView.getLabel(e) != null && graphView.getLabelVisible(e)) {
                    ev.setLabelReferenceLocation(nextToV, nextToW, trans);
                    //ev.setLabelSize(gc);
                    ev.drawLabel(gc, trans, true);
                }
                magnifierUtil.removeAddedInternalPoints(e);
            }


            // then we draw and highlight the selected nodes:
            for (Node v = graphView.selectedNodes.getFirstElement(); v != null; v = graphView.selectedNodes.getNextElement(v)) {
                final NodeView nv = graphView.getNV(v);
                if (nv.getLineWidth() != stroke.getLineWidth()) {
                    stroke = new BasicStroke(graphView.getNV(v).getLineWidth());
                    gc.setStroke(stroke);
                }
                nodeDrawer.drawNodeAndLabel(v, true);
            }

            if (getFoundNode() != null) {
                Node v = getFoundNode();
                NodeView nv = graphView.getNV(v);
                if (nv.getLabel() != null)
                    nv.setLabelSize(BasicSwing.getStringSize(gc, graphView.getLabel(v), graphView.getFont(v)));
                Shape shape = nv.getLabelShape(trans);
                gc.setColor(Color.YELLOW);
                gc.fill(shape);
                gc.setColor(ProgramProperties.SELECTION_COLOR_DARKER);
                nodeDrawer.drawNodeAndLabel(v, false);
            }
        } catch (NotOwnerException ex) {

        }
    }

    /**
     * compute an embedding of the graph
     *
     * @return false, as this drawer cannot compute an embedding
     */
    public boolean computeEmbedding(boolean toScale) {
        return false;
    }

    /**
     * get all nodes hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return nodes hit
     */
    public NodeSet getHitNodes(int x, int y) {
        hitNodes.clear();
        for (Node v = graph.getLastNode(); v != null; v = graph.getPrevNode(v)) {
            NodeView nv = graphView.getNV(v);
            if (nv.getLocation() != null && nv.contains(trans, x, y))
                hitNodes.add(v);
        }
        return hitNodes;
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
        hitNodes.clear();

        final Rectangle rect = new Rectangle(x - d, y - d, 2 * d, 2 * d);
        for (Node v = graph.getLastNode(); v != null; v = graph.getPrevNode(v)) {
            final NodeView nv = graphView.getNV(v);
            if (nv.getLocation() != null && (rect.intersects(nv.getBox(trans)) || nv.contains(trans, x, y))) {
                hitNodes.add(v);
            }
        }
        return hitNodes;
    }

    /**
     * get all node labels hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return node labels
     */
    public NodeSet getHitNodeLabels(int x, int y) {
        hitNodeLabels.clear();
        for (Node v = graph.getLastNode(); v != null; v = graph.getPrevNode(v)) {
            final NodeView nv = graphView.getNV(v);
            if (nv != null && nv.getLabel() != null && nv.getLocation() != null && nv.getLabelVisible()
                    && (graphView.getSelected(v) || labelOverlapAvoider.isVisible(v))
                    && (nv.getLabelShape(trans) != null && nv.getLabelShape(trans).contains(x, y)))
                hitNodeLabels.add(v);
        }
        return hitNodeLabels;
    }

    /**
     * get all nodes contained in rect
     *
     * @param rect
     * @return nodes contained in rect
     */
    public NodeSet getHitNodes(Rectangle rect) {
        hitNodes.clear();

        for (Node v = graph.getLastNode(); v != null; v = graph.getPrevNode(v)) {
            final NodeView nv = graphView.getNV(v);
            if (nv.isEnabled() && nv.getLocation() != null) {
                final Rectangle nodeRect = nv.getBox(trans);
                if (nodeRect != null && rect.contains(nodeRect))
                    hitNodes.add(v);
            }
        }
        return hitNodes;
    }

    /**
     * get all node labels contained in rect
     *
     * @param rect
     * @return node labels contained in rect
     */
    public NodeSet getHitNodeLabels(Rectangle rect) {
        hitNodeLabels.clear();
        for (Node v = graph.getLastNode(); v != null; v = graph.getPrevNode(v)) {
            final NodeView nv = graphView.getNV(v);
            if (nv.getLabel() != null && nv.getLocation() != null
                    && nv.getLabelVisible() &&
                    (graphView.getSelected(v) || labelOverlapAvoider.isVisible(v))) {
                final Rectangle labelRect = nv.getLabelRect(trans);
                if (labelRect != null && rect.contains(labelRect))
                    hitNodeLabels.add(v);
            }
        }
        return hitNodeLabels;
    }


    /**
     * get all edges hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return edges hits
     */
    public EdgeSet getHitEdges(int x, int y) {
        hitEdges.clear();
        final MagnifierUtil magnifierUtil = new MagnifierUtil(graphView);

        for (Edge e = graph.getLastEdge(); e != null; e = graph.getPrevEdge(e)) {
            final EdgeView ev = graphView.getEV(e);
            if (ev.isEnabled() && ev.getColor() != null) {
                final Node v = graph.getSource(e);
                final Node w = graph.getTarget(e);
                final NodeView nv = graphView.getNV(v);
                final NodeView nw = graphView.getNV(w);
                if (nv.getLocation() == null || nw.getLocation() == null)
                    continue;

                magnifierUtil.addInternalPoints(e);

                final Point pv = nv.computeConnectPoint(nw.getLocation(), trans);
                final Point pw = nw.computeConnectPoint(nv.getLocation(), trans);

                if (graph.findDirectedEdge(graph.getTarget(e), graph.getSource(e)) != null)
                    graphView.adjustBiEdge(pv, pw); // adjust for parallel edge

                if (ev.hitEdge(pv, pw, trans, x, y, 3))
                    hitEdges.add(e);
                magnifierUtil.removeAddedInternalPoints(e);
            }
        }
        return hitEdges;
    }

    /**
     * get all edge labels hit by mouse at (x,y)
     *
     * @param x
     * @param y
     * @return edge labels
     */
    public EdgeSet getHitEdgeLabels(int x, int y) {
        hitEdgeLabels.clear();

        for (Edge e = graph.getLastEdge(); e != null; e = graph.getPrevEdge(e)) {
            EdgeView ev = graphView.getEV(e);
            if (ev.isEnabled() && ev.getLabelVisible() && ev.getLabel() != null) {
                Shape labelShape = ev.getLabelShape(trans);
                if (labelShape != null &&
                        labelShape.contains(x, y)) {
                    hitEdgeLabels.add(e);
                }
            }
        }
        return hitEdgeLabels;
    }

    /**
     * get all edges contained in rect
     *
     * @param rect
     * @return edges contained in rect
     */
    public EdgeSet getHitEdges(Rectangle rect) {
        hitEdges.clear();
        for (Edge e = graph.getLastEdge(); e != null; e = graph.getPrevEdge(e)) {
            if (graphView.getLocation(e.getSource()) != null && graphView.getLocation(e.getTarget()) != null &&
                    rect.contains(trans.w2d(graphView.getLocation(e.getSource())))
                    && rect.contains(trans.w2d(graphView.getLocation(e.getTarget()))))
                hitEdges.add(e);
        }
        return hitEdges;
    }

    /**
     * get all edge labels contained in rect
     *
     * @param rect
     * @return edges contained in rect
     */
    public EdgeSet getHitEdgeLabels(Rectangle rect) {
        hitEdgeLabels.clear();
        for (Edge e = graph.getLastEdge(); e != null; e = graph.getPrevEdge(e)) {
            EdgeView ev = graphView.getEV(e);
            if (ev.getLabel() != null && ev.getLabelVisible() &&
                    rect.contains(ev.getLabelRect(trans))) {
                hitEdgeLabels.add(e);
            }
        }
        return hitEdgeLabels;
    }

    /**
     * get the set of flipped nodes
     *
     * @return flipped nodes
     */
    public NodeSet getFlipNodes() {
        return null;
    }

    /**
     * set the set of flipped nodes
     */
    public void setFlipNodes(NodeSet flipNodes) {
    }

    /**
     * set the default label positions for nodes and edges
     *
     * @param resetAll if true, reset positions for user-placed labels, too
     */
    public void resetLabelPositions(boolean resetAll) {
        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            NodeView nv = graphView.getNV(v);
            if (nv.getLabelVisible() && nv.getLabel() != null && nv.getLabel().length() > 0
                    && (resetAll || nv.getLabelLayout() != NodeView.USER)) {
                if (v.getDegree() == 1) {
                    final Edge e = v.getFirstAdjacentEdge();
                    final Node w = e.getOpposite(v);
                    final Point pV = trans.w2d(nv.getLocation());
                    Point2D nextToV = graphView.getNV(w).getLocation();
                    if (graphView.getInternalPoints(e) != null &&
                            graphView.getInternalPoints(e).size() != 0) {
                        if (v == e.getSource())
                            nextToV = graphView.getInternalPoints(e).get(0);
                        else
                            nextToV = graphView.getInternalPoints(e).get(
                                    graphView.getInternalPoints(e).size() - 1);
                    }
                    Point pW = trans.w2d(nextToV);

                    double angle = Geometry.moduloTwoPI(Geometry.computeAngle(Geometry.diff(pW, pV)));
                    if (angle > 1.75 * Math.PI)
                        graphView.getNV(v).setLabelLayout(NodeView.WEST);
                    else if (angle > 1.25 * Math.PI)
                        graphView.getNV(v).setLabelLayout(NodeView.NORTH);
                    else if (angle > 0.75 * Math.PI)
                        graphView.getNV(v).setLabelLayout(NodeView.EAST);
                    else if (angle > 0.25 * Math.PI)
                        graphView.getNV(v).setLabelLayout(NodeView.SOUTH);
                    else
                        graphView.getNV(v).setLabelLayout(NodeView.WEST);
                } else
                    graphView.getNV(v).setLabelLayout(NodeView.NORTHEAST);
            }
        }
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            EdgeView ev = graphView.getEV(e);
            if (resetAll || ev.getLabelLayout() != EdgeView.USER)
                ev.setLabelLayout(EdgeView.CENTRAL);
        }
    }

    /**
     * gets the label overlap avoider
     *
     * @return label overlap avoider
     */
    public LabelOverlapAvoider getLabelOverlapAvoider() {
        return labelOverlapAvoider;
    }


    /**
     * to support bounding-box oriented drawers, report any node whose label has been interactively moved
     *
     * @param v
     */
    public void setNodeHasMovedLabel(Node v) {
    }

    /**
     * to support bounding-box oriented drawers, report any edge whose label has been interactively moved
     *
     * @param e
     */
    public void setEdgesHasMovedLabel(Edge e) {
    }

    public void setEdgesHasMovedInternalPoints(Edge e) {
    }


    /**
     * get the set of collapsed nodes
     *
     * @return collapsed nodes
     */
    public NodeSet getCollapsedNodes() {
        return null;
    }

    /**
     * set the set of collapsed nodes
     */
    public void setCollapsedNodes(NodeSet collapsedNodes) {
    }

    /**
     * gets the bounding box of the graph in world coordinates
     *
     * @return bbox
     */
    public Rectangle2D getBBox() {
        return graphView.getBBox();
    }

    /**
     * rotate node labels to match edge directions?
     *
     * @param radialLabels
     */
    public void setRadialLabels(boolean radialLabels) {
    }

    /**
     * node found by search, must be drawn  if !=null
     *
     * @return found node
     */
    public Node getFoundNode() {
        return foundNode;
    }

    /**
     * node found by search, must be drawn if !=null
     *
     * @param foundNode
     */
    public void setFoundNode(Node foundNode) {
        this.foundNode = foundNode;
    }

    /**
     * set the auxilary parameter
     *
     * @param parameter
     */
    public void setAuxilaryParameter(int parameter) {
    }

    /**
     * get the auxilary parameter
     *
     * @return auxilary parameter
     */
    public int getAuxilaryParameter() {
        return 0;
    }

    public INodeDrawer getNodeDrawer() {
        return nodeDrawer;
    }

    public void setNodeDrawer(INodeDrawer nodeDrawer) {
        this.nodeDrawer = nodeDrawer;
    }
}
