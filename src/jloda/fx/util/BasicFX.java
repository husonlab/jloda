/*
 * BasicFX.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.fx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * basic stuff for FX
 * Daniel Huson, 3.2019
 */
public class BasicFX {
    /**
     * go to given line and given col
     *
     * @param lineNumber
     * @param col        if col<=1 or col>line length, will select the whole line, else selects line starting at given col
     */
    public static void gotoAndSelectLine(TextArea textArea, long lineNumber, int col) {
        if (col < 0)
            col = 0;
        else if (col > 0)
            col--; // because col is 1-based

        lineNumber = Math.max(1, lineNumber);
        final String text = textArea.getText();
        int start = 0;
        for (int i = 1; i < lineNumber; i++) {
            start = text.indexOf('\n', start + 1);
            if (start == -1) {
                System.err.println("No such line number: " + lineNumber);
                return;
            }
        }
        start++;
        if (start < text.length()) {
            int end = text.indexOf('\n', start);
            if (end == -1)
                end = text.length();
            if (start + col < end)
                start = start + col;
            textArea.requestFocus();
            textArea.selectRange(start, end);
        }
    }

    /**
     * get all children, recursively
     *
     * @param children initial set of children
     * @return recursively get all children
     */
    public static Collection<? extends Node> getAllChildrenRecursively(Collection<Node> children) {
        final var all = new ArrayList<Node>();
        final var list = new LinkedList<>(children);
        while (list.size() > 0) {
            final var node = list.remove();
            all.add(node);
            if (node instanceof Parent parent)
                list.addAll(parent.getChildrenUnmodifiable());
        }
        return all;
    }

    /**
     * get the best font size to fit the given width
     *
     * @param title
     * @param font
     * @param width
     * @return best font size to fit
     */
    public static double fitFontSizeToWidthAndHeight(String title, Font font, double width, double height) {
        for (double fontSize = 50; fontSize > 7; fontSize -= 0.5) {
            final Dimension2D dimensions = getTextDimension(title, Font.font(font.getFamily(), fontSize));
            if (dimensions.getWidth() < width && dimensions.getHeight() < height)
                return fontSize;
        }
        return 10;
    }

    /**
     * get the dimension of a text
     *
     * @param string
     * @param font
     * @return text dimension
     */
    public static Dimension2D getTextDimension(String string, Font font) {
        Text text = new Text(string);
        text.setFont(font == null ? Font.getDefault() : font);
        return new Dimension2D(text.getBoundsInLocal().getWidth(), text.getBoundsInLocal().getHeight());
    }

    /**
     * permanentally hide column headers
     *
     * @param tableView
     */
    public static void hideColumnHeaders(TableView tableView) {
        final InvalidationListener tableResizeListener = (e) -> {
            final Pane header = (Pane) tableView.lookup("TableHeaderRow");
            header.setMinHeight(0);
            header.setPrefHeight(0);
            header.setMaxHeight(0);
            header.setVisible(false);
        };

        tableView.widthProperty().addListener(tableResizeListener);
        tableView.heightProperty().addListener(tableResizeListener);
    }

    public static void changeTranslateWidthHeight(Rectangle rectangle, Function<Double, Double> changeX, Function<Double, Double> changeY) {
        rectangle.setTranslateX(changeX.apply(rectangle.getTranslateX()));
        rectangle.setTranslateY(changeY.apply(rectangle.getTranslateY()));
        rectangle.setWidth(changeX.apply(rectangle.getWidth()));
        rectangle.setHeight(changeY.apply(rectangle.getHeight()));
    }

    public static void changeCenterRadii(Arc arc, Function<Double, Double> changeX, Function<Double, Double> changeY) {
        arc.setCenterX(changeX.apply(arc.getCenterX()));
        arc.setCenterY(changeY.apply(arc.getCenterY()));
        arc.setRadiusX(changeX.apply(arc.getRadiusX()));
        arc.setRadiusY(changeY.apply(arc.getRadiusY()));
    }


    public static void changeTranslate(Node node, Function<Double, Double> changeX, Function<Double, Double> changeY) {
        node.setTranslateX(changeX.apply(node.getTranslateX()));
        node.setTranslateY(changeY.apply(node.getTranslateY()));
    }


    public static void centerAndShow(Stage parent, Stage child) {
        child.setX(parent.getX() + 0.5 * parent.getWidth());
        child.setY(parent.getY() + 0.5 * parent.getHeight());

        child.show();
        child.setX(parent.getX() + 0.5 * (parent.getWidth() - child.getWidth()));
        child.setY(parent.getY() + 0.5 * (parent.getHeight() - child.getHeight()));
    }

