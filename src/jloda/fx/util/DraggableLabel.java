/*
 * DraggableLabel.java Copyright (C) 2019. Daniel H. Huson
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
 *
 */

package jloda.fx.util;

import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * maintains a draggable label
 * Daniel Huson, 5.2018
 */
public class DraggableLabel extends AnchorPane {
    private final Text text = new Text();

    private double mouseX = 0;
    private double mouseY = 0;

    /**
     * constructor
     */
    public DraggableLabel() {
        text.setFont(Font.font("Arial", 10));

        AnchorPane.setRightAnchor(text, 5.0);
        AnchorPane.setTopAnchor(text, 5.0);
        getChildren().add(text);

        text.setOnMousePressed((e -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        }));

        text.setOnMouseDragged((e -> {
            double deltaX = e.getScreenX() - mouseX;
            double deltaY = e.getScreenY() - mouseY;
            AnchorPane.setLeftAnchor(text, AnchorPane.getLeftAnchor(text) + deltaX);
            AnchorPane.setBottomAnchor(text, AnchorPane.getBottomAnchor(text) - deltaY);
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        }));
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
}
