/**
 * GraphViewListener.java 
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
 * @version $Id: GraphViewListener.java,v 1.100 2010-06-14 13:34:40 huson Exp $
 *
 * Listener for all graphview events.
 *
 * @author Daniel Huson
 */

import jloda.graph.*;
import jloda.util.Basic;
import jloda.util.Cursors;
import jloda.util.Geometry;
import jloda.util.NotOwnerException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Listener for all GraphView events
 */
public class GraphViewListener implements IGraphViewListener {
    private final static ExecutorService service = Executors.newFixedThreadPool(1);
    private boolean inWait = false;

    private final GraphView viewer;
    private final Transform trans;

    private final int inClick = 1;
    private final int inMove = 2;
    private final int inRubberband = 3;
    private final int inNewEdge = 4;
    private final int inMoveNodeLabel = 5;
    private final int inMoveEdgeLabel = 6;
    private final int inMoveInternalEdgePoint = 7;
    private final int inScrollByMouse = 8;
    private final int inMoveMagnifier = 9;
    private final int inResizeMagnifier = 10;

    private int current;
    private int downX;
    private int downY;
    private Rectangle selRect;
    private Point prevPt;
    private Point offset; // used by move node label

    private boolean allowDeselectAllByMouseClick = true;
    private boolean allowSelectConnectedComponent = false;


    private NodeSet hitNodes;
    private NodeSet hitNodeLabels;
    private EdgeSet hitEdges;
    private EdgeSet hitEdgeLabels;

    private boolean nodeLabelsHaveMoved = false;
    private boolean edgeLabelsHaveMoved = false;

    private boolean inPopup = false;

    // is mouse still pressed?
    private boolean stillDownWithoutMoving = false;

    /**
     * Constructor
     *
     * @param graphView GraphView
     */
    public GraphViewListener(GraphView graphView) {
        this.viewer = graphView;
        this.trans = graphView.trans;
        hitNodes = new NodeSet(this.viewer.getGraph());
        hitNodeLabels = new NodeSet(this.viewer.getGraph());
        hitEdges = new EdgeSet(this.viewer.getGraph());
        hitEdgeLabels = new EdgeSet(this.viewer.getGraph());
    }