    /**
     * ensures that clicking on the slider also generates  value changing events
     *
     * @param slider
     */
    public static void ensureClickGeneratesValueChangingEvents(Slider slider) {
        slider.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> slider.setValueChanging(true));
        slider.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> slider.setValueChanging(false));
    }

    /**
     * adds full screen support
     *
     * @param stage
     */
    public static void setupFullScreenMenuSupport(Stage stage, MenuItem menuItem) {
        stage.fullScreenProperty().addListener((c, o, n) -> menuItem.setText(n ? "Exit Full Screen" : "Enter Full Screen"));
        menuItem.setOnAction((e) -> stage.setFullScreen(!stage.isFullScreen()));
        menuItem.setDisable(false);
    }


    public static boolean acceptableImageFormat(String name) {
        name = name.toLowerCase();
        return name.endsWith(".gif") || name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    /**
     * copy and remove white background
     *
     * @param image
     * @param threshold   between 0 and 255
     * @param outsideOnly only remove white pixels that are accessible from the outside
     * @return image with white removed
     */
    public static Image copyAndRemoveWhiteBackground(Image image, int threshold, boolean outsideOnly) {
        try {
            final int width = (int) image.getWidth();
            final int height = (int) image.getHeight();
            final WritableImage result = new WritableImage(width, height);
            final PixelReader reader = image.getPixelReader();
            final PixelWriter writer = result.getPixelWriter();

            // first copy everything:
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    writer.setArgb(x, y, reader.getArgb(x, y));
                }
            }

            // sweep right and then left:
            for (int y = 0; y < height; y++) {
                int right = 0;
                for (int x = 0; x < width; x++) {
                    if (meetsThreshold(reader.getArgb(x, y), threshold))
                        writer.setArgb(x, y, 0x00FFFFFF);
                    else if (outsideOnly)
                        break;
                    right++;
                }
                for (int x = width - 1; x >= right; x--) {
                    if (meetsThreshold(reader.getArgb(x, y), threshold))
                        writer.setArgb(x, y, 0x00FFFFFF);
                    else if (outsideOnly)
                        break;
                }
            }
            // sweep down and then up:
            for (int x = 0; x < width; x++) {
                int bot = 0;
                for (int y = 0; y < height; y++) {
                    if (meetsThreshold(reader.getArgb(x, y), threshold))
                        writer.setArgb(x, y, 0x00FFFFFF);
                    else if (outsideOnly)
                        break;
                    bot++;
                }
                for (int y = height - 1; y >= bot; y--) {
                    if (meetsThreshold(reader.getArgb(x, y), threshold))
                        writer.setArgb(x, y, 0x00FFFFFF);
                    else if (outsideOnly)
                        break;
                }
            }
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean meetsThreshold(int argb, int threshold) {
        final int r = (argb >> 16) & 0xFF;
        final int g = (argb >> 8) & 0xFF;
        final int b = argb & 0xFF;

        return r >= threshold && g >= threshold && b >= threshold;
    }

    public static FontWeight getWeight(Font font) {
        return font.getName().contains("Bold") ? FontWeight.BOLD : FontWeight.NORMAL;
    }

    public static FontPosture getPosture(Font font) {
        return font.getName().contains("Italic") ? FontPosture.ITALIC : FontPosture.REGULAR;
    }

    /**
     * adds listener to provided property and reports on changes
     *
     * @param property the property to listen to
     */
    public static <T> void reportChanges(ReadOnlyProperty<T> property) {
        reportChanges(null, property);
    }

    /**
     * adds listener to provided property and reports on changes
     *
     * @param property the property to listen to
     */
    public static <T> void reportChanges(String label, ReadOnlyProperty<T> property) {
        ChangeListener<T> listener = (v, o, n) -> System.err.println((label != null ? label + ": " : "") + property.getName() + ": " + (o == null ? null : o.toString()) + " -> " + (n == null ? "null" : n.toString()));
        property.addListener(listener);
    }

    /**
     * gets the angle of a pane on screen
     *
     * @param pane the pane
     * @return angle in degrees
     */
    public static double getAngleOnScreen(Pane pane) {
        var orig = pane.localToScreen(0, 0);
        var one = pane.localToScreen(1, 0);
        if (orig != null && one != null)
            return GeometryUtilsFX.modulo360(one.subtract(orig).angle(1, 0));
        else
            return 0;
    }
}
