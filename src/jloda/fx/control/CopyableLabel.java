/*
 * CopyableLabel.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.control;

import javafx.scene.control.TextField;
import javafx.scene.text.Font;

/**
 * a label that can be copied
 * Daniel Huson, 3.2019
 */
public class CopyableLabel extends TextField {
    /**
     * constructor
     */
    public CopyableLabel() {
        this.setFont(Font.font("Courier new", 12));
        this.prefColumnCountProperty().bind(lengthProperty());
        this.maxWidthProperty().bind(this.prefWidthProperty());

        this.setEditable(false);
        this.setFocusTraversable(false);
        setStyle("-fx-background-color: transparent;");

        this.focusedProperty().addListener((v, o, n) -> {
            if (!n)
                this.deselect();
        });

        textProperty().addListener(e -> {
            if (getParent() != null)
                getParent().requestLayout();
        });
    }

    /**
     * constructor
     */
    public CopyableLabel(String text) {
        this();
        setText(text);
    }
}