    /**
     * Mouse pressed.
     *
     * @param me MouseEvent
     */
    public void mousePressed(MouseEvent me) {
        downX = me.getX();
        downY = me.getY();
        selRect = null;
        prevPt = null;
        offset = new Point();
        nodeLabelsHaveMoved = false;
        edgeLabelsHaveMoved = false;
        stillDownWithoutMoving = true;

        if (viewer.getGraphDrawer() == null)
            return;

        int magnifierHit = trans.getMagnifier().hit(downX, downY);

        if (magnifierHit != Magnifier.HIT_NOTHING) {
            switch (magnifierHit) {
                case Magnifier.HIT_MOVE:
                    current = inMoveMagnifier;
                    viewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    break;
                case Magnifier.HIT_RESIZE:
                    current = inResizeMagnifier;
                    viewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    break;
                case Magnifier.HIT_INCREASE_MAGNIFICATION:
                    if (viewer.trans.getMagnifier().increaseDisplacement())
                        viewer.repaint();
                    break;
                case Magnifier.HIT_DECREASE_MAGNIFICATION:
                    if (viewer.trans.getMagnifier().decreaseDisplacement())
                        viewer.repaint();
                    break;
                default:
                    break;
            }
            return;
        }

        hitNodes = viewer.getGraphDrawer().getHitNodes(downX, downY);
        int numHitNodes = hitNodes.size();
        hitNodeLabels = viewer.getGraphDrawer().getHitNodeLabels(downX, downY);
        int numHitNodeLabels = hitNodeLabels.size();
        hitEdges = viewer.getGraphDrawer().getHitEdges(downX, downY);
        int numHitEdges = hitEdges.size();
        hitEdgeLabels = viewer.getGraphDrawer().getHitEdgeLabels(downX, downY);
        int numHitEdgeLabels = hitEdgeLabels.size();

        if (me.isPopupTrigger()) {
            inPopup = true;
            viewer.setCursor(Cursor.getDefaultCursor());
            if (numHitNodes != 0) {
                viewer.fireNodePopup(me, hitNodes);
            } else if (numHitNodeLabels != 0) {
                viewer.fireNodeLabelPopup(me, hitNodeLabels);
            } else if (numHitEdges != 0) {
                viewer.fireEdgePopup(me, hitEdges);
            } else if (numHitEdgeLabels != 0) {
                viewer.fireEdgeLabelPopup(me, hitEdgeLabels);
            } else {
                viewer.firePanelClicked(me);
                viewer.firePanelPopup(me);
            }
            viewer.resetCursor();
            return;
        }

        viewer.fireDoPress(hitNodes);
        viewer.fireDoPress(hitEdges);

        if (numHitNodes == 0 && numHitNodeLabels == 0 && numHitEdges == 0 && numHitEdgeLabels == 0) {
            if (me.isShiftDown()) {
                current = inRubberband;
                viewer.setCursor(Cursor.getDefaultCursor());
            } else {
                current = inScrollByMouse;
                viewer.setCursor(Cursors.getClosedHand());

                if (!inWait) {
                    service.execute(new Runnable() {
                        public void run() {
                            try {
                                inWait = true;
                                synchronized (this) {
                                    Thread.sleep(500);
                                }
                            } catch (InterruptedException e) {
                            }
                            if (stillDownWithoutMoving) {
                                current = inRubberband;
                                viewer.setCursor(Cursor.getDefaultCursor());
                            }
                            inWait = false;
                        }
                    });
                }
            }
        } else {
            viewer.setCursor(Cursor.getDefaultCursor());
            if (viewer.getAllowEdit() && numHitNodes == 1 && me.isAltDown() && !me.isShiftDown())
                current = inNewEdge;
            else if (numHitNodes == 0 && numHitEdges == 0 && numHitNodeLabels > 0) {
                Node v = hitNodeLabels.getFirstElement();
                //viewer.setSelected(v, true);
                if (viewer.getLabel(v) == null)
                    return;
                current = inMoveNodeLabel;
                viewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else if (numHitNodes == 0 && numHitEdges == 0 && numHitNodeLabels == 0 && numHitEdgeLabels > 0) {
                Edge e = hitEdgeLabels.getFirstElement();
                if (!viewer.getSelected(e) || viewer.getLabel(e) == null)
                    return; // move labels only of selected edges
                current = inMoveEdgeLabel;
                viewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            } else if (numHitNodes > 0 && !me.isAltDown() && !me.isShiftDown()) {
                if (!viewer.getAllowMoveNodes() && viewer.getNumberSelectedNodes() <
                        viewer.getGraph().getNumberOfNodes())
                    return;
                // if no hit node selected, deselect all and then select node
                boolean found = false;
                for (Node v = hitNodes.getFirstElement(); v != null;
                     v = hitNodes.getNextElement(v)) {
                    if (viewer.getSelectedNodes().contains(v)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    current = inMove;
                    viewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            } else if (viewer.isAllowMoveInternalEdgePoints() && (numHitEdges >= 1
                    && viewer.getSelectedEdges().size() == 1 && hitEdges.contains(viewer.getSelectedEdges().getFirstElement()))) {
                current = inMoveInternalEdgePoint;
                viewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        }
    }

    /**
     * Mouse released.
     *
     * @param me MouseEvent
     */
    public void mouseReleased(MouseEvent me) {
        if (me.isShiftDown())
            viewer.setCursor(Cursor.getDefaultCursor());
        else
            viewer.resetCursor();
        stillDownWithoutMoving = false;

        if (viewer.getGraphDrawer() == null)
            return;

        // check whether we have scrolled by mouse:
        if (current == inScrollByMouse && !(me.getX() == downX && me.getY() == downY)) {
            return;
        }

        NodeSet hitNodes = viewer.getGraphDrawer().getHitNodes(me.getX(), me.getY());
        EdgeSet hitEdges = viewer.getGraphDrawer().getHitEdges(me.getX(), me.getY());

        if (me.isPopupTrigger()) {
            inPopup = true;
            viewer.setCursor(Cursor.getDefaultCursor());
            if (hitNodes.size() != 0)
                viewer.fireNodePopup(me, hitNodes);
            else if (hitNodeLabels.size() != 0)
                viewer.fireNodeLabelPopup(me, hitNodeLabels);
            else if (hitEdges.size() != 0)
                viewer.fireEdgePopup(me, hitEdges);
            else if (hitEdgeLabels.size() != 0)
                viewer.fireEdgeLabelPopup(me, hitEdgeLabels);
            else {
                viewer.firePanelClicked(me);
                viewer.firePanelPopup(me);
            }
            viewer.resetCursor();
            return;
        }

        viewer.fireDoRelease(hitNodes);
        viewer.fireDoRelease(hitEdges);
        if (hitNodes.size() == 0 && hitEdges.size() == 0) {
            // try again with more tolerance
            hitNodes = viewer.getGraphDrawer().getHitNodes(me.getX(), me.getY(), 8);
            viewer.fireDoRelease(hitNodes);
        }

        if (current == inRubberband) {
            Rectangle rect = new Rectangle(downX, downY, 0, 0);
            rect.add(me.getX(), me.getY());
            selectNodesEdges(viewer.getGraphDrawer().getHitNodes(rect), viewer.getGraphDrawer().getHitEdges(rect), me.isShiftDown(), me.getClickCount());
            viewer.repaint();
        } else if (current == inNewEdge) {
            NodeSet firstHit = viewer.getGraphDrawer().getHitNodes(downX, downY);
            if (firstHit.size() == 1) {
                Node v = firstHit.getFirstElement();
                NodeSet secondHit = viewer.getGraphDrawer().getHitNodes(me.getX(), me.getY());

                Node w;
                if (secondHit.size() == 0) {
                    int x = me.getX();
                    int y = me.getY();
                    Point2D location = trans.d2w(x, y);
                    viewer.setDefaultNodeLocation(location);
                    Edge e = viewer.newEdge(v, null);
                    if (e != null) {
                        w = viewer.getGraph().getTarget(e);
                        viewer.setLocation(w, location);
                    }
                } else if (secondHit.size() == 1) {
                    w = secondHit.getFirstElement();

                    if (w != null) {
                        if (v != w) {
                            viewer.newEdge(v, w);
                        }
                    }
                }
                viewer.repaint();
            }
        } else if (current == inMoveNodeLabel) {
            if (nodeLabelsHaveMoved) {
                viewer.fireDoNodeLabelsMoved(viewer.getGraphDrawer().getHitNodeLabels(me.getX(), me.getY()));
                viewer.repaint();
            }
        } else if (current == inMoveEdgeLabel) {
            if (edgeLabelsHaveMoved) {
                viewer.fireDoEdgeLabelsMoved(viewer.getGraphDrawer().getHitEdgeLabels(me.getX(), me.getY()));
                viewer.repaint();
            }
        } else if (current == inMove)
            viewer.fireDoNodesMoved();
    }

    /**
     * Mouse entered.
     *
     * @param me MouseEvent
     */
    public void mouseEntered(MouseEvent me) {
    }

    /**
     * Mouse exited.
     *
     * @param me MouseEvent
     */
    public void mouseExited(MouseEvent me) {
        stillDownWithoutMoving = false;
    }

    /**
     * Mouse clicked.
     *
     * @param me MouseEvent
     */
    public void mouseClicked(MouseEvent me) {
        if (inPopup) {
            inPopup = false;
            return;
        }
        if (viewer.getGraphDrawer() == null)
            return;

        int meX = me.getX();
        int meY = me.getY();

        NodeSet hitNodes = viewer.getGraphDrawer().getHitNodes(meX, meY);
        EdgeSet hitEdges = viewer.getGraphDrawer().getHitEdges(meX, meY);
        NodeSet hitNodeLabels = viewer.getGraphDrawer().getHitNodeLabels(meX, meY);
        EdgeSet hitEdgeLabels = viewer.getGraphDrawer().getHitEdgeLabels(meX, meY);

        if (current == inScrollByMouse) // in navigation mode, double-click to lose selection
        {
            if (hitNodes.size() == 0 && hitEdges.size() == 0 && hitNodeLabels.size() == 0 && hitEdgeLabels.size() == 0) {
                if (isAllowDeselectAllByMouseClick()) {
                    viewer.selectAllNodes(false);
                    viewer.selectAllEdges(false);
                    viewer.repaint();
                }
                viewer.firePanelClicked(me);
                return;
            }
        }
        current = inClick;

        if (hitNodes.size() == 0 && hitEdges.size() == 0 && hitNodeLabels.size() == 0 && hitEdgeLabels.size() == 0) {
            viewer.firePanelClicked(me);
            return;
        }

        if (hitNodes.size() != 0)
            viewer.fireDoClick(hitNodes, me.getClickCount());
        if (hitEdges.size() != 0)
            viewer.fireDoClick(hitEdges, me.getClickCount());
        if (hitNodeLabels.size() != 0)
            viewer.fireDoClickLabel(hitNodeLabels, me.getClickCount());
        if (hitEdgeLabels.size() != 0)
            viewer.fireDoClickLabel(hitEdgeLabels, me.getClickCount());


        if (me.getClickCount() == 1 && (hitNodes.size() > 0) || hitEdges.size() > 0)
            selectNodesEdges(hitNodes, hitEdges, me.isShiftDown(), me.getClickCount());
        else if (me.getClickCount() == 1 && (hitNodeLabels.size() > 0) || hitEdgeLabels.size() > 0)
            selectNodesEdges(hitNodeLabels, hitEdgeLabels, me.isShiftDown(), me.getClickCount());

        if (viewer.getAllowEdit() && hitNodes.size() == 0 && hitEdges.size() == 0 && me.getClickCount() == 2) {
            // New node:
            if (viewer.getAllowNewNodeDoubleClick()) {
                viewer.setDefaultNodeLocation(trans.d2w(meX, meY));
                Node v = viewer.newNode();
                if (v != null) {
                    viewer.setLocation(v, trans.d2w(meX, meY));
                    viewer.setDefaultNodeLocation(trans.d2w(meX + 10, meY + 10));
                    viewer.repaint();
                }
            }
        } else if (viewer.isAllowInternalEdgePoints() && hitNodes.size() == 0 && hitEdges.size() == 1 && me.getClickCount() == 3) {
            Edge e = hitEdges.getFirstElement();
            EdgeView ev = viewer.getEV(e);
            Point vp = trans.w2d(viewer.getLocation(viewer.getGraph().getSource(e)));
            Point wp = trans.w2d(viewer.getLocation(viewer.getGraph().getTarget(e)));
            int index = ev.hitEdgeRank(vp, wp, trans, me.getX(), meY, 3);
            java.util.List<Point2D> list = viewer.getInternalPoints(e);
            Point2D aptWorld = trans.d2w(me.getX(), meY);
            if (list == null) {
                list = new LinkedList<>();
                list.add(aptWorld);
                viewer.setInternalPoints(e, list);
            } else
                list.add(index, aptWorld);
        } else if (me.getClickCount() == 2
                && ((viewer.isAllowEditNodeLabelsOnDoubleClick() && hitNodeLabels.size() > 0)
                || (viewer.isAllowEditNodeLabelsOnDoubleClick() && hitNodes.size() > 0))) {
// undo node label
            Node v;
            if (viewer.isAllowEditNodeLabelsOnDoubleClick() && hitNodeLabels.size() > 0)
                v = hitNodeLabels.getLastElement();
            else
                v = hitNodes.getLastElement();
            String label = viewer.getLabel(v);
            label = JOptionPane.showInputDialog(viewer, "Edit Node Label:", label);
            if (label != null && !label.equals(viewer.getLabel(v))) {
                viewer.setLabel(v, label);
                viewer.setLabelVisible(v, label.length() > 0);
                viewer.repaint();
            }

        } else if (me.getClickCount() == 2 && ((viewer.isAllowEditEdgeLabelsOnDoubleClick() && hitEdgeLabels.size() > 0)
                || (viewer.isAllowEditEdgeLabelsOnDoubleClick() && hitEdges.size() > 0))) {
            Edge e;
            if (viewer.isAllowEditEdgeLabelsOnDoubleClick() && hitEdgeLabels.size() > 0)
                e = hitEdgeLabels.getLastElement();
            else
                e = hitEdges.getLastElement();
            String label = viewer.getLabel(e);
            label = JOptionPane.showInputDialog(viewer, "Edit Edge Label:", label);
            if (label != null && !label.equals(viewer.getLabel(e))) {
                viewer.setLabel(e, label);
                viewer.setLabelVisible(e, label.length() > 0);
                viewer.repaint();
            }
        } else if (me.getClickCount() == 2 && hitNodes.size() > 0) {
            // select connected component:
            if (allowSelectConnectedComponent) {
                viewer.selectConnectedComponents(hitNodes);
            }
        } else if (me.getClickCount() == 2 && hitNodeLabels.size() > 0) {
            // select connected component:
            if (allowSelectConnectedComponent) {
                viewer.selectConnectedComponents(hitNodeLabels);
            }
        } else if (me.getClickCount() == 2 && hitEdges.size() > 0) {
            // viewer.selectAllBelow(hitEdges);
        }

        current = 0;
    }


    /**
     * Mouse dragged.
     *
     * @param me MouseEvent
     */
    public void mouseDragged(MouseEvent me) {
        stillDownWithoutMoving = false;

        if (me.isPopupTrigger())
            return;

        if (current == inScrollByMouse) {
            viewer.setCursor(Cursors.getClosedHand());

            JScrollPane scrollPane = viewer.getScrollPane();
            int dX = me.getX() - downX;
            int dY = me.getY() - downY;

            if (dY != 0) {
                JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
                int amount = Math.round(dY * (scrollBar.getMaximum() - scrollBar.getMinimum()) / viewer.getHeight());
                if (amount != 0) {
                    scrollBar.setValue(scrollBar.getValue() - amount);
                }
            }
            if (dX != 0) {
                JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
                int amount = Math.round(dX * (scrollBar.getMaximum() - scrollBar.getMinimum()) / viewer.getWidth());
                if (amount != 0) {
                    scrollBar.setValue(scrollBar.getValue() - amount);
                }
            }
        } else if (current == inRubberband) {
            Graphics2D gc = (Graphics2D) viewer.getGraphics();

            if (gc != null) {
                Color color = viewer.getCanvasColor() != null ? viewer.getCanvasColor() : Color.WHITE;
                gc.setXORMode(color);
                if (selRect != null)
                    gc.drawRect(selRect.x, selRect.y, selRect.width, selRect.height);
                selRect = new Rectangle(downX, downY, 0, 0);
                selRect.add(me.getX(), me.getY());
                gc.drawRect(selRect.x, selRect.y, selRect.width, selRect.height);
            }
        } else if (current == inMove) {
            Point2D p2 = trans.d2w(me.getX(), me.getY());
            Point2D p1 = trans.d2w(downX, downY);
            downX = me.getX();
            downY = me.getY();
            Point2D diff = new Point2D.Double(p2.getX() - p1.getX(),
                    p2.getY() - p1.getY());

            boolean moveAll = false;
            double origLength = -1; // use in maintain edge lengths

            if (viewer.getMaintainEdgeLengths()) {
                origLength = canMaintainEdgeLengths();
                if (origLength == -1)
                    //moveAll = true;
                    return; // move nothing...
                // can't maintain edge lengths, move everything
                // else Basic.message("Can Maintain: "+origLength);
            }

            try {
                Graph G = viewer.getGraph();
                for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                    if (viewer.selectedNodes.contains(v) || moveAll) {
                        Point2D p = viewer.getLocation(v);
                        viewer.setLocation(v, p.getX() + diff.getX(),
                                p.getY() + diff.getY());
                    }
                }
                for (Edge e = G.getFirstEdge(); e != null; e = G.getNextEdge(e)) {
                    if (viewer.getSelected(e.getSource()) && viewer.getSelected(e.getTarget())) {
                        java.util.List internalPoints = viewer.getInternalPoints(e);
                        if (internalPoints != null) {
                            for (Object internalPoint : internalPoints) {
                                Point2D apt = (Point2D) internalPoint;
                                apt.setLocation(apt.getX() + diff.getX(), apt.getY() + diff.getY());
                            }
                        }
                    }
                }
            } catch (NotOwnerException ex) {
                Basic.caught(ex);
            } finally {
                if (viewer.getMaintainEdgeLengths() && origLength != -1)
                    maintainEdgeLengths(origLength);
                viewer.repaint();
            }
        } else if (current == inMoveInternalEdgePoint) {
            if (viewer.isAllowInternalEdgePoints()) {
                Point p1 = new Point(downX, downY); // old [pos
                Edge e = hitEdges.getFirstElement();
                if (e != null && viewer.getEV(e).getShape() != EdgeView.ARC_LINE_EDGE) {
                    downX = me.getX();
                    downY = me.getY();
                    Point p2 = new Point(downX, downY);     // new pos
                    if (e != null) {
                        viewer.getEV(e).moveInternalPoint(trans, p1, p2);
                        viewer.repaint();
                        viewer.getGraphDrawer().setEdgesHasMovedInternalPoints(e);
                    }
                }
            }
        } else if (current == inMoveNodeLabel) {
            if (hitNodeLabels.size() > 0) {
                Node v = hitNodeLabels.getFirstElement();
                if (!viewer.getSelected(v))
                    return; // move labels only of selected node
                NodeView nv = viewer.getNV(v);
                INodeDrawer nodeDrawer = viewer.getGraphDrawer().getNodeDrawer();

                if (nv.getLabel() == null)
                    return;

                Graphics2D gc = (Graphics2D) viewer.getGraphics();

                if (gc != null) {
                    Point apt = trans.w2d(nv.getLocation());
                    int meX = me.getX();
                    int meY = me.getY();
                    gc.setXORMode(viewer.getCanvasColor());
                    if (prevPt != null) {
                        gc.drawLine(apt.x, apt.y, prevPt.x, prevPt.y);
                    } else {
                        prevPt = new Point(downX, downY);
                        Point labPt = nv.getLabelPosition(trans);
                        offset.x = labPt.x - downX;
                        offset.y = labPt.y - downY;
                    }
                    gc.drawLine(apt.x, apt.y, meX, meY);
                    nv.hiliteLabel(gc, viewer.trans, viewer.getFont());

                    int labX = meX + offset.x;
                    int labY = meY + offset.y;

                    nv.setLabelPositionRelative(labX - apt.x, labY - apt.y);
                    nv.hiliteLabel(gc, viewer.trans, viewer.getFont());

                    prevPt.x = meX;
                    prevPt.y = meY;
                    nodeLabelsHaveMoved = true;
                    viewer.getGraphDrawer().setNodeHasMovedLabel(v);
                }
            }
        } else if (current == inMoveEdgeLabel) {
            if (hitEdgeLabels.size() > 0) {
                try {
                    final Edge e = hitEdgeLabels.getFirstElement();
                    if (!viewer.getSelected(e))
                        return; // move labels only of selected edges
                    EdgeView ev = viewer.getEV(e);

                    if (ev.getLabel() == null)
                        return;

                    final Graph G = viewer.getGraph();
                    final NodeView vv = viewer.getNV(G.getSource(e));
                    final NodeView wv = viewer.getNV(G.getTarget(e));

                    Point2D nextToV = wv.getLocation();
                    Point2D nextToW = vv.getLocation();
                    if (viewer.getInternalPoints(e) != null) {
                        if (viewer.getInternalPoints(e).size() != 0) {
                            nextToV = viewer.getInternalPoints(e).get(0);
                            nextToW = viewer.getInternalPoints(e).get(viewer.getInternalPoints(e).size() - 1);
                        }
                    }
                    Point pv = vv.computeConnectPoint(nextToV, trans);
                    Point pw = wv.computeConnectPoint(nextToW, trans);

                    if (G.findDirectedEdge(G.getTarget(e), G.getSource(e)) != null)
                        viewer.adjustBiEdge(pv, pw); // want parallel bi-edges

                    final Graphics2D gc = (Graphics2D) viewer.getGraphics();

                    if (gc != null) {
                        ev.setLabelReferenceLocation(nextToV, nextToW, trans);
                        ev.setLabelSize(gc);

                        Point apt = ev.getLabelReferencePoint();
                        int meX = me.getX();
                        int meY = me.getY();
                        gc.setXORMode(viewer.getCanvasColor());
                        if (prevPt != null)
                            gc.drawLine(apt.x, apt.y, prevPt.x, prevPt.y);
                        else {
                            prevPt = new Point(downX, downY);
                            Point labPt = ev.getLabelPosition(trans);
                            offset.x = labPt.x - downX;
                            offset.y = labPt.y - downY;
                        }
                        gc.drawLine(apt.x, apt.y, meX, meY);
                        ev.drawLabel(gc, trans, viewer.getSelected(e));
                        int labX = meX + offset.x;
                        int labY = meY + offset.y;

                        ev.setLabelPositionRelative(labX - apt.x, labY - apt.y);
                        ev.setLabelLayout(EdgeView.USER);
                        ev.drawLabel(gc, trans, viewer.getSelected(e));

                        prevPt.x = meX;
                        prevPt.y = meY;
                        edgeLabelsHaveMoved = true;
                        viewer.getGraphDrawer().setEdgesHasMovedLabel(e);

                    }
                } catch (NotOwnerException ex) {
                    Basic.caught(ex);
                }
            }
        } else if (current == inNewEdge) {
            final Graphics gc = viewer.getGraphics();

            if (gc != null) {
                gc.setXORMode(viewer.getCanvasColor());
                if (selRect != null) // we misuse the selRect here...
                    gc.drawLine(downX, downY, selRect.x, selRect.y);
                selRect = new Rectangle(me.getX(), me.getY(), 0, 0);
                gc.drawLine(downX, downY, me.getX(), me.getY());
            }
        } else if (current == inMoveMagnifier) {
            int meX = me.getX();
            int meY = me.getY();
            if (meX != downX || meY != downY) {
                trans.getMagnifier().move(downX, downY, meX, meY);
                downX = meX;
                downY = meY;
                viewer.repaint();
            }
        } else if (current == inResizeMagnifier) {
            int meY = me.getY();
            if (meY != downY) {
                trans.getMagnifier().resize(downY, meY);
                downX = me.getX();
                downY = meY;
                viewer.repaint();
            }
        }
    }

    /**
     * Mouse moved
     *
     * @param me MouseEvent
     */
    public void mouseMoved(MouseEvent me) {
        stillDownWithoutMoving = false;
        if (viewer.getGraphDrawer() != null && viewer.getGraph().getNumberOfNodes() <= 50000) {
            NodeSet nodes = viewer.getGraphDrawer().getHitNodes(me.getX(), me.getY());
            if (nodes.size() == 0)
                nodes = viewer.getGraphDrawer().getHitNodeLabels(me.getX(), me.getY());
            if (nodes.size() > 0) {
                viewer.setToolTipText(nodes.getFirstElement());
                return;
            }
        }
        viewer.setToolTipText((String) null);
    }

    /**
     * Updates the selection of nodes and edges.
     *
     * @param hitNodes NodeSet
     * @param hitEdges EdgeSet
     * @param shift    boolean
     * @param clicks   boolean
     */
    void selectNodesEdges(NodeSet hitNodes, EdgeSet hitEdges, boolean shift, int clicks) {
        if (hitNodes.size() == 1) // in this case, only do node selection
            hitEdges.clear();

        Graph G = viewer.getGraph();

        boolean changed = false;

        // synchronized (G)
        {
            // no shift, deselect everything:
            if (!shift && (viewer.getNumberSelectedNodes() > 0 || viewer.getNumberSelectedEdges() > 0)) {
                viewer.selectAllNodes(false);
                viewer.selectAllEdges(false);
                changed = true;
            }

            try {
                if ((clicks > 0 || viewer.isAllowRubberbandNodes()) && hitNodes.size() > 0) {
                    NodeSet select = new NodeSet(G);
                    NodeSet deSelect = new NodeSet(G);
                    for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                        if (hitNodes.contains(v)) {
                            if (!shift) {
                                select.add(v);
                                viewer.setSelected(v, true);
                                if (clicks > 1)
                                    break;
                            } else // shift==true
                            {
                                if (!viewer.getSelected(v))
                                    select.add(v);
                                else //
                                    deSelect.add(v);
                            }
                        }
                    }
                    changed = select.size() > 0 || deSelect.size() > 0;
                    viewer.setSelected(select, true);
                    viewer.setSelected(deSelect, false);
                }

                if ((clicks > 0 || viewer.isAllowRubberbandEdges()) && hitEdges.size() > 0) {
                    EdgeSet select = new EdgeSet(G);
                    EdgeSet deSelect = new EdgeSet(G);

                    for (Edge e = G.getFirstEdge(); e != null; e = G.getNextEdge(e)) {
                        if (hitEdges.contains(e)) {
                            if (!shift) {
                                if (clicks == 0 || viewer.getNumberSelectedNodes() == 0) {
                                    select.add(e);
                                    // selectedNodes.insert(G.source(e));
                                    // selectedNodes.insert(G.target(e));
                                }
                                if (clicks > 1)
                                    break;
                            } else // shift==true
                            {
                                if (!viewer.getSelected(e)) {
                                    select.add(e);
                                    // selectedNodes.insert(G.source(e));
                                    // selectedNodes.insert(G.target(e));
                                } else // selectedEdges.member(e)
                                    deSelect.add(e);
                            }
                        }
                    }
                    changed = select.size() > 0 || deSelect.size() > 0;
                    viewer.setSelected(select, true);
                    viewer.setSelected(deSelect, false);
                }
            } finally {
                if (changed)
                    viewer.repaint();
            }
        }
    }

    // KeyListener methods:

    /**
     * Key typed
     *
     * @param ke Keyevent
     */
    public void keyTyped(KeyEvent ke) {
    }

    /**
     * Key pressed
     *
     * @param ke KeyEvent
     */
    public void keyPressed(KeyEvent ke) {
        int r = 1; // rotate angle
        double s = 1.05; // scale factor
        if ((ke.getModifiers() & InputEvent.ALT_MASK) != 0) {
            s = 1.5;
            r = 5;
        } else if ((ke.getModifiers() & InputEvent.CTRL_MASK) != 0) {
            s = 4;
            r = 90;
        }

        JScrollPane scrollPane = viewer.getScrollPane();
        boolean xyLocked = viewer.isKeepAspectRatio();

        if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
            JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
            if (!ke.isShiftDown() && scrollBar.getVisibleAmount() < scrollBar.getMaximum()) {
                scrollBar.setValue(scrollBar.getValue() + scrollBar.getBlockIncrement(1));
            } else {
                if (xyLocked) {
                    if (viewer.isAllowRotation()) {
                        ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans);
                        double angle = trans.getAngle() - r * Math.PI / 100.0;
                        trans.setAngle(angle);
                        spa.adjust(true, true);
                        viewer.resetLabelPositions(true);
                        // final ICommand cmd = new RotateCommand(viewer, viewer.trans, angle);
                        //new Edit(cmd, "rotate left").execute(viewer.getUndoSupportNetwork());
                    }
                } else   // zoom rectilinear
                {
                    ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans);
                    trans.composeScale(1.0 / s, 1);
                    spa.adjust(false, true);
                    //final ICommand cmd = new ZoomCommand(viewer, viewer.trans, 1.0 / s, 1);
                    //new Edit(cmd, "Zoom In").execute(viewer.getUndoSupportNetwork());
                }
            }
        } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
            JScrollBar scrollBar = scrollPane.getHorizontalScrollBar();
            if (!ke.isShiftDown() && scrollBar.getVisibleAmount() < scrollBar.getMaximum()) {
                scrollBar.setValue(scrollBar.getValue() - scrollBar.getBlockIncrement(1));
            } else { //scale
                if (xyLocked) {
                    if (viewer.isAllowRotation()) {
                        ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans);
                        double angle = trans.getAngle() + r * Math.PI / 100.0;
                        trans.setAngle(angle);
                        spa.adjust(true, true);
                        viewer.resetLabelPositions(true);
                    }
                    //final ICommand cmd = new RotateCommand(viewer, viewer.trans, angle);
                    //new Edit(cmd, "rotate right").execute(viewer.getUndoSupportNetwork());
                } else   // zoom rectilinear
                {
                    ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans);
                    trans.composeScale(s, 1);
                    spa.adjust(true, false);

