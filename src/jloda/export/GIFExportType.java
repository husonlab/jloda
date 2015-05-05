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

import jloda.export.gifEncode.Gif89Encoder;
import jloda.util.Basic;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author Daniel Huson, Michael Schroeder
 */
public class GIFExportType extends FileFilter implements ExportGraphicType {

    /**
     * the mime type of this exportfile type
     */
    private final String mimeType = "image/gif";
    /**
     * the DataFlavor supported by this exportfile type
     */
    private final DataFlavor flavor;

    public GIFExportType() {
        flavor = new DataFlavor(mimeType + ";class=jloda.export.GIFExportType", "gif89 image");
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataFlavor getDataFlavor() {
        return flavor;
    }

    /**
     * <code>getData</code>: <i>currently not implemented since clipboard export of
     * gif images is not intended.</i>
     *
     * @param panel
     * @return
     */
    public Object getData(JPanel panel) {
        return null;
    }

    public static void stream(JPanel panel, ByteArrayOutputStream out) throws IOException {
        (new GIFExportType()).stream(panel, null, false, out);
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
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        panel.paint(img.getGraphics());

        BufferedOutputStream bos = new BufferedOutputStream(out);
        Gif89Encoder enc = new Gif89Encoder(img);
        enc.setTransparentIndex(-1);
        enc.encode(bos);
        bos.close();
    }


    public void writeToFile(File file, final JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage) throws IOException {
        JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);

        Image img = imagePanel.getGraphicsConfiguration().createCompatibleImage(panel.getWidth(), panel.getHeight());
        Graphics2D g = (Graphics2D) img.getGraphics();
        panel.paint(g);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        Gif89Encoder enc = new Gif89Encoder(img);
        enc.setTransparentIndex(-1);
        enc.encode(bos);
        bos.close();
    }

    /**
     * writes the image as a gif file.
     *
     * @param file  the file to write to.
     * @param panel the panel which paints the image.
     */
    public static void writeToFile(File file, JPanel panel) throws IOException {
        (new GIFExportType()).writeToFile(file, panel, null, false);
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = Basic.getSuffix(f.getName());
        if (extension != null) {
            if (extension.equalsIgnoreCase("gif"))
                return true;
        } else {
            return false;
        }
        return false;
    }

    public String getDescription() {
        return "GIF (*.gif)";
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
        return ".gif";
    }
}
