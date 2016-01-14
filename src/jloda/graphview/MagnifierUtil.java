/**
 * MagnifierUtil.java 
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

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

/**
 * utilities used when drawing edges under magnification
 * Daniel Huson, 1.2007
 */
public class MagnifierUtil {
    int spacing = 7;
    private final GraphView graphView;
    private final Transform trans;

    private List oldInternalPoints = null;

    /**
     * constructor
     *
     * @param graphView
     */
    public MagnifierUtil(GraphView graphView) {
        this.graphView = graphView;
        this.trans = graphView.trans;
    }

    /**
     * add internal points to approximate curved edges
     *
     * @param e
     * @return original internal points
     */
    public List addInternalPoints(Edge e) {

        // TODO: add additional points between existing ones!
        oldInternalPoints = graphView.getInternalPoints(e);
        if (trans.getMagnifier().isActive() && !trans.getMagnifier().isInRectilinearMode()) {
            Point2D prevPt = graphView.getNV(e.getSource()).getLocation();

            java.util.List internalPoints = new LinkedList();

            if (oldInternalPoints != null) {
                for (Object oldInternalPoint : oldInternalPoints) {
                    final Point2D curPt = (Point2D) oldInternalPoint;
                    addInternalPoints(prevPt, curPt, internalPoints);
                    internalPoints.add(curPt);
                    prevPt = curPt;
                }
            }
            addInternalPoints(prevPt, graphView.getNV(e.getTarget()).getLocation(), internalPoints);
            graphView.setInternalPoints(e, internalPoints);
        }
        return oldInternalPoints;
    }

    /**
     * add points between two internal points
     *
     * @param pv             support point in world coordinates
     * @param pw             support point in world coordinates
     * @param internalPoints
     */
    private void addInternalPoints(Point2D pv, Point2D pw, java.util.List internalPoints) {
        int count = (int) trans.w2d(pv).distance(trans.w2d(pw)) / spacing;
        if (count > 0) {
            double dX = (pw.getX() - pv.getX()) / count;
            double dY = (pw.getY() - pv.getY()) / count;
            for (int i = 1; i < count; i++) {
                double x = pv.getX() + dX * i;
                double y = pv.getY() + dY * i;
                internalPoints.add(new Point2D.Double(x, y));
            }
        }
    }

    /**
     * remove the added points
     *
     * @param e
     */
    public void removeAddedInternalPoints(Edge e) {
        graphView.setInternalPoints(e, oldInternalPoints);
    }

    /**
     * get spacing between points
     *
     * @return spacing in pixels
     */
    public int getSpacing() {
        return spacing;
    }

    /**
     * set spacing between points
     *
     * @param spacing
     */
    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }
}
