/*
 * ISearcher.java Copyright (C) 2019. Daniel H. Huson
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
 * Base interface for searchers
 * Daniel Huson, 7.2008
 */
public interface ISearcher {
    /**
     * get the name for this type of search
     *
     * @return name
     */
    String getName();

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    boolean isGlobalFindable();

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    boolean isSelectionFindable();

    /**
     * something has been changed or selected, update tree
     */
    void updateView();

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    boolean canFindAll();

    /**
     * set select state of all objects
     *
     * @param select
     */
    void selectAll(boolean select);

    /**
     * get the parent component
     *
     * @return parent
     */
    Component getParent();

    /**
     * get list of additional buttons to be embedded into find tool bar, or null
     */
    Collection<AbstractButton> getAdditionalButtons();

}
