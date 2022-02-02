/*
 * NGonShape.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import jloda.fx.util.GeometryUtilsFX;

/**
 * n-gon
 * Daniel Huson, 4.2019
 */
public class NGonShape extends Polygon implements ISized {
    private final DoubleProperty width = new SimpleDoubleProperty(1);
    private final IntegerProperty n = new SimpleIntegerProperty(4);

    /**
     * default constructor
     */
    public NGonShape() {
        this.n.addListener((e) -> setSize(getWidth(), 0));
        this.width.addListener((c, o, n) -> setSize(n.doubleValue(), n.doubleValue()));
    }

    public NGonShape(Point2D location) {
        this();
        setLayoutX(location.getX());
        setLayoutY(location.getY());
    }

    /**
     * constructor
     *
     * @param n     n-gon
     */
    public NGonShape(double width, Point2D location, int n) {
        this(location);
        setN(n);
        setWidth(width);
    }

    /**
     * constructor
     *
     * @param ngon  n-gon
     */
    public NGonShape(double width, int ngon) {
        this();
        setN(ngon);
        setWidth(width);
    }

    public void setSize(double width, double unused) {
        if (width != getWidth()) {
            setWidth(width);
            if (getN() <= 2) {
                width *= 0.5;
                getPoints().setAll(0.0, -1.2 * width, -width / 1.2, 0.0, 0.0, 1.2 * width, width / 1.2, 0.0);
            } else {
                Point2D point = new Point2D(0, 0.5 * width);
                point = GeometryUtilsFX.rotate(point, 180.0 / getN());

                getPoints().setAll(point.getX(), point.getY());
                for (int i = 1; i < getN(); i++) {
                    point = GeometryUtilsFX.rotate(point, 360.0 / getN());
                    getPoints().addAll(point.getX(), point.getY());
                }
            }
        }
    }

    public void setLayout(Point2D point2D) {
        setLayoutX(point2D.getX());
        setLayoutY(point2D.getY());
    }

    public int getN() {
        return n.get();
    }

    public IntegerProperty nProperty() {
        return n;
    }

    public void setN(int n) {
        this.n.set(n);
    }

    @Override
    public double getWidth() {
        return width.get();
    }

    @Override
    public double getHeight() {
        return getWidth();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }
}
