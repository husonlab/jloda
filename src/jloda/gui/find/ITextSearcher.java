/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
