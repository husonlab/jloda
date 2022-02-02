/*
 * DRect.java Copyright (C) 2022 Daniel H. Huson
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
import jloda.graph.fmm.algorithm.NodeAttributes;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * Simple rectangle class
 * Daniel Huson, 3.2021
 */
public class DRect implements FastMultiLayerMethodLayout.Point, FastMultiLayerMethodLayout.Rectangle {
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;

    public DRect(double minX, double minY, double maxX, double maxY) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public double getArea() {
        return getWidth() * getHeight();
    }

    /**
     * get center coordinate
     *
     * @return x
     */
    @Override
    public double getX() {
        return 0.5 * (minX + maxX);
    }

    /**
     * get center coordinate
     *
     * @return y
     */
    @Override
    public double getY() {
        return 0.5 * (minY + maxY);
    }

    public boolean contains(double... xy) {
        return xy.length == 2 && xy[0] >= minX && xy[0] <= maxX && xy[1] >= minY && xy[1] <= maxY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DRect)) return false;
        DRect dRect = (DRect) o;
        return Double.compare(dRect.minX, minX) == 0 && Double.compare(dRect.minY, minY) == 0 && Double.compare(dRect.maxX, maxX) == 0 && Double.compare(dRect.maxY, maxY) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, maxX, maxY);
    }

    /**
     * computes the bounding box of all locations
     *
     * @return bounding box
     */
    public static DRect computeBBox(Collection<? extends FastMultiLayerMethodLayout.Point> points) {
        if (points.size() > 0) {
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            for (var point : points) {
                minX = Math.min(minX, point.getX());
                maxX = Math.max(maxX, point.getX());
                minY = Math.min(minY, point.getY());
                maxY = Math.max(maxY, point.getY());
            }
            return new DRect(minX, minY, maxX, maxY);
        } else
            return new DRect(0, 0, 0, 0);
    }

    public static void fitToBox(Collection<NodeAttributes> nodeAttributes, DRect boundingBox) {
        var src = computeBBox(nodeAttributes);

        Function<Double, Double> mapX = x -> (x - src.getMinX()) / src.getWidth() * boundingBox.getWidth() + boundingBox.getMinX();
        Function<Double, Double> mapY = y -> (y - src.getMinY()) / src.getHeight() * boundingBox.getHeight() + boundingBox.getMinY();

        for (var na : nodeAttributes) {
            na.setPosition(mapX.apply(na.getX()), mapY.apply(na.getY()));
        }
    }
}
