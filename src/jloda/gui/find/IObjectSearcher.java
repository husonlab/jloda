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
 * implement this interface to support the Find and Find-Replace dialogs
 * Daniel Huson, 7.2008
 */
public interface IObjectSearcher extends ISearcher {

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
     * @param select
     */
    void setCurrentSelected(boolean select);

    /**
     * get the label of the current object
     *
     * @return label
     */
    String getCurrentLabel();

    /**
     * set the label of the current object
     *
     * @param newLabel
     */
    void setCurrentLabel(String newLabel);

    /**
     * how many objects are there?
     *
     * @return number of objects or -1
     */
    int numberOfObjects();
}

