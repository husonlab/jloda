/*
 * IMainWindow.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.window;

import javafx.stage.Stage;

/**
 * manageable main window
 * Daniel Huson, 3.2019
 */
public interface IMainWindow {
    /**
     * get the stage for this window
     *
	 */
    Stage getStage();

    /**
     * create a new, empty window
     *
     * @return window
     */
    IMainWindow createNew();

    /**
     * show this window in the given stage
     *
	 */
    void show(Stage stage, double screenX, double screenY, double width, double height);

    /**
     * is this window empty?
     *
	 */
    boolean isEmpty();

    /**
     * perform last closing duties
     */
    void close();

}
