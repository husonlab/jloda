/*
 * MouseDragToTranslate.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.scene.Node;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * maintains a draggable node
 * Daniel Huson, 1.2020
 */
public class MouseDragToTranslate {
    private double mouseX = 0;
    private double mouseY = 0;
    private final BiConsumer<Double, Double> translate;

    public static void setup(Node node) {
        setup(node, (deltaX, deltaY) -> {
            node.setTranslateX(node.getTranslateX() + deltaX);
            node.setTranslateY(node.getTranslateY() + deltaY);
        });
    }

    public static void setup(Node node, BiConsumer<Double, Double> translate) {
        new MouseDragToTranslate(node, translate);
    }

    /**
     * constructor
     */
    private MouseDragToTranslate(Node node, BiConsumer<Double, Double> translate) {
        this.translate = Objects.requireNonNullElseGet(translate, () -> (deltaX, deltaY) -> {
            node.setTranslateX(node.getTranslateX() + deltaX);
            node.setTranslateY(node.getTranslateY() + deltaY);
        });

        node.setOnMousePressed((e -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        }));

        node.setOnMouseDragged((e -> {
            this.translate.accept(e.getScreenX() - mouseX, e.getScreenY() - mouseY);
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        }));
    }
}
