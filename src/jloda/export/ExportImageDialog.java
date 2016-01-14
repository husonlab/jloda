/**
 * ExportImageDialog.java 
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

import jloda.gui.ChooseFileDialog;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.LinkedList;

/**
 * dialog for setting up image export
 * Daniel Huson, 5.2011
 */
public class ExportImageDialog extends JDialog {
    private final JTextField fileField = new JTextField();
    private final JComboBox formatComboBox = new JComboBox();
    private final JRadioButton saveVisibleOnlyButton;
    private final JCheckBox textAsOutlinesButton;
    private final JButton applyButton;

    private boolean fileNameChangedByText = true; // if file name changed by text, need to check for overwriting of file

    private static final java.util.List<ExportGraphicType> graphicTypes = new LinkedList<>();

    final public static String GRAPHICSFORMAT = "GraphicsFormat";
    final public static String GRAPHICSDIR = "GraphicsDir";

    private boolean inUpdate = false; // use this to prevent bouncing between update of format and update of file name

    private String command = null;

    /**
     * constructs a dialog for exporting an image
     *
     * @param parent
     * @param documentFileName
     * @param allowVisible
     * @param allowWhole
     * @param allowEPS
     * @param event
     */
    public ExportImageDialog(JFrame parent, String documentFileName, boolean allowVisible, boolean allowWhole, boolean allowEPS, final ActionEvent event) {
        super(parent, "Export Image" + (ProgramProperties.getProgramName() != null ? " - " + ProgramProperties.getProgramName() : ""));
        setModal(true);
        setSize(new Dimension(420, 210));
        setLocationRelativeTo(parent);

        getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(4, 2, 2, 2)));

        JPanel filePanel = new JPanel();
        filePanel.setMaximumSize(new Dimension(1000, 20));
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
        filePanel.add(new JLabel("File: "));
        fileField.setMinimumSize(new Dimension(100, 20));
        filePanel.add(fileField);
        filePanel.add(new JButton(new AbstractAction("Browse...") {
            public void actionPerformed(ActionEvent actionEvent) {
                File file = new File(fileField.getText());
                file = ChooseFileDialog.chooseFileToSave(ExportImageDialog.this, file, getExportGraphicType().getFileFilter(), getExportGraphicType().getFileFilter(), event, "Save image", getFileExtension());
                if (file != null) {
                    fileNameChangedByText = false;
                    String fileName = file.getPath();
                    String suffix = Basic.getSuffix(fileName);
                    if (suffix == null || suffix.length() == 0) {
                        file = new File(file.getPath() + getFileExtension());
                    }
                    if (suffix != null && !suffix.equals(getFileExtension())) {
                        setFormat(Basic.getSuffix(file.getPath()));
                    }
                    setFile(file);
                }
            }
        }));
        fileField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                updateEnabledState();
                fileNameChangedByText = true;
                if (!inUpdate) {
                    String extension = Basic.getFileSuffix(fileField.getText());
                    if (extension != null && extension.length() > 0) {
                        extension = extension.trim();
                        if (extension.startsWith(".")) {
                            extension = extension.substring(1);
                            if (extension.length() >= 3) {
                                inUpdate = true;
                                setFormat(extension);
                                inUpdate = false;
                            }
                        }
                    }
                }
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                insertUpdate(documentEvent);
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                insertUpdate(documentEvent);
            }
        });

        String directory = ProgramProperties.get(GRAPHICSDIR, new File(documentFileName).getParent());

        topPanel.add(filePanel);

        JPanel formatPanel = new JPanel();
        formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.X_AXIS));
        formatPanel.add(new JLabel("Format:"));

        // setup format combo list:
        for (ExportGraphicType exportGraphicType : getGraphicTypes()) {
            if (allowEPS || !exportGraphicType.getFileExtension().endsWith("eps")) {
                formatComboBox.addItem(exportGraphicType);
            }
        }
        setFormat(ProgramProperties.get(GRAPHICSFORMAT, (new EPSExportType()).getFileExtension()));
        setFile(new File(directory, Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(documentFileName), getFileExtension())));

        formatComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ExportGraphicType exportGraphicType = (ExportGraphicType) e.getItem();
                if (textAsOutlinesButton != null)
                    textAsOutlinesButton.setEnabled(exportGraphicType instanceof EPSExportType);
                if (!inUpdate) {
                    String fileName = Basic.replaceFileSuffix(fileField.getText(), exportGraphicType.getFileExtension());
                    if (!fileName.equals(exportGraphicType.getFileExtension())) {
                        inUpdate = true;
                        fileField.setText(fileName);
                        inUpdate = false;
                    }
                }
                fileNameChangedByText = true;
            }
        });
        formatPanel.add(formatComboBox);
        formatPanel.add(Box.createHorizontalGlue());
        formatPanel.add(Box.createHorizontalGlue());

        topPanel.add(formatPanel);
        getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

        saveVisibleOnlyButton = new JRadioButton(new AbstractAction("Visible region") {
            public void actionPerformed(ActionEvent actionEvent) {
            }
        });
        saveVisibleOnlyButton.setEnabled(allowVisible);
        middlePanel.add(saveVisibleOnlyButton);

        JRadioButton saveWholeImageButton = new JRadioButton(new AbstractAction("Whole image") {
            public void actionPerformed(ActionEvent actionEvent) {
            }
        });
        saveWholeImageButton.setEnabled(allowWhole);
        ButtonGroup group = new ButtonGroup();
        group.add(saveVisibleOnlyButton);
        group.add(saveWholeImageButton);

        boolean preSelectWholeImage = ProgramProperties.get("graphicsVisibleOnly", true);
        if (preSelectWholeImage && allowWhole)
            saveWholeImageButton.setSelected(true);
        else
            saveVisibleOnlyButton.setSelected(true);

        middlePanel.add(saveWholeImageButton);

        if (allowEPS) {
            textAsOutlinesButton = new JCheckBox(new AbstractAction("Text as outlines (EPS)") {
                public void actionPerformed(ActionEvent actionEvent) {
                }
            });
            textAsOutlinesButton.setEnabled(allowEPS && getFormat().equals("eps"));
            textAsOutlinesButton.setSelected(ProgramProperties.get("graphicsConvertText", true));
            middlePanel.add(textAsOutlinesButton);
        } else
            textAsOutlinesButton = null;

        getContentPane().add(middlePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 20));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBorder(BorderFactory.createEtchedBorder());
        bottomPanel.add(Box.createHorizontalGlue());

        bottomPanel.add(new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        }));

        applyButton = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent actionEvent) {
                String fileName = getFileName();
                String suffix = Basic.getSuffix(fileName);
                if (suffix == null || suffix.length() == 0) {
                    fileName += getFileExtension();
                    fileField.setText(fileName);
                }
                if (fileNameChangedByText && !checkOkToWriteFile(fileName))
                    return;
                setVisible(false);
                ProgramProperties.put("graphicsSaveVisibleOnly", isSaveVisibleOnly());
                ProgramProperties.put("graphicsConvertText", isTextAsOutlinesEPS());
                File tmpFile = new File(fileName);
                if (tmpFile.getParentFile() != null)
                    ProgramProperties.put(GRAPHICSDIR, tmpFile.getParentFile());
                ProgramProperties.put(GRAPHICSFORMAT, getFormat());

                command = "exportImage file='" + fileName + "' format=" + getFormat() + " visibleOnly=" + isSaveVisibleOnly()
                        + (isTextAsOutlinesEPS() ? " textAsShapes=true" : "") + " title=none replace=true;";
            }
        });
        bottomPanel.add(applyButton);
        getRootPane().setDefaultButton(applyButton);

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().validate();
    }

    /**
     * ok to write file?
     *
     * @param fileName
     * @return true, if ok to write file
     */
    private boolean checkOkToWriteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            switch (
                    JOptionPane.showConfirmDialog(this,
                            "This file already exists. Overwrite the existing file?", "Save File", JOptionPane.YES_NO_CANCEL_OPTION)) {
                case JOptionPane.YES_OPTION:
                    return true;
                case JOptionPane.NO_OPTION:
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }
        return true;
    }

    /**
     * update the enable state
     */
    private void updateEnabledState() {
        String fileName = getFileName();
        if (applyButton != null)
            applyButton.setEnabled(fileName.length() > 0);
    }

    /**
     * sets the format
     *
     * @param format
     */
    private void setFormat(String format) {
        for (int i = 0; i < formatComboBox.getItemCount(); i++) {
            ExportGraphicType exportGraphicType = (ExportGraphicType) formatComboBox.getItemAt(i);
            if (exportGraphicType.getFileExtension().endsWith(format))
                formatComboBox.setSelectedItem(exportGraphicType);
        }
    }

    /**
     * displays the dialog. Returns null, if user canceled, otherwise returns command string
     * that specifies image file, format and other options
     *
     * @return
     */
    public String displayDialog() {
        setVisible(true);
        return command;
    }

    /**
     * get the file
     *
     * @return filename
     */
    public String getFileName() {
        return fileField.getText().trim();
    }

    /**
     * set the file
     *
     * @param file
     */
    public void setFile(File file) {
        if (file == null)
            fileField.setText("");
        else
            fileField.setText(file.getPath());
        updateEnabledState();
    }

    public String getFormat() {
        return getFileExtension().substring(1);
    }

    public String getFileExtension() {
        return ((ExportGraphicType) formatComboBox.getSelectedItem()).getFileExtension();
    }

    public ExportGraphicType getExportGraphicType() {
        return (ExportGraphicType) formatComboBox.getSelectedItem();
    }

    public boolean isSaveVisibleOnly() {
        return saveVisibleOnlyButton.isSelected();
    }

    public boolean isTextAsOutlinesEPS() {
        return textAsOutlinesButton != null && textAsOutlinesButton.isSelected();
    }

    /**
     * get list of known graphics types
     *
     * @return list of graphic types
     */
    public static java.util.List<ExportGraphicType> getGraphicTypes() {
        if (graphicTypes.size() == 0) {
            // TODO: use plugin-mechanism to load from directory
            graphicTypes.add(new RenderedExportType());
            graphicTypes.add(new EPSExportType());
            graphicTypes.add(new GIFExportType());
            graphicTypes.add(new JPGExportType());
            graphicTypes.add(new PDFExportType());
            graphicTypes.add(new PNGExportType());
            graphicTypes.add(new SVGExportType());
        }
        return graphicTypes;
    }
}
