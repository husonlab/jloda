/*
 * JPGExportType.java Copyright (C) 2023 Daniel H. Huson
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
 * The export filetype for jpg images.
 *
 * @author Daniel Huson, Michael Schroeder
 */
public class JPGExportType extends FileFilter implements ExportGraphicType {

    /**
     * the mime type of this exportfile type
     */
    private final String mimeType = "image/jpeg";
    /**
     * the DataFlavor supported by this exportfile type
     */
    private final DataFlavor flavor;

    public JPGExportType() {
        flavor = new DataFlavor(mimeType + ";class=jloda.swing.export.JPGExportType", "jpeg image");
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataFlavor getDataFlavor() {
        return flavor;
    }

    /**
     * <code>getData</code>: <i>currently not implemented since clipboard export of
     * jpg images is not intended.</i>
     *
	 */
    public Object getData(JPanel panel) {
        return null;
    }

    public static void stream(JPanel panel, ByteArrayOutputStream out) throws IOException {
        (new JPGExportType()).stream(panel, null, false, out);
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
        ImageIO.write(img, "jpg", out);
    }

    /**
     * writes image to file. If scrollPane given and showWholeImage=true, draws only visible portion
     * of panel
     *
	 */
    public void writeToFile(File file, final JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage) throws IOException {
        final JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
            panel.paint(img.getGraphics());
            ImageIO.write(img, "jpg", out);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(out.toByteArray());
            }
        }
    }

    /**
     * writes the image as a jpg file.
     *
     * @param file  the file to write to.
     * @param panel the panel which paints the image.
     */
    public static void writeToFile(File file, JPanel panel) throws IOException {
        (new JPGExportType()).writeToFile(file, panel, null, false);
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

		String extension = FileUtils.getFileSuffix(f.getName());
        if (extension != null) {
            return extension.equalsIgnoreCase("jpeg") ||
                    extension.equalsIgnoreCase("jpg");
        } else {
            return false;
        }
    }

    public String getDescription() {
        return "JPEG (*.jpg, *.jpeg)";
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
        jloda.swing.util.FileFilter filter = new jloda.swing.util.FileFilter(getFileExtension());
        filter.getFileExtensions().add(".jpeg");
        return filter;
    }

    public String getFileExtension() {
        return ".jpg";
    }
}
