/*
 * MouseDragClosestNode.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import java.util.function.BiConsumer;

/**
 * maintains a draggable node
 * Daniel Huson, 1.2020
 */
public class MouseDragClosestNode {
    private double mouseDownX = 0;
    private double mouseDownY = 0;

    private double mouseX = 0;
    private double mouseY = 0;

    private static boolean moved;
    private Node target;

    public static void setup(boolean useTouch, Node node, ReadOnlyBooleanProperty selected, Node reference1, Node target1, Node reference2, Node target2, BiConsumer<Node, Point2D> totalTranslation) {
        new MouseDragClosestNode(useTouch, node, selected, reference1, target1, reference2, target2, totalTranslation);
    }

    /**
     * constructor
     */
    private MouseDragClosestNode(boolean useTouch, Node node, ReadOnlyBooleanProperty selected, Node reference1, Node target1, Node reference2, Node target2, BiConsumer<Node, Point2D> totalTranslation2) {

        if (useTouch) {
            node.setOnTouchPressed(e -> {
                if (selected.get() && e.getTouchCount() == 1) {
                    mouseDownX = mouseX = e.getTouchPoint().getScreenX();
                    mouseDownY = mouseY = e.getTouchPoint().getScreenY();
                    moved = false;

                    var screenBounds1 = reference1.localToScreen(reference1.getBoundsInLocal());
                    var distance1 = (new Point2D(screenBounds1.getCenterX(), screenBounds1.getCenterY())).distance(mouseX, mouseY);
                    var screenBounds2 = reference2.localToScreen(reference2.getBoundsInLocal());
                    var distance2 = (new Point2D(screenBounds2.getCenterX(), screenBounds2.getCenterY())).distance(mouseX, mouseY);
                    if (distance1 <= distance2)
                        target = target1;
                    else
                        target = target2;
                }
                e.consume();
            });

            node.setOnTouchMoved(e -> {
                if (selected.get()) {
                    target.setTranslateX(target.getTranslateX() + (e.getTouchPoint().getScreenX() - mouseX));
                    target.setTranslateY(target.getTranslateY() + (e.getTouchPoint().getScreenY() - mouseY));
                    mouseX = e.getTouchPoint().getScreenX();
                    mouseY = e.getTouchPoint().getScreenY();
                    moved = true;
                }
                e.consume();
            });

            node.setOnTouchReleased(e -> {
                if (selected.get() && moved) {
                    var dx = e.getTouchPoint().getScreenX() - mouseDownX;
                    var dy = e.getTouchPoint().getScreenY() - mouseDownY;
                    if (dx != 0 && dy != 0)
                        totalTranslation2.accept(target, new Point2D(dx, dy));
                }
                e.consume();
            });
        } else {
            node.setOnMousePressed(e -> {
                if (selected.get()) {
                    mouseDownX = mouseX = e.getScreenX();
                    mouseDownY = mouseY = e.getScreenY();
                    moved = false;

                    var screenBounds1 = reference1.localToScreen(reference1.getBoundsInLocal());
                    var distance1 = (new Point2D(screenBounds1.getCenterX(), screenBounds1.getCenterY())).distance(mouseX, mouseY);
                    var screenBounds2 = reference2.localToScreen(reference2.getBoundsInLocal());
                    var distance2 = (new Point2D(screenBounds2.getCenterX(), screenBounds2.getCenterY())).distance(mouseX, mouseY);
                    if (distance1 <= distance2)
                        target = target1;
                    else
                        target = target2;
                }
                e.consume();
            });

            node.setOnMouseDragged(e -> {
                if (selected.get()) {
                    target.setTranslateX(target.getTranslateX() + (e.getScreenX() - mouseX));
                    target.setTranslateY(target.getTranslateY() + (e.getScreenY() - mouseY));
                    mouseX = e.getScreenX();
                    mouseY = e.getScreenY();
                    moved = true;
                }
                e.consume();
            });

            node.setOnMouseReleased(e -> {
                if (selected.get() && moved) {
                    var dx = e.getScreenX() - mouseDownX;
                    var dy = e.getScreenY() - mouseDownY;
                    if (dx != 0 && dy != 0)
                        totalTranslation2.accept(target, new Point2D(dx, dy));
                }
                e.consume();
            });
        }
    }

    public static boolean wasMoved() {
        boolean result = moved;
        moved = false;
        return result;
    }
}
