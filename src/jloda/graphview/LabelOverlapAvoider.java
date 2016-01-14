/**
 * LabelOverlapAvoider.java 
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

import jloda.graph.*;

import java.awt.*;
import java.awt.geom.Area;

/**
 * this class helps to avoid overlapping labels by suppressing some
 * Daniel Huson, 1.2007
 */
public class LabelOverlapAvoider {
    private final Transform trans;

    // the following code is used to ensure that labels do not overlap:
    private final ViewBase[] history;
    private int historyA = 0;  // first filled pos
    private int historyB = 0; //first empty pos
    private final NodeSet visibleNodeLabels;
    private final EdgeSet visibleEdgeLabels;
    private boolean enabled = false;
    Shape firstShape; // keep first shape to avoid wrap-around problem

    /**
     * constructor
     *
     * @param graphView
     * @param length    number of labels to remember
     */
    public LabelOverlapAvoider(GraphView graphView, int length) {
        this.trans = graphView.trans;
        visibleNodeLabels = new NodeSet(graphView.getGraph());
        visibleEdgeLabels = new EdgeSet(graphView.getGraph());
        history = new NodeView[length];
        historyA = 0;
        historyB = 0;
        firstShape = null;
    }

    // reset the code

    public void resetHasNoOverlapToPreviouslyDrawnLabels() {
        historyA = 0;
        historyB = 0;
        visibleNodeLabels.clear();
        firstShape = null;
    }

    /**
     * determine whether to draw a label
     *
     * @param v  node or edge
     * @param vb nodeview or edgeview
     * @return true, if this label will not overlap the last couple drawn
     */
    public boolean hasNoOverlapToPreviouslyDrawnLabels(NodeEdge v, ViewBase vb) {
        if (!isEnabled())
            return true;
        if (!vb.isLabelVisible())
            return true;
        if (vb.getLabel() == null || vb.getLabel().length() == 0)
            return false;

        Shape shape = vb.getLabelShape(trans);


        Area area = new Area(shape);

        if (firstShape != null && intersects(area, firstShape))
            return false;

        if (historyA <= historyB) {
            for (int i = historyA; i < historyB; i++) {
                if (intersects(area, history[i].getLabelShape(trans)))
                    return false;
            }
            history[historyB] = vb;
            historyB++;
            if (historyB == history.length) {
                historyB = 0;
                if (historyA == 0)
                    historyA++;
            }
        } else {
            for (int i = 0; i < historyB; i++) {
                if (intersects(area, history[i].getLabelShape(trans)))
                    return false;
            }
            for (int i = historyA; i < history.length; i++) {
                if (intersects(area, history[i].getLabelShape(trans)))
                    return false;
            }
            history[historyB] = vb;
            historyB++;
            if (historyB == historyA) {
                historyA++;
                if (historyA == history.length)
                    historyA = 0;
            }
        }
        if (firstShape == null)
            firstShape = shape;

        if (v instanceof Node)
            visibleNodeLabels.add((Node) v);
        else if (v instanceof Edge)
            visibleEdgeLabels.add((Edge) v);
        return true;
    }

    /**
     * does the shape s intersect the shape b? a is the area of s
     *
     * @param a
     * @param b
     * @return true, if a and b intersect
     */
    private boolean intersects(Area a, Shape b) {
        if (b instanceof Rectangle) {
            if (a.intersects((Rectangle) b))
                return true;
        } else {
            Area inter = (Area) a.clone();
            inter.intersect(new Area(b));
            if (!inter.isEmpty())
                return true;
        }
        return false;
    }

    /**
     * gets the set of all nodes whose labels were permitted
     *
     * @return nodes with visible labels
     */
    public boolean isVisible(Node v) {
        return !isEnabled() || visibleNodeLabels.contains(v);
    }

    /**
     * gets the set of all edges whose labels were permitted
     *
     * @return edges with visible labels
     */
    public boolean isVisible(Edge e) {
        return !isEnabled() || visibleEdgeLabels.contains(e);
    }

    /**
     * are we suppressing overlapping labels?
     *
     * @return true, if labels are being suppressed
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * are we suppressing overlapping labels?
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
