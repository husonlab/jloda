/*
 * PNGExportType.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.util.Basic;
import jloda.util.ProgramProperties;

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
        flavor = new DataFlavor(mimeType + ";class=jloda.swing.export.PNGExportType", "png image");
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
        final JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            System.err.println("(Export panel size: " + panel.getWidth() + " x " + panel.getHeight() + ")"); // todo: debugging for weird giant panel bug
            int width = panel.getWidth();
            int height = panel.getHeight();
            if (width <= 0 || width >= 100000) {
                System.err.println("Invalid width=" + width + ", setting to: " + ProgramProperties.get("PNGExportFixWidth", 1000) + " (setprop PNGExportFixWidth to change)");
                width = ProgramProperties.get("PNGExportFixWidth", 1000);
            }
            if (height <= 0 || height >= 100000) {
                System.err.println("Invalid height=" + height + ", setting to: " + ProgramProperties.get("PNGExportFixHeight", 1000) + " (setprop PNGExportFixHeight to change)");
                height = ProgramProperties.get("PNGExportFixHeight", 1000);
            }

            final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            panel.paint(img.getGraphics());
            ImageIO.write(img, "png", out);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(out.toByteArray());
            }
        }
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
        String extension = Basic.getFileSuffix(f.getName());
        if (extension != null) {
            return extension.equalsIgnoreCase("png");
        } else {
            return false;
        }
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
    public jloda.swing.util.FileFilter getFileFilter() {
        return new jloda.swing.util.FileFilter(getFileExtension());
    }

    public String getFileExtension() {
        return ".png";
    }
}
