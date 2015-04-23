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
import java.util.HashSet;
import java.util.Set;

/**
 * JList searcher
 * Daniel Huson, 7.2012
 */

/**
 * Class for finding labels in a JList
 * Daniel Huson, 2.2012
 */
public class JListSearcher implements IObjectSearcher {
    private final String name;
    final JList jList;
    final Frame frame;
    protected int current = -1;

    final Set<Integer> toSelect;
    final Set<Integer> toDeselect;
    public static final String SEARCHER_NAME = "JList";

    /**
     * constructor
     *
     * @param jList
     */
    public JListSearcher(JList jList) {
        this(null, SEARCHER_NAME, jList);
    }

    /**
     * constructor
     *
     * @param frame
     * @param jList
     */
    public JListSearcher(Frame frame, JList jList) {
        this(frame, SEARCHER_NAME, jList);
    }

    /**
     * constructor
     *
     * @param
     * @param jList
     */
    public JListSearcher(Frame frame, String name, JList jList) {
        this.frame = frame;
        this.name = name;
        this.jList = jList;
        toSelect = new HashSet<Integer>();
        toDeselect = new HashSet<Integer>();
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
        current = 0;
        return isCurrentSet();
    }

    /**
     * goto the next object
     */
    public boolean gotoNext() {
        if (isCurrentSet())
            current++;
        else
            gotoFirst();
        return isCurrentSet();
    }

    /**
     * goto the last object
     */
    public boolean gotoLast() {
        current = jList.getComponentCount() - 1;
        return isCurrentSet();
    }

    /**
     * goto the previous object
     */
    public boolean gotoPrevious() {
        if (isCurrentSet())
            current--;
        else
            gotoLast();
        return isCurrentSet();
    }

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    public boolean isCurrentSelected() {
        if (isCurrentSet()) {
            int[] selected = jList.getSelectedIndices();
            for (int aSelected : selected)
                if (aSelected == current)
                    return true;
        }
        return false;
    }

    /**
     * set selection state of current object
     *
     * @param select
     */
    public void setCurrentSelected(boolean select) {
        if (select)
            toSelect.add(current);
        else
            toDeselect.add(current);
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        if (select) {
            jList.setSelectionInterval(0, jList.getComponentCount());
        } else {
            jList.clearSelection();
        }
    }

    /**
     * get the label of the current object
     *
     * @return label
     */
    public String getCurrentLabel() {
        if (!isCurrentSet())
            return null;
        else
            return jList.getModel().getElementAt(current).toString();
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
        return jList.getComponentCount() > 0;
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
        return current >= 0 && current < jList.getModel().getSize();
    }

    /**
     * something has been changed or selected, update view
     */
    public void updateView() {
        selectAll(false);

        int[] alreadySelected = jList.getSelectedIndices();
        for (int i : alreadySelected) {
            toSelect.add(i);
        }
        toSelect.removeAll(toDeselect);

        int[] indices = new int[toSelect.size()];
        int count = 0;
        for (Integer i : toSelect) {
            indices[count++] = i;
        }
        jList.setSelectedIndices(indices);

        if (isCurrentSet())
            jList.ensureIndexIsVisible(jList.getSelectedIndex());

        toSelect.clear();
        toDeselect.clear();
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
        return jList.getModel().getSize();
    }

    @Override
    public Collection<AbstractButton> getAdditionalButtons() {
        return null;
    }
}
