/*
 * SetParameterDialog.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.fx.dialog;

import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;
import jloda.util.ProgramProperties;

import java.util.Collection;

/**
 * simple parameter choice dialog
 * Daniel Huson, 11.2021
 */
public class SetParameterDialog {
    /**
     * quest parameter from user
     *
     * @return value or null
     */
    public static <S> S apply(Stage parent, String message, Collection<S> values, S defaultValue) {
        final ChoiceDialog<S> dialog = new ChoiceDialog<>(defaultValue, values);
        if (parent != null) {
            dialog.setX(Math.max(parent.getX(), parent.getX() + 0.5 * parent.getWidth() - 200));
            dialog.setY(Math.max(parent.getY(), parent.getY() + 0.5 * parent.getHeight() - 200));
        }
        dialog.setTitle("Set Parameter - " + ProgramProperties.getProgramName());
        dialog.setHeaderText(message);
        dialog.setContentText("Choose the value:");

        return dialog.showAndWait().orElse(null);
    }
}
