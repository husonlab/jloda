/*
 * ScaleBar2.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

/**
 * maintains a scale bar
 * Daniel Huson, 5.2018
 */
public class ScaleBar2 extends Pane {
    public static Font font = new Font("Arial", 12);

    private double mouseX = 0;
    private double mouseY = 0;

    private final Line line;
    private final Label label;
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GRAY);

    private final DoubleProperty bp2screen = new SimpleDoubleProperty(0);

    /**
     * constructor
     */
    public ScaleBar2() {
        setMaxHeight(12);
        setPrefHeight(12);

        line = new Line(0, 6, 0, 6);
        line.setStroke(getColor());
        Line begin = new Line(line.getStartX(), 4, line.getStartX(), 8);
        begin.startXProperty().bind(line.startXProperty());
        begin.endXProperty().bind(line.startXProperty());
        begin.setStroke(getColor());

        Line end = new Line(line.getEndX(), 4, line.getEndX(), 8);
        end.startXProperty().bind(line.endXProperty());
        end.endXProperty().bind(line.endXProperty());
        end.setStroke(getColor());

        label = new Label();
        label.setFont(font);
        label.setTextFill(getColor());
        label.translateXProperty().bind(line.endXProperty().add(5));
        label.setTranslateY(5 - 0.5 * font.getSize());

        getChildren().addAll(begin, end, line, label);

        bp2screen.addListener((c, o, n) -> {
            if (n.doubleValue() > 0) {
                double bpPer100Pixel = 50 / n.doubleValue();
                double ceilingPer100Pixel = ceilingPowerOf10(bpPer100Pixel);
                double numberOfPixel = 50 * (ceilingPer100Pixel / bpPer100Pixel);

                line.setEndX(numberOfPixel);
                label.setText(String.format("%,d bp", Math.round(ceilingPer100Pixel)));

                layout();
                //numberAxis.setMaxWidth(numberOfPixel);
            }
        });

        setBp2screen(1);
    }

    public void addToAnchorPane(AnchorPane anchorPane, double left, double top) {
        anchorPane.getChildren().add(this);
        AnchorPane.setLeftAnchor(this, left);
        AnchorPane.setTopAnchor(this, top);

        setOnMousePressed((e -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
        }));

        setOnMouseDragged((e -> {
            double deltaX = e.getScreenX() - mouseX;
            double deltaY = e.getScreenY() - mouseY;
            AnchorPane.setLeftAnchor(this, Math.max(5, AnchorPane.getLeftAnchor(this) + deltaX));
            AnchorPane.setTopAnchor(this, Math.max(5, AnchorPane.getTopAnchor(this) + deltaY));
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
        }));
    }


    public double getBp2screen() {
        return bp2screen.get();
    }

    public DoubleProperty bp2screenProperty() {
        return bp2screen;
    }

    public void setBp2screen(double bp2screen) {
        this.bp2screen.set(bp2screen);
    }

    public static double ceilingPowerOf10(double x) {
        return 10 * Math.pow(10, (Math.floor(Math.log10(x))));
    }

    public Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }
}
