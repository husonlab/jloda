/*
 * GIFExportType.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.swing.export.gifEncode.Gif89Encoder;
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
        flavor = new DataFlavor(mimeType + ";class=jloda.swing.export.GIFExportType", "gif89 image");
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

        try (BufferedOutputStream bos = new BufferedOutputStream(out)) {
            final Gif89Encoder enc = new Gif89Encoder(img);
            enc.setTransparentIndex(-1);
            enc.encode(bos);
        }
    }


    public void writeToFile(File file, final JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage) throws IOException {
        final JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);

        final Image img = imagePanel.getGraphicsConfiguration().createCompatibleImage(panel.getWidth(), panel.getHeight());
        final Graphics2D g = (Graphics2D) img.getGraphics();
        panel.paint(g);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            Gif89Encoder enc = new Gif89Encoder(img);
            enc.setTransparentIndex(-1);
            enc.encode(bos);
        }
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
        String extension = Basic.getFileSuffix(f.getName());
        if (extension != null) {
            return extension.equalsIgnoreCase("gif");
        } else {
            return false;
        }
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
    public jloda.swing.util.FileFilter getFileFilter() {
        return new jloda.swing.util.FileFilter(getFileExtension());
    }

    public String getFileExtension() {
        return ".gif";
    }
}
