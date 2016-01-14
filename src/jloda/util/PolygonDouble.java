/**
 * PolygonDouble.java 
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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * polygon with double coordinates
 * Daniel Huson, 1.2007
 */
public class PolygonDouble {
    public int npoints;
    public double[] xpoints;
    public double[] ypoints;

    /**
     * construct an empty polygon
     */
    public PolygonDouble() {
        npoints = 0;
        xpoints = new double[0];
        ypoints = new double[0];
    }

    /**
     * construct a polygon of size npoints
     *
     * @param npoints number of points
     */
    public PolygonDouble(int npoints) {
        this.npoints = npoints;
        xpoints = new double[npoints];
        ypoints = new double[npoints];
    }

    /**
     * construct a polygon and copy the given points
     *
     * @param npoints
     * @param xpoints
     * @param ypoints
     */
    public PolygonDouble(int npoints, double[] xpoints, double[] ypoints) {
        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        for (int i = 0; i < npoints; i++) {
            this.xpoints[i] = xpoints[i];
            this.ypoints[i] = ypoints[i];
        }
    }

    /**
     * construct a polygon from a rectangle
     *
     * @param box
     */
    public PolygonDouble(Rectangle2D box) {
        this.npoints = 4;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        this.xpoints[0] = box.getX();
        this.ypoints[0] = box.getY();
        this.xpoints[1] = box.getX();
        this.ypoints[1] = box.getY() + box.getHeight();
        this.xpoints[2] = box.getX() + box.getWidth();
        this.ypoints[2] = box.getY() + box.getHeight();
        this.xpoints[3] = box.getX() + box.getWidth();
        this.ypoints[3] = box.getY();
    }

    /**
     * construct a polygon and copy the given points
     *
     * @param npoints
     * @param points
     */
    public PolygonDouble(int npoints, Point2D[] points) {
        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        for (int i = 0; i < npoints; i++) {
            this.xpoints[i] = points[i].getX();
            this.ypoints[i] = points[i].getY();
        }
    }

    /**
     * construct a polygon from a list of points
     *
     * @param points
     */
    public PolygonDouble(List points) {
        this(points.size());
        int i = 0;
        for (Object point : points) {
            Point2D aPt = (Point2D) point;
            xpoints[i] = aPt.getX();
            ypoints[i] = aPt.getY();
            i++;
        }
    }

    /**
     * construct a polygon from a list of points
     *
     * @param a
     * @param b
     * @param points
     * @param c
     */
    public PolygonDouble(Point2D a, Point2D b, List points, Point2D c) {
        this(points.size() + 3);
        int i = 0;
        xpoints[i] = a.getX();
        ypoints[i++] = a.getY();
        xpoints[i] = b.getX();
        ypoints[i++] = b.getY();
        for (Object point : points) {
            Point2D aPt = (Point2D) point;
            xpoints[i] = aPt.getX();
            ypoints[i] = aPt.getY();
            i++;
        }
        xpoints[i] = c.getX();
        ypoints[i++] = c.getY();
    }

    /**
     * set the polygon from two lists of Double
     *
     * @param npoints
     * @param xpoints
     * @param ypoints
     */
    public void set(int npoints, ArrayList xpoints, ArrayList ypoints) {
        this.npoints = npoints;
        this.xpoints = new double[npoints];
        this.ypoints = new double[npoints];
        int i = 0;
        for (Object xpoint : xpoints) this.xpoints[i++] = (Double) xpoint;
        i = 0;
        for (Object ypoint : ypoints) this.ypoints[i++] = (Double) ypoint;
    }
}
