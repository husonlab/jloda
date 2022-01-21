/*
 * DPointMutable.java Copyright (C) 2022 Daniel H. Huson
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

/**
 * Simple point class
 * Daniel Huson, 3.2021
 */
public class DPointMutable extends DPoint {
    public DPointMutable() {
    }

    public DPointMutable(double... xy) {
        setPosition(xy);
    }

    public void setPosition(double... xy) {
        if (xy.length >= 1)
            this.xy[0] = xy[0];
        if (xy.length >= 2)
            this.xy[1] = xy[1];
    }

    public void setX(double x) {
        xy[0] = x;
    }

    public void setY(double y) {
        xy[1] = y;
    }

    public void setPosition(DPoint that) {
        xy[0] = that.getX();
        xy[1] = that.getY();
    }

    public DPoint asDPoint() {
        return new DPoint(xy);
    }
}
