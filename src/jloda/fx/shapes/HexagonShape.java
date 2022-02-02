/*
 * HexagonShape.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.shapes;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import jloda.fx.util.GeometryUtilsFX;

/**
 * hexagon
 * Daniel Huson, 1.2018
 */
public class HexagonShape extends Polygon implements ISized {
    private double width;
    private double height;

    /**
     * constructor
     *
	 */
    public HexagonShape(double width, double height) {
        setSize(width, height);
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
        Point2D point = new Point2D(0, 0.5 * width);
        getPoints().setAll(point.getX(), point.getY());
        for (int i = 1; i < 6; i++) {
            point = GeometryUtilsFX.rotate(point, 60);
            getPoints().addAll(point.getX(), point.getY());
        }
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

}
