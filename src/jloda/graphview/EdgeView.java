/**
 * EdgeView.java 
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
/**
 * Edge visualization
 *
 * @version $Id: EdgeView.java,v 1.61 2010-05-18 15:42:26 huson Exp $
 *
 * @author Daniel Huson
 */
package jloda.graphview;

import gnu.jpdf.PDFGraphics;
import jloda.util.Basic;
import jloda.util.Geometry;
import jloda.util.ProgramProperties;
import jloda.util.parse.NexusStreamParser;

import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Edge visualization
 */
final public class EdgeView extends ViewBase implements Cloneable { //, IEdgeView {
    private Font font;
    private byte shape = POLY_EDGE;
    private byte direction = DIRECTED;
    private java.util.List<Point2D> internalPoints = null;
    private Point labelReferencePoint = null; // label reference point in device coordinates

    /**
     * polygonal edge
     */
    public final static byte POLY_EDGE = 1;
    /**
     * arc+line edge
     */
    public final static byte ARC_LINE_EDGE = 2;
    /**
     * quadratic polynomial edge. Must contain precisely one interior control point.
     */
    public final static byte QUAD_EDGE = 3;
    /**
     * cubic polynomial edge. Must contain precisely two interior control points.
     */
    public final static byte CUBIC_EDGE = 4;
    /**
     * straight edge
     */
    public final static byte STRAIGHT_EDGE = 5;
    /**
     * rounded edge
     */
    public final static byte ROUNDED_EDGE = 6;
    public static int ROUNDED_EDGE_INCREMENT = 50;

    /**
     * Edge type.
     */
    public static final byte UNDIRECTED = 1;
    public static final byte DIRECTED = 2;
    public static final byte BIDIRECTED = 3;
    public static final byte RDIRECTED = 4;

