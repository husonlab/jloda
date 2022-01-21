/*
 * ColorScalePaneController.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class ColorScalePaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label getTitleLabel;

    @FXML
    private Label getLeftLabel;

    @FXML
    private Pane getPane;

    @FXML
    private Canvas canvas;

    @FXML
    private Label getRightLabel;

    @FXML
    void initialize() {
        assert getTitleLabel != null : "fx:id=\"getTitleLabel\" was not injected: check your FXML file 'ColorScalePane.fxml'.";
        assert getLeftLabel != null : "fx:id=\"getLeftLabel\" was not injected: check your FXML file 'ColorScalePane.fxml'.";
        assert getPane != null : "fx:id=\"getPane\" was not injected: check your FXML file 'ColorScalePane.fxml'.";
        assert canvas != null : "fx:id=\"canvas\" was not injected: check your FXML file 'ColorScalePane.fxml'.";
        assert getRightLabel != null : "fx:id=\"getRightLabel\" was not injected: check your FXML file 'ColorScalePane.fxml'.";

    }

    public Label getGetTitleLabel() {
        return getTitleLabel;
    }

    public Label getGetLeftLabel() {
        return getLeftLabel;
    }

    public Pane getGetPane() {
        return getPane;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Label getGetRightLabel() {
        return getRightLabel;
    }
}
