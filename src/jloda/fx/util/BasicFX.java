/*
 * BasicFX.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * basic stuff for FX
 * Daniel Huson, 3.2019
 */
public class BasicFX {
    /**
     * go to given line and given col
     *
     * @param lineNumber line number
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
     * recursively gets node and all nodes below it
     */
    public static <T extends Node> Collection<T> getAllRecursively(Node node, Class<T> clazz) {
        final var all = new ArrayList<T>();
        final var queue = new LinkedList<Node>();
        queue.add(node);
        while (queue.size() > 0) {
            node = queue.pop();
            if (clazz.isAssignableFrom(node.getClass()))
                all.add((T) node);
            if (node instanceof Parent parent)
                queue.addAll(parent.getChildrenUnmodifiable());
        }
        return all;
    }

    /**
     * recursively finds one node
     */
    public static <T extends Node> T findOneRecursively(Node node, Class<T> clazz) {
        final var queue = new LinkedList<Node>();
        queue.add(node);
        while (queue.size() > 0) {
            node = queue.pop();
            if (clazz.isAssignableFrom(node.getClass()))
                return (T) node;
            if (node instanceof Parent parent)
                queue.addAll(parent.getChildrenUnmodifiable());
        }
        return null;
    }

    /**
     * recursively gets node and all nodes below it
     */
    public static Collection<Node> getAllRecursively(Node node, Predicate<Node> nodePredicate) {
        final var all = new ArrayList<Node>();
        final var queue = new LinkedList<Node>();
        queue.add(node);
        while (queue.size() > 0) {
            node = queue.pop();
            if (nodePredicate.test(node))
                all.add(node);
            if (node instanceof Parent parent)
                queue.addAll(parent.getChildrenUnmodifiable());
        }
        return all;
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

    public static ArrayList<Node> findRecursively(Node root, Function<Node, Boolean> condition) {
        var all = new ArrayList<Node>();
        var queue = new LinkedList<Node>();
        queue.add(root);
        while (queue.size() > 0) {
            var node = queue.pop();
            if (condition.apply(node))
                all.add(node);
            if (node instanceof Parent parent) {
                queue.addAll(parent.getChildrenUnmodifiable());
            }
        }
        return all;
    }

    /**
     * get the best font size to fit the given width
     *
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
	 */
    public static void ensureClickGeneratesValueChangingEvents(Slider slider) {
        slider.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> slider.setValueChanging(true));
        slider.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> slider.setValueChanging(false));
    }

    /**
     * adds full screen support
     *
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
        System.err.println((label != null ? label + ": " : "") + property.getValue());
        property.addListener((v, o, n) -> System.err.println((label != null ? label + ": " : "") + property.getName() + ": " + (o == null ? null : o.toString()) + " -> " + (n == null ? "null" : n.toString())));
    }

    public static <T> void reportChanges(String label, ObservableList<T> list) {
        System.err.println((label != null ? label + ":" : ":"));
        list.addListener((ListChangeListener<? super T>) e -> {
            while (e.next()) {
                for (var v : e.getAddedSubList()) {
                    System.err.print("\t+" + v);
                }
                for (var v : e.getRemoved()) {
                    System.err.print("\t-" + v);
                }
            }
            System.err.println();
        });

    }

    /**
     * gets the angle of a pane on screen
     *
     * @param pane the pane
     * @return angle in degrees
     */
    public static Optional<Double> getAngleOnScreen(Pane pane) {
        var orig = pane.localToScreen(0, 0);
        if (orig != null) {
            var x1000 = pane.localToScreen(1000, 0);
            return Optional.of(GeometryUtilsFX.modulo360(x1000.subtract(orig).angle(1000.0, 0.0)));
        } else
            return Optional.empty();
    }

    /**
     * does this pane appear as a mirrored image on the screen?
     *
     * @param pane the pane
     * @return true, if mirror image, false if direct image
     */
    public static Optional<Boolean> isMirrored(Pane pane) {
        var orig = pane.localToScreen(0, 0);
        if (orig != null) {
            var x1000 = pane.localToScreen(1000, 0);
            var y1000 = pane.localToScreen(0, 1000);
            var p1 = x1000.subtract(orig);
            var p2 = y1000.subtract(orig);
            var determinant = p1.getX() * p2.getY() - p1.getY() * p2.getX();
            return Optional.of(determinant < 0);
        } else
            return Optional.empty();
    }

    public static void preorderTraversal(Node node, Consumer<Node> apply) {
        final var queue = new LinkedList<Node>();
        queue.add(node);
        while (queue.size() > 0) {
            node = queue.pop();
            apply.accept(node);
            if (node instanceof Parent parent)
                queue.addAll(parent.getChildrenUnmodifiable());
        }
    }

    public static void putTextOnClipBoard(String text) {
        if (!text.isBlank()) {
            final ClipboardContent contents = new ClipboardContent();
            contents.put(DataFormat.PLAIN_TEXT, text);
            contents.putString(text);
            Clipboard.getSystemClipboard().setContent(contents);
        }
    }

    /**
     * operate a toggle button as if it has multiple states
     *
     * @param button        the toggle button
     * @param defaultState  the defaultState
     * @param stateProperty the state property that should be observed to react to changes of the selected state
     * @param states        the list of states to cycle through. The first state is the non-selected state for the toggle button
     */
    public static void makeMultiStateToggle(ToggleButton button, String defaultState, StringProperty stateProperty, String... states) {
        assert (states.length > 0);

        var list = List.of(states);

        button.textProperty().bind(stateProperty);

        button.setOnAction(e -> {
            var index = button.getText() == null ? 0 : list.indexOf(button.getText()) + 1;
            if (index == 0 || index >= list.size()) {
                Platform.runLater(() -> {
                    button.setSelected(false);
                    stateProperty.set(list.get(0));
                });
            } else {
                button.setSelected(true);
                stateProperty.set(list.get(index));
            }
        });

        if (defaultState != null) {
            button.setSelected(!defaultState.equals(states[0]));
            stateProperty.set(defaultState);
        }
    }

    public static void applyToAllMenus(MenuBar menuBar, Function<Menu, Boolean> accept, Consumer<Menu> callback) {
        var queue = new LinkedList<>(menuBar.getMenus());
        while (queue.size() > 0) {
            var menu = queue.pop();
            if (accept.apply(menu))
                callback.accept(menu);
            for (var item : menu.getItems()) {
                if (item instanceof Menu other)
                    queue.add(other);
            }
        }
    }


    public static ScrollBar getScrollBar(Node scrollable, Orientation orientation) {
        var nodes = scrollable.lookupAll(".scroll-bar");
        var node = nodes.stream()
                .filter(v -> ((ScrollBar) v).getOrientation().equals(orientation))
                .findAny();
        if (node.isPresent() && node.get() instanceof ScrollBar scrollBar)
            return scrollBar;
        else
            return null;
    }

    /**
     * determines whether a scroll-bar is visible
     *
     * @return true, if visible
     */
    public static boolean isScrollBarVisible(ScrollPane scrollPane, Orientation orientation) {
        var scrollBar = getScrollBar(scrollPane, orientation);
        return scrollBar != null && scrollBar.isVisible();
    }

    /**
     * fill the given rectangle with the given color
     */
    public static void fillRectangle(WritableImage image, double x0, double y0, double w, double h, Color color) {
        w = Math.min(image.getWidth(), (int) Math.round(x0 + w)); // use max, not width
        h = Math.min(image.getHeight(), (int) Math.round(y0 + h)); // use max, not height
        var pixelWriter = image.getPixelWriter();
        for (var x = Math.max(0, (int) Math.round(x0)); x < w; x++) {
            for (var y = Math.max(0, (int) Math.round(y0)); y < h; y++) {
                pixelWriter.setColor(x, y, color);
            }
        }
    }

}
