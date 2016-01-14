/**
 * ExportGraphicType.java 
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
package jloda.export;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author huson, schroeder
 *         interface for export graphics classes, 2004, 5.2006
 */
public interface ExportGraphicType {

    /**
     * get the mime type of this exportfile type.
     *
     * @return the mime type.
     */
    String getMimeType();

    /**
     * get the <code>DataFlavor</code> supported by this exportfile type.
     *
     * @return the supported <code>DataFlavor</code>
     */
    DataFlavor getDataFlavor();

    /**
     * return the image data in a specific format.
     *
     * @param panel the <code>JPanel</code> which paints the image data.
     * @return the image data in a specific format.
     */
    Object getData(JPanel panel);

    /**
     * gets the associated file filter
     *
     * @return filter
     */
    jloda.util.FileFilter getFileFilter();

    /**
     * gets the associated file extension for this file
     *
     * @return extension
     */
    String getFileExtension();

    /**
     * writes image to a stream. If scrollPane given and showWholeImage=true, draws only visible portion
     * of panel
     *
     * @param imagePanel
     * @param imageScrollPane
     * @param showWholeImage
     * @param out
     * @throws IOException
     */
    void stream(JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage, OutputStream out) throws IOException;

    /**
     * writes image to file. If scrollPane given and showWholeImage=true, draws only visible portion
     * of panel
     *
     * @param file
     * @param imagePanel
     * @param imageScrollPane
     * @param showWholeImage
     * @throws IOException
     */
    void writeToFile(File file, JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage) throws IOException;
}
