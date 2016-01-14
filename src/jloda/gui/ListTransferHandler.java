/**
 * ListTransferHandler.java 
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
package jloda.gui;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * list transfer handler
 */
public class ListTransferHandler extends TransferHandler {
    private int[] indices = null;
    private int addIndex = -1;
    private int addCount = 0;

    /**
     * constructor
     */
    public ListTransferHandler() {
    }

    /**
     * export string
     *
     * @param component
     * @return string
     */
    protected String exportString(JComponent component) {
        final JList list = (JList) component;
        this.indices = list.getSelectedIndices();
        final StringBuilder buff = new StringBuilder();
        for (Object obj : list.getSelectedValuesList()) {
            buff.append(obj != null ? obj.toString() : "");
            buff.append("\n");

        }
        return buff.toString();
    }

    /**
     * import a string
     *
     * @param component
     * @param str
     */
    protected void importString(JComponent component, String str) {
        JList target = (JList) component;
        DefaultListModel listModel = (DefaultListModel) target.getModel();
        int targetIndex = target.getSelectedIndex();

        if (this.indices.length > 1)
            if (targetIndex >= this.indices[0] - 1 && targetIndex <= this.indices[this.indices.length - 1]) {
                this.indices = null;
                return;
            }

        int max = listModel.getSize();
        if (targetIndex < 0) {
            targetIndex = max;
        } else {
            if (targetIndex > 0) {
                //targetIndex++;
                if (targetIndex > max)
                    targetIndex = max;
            }
        }
        if (targetIndex - this.indices[0] > 0) //shift downwards
            targetIndex++;

        this.addIndex = targetIndex;
        String[] values = str.split("\n");
        this.addCount = values.length;
        for (String value : values) {
            listModel.add(targetIndex, value);
            targetIndex++;
        }
    }


    /**
     * cleanup
     *
     * @param component
     * @param remove
     */
    protected void cleanup(JComponent component, boolean remove) {

        if (remove && this.indices != null) {
            JList source = (JList) component;
            DefaultListModel m = (DefaultListModel) source.getModel();
            source.clearSelection();

            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly, since those
            //after the insertion point have moved.
            if (this.addCount > 0) {
                for (int i = 0; i < this.indices.length; i++) {
                    if (this.indices[i] > this.addIndex)
                        this.indices[i] += this.addCount;
                }
            }
            for (int i = this.indices.length - 1; i >= 0; i--) {
                m.remove(this.indices[i]);
            }
        }
        this.indices = null;
        this.addCount = 0;
        this.addIndex = -1;
    }

    /**
     * create transferable
     */
    @Override
    protected Transferable createTransferable(JComponent component) {
        return new StringSelection(this.exportString(component));
    }

    /**
     * get source acitons
     */
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    /**
     * import data
     */
    @Override
    public boolean importData(JComponent component, Transferable t) {
        if (this.canImport(component, t.getTransferDataFlavors())) {
            try {
                String str = (String) t.getTransferData(DataFlavor.stringFlavor);
                this.importString(component, str);
                return true;
            } catch (UnsupportedFlavorException | IOException ufe) {
            }
        }
        return false;
    }

    /**
     * finished export
     */
    @Override
    protected void exportDone(JComponent component, Transferable data, int action) {
        this.cleanup(component, action == MOVE);
    }

    /**
     * can we import?
     */
    @Override
    public boolean canImport(JComponent component, DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (DataFlavor.stringFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}
