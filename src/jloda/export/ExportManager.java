/**
 * ExportManager.java 
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
