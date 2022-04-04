/*
 * DraggableUtils.java Copyright (C) 2022. Daniel H. Huson
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

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * utilities for making nodes draggable
 * Daniel Huson, 4.2022
 */
public class DraggableUtils {
	private static double mouseX;
	private static double mouseY;

	private static final EventHandler<? super MouseEvent> mousePressedHander;
	private static final EventHandler<? super MouseEvent> mouseDraggedHandlerTranslate;
	private static final EventHandler<? super MouseEvent> mouseDraggedHandlerLayout;

	static {
		mousePressedHander = e -> {
			mouseX = e.getSceneX();
			mouseY = e.getSceneY();
		};
		mouseDraggedHandlerTranslate = e -> {
			if (e.getSource() instanceof Node aNode) {
				var dx = e.getSceneX() - mouseX;
				var dy = e.getSceneY() - mouseY;
				aNode.setTranslateX(aNode.getTranslateX() + dx);
				aNode.setTranslateY(aNode.getTranslateY() + dy);
				mouseX = e.getSceneX();
				mouseY = e.getSceneY();
				e.consume();
			}
		};
		mouseDraggedHandlerLayout = e -> {
			if (e.getSource() instanceof Node aNode) {
				var dx = e.getSceneX() - mouseX;
				var dy = e.getSceneY() - mouseY;
				aNode.setLayoutX(aNode.getLayoutX() + dx);
				aNode.setLayoutY(aNode.getLayoutY() + dy);
				mouseX = e.getSceneX();
				mouseY = e.getSceneY();
				e.consume();
			}
		};

	}

	public static void setupDragMouseTranslate(Node node) {
		node.setOnMousePressed(mousePressedHander);
		node.setOnMouseDragged(mouseDraggedHandlerTranslate);
	}

	public static void setupDragMouseLayout(Node node) {
		node.setOnMousePressed(mousePressedHander);
		node.setOnMouseDragged(mouseDraggedHandlerLayout);
	}
}