                    //final ICommand cmd = new ZoomCommand(viewer, viewer.trans, s, 1);
                    //new Edit(cmd, "Zoom Out").execute(viewer.getUndoSupportNetwork());
                }
            }
        } else if (ke.getKeyCode() == KeyEvent.VK_UP) {
            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            if (!ke.isShiftDown() && scrollBar.getVisibleAmount() < scrollBar.getMaximum()) {
                scrollBar.setValue(scrollBar.getValue() - scrollBar.getBlockIncrement(1));
            } else { //scale
                ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans);
                double f = xyLocked ? 1.0 / s : 1.0;
                trans.composeScale(f, 1.0 / s);
                spa.adjust(f != 1.0, true);

                //final ICommand cmd = new ZoomCommand(viewer, viewer.trans, f, 1.0 / s);
                //new Edit(cmd, "Zoom In").execute(viewer.getUndoSupportNetwork());
            }
        } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            if (!ke.isShiftDown() && scrollBar.getVisibleAmount() < scrollBar.getMaximum()) {
                scrollBar.setValue(scrollBar.getValue() + scrollBar.getBlockIncrement(1));
            } else { //scale
                ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans);
                double f = (xyLocked ? s : 1.0);
                trans.composeScale(f, s);
                spa.adjust(f != 1.0, true);
                //final ICommand cmd = new ZoomCommand(viewer, viewer.trans, f, s);
                //new Edit(cmd, "zoom Out").execute(viewer.getUndoSupportNetwork());
            }
        } else if (ke.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans);
            trans.composeScale(1.0 / s, 1.0 / s);
            spa.adjust(true, true);
            //final ICommand cmd = new ZoomCommand(viewer, viewer.trans, 1.0 / s, 1.0 / s);
            //new Edit(cmd, "zoom In").execute(viewer.getUndoSupportNetwork());
        } else if (ke.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans);
            trans.composeScale(s, s);
            spa.adjust(true, true);
            //final ICommand cmd = new ZoomCommand(viewer, viewer.trans, s, s);
            //new Edit(cmd, "zoom Out").execute(viewer.getUndoSupportNetwork());
        } else if (viewer.getAllowEdit() && (ke.getKeyCode() == KeyEvent.VK_DELETE || ke.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
            viewer.delSelectedNodes();
            viewer.delSelectedEdges();
            viewer.repaint();
        } else if ((ke.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
            viewer.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Key released
     *
     * @param ke KeyEvent
     */
    public void keyReleased(KeyEvent ke) {
        if ((ke.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
            viewer.resetCursor();
        }
    }

    // ComponentListener methods:

    /**
     * component hidded
     *
     * @param ev ComponentEvent
     */
    public void componentHidden(ComponentEvent ev) {
    }

    /**
     * component moved
     *
     * @param ev ComponentEvent
     */
    public void componentMoved(ComponentEvent ev) {
    }

    /**
     * component resized
     *
     * @param ev ComponentEvent
     */
    public void componentResized(ComponentEvent ev) {
        viewer.setSize(viewer.getSize());
    }

    /**
     * component shown
     *
     * @param ev ComponentEvent
     */
    public void componentShown(ComponentEvent ev) {
    }

    /**
     * If edge lengths can be maintained in user interaction, returns
     * the length of any edge connecting the selected from the non-selected
     * nodes. Otherwise, returns -1
     * We can maintain edge lengths if every edge in the set of edges that
     * separate the selected from the none-selected nodes
     * has the same angle and length
     *
     * @return firstlength double
     */

    private double canMaintainEdgeLengths() {
        Graph graph = viewer.getGraph();
        boolean first = true;
        double firstAngle = 0;
        double firstLength = 0;

        try {
            for (Edge e = graph.getFirstEdge(); e != null; e = graph.getNextEdge(e)) {
                if (!graph.isSpecial(e)) {
                    Node v = graph.getSource(e);
                    Node w = graph.getTarget(e);
                    Point2D pv;
                    Point2D pw;
                    if (viewer.selectedNodes.contains(v) && !viewer.selectedNodes.contains(w)) {
                        pv = viewer.getLocation(v);
                        pw = viewer.getLocation(w);
                    } else if (!viewer.selectedNodes.contains(v) && viewer.selectedNodes.contains(w)) {
                        pv = viewer.getLocation(w);
                        pw = viewer.getLocation(v);
                    } else
                        continue;
                    if (pv == null || pw == null)
                        continue;
                    Point2D q = new Point2D.Double(pw.getX() - pv.getX(), pw.getY() - pv.getY());
                    double angle = Geometry.computeAngle(q);
                    double length = pv.distance(pw);
                    if (first) {
                        firstAngle = angle;
                        firstLength = length;
                        first = false;
                    } else // compare with first line
                    {
                        if ((Math.abs(angle - firstAngle) > 0.01
                                && Math.abs(angle - firstAngle - 6.28318530717958647692) > 0.01)
                                || Math.abs(length - firstLength) > 0.01 * firstLength)
                            return -1;
                    }
                }
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
        return firstLength;
    }

    /**
     * Recompute coordinates so that edge lengths are maintained
     * Assumes canMaintainEdgeLengths returned true!
     *
     * @param origLength double
     */
    private void maintainEdgeLengths(double origLength) {
        Graph G = viewer.getGraph();
        NodeSet visited = new NodeSet(G);

        double length = -1;
        Point2D diff = null;

        try {
            // put all selected nodes into visited set:
            for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v))
                if (viewer.selectedNodes.contains(v))
                    visited.add(v);

            for (Edge e = G.getFirstEdge(); e != null; e = G.getNextEdge(e)) {
                if (!G.isSpecial(e)) {
                    Node v = G.getSource(e);
                    Node w = G.getTarget(e);
                    Node z;
                    Point2D pv;
                    Point2D pw;
                    if (viewer.selectedNodes.contains(v) && !viewer.selectedNodes.contains(w)
                            && !visited.contains(w)) {
                        pv = viewer.getLocation(v);
                        pw = viewer.getLocation(w);
                        z = w;
                    } else if (!viewer.selectedNodes.contains(v) && viewer.selectedNodes.contains(w)
                            && !visited.contains(v)) {
                        pv = viewer.getLocation(w);
                        pw = viewer.getLocation(v);
                        z = v;
                    } else
                        continue;
                    if (pv == null || pw == null)
                        continue;

                    if (length == -1) // use first edge to define diff
                    {
                        length = pv.distance(pw);

                        if (Math.abs(length - origLength) < 0.001 * length)
                            return; // no change of length, return

                        diff = new Point2D.Double((length - origLength) * (pw.getX() - pv.getX()) / length,
                                (length - origLength) * (pw.getY() - pv.getY()) / length);
                    }

                    shiftAllNodesRecursively(G, z, diff, visited);
                }
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * recursively shifts all nodes necessary to maintain edge lengths
     *
     * @param G       Graph
     * @param z       Node
     * @param diff    Point2D
     * @param visited NodeSet
     */
    private void shiftAllNodesRecursively(Graph G, Node z, Point2D diff, NodeSet visited) {
        try {
            if (!visited.contains(z)) {
                if (viewer.getLocation(z) != null) {
                    viewer.setLocation(z, viewer.getLocation(z).getX() - diff.getX(),
                            viewer.getLocation(z).getY() - diff.getY());
                }
                visited.add(z);
                for (Edge e = G.getFirstAdjacentEdge(z); e != null; e = G.getNextAdjacentEdge(e, z)) {
                    Node v = G.getOpposite(z, e);
                    shiftAllNodesRecursively(G, v, diff, visited);
                }
            }
        } catch (NotOwnerException ex) {
            Basic.caught(ex);
        }
    }

    /**
     * react to a mouse wheel event
     *
     * @param e
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            boolean xyLocked = viewer.isKeepAspectRatio();

            boolean doScaleVertical = !e.isMetaDown() && !e.isAltDown() && !e.isShiftDown() && !xyLocked;
            boolean doScaleHorizontal = !e.isMetaDown() && !e.isControlDown() && !e.isAltDown() && e.isShiftDown();
            boolean doScrollVertical = !e.isMetaDown() && e.isAltDown() && !e.isShiftDown() && !xyLocked;
            boolean doScrollHorizontal = !e.isMetaDown() && e.isAltDown() && e.isShiftDown();
            boolean doScaleBoth = (e.isMetaDown() || xyLocked) && !e.isAltDown() && !e.isShiftDown();
            boolean doRotate = !e.isMetaDown() && e.isAltDown() && !e.isShiftDown() && xyLocked;

            boolean useMag = trans.getMagnifier().isActive();
            trans.getMagnifier().setActive(false);

            if (doScrollVertical) {
                viewer.getScrollPane().getVerticalScrollBar().setValue(viewer.getScrollPane().getVerticalScrollBar().getValue() + e.getUnitsToScroll());
            } else if (doScaleVertical) {
                ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans, e.getPoint());
                double toScroll = 1.0 + (e.getUnitsToScroll() / 100.0);
                double s = (toScroll > 0 ? 1.0 / toScroll : toScroll);
                double scale = s * trans.getScaleY();
                if (scale >= GraphView.YMIN_SCALE && scale <= GraphView.YMAX_SCALE) {
                    trans.composeScale(1, s);
                    viewer.repaint();
                    spa.adjust(false, true);
                }
            } else if (doScaleBoth) {
                ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans, e.getPoint());
                double toScroll = 1.0 + (e.getUnitsToScroll() / 100.0);
                double s = (toScroll > 0 ? 1.0 / toScroll : toScroll);
                double scaleX = s * trans.getScaleX();
                double scaleY = s * trans.getScaleY();
                if (scaleX >= GraphView.XMIN_SCALE && scaleX <= GraphView.XMAX_SCALE && scaleY >= GraphView.YMIN_SCALE && scaleY <= GraphView.YMAX_SCALE) {
                    trans.composeScale(s, s);
                    viewer.repaint();
                    spa.adjust(true, true);
                }
            } else if (doScrollHorizontal) {
                viewer.getScrollPane().getHorizontalScrollBar().setValue(viewer.getScrollPane().getHorizontalScrollBar().getValue() + e.getUnitsToScroll());
            } else if (doScaleHorizontal && !xyLocked) { //scale
                ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans, e.getPoint());
                double toScroll = 1.0 + (e.getUnitsToScroll() / 100.0);
                double s = (toScroll > 0 ? 1.0 / toScroll : toScroll);
                double scale = s * trans.getScaleX();
                if (scale >= GraphView.XMIN_SCALE && scale <= GraphView.XMAX_SCALE) {
                    trans.composeScale(s, 1);
                    viewer.repaint();
                    spa.adjust(true, false);
                }
            } else if (doRotate) {
                if (viewer.isAllowRotation()) {
                    ScrollPaneAdjuster spa = new ScrollPaneAdjuster(viewer.getScrollPane(), trans, e.getPoint());
                    double angle = trans.getAngle() - e.getUnitsToScroll() * Math.PI / 1000.0;
                    trans.setAngle(angle);
                    viewer.getGraphDrawer().resetLabelPositions(false);
                    viewer.repaint();
                    spa.adjust(true, true);
                }
            }
            trans.getMagnifier().setActive(useMag);
        }
    }

    /**
     * is user allowed to deselect all by mouse click off graph?
     *
     * @return true, if allowed
     */
    public boolean isAllowDeselectAllByMouseClick() {
        return allowDeselectAllByMouseClick;
    }

    /**
     * is user allowed to deselect all by mouse click off graph?
     *
     * @param allowDeselectAllByMouseClick
     */
    public void setAllowDeselectAllByMouseClick(boolean allowDeselectAllByMouseClick) {
        this.allowDeselectAllByMouseClick = allowDeselectAllByMouseClick;
    }

    public boolean isAllowSelectConnectedComponent() {
        return allowSelectConnectedComponent;
    }

    public void setAllowSelectConnectedComponent(boolean allowSelectConnectedComponent) {
        this.allowSelectConnectedComponent = allowSelectConnectedComponent;
    }
}

// EOF
