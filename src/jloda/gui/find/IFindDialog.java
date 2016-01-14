/**
 * IFindDialog.java 
 * Copyright (C) 2016 Daniel H. Huson
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
package jloda.gui.find;

import javax.swing.*;
import java.awt.*;

/**
 * command interface for find dialog and find toolbar
 * Daniel Huson, 2.2012
 */
public interface IFindDialog {
    JFrame getFrame();

    boolean selectTarget(String name);

    void chooseTargetForFrame(Component parent);

    SearchActions getActions();

    void setMessage(String message);

    void clearMessage();

    void updateTargets();

    String getFindText();

    String getReplaceText();
}
