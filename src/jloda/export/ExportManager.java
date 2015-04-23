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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * manages graphics-export
 * Daniel Huson  , 5.2006
 */
public class ExportManager {
    private static ExportManager instance;
    private final List<ExportGraphicType> graphicTypes;

    private ExportManager() {
        graphicTypes = new LinkedList<>();
        loadGraphicTypes();
    }

    static public ExportManager getInstance() {
        if (instance == null)
            instance = new ExportManager();
        return instance;
    }

    private void loadGraphicTypes() {
        // TODO: use plugin-mechanism to load from directory
        graphicTypes.add(new EPSExportType());
        graphicTypes.add(new GIFExportType());
        graphicTypes.add(new JPGExportType());
        graphicTypes.add(new PNGExportType());
        graphicTypes.add(new SVGExportType());
        graphicTypes.add(new RenderedExportType());
        graphicTypes.add(new PDFExportType());
        // graphicTypes.add(new PDFExportType2());
    }

    public List<ExportGraphicType> getGraphicTypes() {
        return graphicTypes;
    }

    private FileFilter allFileFilter = null;

    public FileFilter getAllFileFilter() {
        if (allFileFilter == null) {
            allFileFilter = new FileFilter() {
                public boolean accept(File f) {
                    for (ExportGraphicType ext : getGraphicTypes()) {
                        if (ext.getFileFilter().accept(f))
                            return true;
                    }
                    return false;
                }

                public String getDescription() {
                    StringBuilder buf = new StringBuilder();
                    buf.append("Supported image file types: ");
                    boolean first = true;
                    for (ExportGraphicType exportGraphicType : getGraphicTypes()) {
                        if (first)
                            first = false;
                        else
                            buf.append(", ");
                        buf.append(exportGraphicType.getFileFilter().getDescription());
                    }
                    return buf.toString();
                }
            };
        }
        return allFileFilter;
    }

    /**
     * creates a panel whose paint method draws only what is currently visible in the given scrollpane
     *
     * @param imagePanel
     * @param imageScrollPane
     * @return panel clipped to region visible in scroll pane
     */
    public static JPanel makePanelFromScrollPane(final JPanel imagePanel, JScrollPane imageScrollPane) {
        final Point apt = imageScrollPane.getViewport().getViewPosition();
        final Dimension extent = (Dimension) imageScrollPane.getViewport().getExtentSize().clone();

        final JPanel panel = new JPanel() {
            public void paint(Graphics g0) {
                doPaint(g0, imagePanel, apt, extent);
            }
        };
        panel.setSize(extent);
        return panel;
    }

    static private void doPaint(Graphics g0, JPanel imagePanel, Point apt, Dimension extent) {
        //System.err.println("apt: " + apt);
        //System.err.println("Extent: " + extent);
        g0.translate(-apt.x, -apt.y);
        g0.setClip(apt.x, apt.y, extent.width, extent.height);
        g0.setColor(imagePanel.getBackground());
        g0.fillRect(apt.x, apt.y, extent.width, extent.height);
        imagePanel.paint(g0);
        g0.translate(apt.x, apt.y);

    }


    /**
     * result is true, if we are currently in a writeToFile call.
     * This is used in paint to determine whether we are drawing to the screen or
     * writing to a file
     *
     * @return true, if in writeToFile or getData
     */
    public static boolean inWriteToFileOrGetData() {
        Throwable throwable = new Throwable();
        throwable.fillInStackTrace();
        StackTraceElement[] ste = throwable.getStackTrace();
        for (StackTraceElement aSte : ste)
            if (aSte.getMethodName().equals("writeToFile") || aSte.getMethodName().equals("getData"))
                return true;
        return false;
    }


}
