/**
 * EmptySearcher.java 
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
 * empty searcher
 * Daniel Huson, 4.2014
 */
public class EmptySearcher implements ISearcher {
    /**
     * get the name for this type of search
     *
     * @return name
     */
    @Override
    public String getName() {
        return "Null";
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    @Override
    public boolean isGlobalFindable() {
        return false;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    @Override
    public boolean isSelectionFindable() {
        return false;
    }

    /**
     * something has been changed or selected, update view
     */
    @Override
    public void updateView() {

    }

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    @Override
    public boolean canFindAll() {
        return false;
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    @Override
    public void selectAll(boolean select) {

    }

    /**
     * get the parent component
     *
     * @return parent
     */
    @Override
    public Component getParent() {
        return null;
    }

    /**
     * get list of additional buttons to be embedded into find tool bar, or null
     */
    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }
}
