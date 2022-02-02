/*
 * IObjectSearcher.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.find;

import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.function.Function;

/**
 * implement this interface to support the Find and Find-Replace dialogs
 * Daniel Huson, 7.2016
 */
public interface IObjectSearcher<S> extends ISearcher {

    /**
     * goto the first object
     *
     * @return true, if successful
     */
    boolean gotoFirst();

    /**
     * goto the next object
     *
     * @return true, if successful
     */
    boolean gotoNext();

    /**
     * goto the last object
     *
     * @return true, if successful
     */
    boolean gotoLast();

    /**
     * goto the previous object
     *
     * @return true, if successful
     */
    boolean gotoPrevious();

    /**
     * is the current object set?
     *
     * @return true, if set
     */
    boolean isCurrentSet();

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    boolean isCurrentSelected();

    /**
     * set selection state of current object
     *
	 */
    void setCurrentSelected(boolean select);

    /**
     * get the label of the current object
     *
     * @return label
     */
    String getCurrentLabel();

    /**
     * provides a function that is applied to text label just before replace is performed
     *
     * @return updated label
     */
    Function<String, String> getPrepareTextForReplaceFunction();

    /**
     * set the label of the current object
     *
	 */
    void setCurrentLabel(String newLabel);

    /**
     * how many objects are there?
     *
     * @return number of objects or -1
     */
    int numberOfObjects();

    /**
     * most recent node found or null
     *
     * @return last node found or null
     */
    ReadOnlyObjectProperty<S> foundProperty();
}

