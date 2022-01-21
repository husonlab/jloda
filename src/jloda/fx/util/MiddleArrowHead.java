/*
 * MiddleArrowHead.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.InvalidationListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;

/**
 * a simple arrow head in the middle of a line
 * Daniel Huson, 12.2021
 */
public class MiddleArrowHead extends Group {
	/**
	 * constructor
	 *
	 * @param line the line to obtain an arrow head
	 */
	public MiddleArrowHead(Line line) {
		final var parts = new Line[]{new Line(), new Line()};
		getChildren().addAll(parts);
		for (var part : parts) {
			part.strokeProperty().bind(line.strokeProperty());
			part.strokeWidthProperty().bind(line.strokeWidthProperty());
		}

		InvalidationListener invalidationListener = e -> {
			var start = new Point2D(line.getStartX(), line.getStartY());
			var end = new Point2D(line.getEndX(), line.getEndY());
			var radian = GeometryUtilsFX.deg2rad(GeometryUtilsFX.computeAngle(end.subtract(start)));

			var dx = 5 * Math.cos(radian);
			var dy = 5 * Math.sin(radian);

			var mid = start.midpoint(end);
			var head = mid.add(dx, dy);
			var one = mid.add(-dy, dx);
			var two = mid.add(dy, -dx);

			parts[0].setStartX(one.getX());
			parts[0].setStartY(one.getY());
			parts[0].setEndX(head.getX());
			parts[0].setEndY(head.getY());

			parts[1].setStartX(two.getX());
			parts[1].setStartY(two.getY());
			parts[1].setEndX(head.getX());
			parts[1].setEndY(head.getY());
		};

		line.startXProperty().addListener(invalidationListener);
		line.startYProperty().addListener(invalidationListener);
		line.endXProperty().addListener(invalidationListener);
		line.endYProperty().addListener(invalidationListener);
	}
}
