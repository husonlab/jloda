/*
 * WindowGeometry.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.window;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Stage;
import jloda.util.NumberUtils;
import jloda.util.ProgramProperties;

import java.io.IOException;

/**
 * window geometry
 * Daniel Huson, 3.2019
 */
public class WindowGeometry {
    final private DoubleProperty x = new SimpleDoubleProperty(20);
    final private DoubleProperty y = new SimpleDoubleProperty(20);
    final private DoubleProperty width = new SimpleDoubleProperty(20);
    final private DoubleProperty height = new SimpleDoubleProperty(20);

    public WindowGeometry() {
    }

    public WindowGeometry(String text) throws IOException {
        setFromString(text);
    }

    public WindowGeometry(Stage stage) {
        setFromStage(stage);
    }

    public String toString() {
        return String.format("%.1f %.1f %.1f %.1f", getX(), getY(), getWidth(), getHeight());
    }

    public void setFromString(String text) throws IOException {
        final String[] tokens = text.split("\\s+");
        if (tokens.length == 4) {
            setX(NumberUtils.parseDouble(tokens[0]));
            setY(NumberUtils.parseDouble(tokens[1]));
            setWidth(NumberUtils.parseDouble(tokens[2]));
            setHeight(NumberUtils.parseDouble(tokens[3]));
        } else
            throw new IOException("Invalid geometry string: " + text);
    }

    public void setFromStage(Stage stage) {
        setX(stage.getX());
        setY(stage.getY());
        setWidth(stage.getWidth());
        setHeight(stage.getHeight());
    }

    public static void setToStage(Stage stage) {
        var wg = loadFromProperties();
        stage.setX(wg.getX());
        stage.setY(wg.getY());
        stage.setWidth(wg.getWidth());
        stage.setHeight(wg.getHeight());
    }

    public double getX() {
        return x.get();
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public double getY() {
        return y.get();
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public double getHeight() {
        return height.get();
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public static WindowGeometry loadFromProperties() {
        var windowGeometry = new WindowGeometry();
        try {
            windowGeometry.setFromString(ProgramProperties.get("WindowGeometry", "50 50 800 800"));
        } catch (IOException ignored) {
        }
        return windowGeometry;
    }

    public static void saveToProperties(WindowGeometry wg) {
        ProgramProperties.put("WindowGeometry", "%d %d %d %d".formatted((int) wg.getX(), (int) wg.getY(), (int) wg.getWidth(), (int) wg.getHeight()));
    }

    public static void listenToStage(Stage stage) {
        stage.xProperty().addListener(e -> update(stage.getX(), null, null, null));
        stage.yProperty().addListener(e -> update(null, stage.getY(), null, null));
        stage.widthProperty().addListener(e -> update(null, null, stage.getWidth(), null));
        stage.heightProperty().addListener(e -> update(null, null, null, stage.getHeight()));
    }

    private static void update(Double x, Double y, Double width, Double height) {
        var wg = loadFromProperties();
        if (x != null)
            wg.setX(x);
        if (y != null)
            wg.setY(y);
        if (width != null)
            wg.setWidth(width);
        if (height != null)
            wg.setHeight(height);
        saveToProperties(wg);
    }
}
