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

import jloda.util.Alert;
import jloda.util.Basic;
import jloda.util.CanceledException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

/**
 * A Dialog for saving the image in various graphic file formats.
 * This is old. Please use SaveImageDialog
 *
 * @author Daniel Huson, Michael Schr�der
 * @version $Id: SaveGraphicDialog.java,v 1.19 2006-05-15 03:34:46 huson Exp $
 * @deprecated please use SaveImageDialog
 */
public class SaveGraphicDialog {

    private static final JFileChooser chooser = new JFileChooser();

    static {
        FileFilter allTypesFilter = new GraphicsFileFilters.AllTypesFilter();
        chooser.addChoosableFileFilter(allTypesFilter);
        chooser.addChoosableFileFilter(new GraphicsFileFilters.SvgFilter());
        chooser.addChoosableFileFilter(new GraphicsFileFilters.EpsFilter());
        chooser.addChoosableFileFilter(new GraphicsFileFilters.PngFilter());
        chooser.addChoosableFileFilter(new GraphicsFileFilters.GifFilter());
        chooser.addChoosableFileFilter(new GraphicsFileFilters.JpgFilter());
        chooser.setFileFilter(allTypesFilter);

    }

    static File saveFile = new File(System.getProperties().getProperty("user.dir"));

    /**
     * shows a save dialog for various image filetypes.
     * if the filetype is not supported, an error message is shown.
     *
     * @param owner the component which owns this dialog.
     * @param panel the panel which paints the image.
     */
    public static void showSaveDialog(JFrame owner, JPanel panel) {
        showSaveDialog(owner, panel, saveFile);
    }

    public static File showSaveDialog(JFrame owner, JScrollPane scrollPane, final JPanel panel) {
        return showSaveDialog(owner, scrollPane, panel, null);
    }

    public static File showSaveDialog(JFrame owner, JScrollPane scrollPane, final JPanel panel, File preselectedFile) {
        final Point apt = scrollPane.getViewport().getViewPosition();
        final JPanel newPanel = new JPanel() {
            public void paint(Graphics g0) {
                final Graphics2D g = (Graphics2D) g0;
                g0.translate(-apt.x, -apt.y);
                panel.paint(g);
                g0.translate(apt.x, apt.y);
            }
        };
        Dimension extend = scrollPane.getViewport().getExtentSize();
        newPanel.setSize(extend);
        return showSaveDialog(owner, newPanel, preselectedFile);
    }

