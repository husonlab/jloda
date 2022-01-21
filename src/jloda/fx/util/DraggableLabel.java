/*
 * DraggableLabel.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.property.BooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import jloda.util.Pair;

/**
 * maintains a draggable label
 * Daniel Huson, 5.2018
 */
public class DraggableLabel {
	private final Text text = new Text();
	private final AnchorPane anchorPane;

	private final BooleanProperty visible;

	/**
	 * constructor
	 */
	public DraggableLabel(AnchorPane anchorPane) {
		this.anchorPane = (anchorPane != null ? anchorPane : new AnchorPane());

		visible = text.visibleProperty();

		text.setFont(Font.font("Arial", 10));

		AnchorPane.setRightAnchor(text, 5.0);
		AnchorPane.setTopAnchor(text, 5.0);
		this.anchorPane.getChildren().add(text);

		makeDraggable(text);
	}

	public String getText() {
		return text.getText();
	}

	public void setText(String text) {
		this.text.setText(text);
	}

	public Text get() {
		return text;
	}

	public boolean getVisible() {
		return visible.get();
	}

	public BooleanProperty visibleProperty() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible.set(visible);
	}

	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	/**
	 * if node is contained in an anchor pane, makes it click-draggable
	 *
	 * @param node contained in anchor pane
	 */
	public static void makeDraggable(Node node) {
		var right = AnchorPane.getRightAnchor(node);
		var left = AnchorPane.getLeftAnchor(node);
		var top = AnchorPane.getTopAnchor(node);
		var bottom = AnchorPane.getBottomAnchor(node);

		if ((right == null || left == null) && (top == null || bottom == null)) {
			final var mouseDown = new Pair<Double, Double>();

			node.setOnMousePressed((e -> {
				mouseDown.set(e.getScreenX(), e.getScreenY());
				node.setCursor(Cursor.CLOSED_HAND);
				e.consume();
			}));

			node.setOnMouseDragged((e -> {
				double deltaX = e.getScreenX() - mouseDown.getFirst();
				double deltaY = e.getScreenY() - mouseDown.getSecond();
				if (right != null)
					AnchorPane.setRightAnchor(node, AnchorPane.getRightAnchor(node) - deltaX);
				if (left != null)
					AnchorPane.setLeftAnchor(node, AnchorPane.getLeftAnchor(node) + deltaX);
				if (top != null)
					AnchorPane.setTopAnchor(node, AnchorPane.getTopAnchor(node) + deltaY);
				if (bottom != null)
					AnchorPane.setBottomAnchor(node, AnchorPane.getBottomAnchor(node) - deltaY);
				mouseDown.set(e.getScreenX(), e.getScreenY());
				e.consume();
			}));

			node.setOnMouseReleased(e -> node.setCursor(Cursor.DEFAULT));
		}
	}
}
