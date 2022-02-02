/*
 * ColorScalePane.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.colorscale;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.StringUtils;

/**
 * a pane showing a scale
 * Daniel Huson, 3.2019
 */
public class ColorScalePane extends Pane {
    public static final Font font = new Font("Arial", 12);
    private final ColorScalePaneController controller;
    private final Parent root;

    private double mouseX = 0;
    private double mouseY = 0;

    /**
     * constructor
     *
	 */
    public ColorScalePane() {
        final ExtendedFXMLLoader<ColorScalePaneController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        getChildren().add(root);

        controller.getGetTitleLabel().setFont(font);
        controller.getGetLeftLabel().setFont(font);
        controller.getGetRightLabel().setFont(font);
    }

    /**
     * set the color scale for an ordinal scale
     */
    public void setColorScale(String title, double leftValue, double rightValue, boolean reverse, ObservableList<Color> colors) {
        if (colors.size() > 0) {
			getChildren().setAll(root);
			setTitleText(title != null ? title + ":" : "");
			setLeftText(StringUtils.removeTrailingZerosAfterDot(String.format("%,.2f", leftValue)));
			setRightText(StringUtils.removeTrailingZerosAfterDot(String.format("%,.2f", rightValue)));

			final Canvas canvas = controller.getCanvas();
			final GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

			final double factor = colors.size() / canvas.getWidth();

			for (int x = 0; x < canvas.getWidth(); x++) {
				final Color color = colors.get(Math.max(0, Math.min(colors.size() - 1, (int) Math.round(factor * (reverse ? canvas.getWidth() - x : x)))));
				gc.setFill(color);
                gc.setStroke(color);
                gc.fillRect(x, 0, 1, canvas.getHeight());
                gc.strokeRect(x, 0, 1, canvas.getHeight());
            }
        } else
            getChildren().remove(root);
    }

    /**
     * set the color scale for a nominal scale
     */
    public void setColorScale(String title, ObservableList<Color> colors, double opacity) {
        if (colors.size() > 0) {
            getChildren().setAll(root);
            setTitleText(title != null ? title + ":" : "");
            setLeftText("");
            setRightText("");

            final Canvas canvas = controller.getCanvas();
            final GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            final double dx = Math.sqrt(canvas.getHeight() * canvas.getWidth() / colors.size());
            final int cols = (int) (canvas.getWidth() / dx);
            int rows = (int) (canvas.getHeight() / dx);
            final double dy;
            if (rows * cols < colors.size()) { // need an extra row
                rows++;
                dy = ((rows - 1.0) / rows) * dx;
            } else
                dy = dx;

            int count = 0;
            doubleLoop:
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (count == colors.size())
                        break doubleLoop;
                    final Color color = colors.get(count++).deriveColor(1, 1, 1, opacity);
                    gc.setFill(color);
                    gc.setStroke(color);
                    gc.fillRect(x * dx, y * dy, dx, dy);
                    gc.strokeRect(x * dx, y * dy, dx, dy);
                }
            }
        } else
            getChildren().remove(root);
    }

    public String getTitleText() {
        return titleTextProperty().get();
    }

    public StringProperty titleTextProperty() {
        return controller.getGetTitleLabel().textProperty();
    }

    public void setTitleText(String titleText) {
        titleTextProperty().set(titleText);
    }

    public String getLeftText() {
        return leftTextProperty().get();
    }

    public StringProperty leftTextProperty() {
        return controller.getGetLeftLabel().textProperty();
    }

    public void setLeftText(String leftText) {
        leftTextProperty().set(leftText);
    }

    public String getRightText() {
        return rightTextProperty().get();
    }

    public StringProperty rightTextProperty() {
        return controller.getGetRightLabel().textProperty();
    }

    public void setRightText(String rightText) {
        rightTextProperty().set(rightText);
    }

    public Canvas getCanvas() {
        return controller.getCanvas();
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
}
