/*
 * BBoxUtils.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.util;

import javafx.geometry.BoundingBox;
import jloda.graph.NodeArray;
import jloda.util.APoint2D;

import java.util.Collection;
import java.util.function.Function;

/**
 * simple bounding box utilities
 * Daniel Huson, 3.2021
 */
public class BBoxUtils {
	public static <T> void fitToBox(NodeArray<APoint2D<? extends T>> points, double[] tar) {
		var src = computeBBox(points.values());

		Function<Double, Double> mapX = x -> (x - src[0]) / (src[2]-src[0]) * (tar[2]-tar[0]) + tar[0];
		Function<Double, Double> mapY = y -> (y - src[1]) / (src[3]-src[1]) * (tar[3]-tar[1]) + tar[1];

		for (var v : points.keys()) {
			var point = points.get(v);
			points.put(v, new APoint2D<>(mapX.apply(point.getX()), mapY.apply(point.getY()), point.getUserData()));
		}
	}

	public static <T> void fitToBox(NodeArray<APoint2D<? extends T>> points, BoundingBox boundingBox) {
		var src = computeBBox(points.values());

		Function<Double, Double> mapX = x -> (x - src[0]) / (src[2]-src[0]) * boundingBox.getWidth() + boundingBox.getMinX();
		Function<Double, Double> mapY = y -> (y - src[1]) / (src[3]-src[1]) * boundingBox.getHeight()+ boundingBox.getMinY();

		for (var v : points.keys()) {
			var point = points.get(v);
			points.put(v, new APoint2D<>(mapX.apply(point.getX()), mapY.apply(point.getY()), point.getUserData()));
		}
	}

	/**
	 * computes the bounding box of all locations
	 *
	 * @return bounding box
	 */
	public static <T> double[] computeBBox(Collection<? extends APoint2D<?>> points) {
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
		return new double[]{minX, minY, maxX, maxY};
	}
}
