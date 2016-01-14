/**
 * ChooseFileDialog.java 
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
package jloda.gui;

import jloda.util.Basic;
import jloda.util.ProgramProperties;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * file chooser
 * Daniel Huson, 9.2008
 */
public class ChooseFileDialog {
    /**
     * choose file to open dialog
     *
     * @param parent
     * @param lastOpenFile
     * @param fileFilter
     * @param fileNameFilter
     * @param event
     * @param message
     * @return file or null
     */
    public static File chooseFileToOpen(Component parent, File lastOpenFile, FileFilter fileFilter, FilenameFilter fileNameFilter, ActionEvent event, String message) {
        File file = null;

        JFrame frame = null;
        if (parent != null && parent instanceof JFrame)
            frame = (JFrame) parent;
        if (frame != null && frame.getJMenuBar() != null) {
            // frame.getJMenuBar().setEnabled(false); // todo: to do this we need to remember the state of all menu items and then reenable them below...
        }

        try {
            if (ProgramProperties.isMacOS() && (event == null || (event.getModifiers() & Event.SHIFT_MASK) == 0)) {
                //Use native file dialog on mac
                java.awt.FileDialog dialog;
                if (parent != null && parent instanceof JFrame)
                    dialog = new java.awt.FileDialog((JFrame) parent, message, java.awt.FileDialog.LOAD);
                else if (parent != null && parent instanceof Dialog)
                    dialog = new java.awt.FileDialog((Dialog) parent, message, java.awt.FileDialog.LOAD);
                else
                    dialog = new java.awt.FileDialog((JFrame) null, message, java.awt.FileDialog.LOAD);
                if (parent != null)
                    dialog.setLocationRelativeTo(parent);
                //dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setModal(true);
                if (fileNameFilter != null)
                    dialog.setFilenameFilter(fileNameFilter);
                if (lastOpenFile != null) {
                    dialog.setDirectory(lastOpenFile.getParent());
                    dialog.setFile(lastOpenFile.getName());
                }
                dialog.setVisible(true);
                if (dialog.getFile() != null) {
                    file = new File(dialog.getDirectory(), dialog.getFile());
                }
            } else {
                JFileChooser chooser;
                try {
                    chooser = new JFileChooser(lastOpenFile);
                    chooser.setSelectedFile(lastOpenFile);
                } catch (Exception ex) {
                    chooser = new JFileChooser();
                }
                chooser.setAcceptAllFileFilterUsed(true);
                if (fileFilter != null)
                    chooser.setFileFilter(fileFilter);

                int result = chooser.showOpenDialog(parent);
                if (result == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                }
            }
        } finally {
            if (frame != null && frame.getJMenuBar() != null) {
                // frame.getJMenuBar().setEnabled(true);
            }
        }
        return file;
    }

    /**
     * choose file to open dialog
     *
     * @param parent
     * @param lastOpenFile
     * @param fileFilter
     * @param fileNameFilter
     * @param event
     * @param message
     * @return file or null
     */
    public static java.util.List<File> chooseFilesToOpen(Component parent, File lastOpenFile, FileFilter fileFilter, FilenameFilter fileNameFilter, ActionEvent event, String message) {
        final LinkedList<File> list = new LinkedList<>();

        final JFrame frame;
        if (parent != null && parent instanceof JFrame)
            frame = (JFrame) parent;
        else
            frame = null;

        if (ProgramProperties.isMacOS() && (event == null || (event.getModifiers() & Event.SHIFT_MASK) == 0)) {
            //Use native file dialog on mac
            java.awt.FileDialog dialog;
            if (parent != null && parent instanceof JFrame)
                dialog = new java.awt.FileDialog((JFrame) parent, message, java.awt.FileDialog.LOAD);
            else if (parent != null && parent instanceof Dialog)
                dialog = new java.awt.FileDialog((Dialog) parent, message, java.awt.FileDialog.LOAD);
            else
                dialog = new java.awt.FileDialog((JFrame) null, message, java.awt.FileDialog.LOAD);
            dialog.setMultipleMode(true);
            if (frame != null)
                dialog.setLocationRelativeTo(frame);
            //dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setModal(true);
            if (fileNameFilter != null)
                dialog.setFilenameFilter(fileNameFilter);
            if (lastOpenFile != null) {
                dialog.setDirectory(lastOpenFile.getParent());
                dialog.setFile(lastOpenFile.getName());
            }
            dialog.setVisible(true);
            list.addAll(Arrays.asList(dialog.getFiles()));

            dialog.setVisible(false);
        } else {
            JFileChooser chooser;
            try {
                chooser = new JFileChooser(lastOpenFile);
                chooser.setSelectedFile(lastOpenFile);
            } catch (Exception ex) {
                chooser = new JFileChooser();
            }
            chooser.setMultiSelectionEnabled(true);
            chooser.setAcceptAllFileFilterUsed(true);
            if (fileFilter != null)
                chooser.setFileFilter(fileFilter);

            int result = chooser.showOpenDialog(parent);
            if (result == JFileChooser.APPROVE_OPTION) {
                list.addAll(Arrays.asList(chooser.getSelectedFiles()));
            }
        }

        return list;
    }

