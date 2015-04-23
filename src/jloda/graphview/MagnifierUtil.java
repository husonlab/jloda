/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