    /**
     * shows a save dialog for various image filetypes.
     * if the filetype is not supported, an error message is shown.
     *
     * @param owner the component which owns this dialog.
     * @param panel the panel which paints the image.
     * @return file that was written to, or null
     */
    public static File showSaveDialog(JFrame owner, JPanel panel, File preselectedFile) {
        if (preselectedFile == null)
            preselectedFile = saveFile;
        // set directory here
        chooser.setCurrentDirectory(preselectedFile);
        chooser.setSelectedFile(preselectedFile);
        int result = chooser.showDialog(owner, "Save Image..");

        if (result == JFileChooser.APPROVE_OPTION) {

            File file = chooser.getSelectedFile();
            //JFileChooser.FILE_FILTER_CHANGED_PROPERTY
            String ext = GraphicsFileFilters.getExtension(file);

            //if(ext.equalsIgnoreCase("tiff") || ext.equalsIgnoreCase("tiff")) {

            //}
            try {
                if (ext != null) {
                    if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
                        if (isOkToWrite(owner, file))
                            JPGExportType.writeToFile(file, panel);
                    } else if (ext.equalsIgnoreCase("gif")) {
                        if (isOkToWrite(owner, file))
                            GIFExportType.writeToFile(file, panel);
                    } else if (ext.equalsIgnoreCase("eps")) {
                        if (isOkToWrite(owner, file)) {
                            String msg = "Convert text to shapes?";
                            int reply = JOptionPane.showConfirmDialog(owner, msg, "EPS export",
                                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                            switch (reply) {
                                case JOptionPane.OK_OPTION:

                                    EPSExportType.writeToFile(file, panel, EPSGraphics.FONT_OUTLINES);
                                    break;
                                case JOptionPane.NO_OPTION:
                                    EPSExportType.writeToFile(file, panel, EPSGraphics.FONT_TEXT);
                                    break;
                                case JOptionPane.CANCEL_OPTION:
                                    throw new CanceledException();
                            }
                        }
                    } else if (ext.equalsIgnoreCase("svg")) {
                        if (isOkToWrite(owner, file))
                            SVGExportType.writeToFile(file, panel);
                    } else if (ext.equalsIgnoreCase("png")) {
                        if (isOkToWrite(owner, file))
                            PNGExportType.writeToFile(file, panel);
                    } else {
                        System.err.println("file type not supported: " + ext);
                        new Alert(owner, "File type not supported: " + ext);
                    }
                } else {
                    String fileName = file.getName();
                    FileFilter filter = chooser.getFileFilter();
                    if (filter.getClass().isAssignableFrom(GraphicsFileFilters.SvgFilter.class)) {
                        fileName += ".svg";
                        if (isOkToWrite(owner, file))
                            SVGExportType.writeToFile(new File(fileName), panel);
                    } else if (filter.getClass().isAssignableFrom(GraphicsFileFilters.EpsFilter.class)) {
                        fileName += ".eps";
                        if (isOkToWrite(owner, file)) {
                            String msg = "Convert text to shapes?";
                            int reply = JOptionPane.showConfirmDialog(owner, msg, "EPS export",
                                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                            switch (reply) {
                                case JOptionPane.OK_OPTION:

                                    EPSExportType.writeToFile(file, panel, EPSGraphics.FONT_OUTLINES);
                                    break;
                                case JOptionPane.NO_OPTION:
                                    EPSExportType.writeToFile(file, panel, EPSGraphics.FONT_TEXT);
                                    break;
                                case JOptionPane.CANCEL_OPTION:
                                    throw new CanceledException();
                            }
                        }
                    } else if (filter.getClass().isAssignableFrom(GraphicsFileFilters.PngFilter.class)) {
                        fileName += ".png";
                        if (isOkToWrite(owner, file))
                            PNGExportType.writeToFile(new File(fileName), panel);
                    } else if (filter.getClass().isAssignableFrom(GraphicsFileFilters.GifFilter.class)) {
                        fileName += ".gif";
                        if (isOkToWrite(owner, file))
                            GIFExportType.writeToFile(new File(fileName), panel);
                    } else if (filter.getClass().isAssignableFrom(GraphicsFileFilters.JpgFilter.class)) {
                        fileName += ".jpg";
                        if (isOkToWrite(owner, file))
                            JPGExportType.writeToFile(new File(fileName), panel);
                    } else {
                        new Alert(owner, "Unknown filetype");
                    }
                    System.err.println("fileName: " + fileName);
                    saveFile = file.getParentFile();
                }
            } catch (CanceledException ex) {
                new Alert(owner, "Canceled, image NOT saved");
            } catch (Exception ex) {
                Basic.caught(ex);
                new Alert(owner, "Error, image NOT saved\n" + ex);
            }
            return file;
        } else if (result == JFileChooser.ERROR_OPTION) {
            new Alert(owner, "Unspecified error, image NOT saved");
        }
        return null;
    }

    /**
     * ok to write file?
     *
     * @param file
     * @return false, if file exists and user doesn't want to overwrite
     */
    private static boolean isOkToWrite(JFrame owner, File file) throws CanceledException {
        if (!file.exists() ||
                JOptionPane.showConfirmDialog(owner,
                        "This file already exists. " +
                                "Would you like to overwrite the existing file?",
                        "Save File",
                        JOptionPane.YES_NO_OPTION
                ) == 0)
            return true;
        throw new CanceledException();
    }

    /**
     * result is true, if we are currently in a writeToFile call.
     * This is used in paint to determine whether we are drawing to the screen or
     * writing to a file
     *
     * @return true, if in writeToFile
     */
    public static boolean inWriteToFile() {
        Throwable throwable = new Throwable();
        throwable.fillInStackTrace();
        StackTraceElement[] ste = throwable.getStackTrace();
        for (StackTraceElement aSte : ste)
            if (aSte.getMethodName().equals("writeToFile"))
                return true;
        return false;
    }
}
