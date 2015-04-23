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
 * Base interface for searchers
 * Daniel Huson, 7.2008
 */
public interface ISearcher {
    /**
     * get the name for this type of search
     *
     * @return name
     */
    public String getName();

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
     * something has been changed or selected, update view
     */
    public void updateView();

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    public boolean canFindAll();

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select);

    /**
     * get the parent component
     *
     * @return parent
     */
    public Component getParent();

    /**
     * get list of additional buttons to be embedded into find tool bar, or null
     */
    public Collection<AbstractButton> getAdditionalButtons();

}
