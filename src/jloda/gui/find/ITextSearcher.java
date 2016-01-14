/**
 * ITextSearcher.java 
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

/**
 * Interface for text-based searcher
 * Daniel Huson, 7.2008
 */
public interface ITextSearcher extends ISearcher {
    /**
     * Find first instance
     *
     * @param regularExpression
     * @return - returns boolean: true if text found, false otherwise
     */
    boolean findFirst(String regularExpression);

    /**
     * Find next instance
     *
     * @param regularExpression
     * @return - returns boolean: true if text found, false otherwise
     */
    boolean findNext(String regularExpression);


    /**
     * Find previous instance
     *
     * @param regularExpression
     * @return - returns boolean: true if text found, false otherwise
     */
    boolean findPrevious(String regularExpression);

    /**
     * Replace the next instance with current. Does nothing if selection invalid.
     *
     * @param regularExpression
     */
    boolean replaceNext(String regularExpression, String replaceText);


    /**
     * Replace all occurrences of text in document, subject to options.
     *
     * @param regularExpression
     * @param replaceText
     * @param selectionOnly
     * @return number of instances replaced
     */
    int replaceAll(String regularExpression, String replaceText, boolean selectionOnly);

    /**
     * Selects all occurrences of text in document, subject to options and constraints of document type
     *
     * @param regularExpression
     */
    int findAll(String regularExpression);

    /**
     * set scope global rather than selected
     *
     * @param globalScope
     */
    void setGlobalScope(boolean globalScope);

    /**
     * get scope global rather than selected
     *
     * @return true, if search scope is global
     */
    boolean isGlobalScope();
}