    /**
     * choose file to save dialog
     *
     * @param frame
     * @param lastOpenFile
     * @param fileFilter
     * @param fileNameFilter
     * @param event
     * @param message
     * @return file or null
     */
    public static File chooseFileToSave(JFrame frame, File lastOpenFile, FileFilter fileFilter, FilenameFilter fileNameFilter, ActionEvent event, String message) {
        return chooseFileToSave(frame, lastOpenFile, fileFilter, fileNameFilter, event, message, null);
    }

    /**
     * choose file to save dialog
     *
     * @param parent
     * @param lastOpenFile
     * @param fileFilter
     * @param fileNameFilter
     * @param event
     * @param message
     * @param defaultSuffix  .suff or null
     * @return file or null
     */
    public static File chooseFileToSave(Component parent, File lastOpenFile, FileFilter fileFilter, FilenameFilter fileNameFilter, ActionEvent event, String message,
                                        String defaultSuffix) {
        if (defaultSuffix != null && !defaultSuffix.startsWith("."))
            defaultSuffix = "." + defaultSuffix;
        File file = null;

        boolean okToWrite = false;
        while (!okToWrite) {
            if (ProgramProperties.isMacOS() && (event == null || (event.getModifiers() & Event.SHIFT_MASK) == 0)) {
                //Use native file dialog on mac
                java.awt.FileDialog dialog;
                if (parent != null && parent instanceof JFrame)
                    dialog = new java.awt.FileDialog((JFrame) parent, message, java.awt.FileDialog.SAVE);
                else if (parent != null && parent instanceof Dialog)
                    dialog = new java.awt.FileDialog((Dialog) parent, message, java.awt.FileDialog.SAVE);
                else
                    dialog = new java.awt.FileDialog((JFrame) null, message, java.awt.FileDialog.SAVE);

                if (fileNameFilter != null)
                    dialog.setFilenameFilter(fileNameFilter);
                if (lastOpenFile != null) {
                    if (lastOpenFile.getParentFile() != null && lastOpenFile.getParentFile().exists())
                        dialog.setDirectory(lastOpenFile.getParent());
                    //if (lastOpenFile.exists())
                    dialog.setFile(lastOpenFile.getName());
                }
                dialog.setVisible(true);
                if (dialog.getFile() != null) {
                    file = new File(dialog.getDirectory(), dialog.getFile());
                    okToWrite = true;
                    if (defaultSuffix != null) {
                        String suffix = Basic.getSuffix(file.getName());
                        if (suffix == null || suffix.equals(file.getName())) {
                            file = new File(file.getParent(), file.getName() + defaultSuffix);
                            // todo: don't seem to need this:
                                /*
                                if (file.exists()) {
                                    int result = JOptionPane.showConfirmDialog(parent, "This file already exists. Overwrite the existing file?",
                                            "Save File", JOptionPane.YES_NO_OPTION);
                                    if (result != JOptionPane.YES_OPTION)
                                        okToWrite = false;
                                }
                                */
                        }
                    }
                } else
                    return file;
            } else {
                JFileChooser chooser;
                try {
                    chooser = new JFileChooser(lastOpenFile);
                    chooser.setSelectedFile(lastOpenFile);
                } catch (Exception ex) {
                    chooser = new JFileChooser();
                }// Add the FileFilter for the Import Plugins

                chooser.setAcceptAllFileFilterUsed(true);
                if (fileFilter != null)
                    chooser.setFileFilter(fileFilter);

                int result = chooser.showSaveDialog(parent);
                if (result != JFileChooser.APPROVE_OPTION) {
                    System.err.println("Save canceled");
                    return null;
                }
                file = chooser.getSelectedFile();
                okToWrite = true;

                if (defaultSuffix != null) {
                    String suffix = Basic.getSuffix(file.getName());
                    if (suffix == null || suffix.equals(file.getName())) {
                        file = new File(file.getParent(), file.getName() + defaultSuffix);
                    }
                }
                if (file.exists()) {
                    switch (
                            JOptionPane.showConfirmDialog(parent,
                                    "This file already exists. Overwrite the existing file?",
                                    "Save File",
                                    JOptionPane.YES_NO_CANCEL_OPTION)) {
                        case JOptionPane.YES_OPTION:
                            okToWrite = true;
                            break;
                        case JOptionPane.NO_OPTION:
                            okToWrite = false;
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            return file;
                    }
                } else
                    okToWrite = true;

            }
        }

        return file;
    }
}
