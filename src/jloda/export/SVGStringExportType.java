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
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author huson, schroeder
 * @version $Id: SVGStringExportType.java,v 1.6 2007-03-11 07:38:17 huson Exp $
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
