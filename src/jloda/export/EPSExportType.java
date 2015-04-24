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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.datatransfer.DataFlavor;
import java.io.*;

/**
 * Export using the <i>encapsulated postscript</i> file format.
 * The export itself is done by {@link jloda.export.EPSGraphics}.
 *
 * @author Daniel Huson, Michael Schr�der
 * @version $Id: EPSExportType.java,v 1.11 2007-07-15 11:02:36 huson Exp $
 * @see jloda.export.EPSGraphics
 */
public class EPSExportType extends FileFilter implements ExportGraphicType {

    /**
     * the mime type of this exportfile type
     */
    private final String mimeType = "image/x-eps";
    /**
     * the DataFlavor supported by this exportfile type
     */
    private final DataFlavor flavor;

    private boolean drawTextAsOutlines = false;


    public EPSExportType() {
        flavor = new DataFlavor(mimeType + ";class=jloda.export.EPSExportType", "EPS graphic");
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataFlavor getDataFlavor() {
        return flavor;
    }

    public Object getData(JPanel panel) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        stream(panel, out);
        return new ByteArrayInputStream(out.toByteArray());
    }


    /**
     * stream the image data to a given <code>ByteArrayOutputStream</code>.
     *
     * @param panel the panel which paints the image.
     * @param out   the OutputStream.
     */
    public static void stream(JPanel panel, OutputStream out) {
        EPSExportType eps = new EPSExportType();
        eps.setDrawTextAsOutlines(true);
        eps.stream(panel, null, false, out);
    }

    /**
     * stream the image data to a given <code>ByteArrayOutputStream</code>.
     *
     * @param imagePanel the panel which paints the image.
     * @param out        the ByteArrayOutputStream.
     */
    public void stream(JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage, OutputStream out) {
        JPanel panel;
        if (showWholeImage || imageScrollPane == null)
            panel = imagePanel;
        else
            panel = ExportManager.makePanelFromScrollPane(imagePanel, imageScrollPane);
        EPSGraphics epsGraphics = new EPSGraphics(panel.getWidth(), panel.getHeight(), out, getDrawTextAsOutlines());
        panel.paint(epsGraphics);
        epsGraphics.finish();
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
        FileOutputStream fos = new FileOutputStream(file);
        stream(imagePanel, imageScrollPane, showWholeImage, fos);
        fos.close();
    }

    /**
     * write the image into an eps file.
     *
     * @param file  the file to write to.
     * @param panel the panel which paints the image.
     */
    public static void writeToFile(File file, JPanel panel) throws IOException {
        writeToFile(file, panel, true);
    }

    /**
     * write the image into an eps file.
     *
     * @param file     the file to write to.
     * @param panel    the panel which paints the image.
     * @param fontMode whether font are converted to outlines
     */
    public static void writeToFile(File file, JPanel panel, boolean fontMode) throws IOException {
        EPSExportType eps = new EPSExportType();
        eps.setDrawTextAsOutlines(fontMode);
        eps.writeToFile(file, panel, null, false);
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = Basic.getSuffix(f.getName());
        if (extension != null) {
            if (extension.equalsIgnoreCase(".eps"))
                return true;
        } else {
            return false;
        }
        return false;
    }

    public String getDescription() {
        return "EPS (*.eps)";
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
        jloda.util.FileFilter filter = new jloda.util.FileFilter(getFileExtension());
        filter.getFileExtensions().add(".ps");
        return filter;
    }

    public String getFileExtension() {
        return ".eps";
    }

    public boolean getDrawTextAsOutlines() {
        return drawTextAsOutlines;
    }

    public void setDrawTextAsOutlines(boolean drawTextAsOutlines) {
        this.drawTextAsOutlines = drawTextAsOutlines;
    }
}
