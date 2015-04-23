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
