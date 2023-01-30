/*
 * RenderedExportType.java Copyright (C) 2023 Daniel H. Huson
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

package jloda.swing.export;

import jloda.util.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * a pixel-based export type.
 * Since the DataFlavor is DataFlavor.imageFlavor, the JVM will
 * take care of the mapping to native clipboard types (e.g. WIN32: BMP, MAC OS: PICT).
 *
 * @author huson, schroeder
 */
public class RenderedExportType extends FileFilter implements ExportGraphicType {

    /**
     * the mime type of this exportfile type
     */
    private final String mimeType = DataFlavor.imageFlavor.getMimeType();
    /**
     * the DataFlavor supported by this exportfile type
     */
    private final DataFlavor flavor;

    public RenderedExportType() {
        flavor = DataFlavor.imageFlavor;
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataFlavor getDataFlavor() {
        return flavor;
    }

    public Object getData(JPanel panel) {
        /*
        Image img = panel.createImage(panel.getWidth(), panel.getHeight());
        Graphics g = img.getGraphics();

        panel.paint(g);
        g.dispose();
        */
        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        panel.paint(img.getGraphics());

        return img;

    }

    public static void stream(JPanel panel, ByteArrayOutputStream out) throws IOException {
        (new RenderedExportType()).stream(panel, null, false, out);
    }

    /**
     * writes image to a stream. If scrollPane given and showWholeImage=true, draws only visible portion
     * of panel
     *
	 */
    public void stream(JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage, OutputStream out) throws IOException {
        final JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);
        final BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        panel.paint(img.getGraphics());
        ImageIO.write(img, "bmp", out);
    }

    public void writeToFile(File file, final JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage) throws IOException {
        final JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            panel.paint(img.getGraphics());

            ImageIO.write(img, "bmp", out);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(out.toByteArray());
            }
        }
    }

    /**
     * writes the image in the bmp file format.
     *
	 */
    public void writeToFile(File file, JPanel panel) throws IOException {
        (new RenderedExportType()).writeToFile(file, panel, null, false);
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
		String extension = FileUtils.getFileSuffix(f.getName());
        if (extension != null) {
            return extension.equalsIgnoreCase(".eps");
        } else {
            return false;
        }
    }

    public String getDescription() {
        return "BMP (*.bmp)";
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
        return ".bmp";
    }
}
