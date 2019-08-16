/*
 * CompositeObjectSearcher.java Copyright (C) 2019. Daniel H. Huson
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

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Composition of multiple object searchers
 * Daniel Huson, 4.2013
 */
public class CompositeObjectSearcher implements IObjectSearcher {
    public static final String SEARCHER_NAME = "Composite";
    private final Component frame;
    private final String name;

    private IObjectSearcher[] searchers;

    private final int None = -1;

    private int whichSearcher = None;

    /**
     * constructor
     *
     * @param searchers
     */
    public CompositeObjectSearcher(String name, Component frame, IObjectSearcher... searchers) {
        this.name = name;
        this.frame = frame;
        this.searchers = searchers;
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
        for (int i = 0; i < searchers.length; i++) {
            if (searchers[i].gotoFirst()) {
                whichSearcher = i;
                return true;
            }
        }
        whichSearcher = None;
        return false;
    }

    /**
     * goto the next object
     */
    public boolean gotoNext() {
        if (whichSearcher == None)
            return false;
        else if (searchers[whichSearcher].gotoNext()) {
            return true;
        }
        else {
            for (int i = whichSearcher + 1; i < searchers.length; i++) {
                if (searchers[i].gotoFirst()) {
                    whichSearcher = i;
                    return true;
                }
            }
            whichSearcher = None;
            return false;
        }
    }

    /**
     * goto the last object
     */
    public boolean gotoLast() {
        for (int i = searchers.length - 1; i >= 0; i--) {
            if (searchers[i].gotoLast()) {
                whichSearcher = i;
                return true;
            }
        }
        whichSearcher = None;
        return false;

    }

    /**
     * goto the previous object
     */
    public boolean gotoPrevious() {
        if (whichSearcher == None)
            return false;
        else if (searchers[whichSearcher].gotoPrevious())
            return true;
        else {
            for (int i = whichSearcher - 1; i >= 0; i--) {
                if (searchers[i].gotoLast()) {
                    whichSearcher = i;
                    return true;
                }
            }
            whichSearcher = None;
            return false;
        }
    }

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    public boolean isCurrentSelected() {
        return whichSearcher != None && searchers[whichSearcher].isCurrentSelected();
    }

    /**
     * set selection state of current object
     *
     * @param select
     */
    public void setCurrentSelected(boolean select) {
        if (whichSearcher != None)
            searchers[whichSearcher].setCurrentSelected(select);
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        for (IObjectSearcher searcher : searchers) {
            searcher.selectAll(select);
        }
    }

    /**
     * get the label of the current object
     *
     * @return label
     */
    public String getCurrentLabel() {
        if (whichSearcher != None) {
            return searchers[whichSearcher].getCurrentLabel();
        }
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
        for (IObjectSearcher searcher : searchers) {
            if (searcher.isGlobalFindable())
                return true;
        }
        return false;
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
        return whichSearcher != None && searchers[whichSearcher].isCurrentSet();
    }

    /**
     * something has been changed or selected, update tree
     */
    public void updateView() {
        for (IObjectSearcher searcher : searchers) {
            searcher.updateView();
        }
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
        int sum = 0;

        for (IObjectSearcher searcher : searchers) {
            int n = searcher.numberOfObjects();
            if (n == -1)
                return -1;
            else
                sum += n;
        }
        return sum;
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }

    /**
     * set the searchers
     *
     * @param searchers
     */
    public void setSearchers(IObjectSearcher... searchers) {
        whichSearcher = None;
        this.searchers = searchers;
    }

    /**
     * get the searchers
     *
     * @return searchers
     */
    public IObjectSearcher[] getSearchers() {
        return searchers;
    }
}
