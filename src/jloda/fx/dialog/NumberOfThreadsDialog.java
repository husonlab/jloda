/*
 * NumberOfThreadsDialog.java Copyright (C) 2020. Daniel H. Huson
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

import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.util.Optional;

/**
 * set number of threads
 * Daniel Huson, 12.2019
 */
public class NumberOfThreadsDialog {
    public static Integer apply(Stage parent, int defaultValue) {

        final TextInputDialog dialog = new TextInputDialog("" + defaultValue);
        dialog.initOwner(parent);
        dialog.setTitle("Set Parameter -" + ProgramProperties.getProgramName());
        dialog.setHeaderText("Set the number of threads to use (max. " + Runtime.getRuntime().availableProcessors() + ")");
        dialog.setContentText("Please enter the new value:");

        final Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && Basic.isInteger(result.get())) {
            return Basic.parseInt(result.get());
        } else
            return null;
    }
}
