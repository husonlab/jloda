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
