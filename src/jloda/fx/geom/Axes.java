/*
 * Axes.java Copyright (C) 2022 Daniel H. Huson
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
package jloda.fx.geom;

import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * 3D axes
 * Created by huson on 9/25/15.
 */
public class Axes extends Group {
    private final javafx.scene.control.Label labelX;
    private final javafx.scene.control.Label labelY;
    private final javafx.scene.control.Label labelZ;

    /**
     * constructor
     */
    public Axes(float xLength, float yLength, float zLength, Color color, ReadOnlyProperty... properties) {
        final LabeledArrow arrowX = new LabeledArrow(Point3D.ZERO, new Point3D(xLength, 0, 0), "x", color, properties);
        getChildren().add(arrowX);
        labelX = arrowX.getLabel();

        final LabeledArrow arrowY = new LabeledArrow(Point3D.ZERO, new Point3D(0, -yLength, 0), "y", color, properties);
        getChildren().add(arrowY);
        labelY = arrowY.getLabel();

        final LabeledArrow arrowZ = new LabeledArrow(Point3D.ZERO, new Point3D(0, 0, -zLength), "z", color, properties);
        getChildren().add(arrowZ);
        labelZ = arrowZ.getLabel();
    }

    /**
     * label to place in 2D overlay
     *
     * @return x
     */
    public Label getLabelX() {
        return labelX;
    }

    /**
     * label to place in 2D overlay
     *
     * @return y
     */
    public Label getLabelY() {
        return labelY;
    }

    /**
     * label to place in 2D overlay
     *
     * @return z
     */
    public Label getLabelZ() {
        return labelZ;
    }

    public boolean isShow() {
        return isVisible();
    }

    public void setShow(boolean show) {
        setVisible(show);
        labelX.setVisible(show);
        labelY.setVisible(show);
        labelZ.setVisible(show);
    }
}
