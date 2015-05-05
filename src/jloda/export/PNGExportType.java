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

import jloda.util.Basic;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author Daniel Huson, Michael Schroeder
 */
public class PNGExportType extends FileFilter implements ExportGraphicType {

    /**
     * the mime type of this exportfile type
     */
    private final String mimeType = "image/png";
    /**
     * the DataFlavor supported by this exportfile type
     */
    private final DataFlavor flavor;

    public PNGExportType() {
        flavor = new DataFlavor(mimeType + ";class=jloda.export.PNGExportType", "png image");
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataFlavor getDataFlavor() {
        return flavor;
    }

    /**
     * <code>getData</code>: <i>currently not implemented since clipboard export of
     * png images is not intended.</i>
     *
     * @param panel
     * @return
     */
    public Object getData(JPanel panel) {
        return null;
    }

    public static void stream(JPanel panel, ByteArrayOutputStream out) throws IOException {
        (new PNGExportType()).stream(panel, null, false, out);
    }

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
    public void stream(JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage, OutputStream out) throws IOException {
        JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        panel.paint(img.getGraphics());
        ImageIO.write(img, "png", out);
    }

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
    public void writeToFile(File file, final JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage) throws IOException {
        JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        panel.paint(img.getGraphics());

        ImageIO.write(img, "png", out);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(out.toByteArray());
        fos.close();
    }

    /**
     * writes the image as a gif file.
     *
     * @param file  the file to write to.
     * @param panel the panel which paints the image.
     */
    public static void writeToFile(File file, JPanel panel) throws IOException {
        (new PNGExportType()).writeToFile(file, panel, null, false);
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = Basic.getSuffix(f.getName());
        if (extension != null) {
            if (extension.equalsIgnoreCase("png"))
                return true;
        } else {
            return false;
        }
        return false;
    }

    public String getDescription() {
        return "PNG (*.png)";
    }

    public String toString() {
        return getDescription();
    }

    /**
     * gets the associated file filter and filename filter
     *
     * @return filename filter
     */
    public jloda.util.FileFilter getFileFilter() {
        return new jloda.util.FileFilter(getFileExtension());
    }

    public String getFileExtension() {
        return ".png";
    }
}
