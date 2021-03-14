/*
 *  BBox.java Copyright (C) 2021. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.util;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import jloda.graph.NodeArray;

import java.util.function.Function;

/**
 * simple bounding box utilities
 * Daniel Huson, 3.2021
 */
public class BBoxUtils {
    public static void fitToBox(NodeArray<APoint2D<?>> points, Bounds tar) {
        var src = computeBBox(points);

        Function<Double, Double> mapX = x -> (x - src.getMinX()) / (src.getWidth()) * tar.getWidth() + tar.getMinX();
        Function<Double, Double> mapY = y -> (y - src.getMinY()) / (src.getHeight()) * tar.getHeight() + tar.getMinY();

        for (var v : points.keys()) {
            var point = points.get(v);
            points.put(v, new APoint2D<>(mapX.apply(point.getX()), mapY.apply(point.getY()), point.getUserData()));
        }
    }

    /**
     * computes the bounding box of all locations
     *
     * @param node2location
     * @return bounding box
     */
    private static Bounds computeBBox(NodeArray<APoint2D<?>> node2location) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (var point : node2location.values()) {
            minX = Math.min(minX, point.getX());
            maxX = Math.max(maxX, point.getX());
            minY = Math.min(minY, point.getY());
            maxY = Math.max(maxY, point.getY());
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
}
