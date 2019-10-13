/*
 * SaveImageDialog.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.swing.util.Alert;
import jloda.swing.util.ChooseFileDialog;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * A Dialog for saving the image in various graphic file formats.
 *
 * @author Daniel Huson, Michael Schroeder, 2005
 */
public class SaveImageDialog extends JDialog {
    static public boolean useAWTDialog = false; // by default, use Swing file dialog to save file
    static public final boolean allowEPS = true;

    final JComboBox formatComboBox;
    JRadioButton visibleRegionButton = null;
    JRadioButton wholeImageButton = null;
    final JCheckBox drawTextAsOutlinesCB;
    final JFrame parent;

    final ExportManager exportManager = ExportManager.getInstance();

    final JPanel imagePanel;
    final JScrollPane imageScrollPane;

    final String fileBaseName;
    final public static String GRAPHICSFORMAT = "GraphicsFormat";
    final public static String GRAPHICSDIR = "GraphicsDir";


    /**
     * creates and displays a save image dialog and saves image, if desired. If performSave is true, saves image, other
     * not, in which case a command string is returned via the getCommand method
     *
     * @param parent
     * @param imagePanel
     * @param imageScrollPane
     * @param fileBaseName
     */
    public SaveImageDialog(JFrame parent, JPanel imagePanel, JScrollPane imageScrollPane, String fileBaseName) {
        super(parent, "Export Image");
        this.parent = parent;
        this.imagePanel = imagePanel;
        this.imageScrollPane = imageScrollPane;
        this.fileBaseName = fileBaseName;

        setModal(true);
        setSize(new Dimension(260, 200));
        setLocationRelativeTo(parent);

        getContentPane().setLayout(new BorderLayout());

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.add(new JLabel("Format:"), BorderLayout.WEST);
        panel1.setBorder(BorderFactory.createEtchedBorder());

        formatComboBox = new JComboBox();

        for (ExportGraphicType exportGraphicType : exportManager.getGraphicTypes()) {
            if (true)
                formatComboBox.addItem(exportGraphicType);
        }
        panel1.add(formatComboBox, BorderLayout.CENTER);

        getContentPane().add(panel1, BorderLayout.NORTH);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));

        if (imageScrollPane != null) {
            ButtonGroup group = new ButtonGroup();
            visibleRegionButton = new JRadioButton("Save visible region");
            group.add(visibleRegionButton);
            panel2.add(visibleRegionButton);

            wholeImageButton = new JRadioButton("Save whole image");
            group.add(wholeImageButton);
            panel2.add(wholeImageButton);
        }

        drawTextAsOutlinesCB = new JCheckBox("Convert Text to Graphics");
        if (allowEPS)
            panel2.add(drawTextAsOutlinesCB);
        getContentPane().add(panel2, BorderLayout.CENTER);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout());
        panel3.setBorder(BorderFactory.createEtchedBorder());
        panel3.add(new JButton(getCancelAction()), BorderLayout.WEST);
        panel3.add(new JButton(getApplyAction()), BorderLayout.EAST);
        getContentPane().add(panel3, BorderLayout.SOUTH);

        formatComboBox.addItemListener(e -> {
            Object item = e.getItem();
            drawTextAsOutlinesCB.setEnabled(item instanceof EPSExportType);
        });

        String preSelectFormat = ProgramProperties.get(GRAPHICSFORMAT, (new EPSExportType()).getFileExtension());
        for (int i = 0; i < formatComboBox.getItemCount(); i++) {
            if (((ExportGraphicType) formatComboBox.getItemAt(i)).getFileExtension().equals(preSelectFormat)) {
                formatComboBox.setSelectedIndex(i);
                break;
            }
        }

        boolean preSelectWholeImage = ProgramProperties.get("graphicsWholeImage", true);
        if (preSelectWholeImage) {
            if (wholeImageButton != null)
                wholeImageButton.setSelected(true);
        } else {
            if (visibleRegionButton != null)
                visibleRegionButton.setSelected(true);
        }
        drawTextAsOutlinesCB.setSelected(ProgramProperties.get("graphicsConvertText", true));

        setVisible(true);
    }

    /**
     * the cancel action
     *
     * @return cancel action
     */
    AbstractAction getCancelAction() {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };
        action.putValue(AbstractAction.NAME, "Cancel");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Cancel");
        return action;
    }

    /**
     * the apply action
     *
     * @return apply action
     */
    AbstractAction getApplyAction() {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSaveDialog(parent);
                setVisible(false);
                dispose();
            }
        };
        action.putValue(AbstractAction.NAME, "Apply");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Apply");
        return action;
    }

    /**
     * displays the file chooser and saves
     *
     * @param parent
     */
    private void doSaveDialog(JFrame parent) {
        final ExportGraphicType graphicsType = (ExportGraphicType) formatComboBox.getSelectedItem();

        FilenameFilter fileNameFilter = (dir, name) -> graphicsType.getFileFilter().accept(new File(dir, name));

        File file = ChooseFileDialog.chooseFileToSave(parent, new File(fileBaseName), graphicsType.getFileFilter(), fileNameFilter, null, "Save Image", graphicsType.getFileExtension());

        if (file == null)
            return;

        if (wholeImageButton != null)
            ProgramProperties.put("graphicsWholeImage", wholeImageButton.isSelected());
        ProgramProperties.put("graphicsConvertText", drawTextAsOutlinesCB.isSelected());
        ProgramProperties.put(GRAPHICSDIR, file.getParentFile());
        ProgramProperties.put(GRAPHICSFORMAT, graphicsType.getFileExtension());

        try {
            boolean textAsOutlines;
            if (graphicsType instanceof EPSExportType) {
                EPSExportType eps = (EPSExportType) graphicsType;
                textAsOutlines = drawTextAsOutlinesCB.isSelected();
                eps.setDrawTextAsOutlines(textAsOutlines);
            }
            boolean visibleOnly = wholeImageButton == null || !wholeImageButton.isSelected();

            graphicsType.writeToFile(file, imagePanel, imageScrollPane, !visibleOnly);
            System.err.println("Written to file: " + file);

        } catch (IOException ex) {
            Basic.caught(ex);
            new Alert("Image NOT saved: " + ex);
        }
    }
}
