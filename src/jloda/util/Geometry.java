/**
 * Geometry.java 
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
package jloda.util;

/**
 * Some useful geometry stuff.
 * @author Daniel Huson
 * 7.01
 */

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Geometry {
    /**
     * Translate a point in the direction specified by an angle.
     *
     * @param apt   Point2D
     * @param alpha double
     * @param dist  double
     * @return Point2D
     */
    public static Point2D translateByAngle(Point2D apt, double alpha, double dist) {
        double dx = dist * Math.cos(alpha);
        double dy = dist * Math.sin(alpha);
        if (Math.abs(dx) < 0.000000001)
            dx = 0;
        if (Math.abs(dy) < 0.000000001)
            dy = 0;
        return new Point2D.Double(apt.getX() + dx, apt.getY() + dy);
    }

    /**
     * Does line segment between a and b contain point (x,y)?
     *
     * @param a       Point
     * @param b       Point
     * @param x       double
     * @param y       double
     * @param maxDist double
     * @return true if line segment between a and b contain point (x,y)
     */
    public static boolean hitSegment(Point a, Point b, double x, double y,
                                     double maxDist) {
        if (Math.min(a.x, b.x) <= x + 1 && x <= Math.max(a.x, b.x + 1)
                && Math.min(a.y, b.y) <= y + 1 && y <= Math.max(a.y, b.y) + 1) {
            Line2D.Float line = new Line2D.Float(a, b);
            if (line.ptLineDist(x, y) <= maxDist)
                return true;
        }
        return false;
    }

    /**
     * Computes the angle of a two-dimensional vector.
     *
     * @param p Point2D
     * @return angle double
     */
    public static double computeAngle(Point2D p) {
        if (p.getX() != 0) {
            double x = Math.abs(p.getX());
            double y = Math.abs(p.getY());
            double a = Math.atan(y / x);

            if (p.getX() > 0) {
                if (p.getY() > 0)
                    return a;
                else
                    return 2.0 * Math.PI - a;
            } else // p.getX()<0
            {
                if (p.getY() > 0)
                    return Math.PI - a;
                else
                    return Math.PI + a;
            }
        } else if (p.getY() > 0)
            return 0.5 * Math.PI;
        else // p.y<0
            return -0.5 * Math.PI;
    }

    /**
     * Computes the angle of a two-dimensional vector.
     *
     * @param p Point
     * @return angle double
     */
    public static double computeAngle(Point p) {
        if (p.getX() != 0) {
            double x = Math.abs(p.getX());
            double y = Math.abs(p.getY());
            double a = Math.atan(y / x);

            if (p.getX() > 0) {
                if (p.getY() > 0)
                    return a;
                else
                    return 2.0 * Math.PI - a;
            } else // p.getX()<0
            {
                if (p.getY() > 0)
                    return Math.PI - a;
                else
                    return Math.PI + a;
            }
        } else if (p.getY() > 0)
            return 0.5 * Math.PI;
        else // p.y<0
            return -0.5 * Math.PI;
    }

    /**
     * computes the angle difference between a and b as viewed from center
     *
     * @param center
     * @param a
     * @param b
     * @return angle
     */
    public static double computeObservedAngle(Point2D center, Point2D a, Point2D b) {
        Point2D da = Geometry.diff(a, center);
        Point2D db = Geometry.diff(b, center);
        double angle = Math.abs(Geometry.computeAngle(da) - Geometry.computeAngle(db));
        if (angle > Math.PI)
            angle = 2 * Math.PI - angle;

        double det = da.getX() * db.getY() - da.getY() * db.getX();

        if (det >= 0)
            return angle;
        else
            return -angle;
    }

    /**
     * Rotates a two-dimensional vector by the angle alpha.
     *
     * @param p     point
     * @param alpha angle in radian
     * @return q point rotated around origin
     */
    public static Point2D rotate(Point2D p, double alpha) {
        double sina = Math.sin(alpha);
        double cosa = Math.cos(alpha);
        Point2D q = new Point2D.Double();
        q.setLocation(p.getX() * cosa - p.getY() * sina, p.getX() * sina + p.getY() * cosa);
        return q;
    }

    /**
     * Rotates a two-dimensional vector by the angle alpha.
     *
     * @param p     Point
     * @param alpha double
     * @return q Point
     */
    public static Point rotate(Point p, double alpha) {
        double sina = Math.sin(alpha);
        double cosa = Math.cos(alpha);
        Point q = new Point();
        q.setLocation(p.getX() * cosa - p.getY() * sina, p.getX() * sina + p.getY() * cosa);
        return q;
    }

    /**
     * Rotates a point by angle alpha around a second point
     *
     * @param pt     the point to be rotated
     * @param alpha  the angle
     * @param anchor the anchor point
     * @return the rotated point
     */
    public static Point2D rotateAbout(Point2D pt, double alpha, Point2D anchor) {
        return rotateAbout(pt, alpha, anchor, new Point2D.Double());
    }

    /**
     * Rotates a point by angle alpha around a second point
     *
     * @param src    the point to be rotated
     * @param alpha  the angle
     * @param anchor the anchor point
     * @param tar    the target point
     * @return the rotated point
     */
    public static Point2D rotateAbout(Point2D src, double alpha, Point2D anchor, Point2D tar) {
        tar.setLocation(src.getX() - anchor.getX(), src.getY() - anchor.getY());
        tar.setLocation(rotate(tar, alpha));
        tar.setLocation(tar.getX() + anchor.getX(), tar.getY() + anchor.getY());
        return tar;

    }

    /**
     * Rotates a point by angle alpha around a second point
     *
     * @param x      the point to be rotated
     * @param y      the anchor poin
     * @param alpha  the angle
     * @param anchor the anchor point
     * @param tar    the target point
     * @return the rotated point
     */
    public static Point2D rotateAbout(double x, double y, double alpha, Point2D anchor, Point2D tar) {
        tar.setLocation(x - anchor.getX(), y - anchor.getY());
        tar.setLocation(rotate(tar, alpha));
        tar.setLocation(tar.getX() + anchor.getX(), tar.getY() + anchor.getY());
        return tar;
    }

    static final double PI2 = 2 * Math.PI;

    /**
     * clamp to range 0..2PI
     *
     * @param x
     * @return modulo 2PI
     */
    static public double moduloTwoPI(double x) {
        while (x < 0)
            x += PI2;
        while (x > PI2)
            x -= PI2;
        return x;
    }

    /**
     * gets the difference of two points
     *
     * @param tar
     * @param src
     * @return difference
     */
    public static Point2D diff(Point2D tar, Point2D src) {
        Point2D result = (Point2D) tar.clone();
        result.setLocation(result.getX() - src.getX(), result.getY() - src.getY());
        return result;
    }

    /**
     * gets the intersection between two secant segments [O,T] qnd [P,S]
     *
     * @param PointO
     * @param PointP
     * @param PointS
     * @param PointT
     */
    public static Point2D intersect(Point2D PointO, Point2D PointP, Point2D PointS, Point2D PointT) {
        double xo = PointO.getX();
        double yo = PointO.getY();
        double xt = PointT.getX();
        double yt = PointT.getY();
        double xp = PointP.getX();
        double yp = PointP.getY();
        double xs = PointS.getX();
        double ys = PointS.getY();

        double DA = yt - yo;
        double DB = xo - xt;
        double DC = yo * DB - xo * DA;
        double DD = ys - yp;
        double DE = xp - xs;
        double DF = yp * DE - xp * DD;

        return new Point2D.Double((DB * DF - DC * DE) / (DA * DE - DB * DD), (DC * DD - DA * DF) / (DA * DE - DB * DD));
    }


    /**
     * returns the scalar product O.P
     *
     * @param PointO
     * @param PointP
     */
    public static double scalar(Point2D PointO, Point2D PointP) {
        return PointP.getX() * PointO.getX() + PointP.getY() * PointO.getY();
    }


    /**
     * returns the average of angles A and B
     *
     * @param AngleA
     * @param AngleB
     */
    public static double midAngle(double AngleA, double AngleB) {
        if (moduloTwoPI(AngleA - AngleB) < Math.PI) {
            return moduloTwoPI(AngleB + (moduloTwoPI(AngleA - AngleB)) / 2);
        } else {
            return moduloTwoPI(AngleB - (moduloTwoPI(AngleB - AngleA)) / 2);
        }

    }

    /**
     * returns the difference of angles A and B
     *
     * @param AngleA
     * @param AngleB
     */
    public static double diffAngle(double AngleA, double AngleB) {
        if (moduloTwoPI(AngleA - AngleB) > Math.PI) {
            return 2 * Math.PI - moduloTwoPI(AngleA - AngleB);
        } else {
            return moduloTwoPI(AngleA - AngleB);
        }
    }


    /**
     * returns the difference of angles A and B
     *
     * @param AngleA
     * @param AngleB
     */
    public static double signedDiffAngle(double AngleA, double AngleB) {
        if (moduloTwoPI(AngleA - AngleB) > Math.PI) {
            return -(2 * Math.PI - moduloTwoPI(AngleA - AngleB));
        } else {
            return moduloTwoPI(AngleA - AngleB);
        }

    }


    /**
     * returns the difference of angles A and B
     *
     * @param A
     * @param B
     */
    public static double squaredDistance(Point2D A, Point2D B) {

        return (B.getX() - A.getX()) * (B.getX() - A.getX()) + (B.getY() - A.getY()) * (B.getY() - A.getY());
    }

    /**
     * computes the angle difference between a and b as viewed from center
     *
     * @param center
     * @param a
     * @param b
     * @return angle
     */
    public static double basicComputeAngle(Point2D center, Point2D a, Point2D b) {
        Point2D da = Geometry.diff(a, center);
        Point2D db = Geometry.diff(b, center);
        return Geometry.moduloTwoPI(Geometry.computeAngle(db) - Geometry.computeAngle(da));
    }

    final static double factor1 = Math.PI / 180.0;

    /**
     * convert degree to radian
     *
     * @param deg angle in degrees
     * @return angle in radian
     */
    public static double deg2rad(double deg) {
        return deg * factor1;
    }

    final static double factor2 = 180.0 / Math.PI;

    /**
     * convert radian   to degree
     *
     * @param rad angle in radian
     * @return angle in degrees
     */
    public static double rad2deg(double rad) {
        return rad * factor2;
    }

    /**
     * computes the squared distance between points (x1,y1) and (x2,y2)
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return squared distance
     */
    public static double squaredDistance(double x1, double y1, double x2, double y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    /**
     * gets the length of a vector
     *
     * @param vector
     * @return length
     */
    public static double length(Point2D vector) {
        return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
    }
}

// EOF
