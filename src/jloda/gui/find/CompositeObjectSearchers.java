/**
 * CompositeObjectSearchers.java 
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
import java.util.Collection;

/**
 * Composition of two object searchers
 * Daniel Huson, 4.2013
 */
public class CompositeObjectSearchers implements IObjectSearcher {
    public static final String SEARCHER_NAME = "Composite";
    private final Component frame;
    private final String name;
    private final IObjectSearcher first;
    private final IObjectSearcher second;

    private enum Which {First, Second, None}

    private Which which = Which.None;


    /**
     * constructor
     *
     * @param first
     * @param second
     */
    public CompositeObjectSearchers(String name, Component frame, IObjectSearcher first, IObjectSearcher second) {
        this.name = name;
        this.frame = frame;
        this.first = first;
        this.second = second;
    }

    /**
     * get the parent component
     *
     * @return parent
     */
    public Component getParent() {
        return frame;
    }

    /**
     * get the name for this type of search
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * goto the first object
     */
    public boolean gotoFirst() {
        if (first.gotoFirst()) {
            which = Which.First;
            return true;
        } else if (second.gotoFirst()) {
            which = Which.Second;
            return true;
        }
        which = Which.None;
        return false;
    }

    /**
     * goto the next object
     */
    public boolean gotoNext() {
        if (which == Which.First) {
            if (first.gotoNext())
                return true;
            else if (second.gotoFirst()) {
                which = Which.Second;
                return true;
            } else {
                which = Which.None;
                return false;
            }
        } else if (which == Which.Second) {
            if (second.gotoNext())
                return true;
            else {
                which = Which.None;
                return false;
            }
        } else {
            if (first.gotoFirst()) {
                which = Which.First;
                return true;
            } else if (second.gotoFirst()) {
                which = Which.Second;
                return true;
            } else
                return false;
        }
    }

    /**
     * goto the last object
     */
    public boolean gotoLast() {
        if (second.gotoLast()) {
            which = Which.Second;
            return true;
        } else if (first.gotoLast()) {
            which = Which.First;
            return true;
        }
        which = Which.None;
        return false;
    }

    /**
     * goto the previous object
     */
    public boolean gotoPrevious() {
        if (which == Which.Second) {
            if (second.gotoPrevious())
                return true;
            else if (first.gotoLast()) {
                which = Which.First;
                return true;
            } else {
                which = Which.None;
                return false;
            }
        } else if (which == Which.First) {
            if (first.gotoPrevious())
                return true;
            else {
                which = Which.None;
                return false;
            }
        } else {
            if (second.gotoLast()) {
                which = Which.Second;
                return true;
            } else if (first.gotoLast()) {
                which = Which.First;
                return true;
            } else
                return false;
        }
    }

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    public boolean isCurrentSelected() {
        return which == Which.First && first.isCurrentSelected() || which == Which.Second && second.isCurrentSelected();
    }

    /**
     * set selection state of current object
     *
     * @param select
     */
    public void setCurrentSelected(boolean select) {
        if (which == Which.First)
            first.setCurrentSelected(select);
        else if (which == Which.Second)
            second.setCurrentSelected(select);
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        first.selectAll(select);
        second.selectAll(select);
    }

    /**
     * get the label of the current object
     *
     * @return label
     */
    public String getCurrentLabel() {
        if (which == Which.First)
            return first.getCurrentLabel();
        else if (which == Which.Second)
            return second.getCurrentLabel();
        else
            return null;
    }

    /**
     * set the label of the current object
     *
     * @param newLabel
     */
    public void setCurrentLabel(String newLabel) {
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public boolean isGlobalFindable() {
        return first.isGlobalFindable() || second.isGlobalFindable();
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public boolean isSelectionFindable() {
        return false;
    }

    /**
     * is the current object set?
     *
     * @return true, if set
     */
    public boolean isCurrentSet() {
        return which == Which.First && first.isCurrentSet() || which == Which.Second && second.isCurrentSet();
    }

    /**
     * something has been changed or selected, update view
     */
    public void updateView() {
        first.updateView();
        second.updateView();
    }

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    public boolean canFindAll() {
        return true;
    }

    /**
     * how many objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfObjects() {
        return first.numberOfObjects() + second.numberOfObjects();
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }
}
