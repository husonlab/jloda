/*
 * ScaleBar.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.util;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

/**
 * maintains a scale bar
 * Daniel Huson, 5.2018
 */
public class ScaleBar extends AnchorPane {
    private final Pane pane = new Pane();
    private final NumberAxis numberAxis;
    private double unitLengthX = 1;
    private double factorX = 1;

    private double mouseX = 0;
    private double mouseY = 0;

    /**
     * constructor
     */
    public ScaleBar() {
        numberAxis = new NumberAxis();

        pane.getChildren().add(numberAxis);

        AnchorPane.setLeftAnchor(pane, 5.0);
        AnchorPane.setTopAnchor(pane, 2.0);
        getChildren().add(pane);

        numberAxis.setSide(Side.TOP);
        numberAxis.setAutoRanging(false);
        numberAxis.setLowerBound(0);
        numberAxis.prefHeightProperty().set(20);
        numberAxis.prefWidthProperty().set(150);
        numberAxis.setTickLabelFont(Font.font("Arial", 10));
        update();

        pane.setOnMousePressed((e -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        }));

        pane.setOnMouseDragged((e -> {
            double deltaX = e.getScreenX() - mouseX;
            double deltaY = e.getScreenY() - mouseY;
            AnchorPane.setLeftAnchor(pane, AnchorPane.getLeftAnchor(pane) + deltaX);
            AnchorPane.setTopAnchor(pane, AnchorPane.getTopAnchor(pane) - deltaY);
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        }));
    }

    public double getFactorX() {
        return factorX;
    }

    public void setFactorX(double factorX) {
        this.factorX = factorX;
        update();
    }

    public double getUnitLengthX() {
        return unitLengthX;
    }

    public void setUnitLengthX(double unitLengthX) {
        this.unitLengthX = unitLengthX;
        update();
    }

    public void update() {
        Platform.runLater(() -> {
            final double value = numberAxis.getWidth() / (unitLengthX * factorX);
            numberAxis.setUpperBound(value);
            numberAxis.setTickUnit(ceilingPowerOf10(value));
            pane.layout();
        });
    }

    public static double ceilingPowerOf10(double x) {
        return 10 * Math.pow(10, (Math.floor(Math.log10(x))));
    }

    public NumberAxis getNumberAxis() {
        return numberAxis;
    }
}
