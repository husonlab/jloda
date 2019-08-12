/*
 * PDFExportType.java Copyright (C) 2019. Daniel H. Huson
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


import de.erichseifert.vectorgraphics2d.Document;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.pdf.PDFProcessor;
import de.erichseifert.vectorgraphics2d.util.PageSize;
import jloda.swing.util.FileFilter;
import jloda.util.Basic;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.io.*;


/**
 * The export filetype for pdf images.
 *
 * @author huson
 * @version 2011
 */
public class PDFExportType extends SVGExportType implements ExportGraphicType {

    /**
     * the mime type of this exportfile type
     */
    private final String mimeType = "image/pdf";
    /**
     * the DataFlavor supported by this exportfile type
     */
    private final DataFlavor flavor;

    public PDFExportType() {
        flavor = new DataFlavor(mimeType + ";class=jloda.swing.export.PDFExportType", "Portable Document Format");
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
    public static void stream(JPanel panel, OutputStream out) throws IOException {
        int width = panel.getWidth();
        int height = panel.getHeight();

        // Get the Graphics object for pdf writing
        final VectorGraphics2D pdfGraphics = new VectorGraphics2D();
        panel.paint(pdfGraphics);

        final PDFProcessor processor = new PDFProcessor(false);
        final Document document = processor.getDocument(pdfGraphics.getCommands(), new PageSize(width, height));
        document.writeTo(out);
        out.flush();
    }


    /**
     * writes image to a stream. If scrollPane given and showWholeImage=true, draws only visible portion
     * of panel
     *
     * @param imagePanel
     * @param imageScrollPane
     * @param showWholeImage
     * @param out
     * @throws java.io.IOException
     */
    public void stream(JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage, OutputStream out) throws IOException {
        final JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else {
            // panel=(JPanel)((JViewport)imageScrollPane.getComponent(0)).getComponent(0) ;
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);
        }
        stream(panel, out);
    }

    /**
     * writes image to file. If scrollPane given and showWholeImage=true, draws only visible portion
     * of panel
     *
     * @param file
     * @param imagePanel
     * @param imageScrollPane
     * @param showWholeImage
     * @throws java.io.IOException
     */
    public void writeToFile(File file, final JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage) throws IOException {
        try (OutputStream fos = new FileOutputStream(file)) {
            stream(imagePanel, imageScrollPane, showWholeImage, fos);
        }
    }

    /**
     * write the image into an pdf file.
     *
     * @param file  the file to write to.
     * @param panel the panel which paints the image.
     */
    public static void writeToFile(File file, JPanel panel) throws IOException {
        (new PDFExportType()).writeToFile(file, panel, null, false);
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = Basic.getFileSuffix(f.getName());
        if (extension != null) {
            return extension.equalsIgnoreCase("pdf");
        } else {
            return false;
        }
    }

    public String getDescription() {
        return "PDF (*.pdf)";
    }

    public String toString() {
        return getDescription();
    }

    /**
     * gets the associated file filter and filename filter
     *
     * @return filename filter
     */
    public FileFilter getFileFilter() {
        return new FileFilter(getFileExtension());
    }

    public String getFileExtension() {
        return ".pdf";
    }
}