    /**
     * Construct an edge view.
     */
    public EdgeView() {
        labelLayout = CENTRAL;
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeView
     */
    public EdgeView(EdgeView src) {
        this();
        copy(src);
    }

    /**
     * copies the given src edge view
     *
     * @param src
     */
    public void copy(EdgeView src) {
        super.copy(src);
        linewidth = src.linewidth;
        shape = src.shape;
        direction = src.direction;
        if (src.internalPoints == null)
            internalPoints = null;
        else {
            internalPoints = new LinkedList<>();
            for (Point2D apt : src.internalPoints) {
                internalPoints.add((Point2D) apt.clone());
            }
        }
        labelLayout = src.getLabelLayout();
    }

    /**
     * Gets the direction.
     *
     * @return direction int
     */
    public int getDirection() {
        return direction;
    }


    /**
     * Sets the edge shape.
     *
     * @param a int
     */
    public void setShape(byte a) {
        shape = a;
    }

    /**
     * gets the edge shape
     *
     * @return edge shape
     */
    public byte getShape() {
        return shape;
    }

    /**
     * Sets the edge direction.
     *
     * @param a int
     */
    public void setDirection(byte a) {
        direction = a;
    }


    /**
     * gets the font
     *
     * @return font used for drawing label, or null, if default is to be used
     */
    public Font getFont() {
        return font;
    }

    /**
     * sets the font
     *
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Draw the edge given device coordinates.
     *
     * @param gc Graphics
     * @param vp Point in device coordinates
     * @param wp Point in device coordiantes
     */
    public void draw2(Graphics2D gc, Point vp, Point wp, Transform trans, boolean hilited) {
        if (fgColor != null) {
            if (hilited) {// draw edge highlighting first
                if (fgColor.equals(ProgramProperties.SELECTION_COLOR))
                    gc.setColor(Color.ORANGE);
                else
                    gc.setColor(ProgramProperties.SELECTION_COLOR);
                if (shape == STRAIGHT_EDGE || getInternalPoints() == null || getInternalPoints().size() == 0) {
                    gc.drawLine(vp.x - 1, vp.y - 1, wp.x - 1, wp.y - 1);
                    gc.drawLine(vp.x - 1, vp.y + 1, wp.x - 1, wp.y + 1);
                    gc.drawLine(vp.x + 1, vp.y - 1, wp.x + 1, wp.y - 1);
                    gc.drawLine(vp.x + 1, vp.y + 1, wp.x + 1, wp.y + 1);

                } else // some internal points are given
                {
                    Point prev = vp;
                    for (Point2D aptWorld : getInternalPoints()) {
                        final Point apt = trans.w2d(aptWorld);
// if(shape==GraphView.poly_edge)
                        gc.drawLine(prev.x - 1, prev.y - 1, apt.x - 1, apt.y - 1);
                        gc.drawLine(prev.x - 1, prev.y + 1, apt.x - 1, apt.y + 1);
                        gc.drawLine(prev.x + 1, prev.y - 1, apt.x + 1, apt.y - 1);
                        gc.drawLine(prev.x + 1, prev.y + 1, apt.x + 1, apt.y + 1);

                        prev = apt;
                    }
                    gc.drawLine(prev.x - 1, prev.y - 1, wp.x - 1, wp.y - 1);
                    gc.drawLine(prev.x - 1, prev.y + 1, wp.x - 1, wp.y + 1);
                    gc.drawLine(prev.x + 1, prev.y - 1, wp.x + 1, wp.y - 1);
                    gc.drawLine(prev.x + 1, prev.y + 1, wp.x + 1, wp.y + 1);
                }
            }

            // now draw un-highlighted edge:
            if (enabled)
                gc.setColor(fgColor);
            else
                gc.setColor(DISABLED_COLOR);

            Point vp1 = null;
            final Point wp1;

            if (shape == STRAIGHT_EDGE || getInternalPoints() == null || getInternalPoints().size() == 0) {
                vp1 = wp;
                wp1 = vp;

                gc.drawLine(vp.x, vp.y, wp.x, wp.y);
            } else // some internal points are given
            {
                Point prev = vp;
                for (Point2D point2D : getInternalPoints()) {
                    final Point apt = trans.w2d(point2D);
                    if (vp1 == null)
                        vp1 = apt;

                    gc.drawLine(prev.x, prev.y, apt.x, apt.y);
                    prev = apt;
                }
                wp1 = prev;
                gc.drawLine(prev.x, prev.y, wp.x, wp.y);
            }

            if (direction == DIRECTED ||
                    direction == BIDIRECTED)
                drawArrowHead(gc, wp1, wp);
            else if (direction == RDIRECTED)
                drawArrowHead(gc, vp1, vp);
        }
    }

    /**
     * Draw the edge given device coordinates.
     *
     * @param gc Graphics
     * @param vp Point in device coordinates
     * @param wp Point in device coordinates
     */
    public void draw(Graphics2D gc, Point vp, Point wp, Transform trans, boolean hilited) {
        if (hilited) {// draw edge highlighting first
            gc.setStroke(HEAVY_STROKE);

            if (fgColor != null && fgColor.equals(ProgramProperties.SELECTION_COLOR))
                gc.setColor(Color.ORANGE);
            else
                gc.setColor(ProgramProperties.SELECTION_COLOR);
            if (shape == STRAIGHT_EDGE || getInternalPoints() == null || getInternalPoints().size() == 0) {
                gc.drawLine(vp.x - 1, vp.y - 1, wp.x - 1, wp.y - 1);
                gc.drawLine(vp.x - 1, vp.y + 1, wp.x - 1, wp.y + 1);
                gc.drawLine(vp.x + 1, vp.y - 1, wp.x + 1, wp.y - 1);
                gc.drawLine(vp.x + 1, vp.y + 1, wp.x + 1, wp.y + 1);
            } else if (shape == ROUNDED_EDGE && getInternalPoints().size() == 1) {
                final Point vp1 = trans.w2d(getInternalPoints().get(0));
                final int dist = (int) trans.w2d(ROUNDED_EDGE_INCREMENT, 0).getX() - (int) trans.w2d(0, 0).getX();
                final Point center = new Point(dist < wp.x - vp.x ? vp.x + dist : wp.x, wp.y);

                gc.draw(new QuadCurve2D.Double(vp.x - 1, vp.y - 1, vp1.x - 1, vp1.y - 1, center.x - 1, center.y - 1));
                gc.draw(new QuadCurve2D.Double(vp.x - 1, vp.y + 1, vp1.x - 1, vp1.y + 1, center.x - 1, center.y + 1));
                gc.draw(new QuadCurve2D.Double(vp.x + 1, vp.y - 1, vp1.x + 1, vp1.y - 1, center.x + 1, center.y - 1));
                gc.draw(new QuadCurve2D.Double(vp.x + 1, vp.y + 1, vp1.x + 1, vp1.y + 1, center.x + 1, center.y + 1));

                gc.drawLine(center.x - 1, center.y - 1, wp.x - 1, wp.y - 1);
                gc.drawLine(center.x - 1, center.y + 1, wp.x - 1, wp.y + 1);
                gc.drawLine(center.x + 1, center.y - 1, wp.x + 1, wp.y - 1);
                gc.drawLine(center.x + 1, center.y + 1, wp.x + 1, wp.y + 1);

            } else if (shape == QUAD_EDGE && getInternalPoints().size() == 1) {
                Point aPt = trans.w2d(getInternalPoints().get(0));
                gc.draw(new QuadCurve2D.Double(vp.x - 1, vp.y - 1, aPt.x - 1, aPt.y - 1, wp.x - 1, wp.y - 1));
                gc.draw(new QuadCurve2D.Double(vp.x - 1, vp.y + 1, aPt.x - 1, aPt.y + 1, wp.x - 1, wp.y + 1));
                gc.draw(new QuadCurve2D.Double(vp.x + 1, vp.y - 1, aPt.x + 1, aPt.y - 1, wp.x + 1, wp.y - 1));
                gc.draw(new QuadCurve2D.Double(vp.x + 1, vp.y + 1, aPt.x + 1, aPt.y + 1, wp.x + 1, wp.y + 1));
                final Stroke stroke = gc.getStroke();
                gc.setStroke(NORMAL_STROKE);
                gc.drawRect(aPt.x - 2, aPt.y - 2, 4, 4);
                gc.setStroke(stroke);
            } else if (shape == CUBIC_EDGE && getInternalPoints().size() == 2) {
                Point aPt = trans.w2d(getInternalPoints().get(0));
                Point bPt = trans.w2d(getInternalPoints().get(1));
                gc.draw(new CubicCurve2D.Double(vp.x - 1, vp.y - 1, aPt.x - 1, aPt.y - 1, bPt.x - 1, bPt.y - 1, wp.x - 1, wp.y - 1));
                gc.draw(new CubicCurve2D.Double(vp.x - 1, vp.y + 1, aPt.x - 1, aPt.y + 1, bPt.x - 1, bPt.y + 1, wp.x - 1, wp.y + 1));
                gc.draw(new CubicCurve2D.Double(vp.x + 1, vp.y - 1, aPt.x + 1, aPt.y - 1, bPt.x + 1, bPt.y - 1, wp.x + 1, wp.y - 1));
                gc.draw(new CubicCurve2D.Double(vp.x + 1, vp.y + 1, aPt.x + 1, aPt.y + 1, bPt.x + 1, bPt.y + 1, wp.x + 1, wp.y + 1));
                final Stroke stroke = gc.getStroke();
                gc.setStroke(NORMAL_STROKE);
                gc.drawRect(aPt.x - 2, aPt.y - 2, 4, 4);
                gc.drawRect(bPt.x - 2, bPt.y - 2, 4, 4);
                gc.setStroke(stroke);
            } else if (shape == ARC_LINE_EDGE && getInternalPoints().size() == 2) {
                // node vp is start of arc, first internal point is center of circle, second is end of arc, second is joined to wp by straight line
                Iterator it = getInternalPoints().iterator();
                Point center = trans.w2d((Point2D) it.next());
                Point arcStart = (Point) vp.clone();
                Point arcEnd = trans.w2d((Point2D) it.next());
                Point lineStart = (Point) arcEnd.clone();
                // flip along h-axis:
                arcStart.y = center.y - (arcStart.y - center.y);
                arcEnd.y = center.y - (arcEnd.y - center.y);

                double dist = arcStart.distance(center);
                Point2D diffV = Geometry.diff(arcStart, center);
                double angleV = Geometry.computeAngle(diffV);
                Point2D diffEnd = Geometry.diff(arcEnd, center);
                double angleEnd = Geometry.computeAngle(diffEnd);

                double minAngle = angleV;
                double maxAngle = angleEnd;
                if (minAngle > maxAngle) {
                    double tmp = minAngle;
                    minAngle = maxAngle;
                    maxAngle = tmp;
                }
                if (maxAngle - minAngle > Math.PI) {
                    double tmp = minAngle + 2 * Math.PI;
                    minAngle = maxAngle;
                    maxAngle = tmp;
                }
                double extent = maxAngle - minAngle;

                dist += 1;
                Arc2D arc = new Arc2D.Double(center.getX() - dist, center.getY() - dist, 2 * dist, 2 * dist, Geometry.rad2deg(minAngle), Geometry.rad2deg(extent), Arc2D.OPEN);
                gc.draw(arc);
                dist -= 2;
                arc = new Arc2D.Double(center.getX() - dist, center.getY() - dist, 2 * dist, 2 * dist, Geometry.rad2deg(minAngle), Geometry.rad2deg(extent), Arc2D.OPEN);
                gc.draw(arc);
                gc.drawLine(lineStart.x - 1, lineStart.y - 1, wp.x - 1, wp.y - 1);
                gc.drawLine(lineStart.x - 1, lineStart.y + 1, wp.x - 1, wp.y + 1);
                gc.drawLine(lineStart.x + 1, lineStart.y - 1, wp.x + 1, wp.y - 1);
                gc.drawLine(lineStart.x + 1, lineStart.y + 1, wp.x + 1, wp.y + 1);
            } else // some internal points are given
            {
                Point prev = vp;
                for (Point2D point2D : getInternalPoints()) {
                    final Point aPt = trans.w2d(point2D);
                    gc.drawLine(prev.x - 1, prev.y - 1, aPt.x - 1, aPt.y - 1);
                    gc.drawLine(prev.x - 1, prev.y + 1, aPt.x - 1, aPt.y + 1);
                    gc.drawLine(prev.x + 1, prev.y - 1, aPt.x + 1, aPt.y - 1);
                    gc.drawLine(prev.x + 1, prev.y + 1, aPt.x + 1, aPt.y + 1);
                    gc.drawRect(aPt.x - 2, aPt.y - 2, 4, 4);
                    prev = aPt;
                }
                gc.drawLine(prev.x - 1, prev.y - 1, wp.x - 1, wp.y - 1);
                gc.drawLine(prev.x - 1, prev.y + 1, wp.x - 1, wp.y + 1);
                gc.drawLine(prev.x + 1, prev.y - 1, wp.x + 1, wp.y - 1);
                gc.drawLine(prev.x + 1, prev.y + 1, wp.x + 1, wp.y + 1);
            }
        }

        if (fgColor != null) {
            // now draw un-highlighted edge:
            if (enabled)
                gc.setColor(fgColor);
            else
                gc.setColor(DISABLED_COLOR);

            Point vp1 = null;
            final Point wp1;

            if (shape == STRAIGHT_EDGE || getInternalPoints() == null || getInternalPoints().size() == 0) {
                vp1 = wp;
                wp1 = vp;
                gc.drawLine(vp.x, vp.y, wp.x, wp.y);
            } else if (shape == ROUNDED_EDGE && getInternalPoints().size() == 1) {
                vp1 = wp1 = trans.w2d(getInternalPoints().get(0));
                final int dist = (int) trans.w2d(ROUNDED_EDGE_INCREMENT, 0).getX() - (int) trans.w2d(0, 0).getX();
                final Point center = new Point(dist < wp.x - vp.x ? vp.x + dist : wp.x, wp.y);
                gc.draw(new QuadCurve2D.Double(vp.x, vp.y, vp1.x, vp1.y, center.x, center.y));
                gc.drawLine(center.x, center.y, wp.x, wp.y);
            } else if (shape == QUAD_EDGE && getInternalPoints().size() == 1) {
                Point aPt = trans.w2d(getInternalPoints().get(0));
                vp1 = wp1 = aPt;
                gc.draw(new QuadCurve2D.Double(vp.x, vp.y, aPt.x, aPt.y, wp.x, wp.y));
            } else if (shape == CUBIC_EDGE && getInternalPoints().size() == 2) {
                Point aPt = trans.w2d(getInternalPoints().get(0));
                Point bPt = trans.w2d(getInternalPoints().get(1));
                vp1 = aPt;
                wp1 = bPt;
                gc.draw(new CubicCurve2D.Double(vp.x, vp.y, aPt.x, aPt.y, bPt.x, bPt.y, wp.x, wp.y));
            } else if (shape == ARC_LINE_EDGE && getInternalPoints().size() == 2) {
                // node vp is start of arc, first internal point is center of circle, second is end of arc, second is joined to wp by straight line
                Iterator it = getInternalPoints().iterator();
                Point center = trans.w2d((Point2D) it.next());
                Point arcStart = (Point) vp.clone();
                Point arcEnd = trans.w2d((Point2D) it.next());
                Point lineStart = (Point) arcEnd.clone();
                // flip along h-axis:
                arcStart.y = center.y - (arcStart.y - center.y);
                arcEnd.y = center.y - (arcEnd.y - center.y);

                vp1 = wp1 = arcEnd;

                double dist = arcStart.distance(center);
                Point2D diffV = Geometry.diff(arcStart, center);
                double angleV = Geometry.computeAngle(diffV);
                Point2D diffEnd = Geometry.diff(arcEnd, center);
                double angleEnd = Geometry.computeAngle(diffEnd);

                double minAngle = angleV;
                double maxAngle = angleEnd;
                if (minAngle > maxAngle) {
                    double tmp = minAngle;
                    minAngle = maxAngle;
                    maxAngle = tmp;
                }
                if (maxAngle - minAngle > Math.PI) {
                    double tmp = minAngle + 2 * Math.PI;
                    minAngle = maxAngle;
                    maxAngle = tmp;
                }
                double extent = maxAngle - minAngle;

                Arc2D arc = new Arc2D.Double(center.getX() - dist, center.getY() - dist, 2 * dist, 2 * dist, Geometry.rad2deg(minAngle), Geometry.rad2deg(extent), Arc2D.OPEN);
                gc.draw(arc);
                gc.drawLine(lineStart.x, lineStart.y, wp.x, wp.y);
            } else  // some internal points are given
            {
                Point prev = vp;
                for (Point2D point2D : getInternalPoints()) {
                    final Point apt = trans.w2d(point2D);
                    if (vp1 == null)
                        vp1 = apt;

                    gc.drawLine(prev.x, prev.y, apt.x, apt.y);
                    prev = apt;
                }
                wp1 = prev;
                gc.drawLine(prev.x, prev.y, wp.x, wp.y);
            }

            if (direction == DIRECTED ||
                    direction == BIDIRECTED)
                drawArrowHead(gc, wp1, wp);
            else if (direction == RDIRECTED)
                drawArrowHead(gc, vp1, vp);
        }
    }


    /**
     * Does given point (x,y) hit the edge, and if so, after which point?
     * Returns -1, if edge not hit
     *
     * @param vp
     * @param wp
     * @param trans
     * @param x
     * @param y
     * @param i
     * @return the rank of the point preceding the point where the edge was hit
     */
    public int hitEdgeRank(Point vp, Point wp, Transform trans, int x, int y, int i) {
        if (shape == STRAIGHT_EDGE || getInternalPoints() == null || getInternalPoints().size() == 0) {
            if (Geometry.hitSegment(vp, wp, x, y, i))
                return 0;
        } else // some internal points are given
        {
            int rank = 0;
            Point prev = vp;
            for (Point2D point2D : getInternalPoints()) {
                Point apt = trans.w2d(point2D);
                if (Geometry.hitSegment(prev, apt, x, y, i))
                    return rank;
                prev = apt;
                rank++;
            }
            if (Geometry.hitSegment(prev, wp, x, y, i))
                return rank;
        }
        return -1;
    }

    /**
     * Does given point (x,y) hit the edge?
     *
     * @param vp    source node location in device coordinates
     * @param wp    target node location in device coordinates
     * @param trans
     * @param x     mouse x
     * @param y     mouse y
     * @param i     tolerance in pixels
     * @return true, if point (x,y) lies on edge
     */
    public boolean hitEdge(Point vp, Point wp, Transform trans, int x, int y, int i) {
        if (shape == STRAIGHT_EDGE || getInternalPoints() == null || getInternalPoints().size() == 0) {
            if (Geometry.hitSegment(vp, wp, x, y, i))
                return true;
        } else if (shape == ROUNDED_EDGE && getInternalPoints().size() == 1) {
            final Point vp1 = trans.w2d(getInternalPoints().get(0));
            final int dist = (int) trans.w2d(ROUNDED_EDGE_INCREMENT, 0).getX() - (int) trans.w2d(0, 0).getX();
            final Point center = new Point(dist < wp.x - vp.x ? vp.x + dist : wp.x, wp.y);
            return (new QuadCurve2D.Double(vp.x, vp.y, vp1.x, vp1.y, center.x, center.y)).contains(x, y) ||
                    (new Line2D.Double(center.x, center.y, wp.x, wp.y)).contains(x, y);
        } else if (shape == QUAD_EDGE && getInternalPoints().size() == 1) {
            Point aPt = trans.w2d(getInternalPoints().get(0));
            return (new QuadCurve2D.Double(vp.x, vp.y, aPt.x, aPt.y, wp.x, wp.y)).contains(x, y);
        } else if (shape == CUBIC_EDGE && getInternalPoints().size() == 2) {
            Point aPt = trans.w2d(getInternalPoints().get(0));
            Point bPt = trans.w2d(getInternalPoints().get(1));
            return new CubicCurve2D.Double(vp.x, vp.y, aPt.x, aPt.y, bPt.x, bPt.y, wp.x, wp.y).contains(x, y);
        } else if (shape == ARC_LINE_EDGE && getInternalPoints().size() == 2) {
            // node vp is start of arc, first internal point is center of circle, second is end of arc, second is joined to wp by straight line
            Iterator it = getInternalPoints().iterator();
            Point center = trans.w2d((Point2D) it.next());
            Point arcStart = (Point) vp.clone();
            Point arcEnd = trans.w2d((Point2D) it.next());
            Point lineStart = (Point) arcEnd.clone();
            // flip along h-axis:
            arcStart.y = center.y - (arcStart.y - center.y);
            arcEnd.y = center.y - (arcEnd.y - center.y);

            double dist = arcStart.distance(center);
            Point2D diffV = Geometry.diff(arcStart, center);
            double angleV = Geometry.computeAngle(diffV);
            Point2D diffEnd = Geometry.diff(arcEnd, center);
            double angleEnd = Geometry.computeAngle(diffEnd);

            double minAngle = angleV;
            double maxAngle = angleEnd;
            if (minAngle > maxAngle) {
                double tmp = minAngle;
                minAngle = maxAngle;
                maxAngle = tmp;
            }
            if (maxAngle - minAngle > Math.PI) {
                double tmp = minAngle + 2 * Math.PI;
                minAngle = maxAngle;
                maxAngle = tmp;
            }
            double extent = maxAngle - minAngle;

            Arc2D arc = new Arc2D.Double(center.getX() - dist, center.getY() - dist, 2 * dist, 2 * dist, Geometry.rad2deg(minAngle), Geometry.rad2deg(extent), Arc2D.OPEN);
            if (arc.contains(x, y))
                return true;

            if (Geometry.hitSegment(lineStart, wp, x, y, i))
                return true;
        } else // some internal points are given
        {
            Point prev = vp;
            for (Point2D point2D : getInternalPoints()) {
                Point apt = trans.w2d(point2D);
                if (Geometry.hitSegment(prev, apt, x, y, i))
                    return true;
                prev = apt;
            }
            if (Geometry.hitSegment(prev, wp, x, y, i))
                return true;
        }
        return false;
    }

    /**
     * moves the first internal point located at p1 to position p2
     *
     * @param trans
     * @param p1
     * @param p2
     */
    public void moveInternalPoint(Transform trans, Point p1, Point p2) {
        if (shape != STRAIGHT_EDGE && getInternalPoints() != null) {
            for (Point2D point2D : getInternalPoints()) {
                Point apt = trans.w2d(point2D);
                if (apt.distance(p1) <= 300) // todo: why do we check this?
                {
                    point2D.setLocation(trans.d2w(p2));
                    return;
                }
            }
        }
    }


    /**
     * returns the list of internal points
     *
     * @return list of internal points or null
     */
    public java.util.List<Point2D> getInternalPoints() {
        return internalPoints;
    }

    /**
     * sets the list of internal points
     *
     * @param internalPoints a list of internal points from source to target
     */
    public void setInternalPoints(List<Point2D> internalPoints) {
        this.internalPoints = internalPoints;
    }


    /**
     * Draw an arrow head.
     *
     * @param gc Graphics
     * @param vp Point
     * @param wp Point
     */
    // Used to be private. Changed it to public in order to access from Function.java
    public static void drawArrowHead(Graphics gc, Point vp, Point wp) {
        final int arrowLength = 5;
        final double arrowAngle = 2.2;
        double alpha = Geometry.computeAngle(new Point(wp.x - vp.x, wp.y - vp.y));
        Point a = new Point(arrowLength, 0);
        a = Geometry.rotate(a, alpha + arrowAngle);
        a.translate(wp.x, wp.y);
        Point b = new Point(arrowLength, 0);
        b = Geometry.rotate(b, alpha - arrowAngle);
        b.translate(wp.x, wp.y);
        gc.drawLine(a.x, a.y, wp.x, wp.y);
        gc.drawLine(wp.x, wp.y, b.x, b.y);
    }

    /**
     * Draw the edge label at the position given in device coordinates.
     *
     * @param gc Graphics
     */
    public void drawLabel(Graphics2D gc, Transform trans) {
        if (this.isLabelVisible() && labelColor != null && label != null && enabled) {
            if (labelBackgroundColor != null) {
                gc.setColor(labelBackgroundColor);
                gc.fill(getLabelShape(trans));
            }

            if (enabled)
                gc.setColor(labelColor);
            else
                gc.setColor(DISABLED_COLOR);

            if (getFont() != null)
                gc.setFont(getFont());

            Point aPt = getLabelPosition(trans);

            gc.drawString(label, aPt.x, aPt.y);

            setLabelSize(gc);
        }
    }


    /**
     * Draw the edge label at the position given in device coordinates.
     *
     * @param gc Graphics
     */
    public void drawLabel(Graphics2D gc, Transform trans, boolean hilited) {
        if (this.isLabelVisible() && labelColor != null && label != null) {

            if (getFont() != null)
                gc.setFont(getFont());

            setLabelSize(gc);

            if (hilited) {
                hiliteLabel(gc, trans);
            }

            Point2D apt = getLabelPosition(trans);
            if (enabled && labelBackgroundColor != null) {
                gc.setColor(labelBackgroundColor);
                gc.fill(getLabelShape(trans));
            }
            if (enabled)
                gc.setColor(labelColor);
            else
                gc.setColor(DISABLED_COLOR);

            if (labelAngle == 0) {
                gc.drawString(label, (int) apt.getX(), (int) apt.getY());
            } else if (gc instanceof PDFGraphics) {
                if (labelAngle >= 0.5 * Math.PI && labelAngle <= 1.5 * Math.PI) {
                    double d = getLabelSize().getWidth();
                    apt = Geometry.translateByAngle(apt, labelAngle, d);
                    ((PDFGraphics) gc).drawString(label, (float) apt.getX(), (float) apt.getY(), (float) (labelAngle - Math.PI));
                } else {
                    ((PDFGraphics) gc).drawString(label, (float) apt.getX(), (float) apt.getY(), labelAngle);
                }
            } else {
                // save current transform:
                AffineTransform saveTransform = gc.getTransform();

                // rotate label to desired angle
                if (labelAngle >= 0.5 * Math.PI && labelAngle <= 1.5 * Math.PI) {
                    double d = getLabelSize().getWidth();
                    apt = Geometry.translateByAngle(apt, labelAngle, d);
                    gc.rotate(Geometry.moduloTwoPI(labelAngle - Math.PI), apt.getX(), apt.getY());
                } else
                    gc.rotate(labelAngle, apt.getX(), apt.getY());

                gc.drawString(label, (int) apt.getX(), (int) apt.getY());
                gc.setTransform(saveTransform);
            }
        }
    }

    /**
     * hilites the label
     *
     * @param gc
     * @param trans
     */
    public void hiliteLabel(Graphics2D gc, Transform trans) {
        if (this.isLabelVisible() && labelColor != null && label != null) {
            if (enabled)
                gc.setColor(labelColor);
            else
                gc.setColor(DISABLED_COLOR);

            if (getFont() != null)
                gc.setFont(getFont());

            setLabelSize(gc);

            gc.setColor(ProgramProperties.SELECTION_COLOR);
            Shape shape = getLabelShape(trans);
            gc.fill(shape);
            gc.setColor(ProgramProperties.SELECTION_COLOR_DARKER);
            final Stroke oldStroke = gc.getStroke();
            gc.setStroke(NORMAL_STROKE);
            gc.draw(shape);
            gc.setStroke(oldStroke);
        }
    }

    /**
     * Sets the label reference point from the device coordinates of the two edge end nodes
     *
     * @param vp Point
     * @param wp Point
     */
    public void setLabelReferencePosition(Point vp, Point wp, Transform trans) {
        if (shape != STRAIGHT_EDGE && getInternalPoints() != null && getInternalPoints().size() != 0) {
            Point bestPt = null;
            double bestDist = -1;
            for (Point2D point2D : getInternalPoints()) {
                Point apt = trans.w2d(point2D);
                double dist = apt.distance(vp) + apt.distance(wp);
                if (dist > bestDist) {
                    bestDist = dist;
                    bestPt = apt;
                }
            }
            labelReferencePoint = bestPt;
        } else {
            int x = (int) (0.5 * (vp.x + wp.x));
            int y = (int) (0.5 * (vp.y + wp.y));
            labelReferencePoint = new Point(x, y);
        }
    }

    /**
     * Sets the label reference point from the world coordinates of the two edge end nodes
     *
     * @param vp Point2D
     * @param wp Point2D
     */
    public void setLabelReferenceLocation(Point2D vp, Point2D wp, Transform trans) {
        setLabelReferencePosition(trans.w2d(vp), trans.w2d(wp), trans);
    }

    /**
     * Gets the label position, computed from the given reference point, in device  coordinates
     *
     * @param trans Transform
     * @return location
     */
    public Point getLabelPosition(Transform trans) {
        if (labelReferencePoint == null || labelSize == null || label == null)
            return null;

        Point apt = new Point(labelReferencePoint);
        Dimension size = getLabelSize();
        switch (labelLayout) {
            case RADIAL:
            case USER:
                apt.x += dxLabel;
                apt.y += dyLabel;
                break;
            case CENTRAL:
                apt.x -= size.width / 2;
                apt.y += size.height / 2;
                break;
            case NORTH:
                apt.x -= size.width / 2;
                apt.y -= 3;
                break;
            case NORTHEAST:
                apt.x += 3;
                apt.y -= 3;
                break;
            case EAST:
                apt.x += 3;
                apt.y += size.height / 2;
                break;
            case SOUTHEAST:
                apt.x -= 3;
                apt.y += 3 + size.height;
                break;
            case SOUTH:
                apt.x -= size.width / 2;
                apt.y += 3 + size.height;
                break;
            case SOUTHWEST:
                apt.x -= 3 + size.width;
                apt.y += 3 + size.height;
                break;
            case WEST:
                apt.x -= 3 + size.width;
                apt.y += size.height / 2;
                break;
            case NORTHWEST:
                apt.x -= 3 + size.width;
                apt.y -= 3;
                break;
        }
        return apt;
    }

    /**
     * Sets the location of the edge label in world coordinates
     *
     * @param x     double
     * @param y     double
     * @param trans Transform
     */
    public void setLabelPosition(int x, int y, Transform trans) {
        if (labelLayout != USER && labelLayout != LAYOUT)
            setLabelLayout(USER);
        Point apt = trans.w2d(x, y);
        if (labelReferencePoint == null)
            throw new RuntimeException("setLabelPosition(): labelReferencePoint not set");
        dxLabel = apt.x - labelReferencePoint.x;
        dyLabel = apt.y - labelReferencePoint.y;
    }

    /**
     * gets the relative position of the label in device coordinates
     *
     * @return relative position
     */
    public Point getLabelPositionRelative(Transform trans) {
        if (labelLayout == USER)
            return new Point(dxLabel, dyLabel);
        else {
            if (labelReferencePoint == null)
                throw new RuntimeException("getLabelPositionRelative(): labelReferencePoint not set");
            Point loc = getLabelPosition(trans);
            return new Point(loc.x - labelReferencePoint.x, loc.y - labelReferencePoint.y);
        }
    }

    /**
     * gets the label reference point in device coordinates
     *
     * @return label reference
     */
    public Point getLabelReferencePoint() {
        return labelReferencePoint;
    }

    /**
     * sets the edge label reference point in world coordinates
     *
     * @param labelReferencePoint
     */
    public void setLabelReferencePoint(Point labelReferencePoint) {
        this.labelReferencePoint = labelReferencePoint;
    }

    /**
     * sets the edge label reference point in world coordinates
     *
     * @param x
     * @param y
     */
    public void setLabelReferencePoint(int x, int y) {
        this.labelReferencePoint = new Point(x, y);
    }

    /**
     * gets the rectangle of the label
     *
     * @param trans
     * @return bounding box of label
     */
    public Rectangle getLabelRect(Transform trans) {
        Point apt = getLabelPosition(trans);

        if (apt != null && labelSize != null)
            return new Rectangle(apt.x, apt.y - labelSize.height, labelSize.width, labelSize.height);
        else
            return null;
    }

    /**
     * gets the bounding box of the label in device coordinates as a shape (rectangle or polygon)
     *
     * @param trans
     * @return bounding box
     */
    public Shape getLabelShape(Transform trans) {
        if (labelSize != null) {
            Point2D apt = getLabelPosition(trans);
            if (apt != null) {
                if (labelAngle == 0) {
                    return new Rectangle((int) apt.getX(), (int) apt.getY() - labelSize.height + 1, labelSize.width, labelSize.height);
                } else {
                    AffineTransform localTransform = new AffineTransform();
                    // rotate label to desired angle
                    if (labelAngle >= 0.5 * Math.PI && labelAngle <= 1.5 * Math.PI) {
                        double d = getLabelSize().getWidth();
                        apt = Geometry.translateByAngle(apt, labelAngle, d);
                        localTransform.rotate(Geometry.moduloTwoPI(labelAngle - Math.PI), apt.getX(), apt.getY());
                    } else
                        localTransform.rotate(labelAngle, apt.getX(), apt.getY());
                    double[] pts = new double[]{apt.getX(), apt.getY(),
                            apt.getX() + labelSize.width, apt.getY(),
                            apt.getX() + labelSize.width, apt.getY() - labelSize.height,
                            apt.getX(), apt.getY() - labelSize.height};
                    localTransform.transform(pts, 0, pts, 0, 4);
                    return new Polygon(new int[]{(int) pts[0], (int) pts[2], (int) pts[4], (int) pts[6]}, new int[]{(int) pts[1], (int) pts[3],
                            (int) pts[5], (int) pts[7]}, 4);
                }
            }
        }
        return null;
    }


    /**
     * writes this edge view. Include internal points
     *
     * @param w
     * @param previousEV if not null, only write those fields that differ from the values in previousNV
     */
    public void write(Writer w, EdgeView previousEV) throws IOException {
        w.write(toString(previousEV, true, true));
        w.write("\n");
    }

    /**
     * gets a string representation of this node view, including internal points
     *
     * @return string representation
     */
    public String toString() {
        return toString(null, true, true);
    }

    /**
     * gets a string representation of this node view
     *
     * @param withInternalPoints show internal points as well?
     * @return string representation
     */
    public String toString(boolean withInternalPoints) {
        return toString(null, withInternalPoints, true);
    }

    /**
     * gets a string representation of this edge view
     *
     * @param previousEV if not null, only write those fields that differ from the values in previousNV
     * @return string representation
     */
    public String toString(EdgeView previousEV, boolean withInternalPoints, boolean withLabel) {

        StringBuilder buf = new StringBuilder();

        if (fgColor != null && (previousEV == null || previousEV.fgColor == null || !fgColor.equals(previousEV.fgColor)))
            buf.append(" fg=").append(Basic.toString3Int(fgColor));

        if (previousEV == null || linewidth != previousEV.linewidth)
            buf.append(" w=").append(linewidth);
        if (previousEV == null || shape != previousEV.shape)
            buf.append(" sh=").append(shape);
        if (labelReferencePoint != null && (previousEV == null || previousEV.labelReferencePoint == null || !labelReferencePoint.equals(previousEV.labelReferencePoint))) {
            buf.append(" rx=").append((float) labelReferencePoint.getX());
            buf.append(" ry=").append((float) labelReferencePoint.getY());
        }
        if (withInternalPoints && internalPoints != null && internalPoints.size() > 0) {
            buf.append(" ip= <");
            for (Point2D apt : internalPoints) {
                buf.append(" ").append((float) apt.getX()).append(" ").append((float) apt.getY());
            }
            buf.append(">");
        }
        buf.append(" dr=").append(direction);

        if (labelColor != null && (previousEV == null || previousEV.labelColor == null || !labelColor.equals(previousEV.labelColor)))
            buf.append(" lc=").append(Basic.toString3Int(labelColor));

        if (previousEV == null || ((previousEV.labelBackgroundColor == null) != (labelBackgroundColor == null))
                || (previousEV.labelBackgroundColor != null && labelBackgroundColor != null && !previousEV.labelBackgroundColor.equals(labelBackgroundColor)))
            buf.append(" lk=").append(Basic.toString3Int(labelBackgroundColor));

        if (font != null && (previousEV == null || previousEV.font == null || !font.equals(previousEV.font)))
            buf.append(" ft='").append(Basic.getCode(font)).append("'");
        if (previousEV == null || dxLabel != previousEV.dxLabel)
            buf.append(" lx=").append(dxLabel);
        if (previousEV == null || dyLabel != previousEV.dyLabel)
            buf.append(" ly=").append(dyLabel);
        if (previousEV == null || labelLayout != previousEV.labelLayout)
            buf.append(" ll=").append(labelLayout);
        if (previousEV == null || labelVisible != previousEV.labelVisible)
            buf.append(" lv=").append(labelVisible ? 1 : 0);
        if (labelAngle != 0)
            buf.append(" la=").append(labelAngle);

        if (withLabel && label != null && label.length() > 0)
            buf.append(" lb='").append(label).append("'");

        buf.append(";");
        return buf.toString();
    }

    /**
     * read edge format from a string
     *
     * @param src
     * @throws IOException
     */
    public void read(String src) throws IOException {
        NexusStreamParser np = new NexusStreamParser(new StringReader(src));
        java.util.List<String> tokens = np.getTokensRespectCase(null, ";");
        read(np, tokens, this);
    }

    /**
     * read edge format from a string
     *
     * @param src
     * @throws IOException
     */
    public void read(String src, EdgeView prevEV) throws IOException {
        NexusStreamParser np = new NexusStreamParser(new StringReader(src));
        java.util.List<String> tokens = np.getTokensRespectCase(null, ";");
        read(np, tokens, prevEV != null ? prevEV : this);
    }

    /**
     * reads a edge view from a line
     *
     * @param tokens
     * @param prevEV this must be !=null, for example can be set to graphView.defaultEdgeView
     */
    public void read(NexusStreamParser np, java.util.List<String> tokens, EdgeView prevEV) throws IOException {
        if (prevEV == null)
            throw new IOException("prevEV=null");

        fgColor = np.findIgnoreCase(tokens, "fg=", prevEV.fgColor);
        linewidth = (byte) np.findIgnoreCase(tokens, "w=", prevEV.linewidth);
        shape = (byte) np.findIgnoreCase(tokens, "sh=", prevEV.shape);

        int x = (int) np.findIgnoreCase(tokens, "rx=",
                prevEV.labelReferencePoint != null ? (float) prevEV.labelReferencePoint.getX() : 0);
        int y = (int) np.findIgnoreCase(tokens, "ry=",
                prevEV.labelReferencePoint != null ? (float) prevEV.labelReferencePoint.getY() : 0);
        setLabelReferencePoint(new Point(x, y));

        String internalPointsStr = np.findIgnoreCase(tokens, "ip=", "<", ">", "");
        if (internalPointsStr != null && internalPointsStr.length() > 0) {
            try {
                StringTokenizer st = new StringTokenizer(internalPointsStr);

                List<Point2D> list = new LinkedList<>();
                while (st.hasMoreTokens()) {
                    double ix = Double.parseDouble(st.nextToken());
                    double iy = Double.parseDouble(st.nextToken());
                    list.add(new Point2D.Double(ix, iy));

                }
                setInternalPoints(list);
            } catch (Exception ex) {
                throw new IOException("line " + np.lineno() + ": error parsing internal points: " + internalPointsStr
                        + ": " + ex);
            }
        }

        direction = (byte) np.findIgnoreCase(tokens, "dr=", prevEV.direction);

        labelColor = np.findIgnoreCase(tokens, "lc=", prevEV.labelColor);
        labelBackgroundColor = np.findIgnoreCase(tokens, "lk=", prevEV.labelBackgroundColor);

        String fontName = np.findIgnoreCase(tokens, "ft=", null, "");
        if (fontName != null && fontName.length() > 0)
            font = Font.decode(fontName);
        else
            font = GraphView.defaultEdgeView.getFont(); // will use default font
        dxLabel = (int) np.findIgnoreCase(tokens, "lx=", prevEV.dxLabel);
        dyLabel = (int) np.findIgnoreCase(tokens, "ly=", prevEV.dyLabel);
        setLabelAngle(np.findIgnoreCase(tokens, "la=", 0));
        labelLayout = (byte) np.findIgnoreCase(tokens, "ll=", prevEV.labelLayout);
        labelVisible = (np.findIgnoreCase(tokens, "lv=", prevEV.labelVisible ? 1 : 0) != 0);
        label = (np.findIgnoreCase(tokens, "lb=", null, ""));
        if (label != null && label.length() == 0)
            label = null;
        setLabel(label);
        if (label == null)
            labelReferencePoint = null; // don't need this, will remake it later, if necessary
        if (tokens.size() > 0) {
            if (tokens.size() == 2 && tokens.get(0).equals("0") && tokens.get(1).equals("0") && linewidth == (byte) 255)  // this is the w=255 0 0 bug
            {
                linewidth = 1;
            } else
                throw new IOException("Unexpected tokens: " + tokens);
        }
    }
}

// EOF
