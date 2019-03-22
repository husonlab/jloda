/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package jloda.fx.shapes;

import javafx.scene.shape.Ellipse;

/**
 * diamond shape
 * Daniel Huson, 1.2018
 */
public class OvalShape extends Ellipse implements ISized {
    private double width;
    private double height;

    /**
     * constructor
     *
     * @param width
     * @param height
     */
    public OvalShape(double width, double height) {
        setSize(width, height);
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
        setRadiusX(0.5 * width);
        setRadiusY(0.5 * height);
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
