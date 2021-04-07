/*
 * DraggableLabel.java Copyright (C) 2021. Daniel H. Huson
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

import javafx.beans.property.BooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * maintains a draggable label
 * Daniel Huson, 5.2018
 */
public class DraggableLabel {
    private final Text text = new Text();
    private final AnchorPane anchorPane;

    private double mouseX = 0;
    private double mouseY = 0;

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

        text.setOnMousePressed((e -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            text.setCursor(Cursor.CLOSED_HAND);
            e.consume();
        }));

        text.setOnMouseDragged((e -> {
            double deltaX = e.getScreenX() - mouseX;
            double deltaY = e.getScreenY() - mouseY;
            AnchorPane.setRightAnchor(text, AnchorPane.getRightAnchor(text) - deltaX);
            AnchorPane.setTopAnchor(text, AnchorPane.getTopAnchor(text) + deltaY);
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        }));

        text.setOnMouseReleased(e -> text.setCursor(Cursor.DEFAULT));

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
}
