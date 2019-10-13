/*
 * SingleStringSearcher.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.find;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * single string searcher
 * Daniel Huson, 4.2013
 */
public class SingleStringSearcher implements IObjectSearcher {
    private final Component parent;
    private final String string;
    private final BooleanProperty selected;

    private boolean isSet = false;

    /**
     * constructor
     */
    public SingleStringSearcher(Component parent, String string, BooleanProperty selected) {
        this.parent = parent;
        this.string = string;
        this.selected = selected;
    }


    @Override
    public String getName() {
        return "Alignments";
    }

    @Override
    public boolean gotoFirst() {
        isSet = true;
        return true;
    }

    @Override
    public boolean gotoNext() {
        isSet = false;
        return false;
    }


    @Override
    public boolean isGlobalFindable() {
        return true;
    }

    @Override
    public boolean gotoLast() {
        isSet = true;
        return true;
    }

    @Override
    public boolean isSelectionFindable() {
        return false;
    }

    @Override
    public boolean gotoPrevious() {
        isSet = false;
        return false;
    }

    @Override
    public void updateView() {
    }

    @Override
    public boolean isCurrentSet() {
        return isSet;
    }

    @Override
    public boolean canFindAll() {
        return true;
    }

    @Override
    public boolean isCurrentSelected() {
        return isSet && selected.get();
    }

    @Override
    public void selectAll(final boolean select) {
        setCurrentSelected(select);
    }

    @Override
    public void setCurrentSelected(final boolean select) {
        Runnable runnable = () -> selected.set(select);
        if (Platform.isFxApplicationThread())
            runnable.run();
        else
            Platform.runLater(runnable);
    }

    @Override
    public Component getParent() {
        return parent;
    }

    @Override
    public String getCurrentLabel() {
        if (isSet)
            return string;
        else
            return null;
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }

    @Override
    public void setCurrentLabel(String newLabel) {
    }

    @Override
    public int numberOfObjects() {
        return 1;
    }

}
