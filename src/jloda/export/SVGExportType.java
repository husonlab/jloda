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
 * @author huson, schr�der, 2007
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
        flavor = new DataFlavor(mimeType + ";class=jloda.export.SVGExportType", "Scalable Vector Graphic");
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataFlavor getDataFlavor() {
        return flavor;
    }

    public Object getData(JPanel panel) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            stream(panel, out);
        } catch (IOException ex) {
            Basic.caught(ex);
        }
        return new ByteArrayInputStream(out.toByteArray());
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
        JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);
        DOMImplementation dom = GenericDOMImplementation.getDOMImplementation();
        Document doc = dom.createDocument(null, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);

        panel.paint(svgGenerator);

        svgGenerator.stream(new OutputStreamWriter(out, "UTF-8"));
        out.flush();
        out.close();
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

        DOMImplementation dom = GenericDOMImplementation.getDOMImplementation();
        Document doc = dom.createDocument(null, "svg", null);
        SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);
        svgGenerator.setSVGCanvasSize(panel.getSize());
        panel.paint(svgGenerator);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        svgGenerator.stream(bw);
        bw.close();
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
        String extension = Basic.getSuffix(f.getName());
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
    public jloda.util.FileFilter getFileFilter() {
        return new jloda.util.FileFilter(getFileExtension());
    }

    public String getFileExtension() {
        return ".svg";
    }
}
