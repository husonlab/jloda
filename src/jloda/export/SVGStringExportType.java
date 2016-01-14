/**
 * SVGStringExportType.java 
 * Copyright (C) 2016 Daniel H. Huson
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
package jloda.export;

import jloda.util.Basic;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author huson, schroeder
 */
public class SVGStringExportType implements ExportGraphicType {

    private final String mimeType = "text/xml";
    private final DataFlavor flavor;

    public SVGStringExportType() {
        flavor = new DataFlavor(mimeType, "SVG as plain xml");
    }

    public String getMimeType() {
        return mimeType;
    }

    public DataFlavor getDataFlavor() {
        return flavor;
    }

    public Object getData(JPanel panel) {
        /*ByteArrayOutputStream out = new ByteArrayOutputStream();
        SVGExport.export(gv,out);
        try {
            out.close();
        } catch (IOException e) {
            Basic.caught(e);
        }
        return new ByteArrayInputStream(out.toByteArray()); */
        return null;
    }

    public void stream(JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage, OutputStream out) throws IOException {

    }

    public void writeToFile(File file, JPanel imagePanel, JScrollPane imageScrollPane, boolean showWholeImage) throws IOException {
        writeToFile(file, imagePanel);
    }

    public static void writeToFile(File file, JPanel panel) {

        /*ByteArrayOutputStream out = new ByteArrayOutputStream();
        SVGExport.export(gv,out);
        try {
            out.close();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(out.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Basic.caught(e);
        }  */

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

    public boolean accept(File file, String s) {
        return false;
    }

    public String getFileExtension() {
        return ".svg";
    }
}
