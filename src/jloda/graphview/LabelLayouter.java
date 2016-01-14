/**
 * LabelLayouter.java 
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

import jloda.graph.Edge;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.util.Basic;
import jloda.util.Geometry;
import jloda.util.ProgramProperties;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * automatic layout of labels
 *
 * @author huson
 *         Date: 09-Jan-2004
 */
public class LabelLayouter {
    final Graphics2D gc;
    final List<Rectangle> rects;

    /**
     * constructor
     *
     * @param gc
     */
    public LabelLayouter(Graphics2D gc) {
        this.gc = gc;
        rects = new LinkedList<>();
    }

    /**
     * layouts out label so that it does not cover any already visible
     *
     * @param graphView
     * @param v
     */
    private int layout(GraphView graphView, Node v, boolean changeLocations) {
        NodeView nv = graphView.getNV(v);
        Point apt = graphView.trans.w2d(nv.getLocation());
        Rectangle arect = nv.getLabelRect(graphView.trans);

        if (arect == null)
            return 0;

        if (nv != null)
            arect.y -= nv.getLabelSize().height;

        int count = 0;
        boolean ok;

        int sign;
        do {
            if ((count % 2) == 0)
                sign = 1;
            else
                sign = -1;
            ok = true;
            for (Rectangle brect : rects) {
                if (brect != null && arect.intersects(brect)) {
                    arect.translate(0, sign * count * 5);
                    ok = false;
                    break;
                }
            }
            count++;
        } while (!ok && count < 200);

        //gc.drawRect(arect.x, arect.y, arect.width, arect.height);

        rects.add((Rectangle) arect.clone());

        if (nv.getLabelSize() != null)
            arect.y += nv.getLabelSize().height;

        apt.x = arect.x - apt.x;
        apt.y = arect.y - apt.y;

        if (changeLocations)
            nv.setLabelPositionRelative(apt);

        return count * count;
    }


    /**
     * initial layout of node labels opposite the edges entering the node
     *
     * @param trans
     * @param graphView
     */
    public void initialLayout(Transform trans, GraphView graphView) {
        Graph graph = graphView.getGraph();

        for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
            NodeView nv = graphView.getNV(v);
            if (nv.getLabelLayout() == NodeView.LAYOUT && nv.getLabel() != null && nv.getLabel().length() > 0) {
                nv.setLabelAngle(0);
                Point2D vpt = trans.w2d(nv.getLocation());
                Rectangle vrect = nv.getLabelRect(trans);
                if (vrect == null)
                    continue;
                int count = 0;
                Point2D apt = new Point2D.Double();
                for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
                    Node w = v.getOpposite(e);
                    Point2D bpt = trans.w2d(graphView.getLocation(w));
                    List internalPoints = graphView.getInternalPoints(e);
                    if (internalPoints != null && internalPoints.size() > 0) {
                        if (e.getSource() == v)
                            bpt = trans.w2d((Point2D) internalPoints.get(0));
                        else
                            bpt = trans.w2d((Point2D) internalPoints.get(internalPoints.size() - 1));
                    }
                    apt.setLocation(apt.getX() + bpt.getX(), apt.getY() + bpt.getY());
                    count++;
                }
                if (count > 0)
                    apt.setLocation(apt.getX() / count, apt.getY() / count);
                apt.setLocation(apt.getX() - vpt.getX(), apt.getY() - vpt.getY());

                double angle;
                if (Math.abs(apt.getX()) < 0.0001 && Math.abs(apt.getY()) < 0.0001)
                    angle = Math.PI;
                else
                    angle = Geometry.moduloTwoPI(Geometry.computeAngle(apt));

                apt = Geometry.translateByAngle(new Point2D.Double(0, 0), angle + Math.PI, 12);

                Point pt = new Point();
                pt.setLocation(apt);
                int width = vrect.width;
                int height = vrect.height;
                int xoffset;
                int yoffset;
                if (angle >= 0.25 * Math.PI && angle <= 0.75 * Math.PI) // north
                {
                    xoffset = -width / 2;
                    yoffset = height / 2;
                } else if (angle >= 0.75 * Math.PI && angle <= 1.25 * Math.PI)   // east
                {
                    xoffset = 0;
                    yoffset = (3 * height) / 2;
                } else if (angle >= 1.25 * Math.PI && angle <= 1.75 * Math.PI) //south
                {
                    xoffset = -width / 2;
                    yoffset = 2 * height;
                } else // west
                {
                    xoffset = -width;
                    yoffset = (3 * height) / 2;
                }
                pt.x += xoffset;
                pt.y += yoffset;
                nv.setLabelPositionRelative(pt);
                nv.setLabelLayout(NodeView.LAYOUT);
            }
        }
    }

    /**
     * lays out all node labels so that they don't overlap
     *
     * @param trans
     * @param graphView
     */
    public void nonOverlappingLayout(Transform trans, GraphView graphView) {
        Graph G = graphView.getGraph();

        int bestCost = Integer.MAX_VALUE;
        int bestRun = 0;

        List<Node> nodes = new LinkedList<>();
        for (Node v = G.getFirstNode(); v != null; v = v.getNext()) {
            nodes.add(v);
        }

        int runs = ProgramProperties.get("label-layout-iterations", 10);
        for (int i = 0; i < runs; i++) {
            rects.clear();

            for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
                NodeView nv = graphView.getNV(v);
                if (nv.getLabelColor() != null && nv.getLabel() != null && nv.getLabel().length() > 0) {

                    if (nv.getLabelLayout() != NodeView.LAYOUT) {
                        {
                            Rectangle arect = nv.getLabelRect(trans);
                            rects.add(arect);
                            // gc.drawRect(arect.x, arect.y, arect.width, arect.height);

                        }
                    }
                }
            }

            int cost = 0;
            for (Iterator it = Basic.randomize(nodes.iterator(), 17 * i); it.hasNext(); ) {
                Node v = (Node) it.next();
                NodeView nv = graphView.getNV(v);
                if (nv.getLabelColor() != null && nv.getLabel() != null && nv.getLabel().length() > 0) {

                    if (nv.getLabelLayout() == NodeView.LAYOUT) {
                        cost += layout(graphView, v, false);
                    }
                }
            }

            if (cost < bestCost) {
                //System.err.println("Cost: " + bestCost+" -> "+cost);
                bestCost = cost;
                bestRun = i;
            }
        }

        rects.clear();

        for (Node v = G.getFirstNode(); v != null; v = G.getNextNode(v)) {
            NodeView nv = graphView.getNV(v);
            if (nv.getLabelColor() != null && nv.getLabel() != null && nv.getLabel().length() > 0) {

                if (nv.getLabelLayout() != NodeView.LAYOUT) {
                    {
                        Rectangle arect = nv.getLabelRect(trans);
                        rects.add(arect);
                        // gc.drawRect(arect.x, arect.y, arect.width, arect.height);

                    }
                }
            }
        }

        for (Iterator it = Basic.randomize(nodes.iterator(), 17 * bestRun); it.hasNext(); ) {
            Node v = (Node) it.next();
            NodeView nv = graphView.getNV(v);
            if (nv.getLabelColor() != null && nv.getLabel() != null && nv.getLabel().length() > 0) {

                if (nv.getLabelLayout() == NodeView.LAYOUT) {
                    layout(graphView, v, true);
                }
            }
        }
    }
}
