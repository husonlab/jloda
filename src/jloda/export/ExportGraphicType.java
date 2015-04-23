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
