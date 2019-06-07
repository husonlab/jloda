/*
 * SVGExportType.java Copyright (C) 2019. Daniel H. Huson
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
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.datatransfer.DataFlavor;
import java.io.*;

/**
 * The export filetype for svg images.
 *
 * @author huson, schroeder, 2007
 */
public class SVGExportType extends FileFilter implements ExportGraphicType {

    /**
     * the mime type of this exportfile type
     */
    private final String mimeType = "image/svg+xml";
    /**
     * the DataFlavor supported by this exportfile type
     */
    private final DataFlavor flavor;

    public SVGExportType() {
        flavor = new DataFlavor(mimeType + ";class=jloda.swing.export.SVGExportType", "Scalable Vector Graphic");
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataFlavor getDataFlavor() {
        return flavor;
    }

    public Object getData(JPanel panel) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            stream(panel, out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * stream the image data to a given <code>ByteArrayOutputStream</code>.
     *
     * @param panel the panel which paints the image.
     * @param out   the ByteArrayOutputStream.
     */
    public static void stream(JPanel panel, ByteArrayOutputStream out) throws IOException {
        (new SVGExportType()).stream(panel, null, false, out);
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
        final JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);
        final DOMImplementation dom = GenericDOMImplementation.getDOMImplementation();
        final Document doc = dom.createDocument(null, "svg", null);
        final SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);

        panel.paint(svgGenerator);

        svgGenerator.stream(new OutputStreamWriter(out, "UTF-8"));
        out.flush();
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

        final DOMImplementation dom = GenericDOMImplementation.getDOMImplementation();
        final Document doc = dom.createDocument(null, "svg", null);
        final SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);
        svgGenerator.setSVGCanvasSize(panel.getSize());
        panel.paint(svgGenerator);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            svgGenerator.stream(bw);
        }
    }

    /**
     * write the image into an svg file.
     *
     * @param file  the file to write to.
     * @param panel the panel which paints the image.
     */
    public static void writeToFile(File file, JPanel panel) throws IOException, FileNotFoundException {
        (new SVGExportType()).writeToFile(file, panel, null, false);
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = Basic.getFileSuffix(f.getName());
        if (extension != null) {
            if (extension.equalsIgnoreCase("svg"))
                return true;
        } else {
            return false;
        }
        return false;
    }

    public String getDescription() {
        return "SVG (*.svg)";
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
        return ".svg";
    }

}
