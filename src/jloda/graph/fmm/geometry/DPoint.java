/*
 * DPoint.java Copyright (C) 2022 Daniel H. Huson
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
package jloda.graph.fmm.geometry;

import jloda.graph.fmm.FastMultiLayerMethodLayout;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple point class
 * Daniel Huson, 3.2021
 */
public class DPoint implements FastMultiLayerMethodLayout.Point {
    public static final DPoint ORIGIN = new DPoint(0, 0);

    protected final double[] xy = {0, 0};

    public DPoint(double... xy) {
        if (xy.length >= 1)
            this.xy[0] = xy[0];
        if (xy.length >= 2)
            this.xy[1] = xy[1];
    }

    /**
     * compute the angle between three points
     *
     * @return angle
     */
    public static double angle(DPoint p, DPoint q, DPoint r) {
        var dx1 = q.getX() - p.getX();
        var dy1 = q.getY() - p.getY();
        var dx2 = r.getX() - p.getX();
        var dy2 = r.getY() - p.getY();

        var norm = Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2));
        var cosfi = (dx1 * dx2 + dy1 * dy2) / norm;

        double fi;
        if (cosfi >= 1.0)
            fi = 0;
        else if (cosfi <= -1.0)
            fi = Math.PI;
        else {
            fi = Math.acos(cosfi);
            if (dx1 * dy2 < dy1 * dx2) fi = -fi;
            if (fi < 0) fi += 2 * Math.PI;
        }
        return fi;
    }

    public static DPoint computeBarycenter(Collection<DPoint> list) {
        var x = 0.0;
        var y = 0.0;
        if (list.size() > 0) {
            for (var p : list) {
                x += p.getX();
                y += p.getY();
            }
            x /= list.size();
            y /= list.size();
        }
        return new DPoint(x, y);
    }

    public double getX() {
        return xy[0];
    }

    public double getY() {
        return xy[1];
    }

    public double norm() {
        return Math.sqrt(xy[0] * xy[0] + xy[1] * xy[1]);
    }

    public double distance(DPoint t) {
        return Math.sqrt((xy[0] - t.xy[0]) * (xy[0] - t.xy[0]) + (xy[1] - t.xy[1]) * (xy[1] - t.xy[1]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DPoint that = (DPoint) o;
        return xy[0] == that.xy[0] && xy[1] == that.xy[1];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(xy);
    }

    public DPoint subtract(DPoint a) {
        return new DPoint(xy[0] - a.xy[0], xy[1] - a.xy[1]);
    }

    public DPoint add(DPoint a) {
        return new DPoint(xy[0] + a.xy[0], xy[1] + a.xy[1]);
    }

    public DPoint scaleBy(double a) {
        return new DPoint(xy[0] * a, xy[1] * a);
    }

    @Override
    public String toString() {
        return String.format("DPoint{%.8f,%.8f}", xy[0], xy[1]);
    }
}
