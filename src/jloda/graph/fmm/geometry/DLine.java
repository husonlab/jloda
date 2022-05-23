/*
 * DLine.java Copyright (C) 2022 Daniel H. Huson
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
 * Simple line class
 * <p>
 * Original C++ author: Joachim Kupke, original license: GPL
 * Reimplemented in Java by Daniel Huson, 3.2021
 */
public class DLine {
    private final DPointMutable start = new DPointMutable();
    private final DPointMutable end = new DPointMutable();

    public DLine() {
    }

    public DLine(DPoint start, DPoint end) {
        this.start.setPosition(start);
        this.end.setPosition(end);
    }

    public double dx() {
        return end.getX() - start.getX();
    }

    public double dy() {
        return end.getY() - start.getY();
    }

    public double slope() {
        return dx() == 0 ? Double.MAX_VALUE : dy() / dx();
    }

    public boolean isVertical() {
        return Math.abs(dx()) < 0.00000001;
    }

    public double yAbs() {
        return (dx() == 0) ? Double.MAX_VALUE : start.getY() - (slope() * start.getX());
    }

    public boolean intersection(DLine line, DPointMutable intersection) {
        return intersection(line, intersection, true);
    }

    public boolean intersection(DLine line, DPointMutable intersection, boolean includingEndpoints) {
        if (slope() == line.slope()) return false; // lines are parallel, so there is no unique intersection point

        // check whether intersects on endpoints

        if (start.getX() == line.start.getX() && start.getY() == line.start.getY() || start.getX() == line.end.getX() && start.getY() == line.end.getY()) {
            if (includingEndpoints) {
                intersection.setPosition(start);
                return true;
            } else
                return false;
        }

        if (end.getX() == line.start.getX() && end.getY() == line.start.getY() || end.getX() == line.end.getX() && end.getY() == line.end.getY()) {
            if (includingEndpoints) {
                intersection.setPosition(end);
                return true;
            } else
                return false;
        }

        //if the edge is vertical, we cannot compute the slope
        double ix, iy;
        if (isVertical())
            ix = start.getX();
        else if (line.isVertical())
            ix = line.start.getX();
        else
            ix = (line.yAbs() - yAbs()) / (slope() - line.slope());

        //set iy to the value of the infinite line at xvalue ix
        //use a non-vertical line (can't both be vertical, as they are not parallel)
        if (isVertical())
            iy = line.slope() * ix + line.yAbs();
        else
            iy = slope() * ix + yAbs();

        double[] pt = {ix, iy};
        if (createBBox().contains(pt) && line.createBBox().contains(pt)) {
            intersection.setPosition(ix, iy);
            return true;
        } else return false;
    }

    public DRect createBBox() {
        return new DRect(start.getX(), start.getY(), end.getX(), end.getY());
    }
}
