/*
 * RememberingComboBox.java Copyright (C) 2024 Daniel H. Huson
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

package jloda.swing.util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * remembering combo box
 * bryant, huson
 * Date: Feb 1, 2006
 */
public class RememberingComboBox extends JComboBox<String> {
    private final BasicComboBoxEditor editor;

    /**
     * constructor
     */
    public RememberingComboBox() {
        super();
        setEnabled(true);
        setEditable(true);
        //clearHistory();

        editor = new BasicComboBoxEditor();
        setEditor(editor);

        editor.getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent keyEvent) {
                if (!RememberingComboBox.this.getBackground().equals(Color.WHITE))
                    RememberingComboBox.this.setBackground(Color.WHITE);
            }
        });
    }

    public Color getBackground() {
        if (editor != null)
            return editor.getEditorComponent().getBackground();
        else
            return super.getBackground();
    }

    public void setBackground(Color background) {
        if (editor != null)
            editor.getEditorComponent().setBackground(background);
        else
            super.setBackground(background);
    }

    public Color getForeground() {
        if (editor != null)
            return editor.getEditorComponent().getForeground();
        else
            return super.getForeground();
    }

    public void setForeground(Color foreground) {
        if (editor != null)
            editor.getEditorComponent().setForeground(foreground);
        else
            super.setForeground(foreground);
    }

    /**
     * Gets the current typed text. If save is true, then this is inserted into the list, after removing any
     * duplicate entries.
     *
     * @return current text
     */
    public String getCurrentText(boolean save) {
        String newEntry = null;
        if (getSelectedItem() != null)
            newEntry = getSelectedItem().toString();

        if (newEntry == null)
            newEntry = "";
        if (save && newEntry.length() > 0) {
            //Check to see if it already appears. If it does, remove, as well as any null entires. Then insert item at start of list.
            int index = 0;
            while (index < getItemCount()) {
                String thisEntry = getItemAt(index);
                if (thisEntry.length() == 0)
                    removeItemAt(index);
                else if (thisEntry.equals(newEntry))
                    removeItemAt(index);
                else
                    index++;
            }

            insertItemAt(newEntry, 0);
            setSelectedIndex(0);
        }
        return newEntry;
    }

    /**
     * Removes all entries and sets current text to ""
     */
    public void clearHistory() {
        removeAllItems();
        addItem("");
    }

    public void addItems(Collection<String> items) {
        items.stream().filter(s -> s.length() > 0).forEach(this::addItem);
    }

    /**
     * gets the list of items
     *
     * @return first maxNumber items
     */
    public List<String> getItems(int maxNumber) {
        maxNumber = Math.min(maxNumber, this.getItemCount());

        final List<String> list = new ArrayList<>(maxNumber);
        for (int i = 0; i < maxNumber; i++) {
            list.add(getItemAt(i));
        }
        return list;
    }

    /**
     * add a list of sep-separated items
     *
	 */
    public void addItemsFromString(String str, String sep) {
        if (str != null && str.length() > 0)
            for (StringTokenizer tok = new StringTokenizer(str, sep); tok.hasMoreElements(); ) {
                String string = tok.nextToken();
                if (string.length() > 0)
                    addItem(string);
            }
        //  if (getItemCount() > 0)
        //    setSelectedIndex(0);
    }

    /**
     * gets the list of items as a sep-separated string
     *
     * @return first maxNumber items
     */
    public String getItemsAsString(int maxNumber, String sep) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < Math.min(maxNumber, this.getItemCount()); i++) {
            buf.append(getItemAt(i));
            buf.append(sep);
        }
        return buf.toString();
    }
}
