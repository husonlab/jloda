/*
 * TransferableGraphic.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.export;

import jloda.swing.util.Alert;

import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Transferable for exporting graphics to the clipboard.
 * To add a new export type, implement the <code>jloda.swing.export.ExportGraphicType</code>
 * interface and add it to the addCommonTypes or addCustomTypes() method.
 *
 * @author huson, schroeder
 */
public class TransferableGraphic implements ClipboardOwner, Transferable {
    private final Map<DataFlavor, ExportGraphicType> types = new HashMap<>();

    /**
     * the JPanel doing the paint work
     */
    private final JPanel panel;

    public TransferableGraphic(JPanel panel) {
        this(panel, null);
    }

    public TransferableGraphic(JPanel panel, JScrollPane scrollPane) {
        if (scrollPane != null)
            this.panel = ExportManager.makePanelFromScrollPane(panel, scrollPane);
        else
            this.panel = panel;
        addCommonTypes();
        //addCustomTypes();
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }


    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = new DataFlavor[types.size()];
        types.keySet().toArray(flavors);
        return flavors;
    }


    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return types.containsKey(flavor);
    }

    /**
     * get the transfer data from supported exportTypes
     *
     * @param dataFlavor the requested dataFlavor
     * @return the data to be transferred to the clipboard
     * @throws UnsupportedFlavorException
     * @throws IOException
     */
    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {

        ExportGraphicType type = types.get(dataFlavor);
        if (type != null) {
            return type.getData(panel);
        } else {
            throw new UnsupportedFlavorException(dataFlavor);
        }
    }

    /**
     * add types which don't need to be added to the native-mime mapping.
     */
    private void addCommonTypes() {

        ExportGraphicType renderedType = new RenderedExportType();
        types.put(renderedType.getDataFlavor(), renderedType);
    }

    /**
     * add exportTypes which alter the mapping of native clipboard-types
     * to mime types.
     */
    private void addCustomTypes() {

        addType("Encapsulated PostScript", "image/x-eps",
                "EPS graphic",
                "jloda.swing.export.EPSExportType");
    }

    /**
     * add exportType to native-mime mapping.
     *
     * @param atom        name of the type in native clipboard
     * @param mimeType    the mime type
     * @param description human-readable name
     * @param className   the corresponding java class
     */
    private void addType(String atom, String mimeType, String description, String className) {

        try {
            DataFlavor df = new DataFlavor(mimeType, description);
            SystemFlavorMap map = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
            map.addUnencodedNativeForFlavor(df, atom);

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class cls = loader == null ? Class.forName(className) : loader.loadClass(className);
            ExportGraphicType type = (ExportGraphicType) cls.getConstructor().newInstance();
            types.put(df, type);

        } catch (Throwable x) {
            new Alert("Unable to install flavor for mime type '" + mimeType + "'");
        }
    }
}
