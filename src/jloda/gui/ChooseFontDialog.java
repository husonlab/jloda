/**
 * ChooseFontDialog.java 
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
import jloda.util.Pair;
import jloda.util.Single;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * choose a font
 * Daniel Huson, 9.2012
 */
public class ChooseFontDialog {
    static private Pair<Font, Color> result = null;

    /**
     * show a choose font dialog
     *
     * @param parent
     * @param title
     * @param defaultFont
     * @return color chosen or null
     */
    public static Pair<Font, Color> showChooseFontDialog(JFrame parent, String title, Font defaultFont, Color defaultColor) {
        final Single<Color> theColor = new Single<>(defaultColor);

        final JDialog dialog = new JDialog(parent, title);
        dialog.setLocationRelativeTo(parent);
        dialog.setSize(new Dimension(500, 180));
        dialog.setModal(true);
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        dialog.getContentPane().add(mainPanel);
        mainPanel.setLayout(new BorderLayout());

        // Top panel:
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setPreferredSize(new Dimension(1000, 50));
        topPanel.setMaximumSize(new Dimension(1000, 50));
        topPanel.add(new JLabel("Font:"));
        final JComboBox fontNames = makeFontNames();
        if (defaultFont != null)
            fontNames.setSelectedItem(defaultFont.getFamily());
        topPanel.add(fontNames);

        topPanel.add(new JLabel("Size:"));
        final JComboBox fontSizes = makeFontSizes();
        if (defaultFont != null)
            fontSizes.setSelectedItem("" + defaultFont.getSize());

        topPanel.add(fontSizes);

        final JCheckBox boldCBox = new JCheckBox("Bold");
        if (defaultFont != null && defaultFont.isBold())
            boldCBox.setSelected(true);

        topPanel.add(boldCBox);

        final JCheckBox italicCBox = new JCheckBox("Italic");
        if (defaultFont != null && defaultFont.isItalic())
            italicCBox.setSelected(true);

        topPanel.add(italicCBox);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // middle panel:
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BorderLayout());

        // text area in middle panel:
        final JTextArea preview = new JTextArea();
        preview.setEditable(true);
        preview.setText("The quick brown fox jumps over the lazy dog");
        preview.setFont(defaultFont);
        preview.setBorder(BorderFactory.createBevelBorder(1));

        fontNames.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                preview.setFont(getCurrentFont(fontNames, fontSizes, boldCBox, italicCBox));
            }
        });
        fontSizes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                preview.setFont(getCurrentFont(fontNames, fontSizes, boldCBox, italicCBox));
            }
        });
        boldCBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                preview.setFont(getCurrentFont(fontNames, fontSizes, boldCBox, italicCBox));
            }
        });
        italicCBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                preview.setFont(getCurrentFont(fontNames, fontSizes, boldCBox, italicCBox));
            }
        });
        middlePanel.add(preview, BorderLayout.CENTER);

        // color choice in middle panel:
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));

        colorPanel.add(new JLabel("Color:"));

        JRadioButton defaultColorButton = new JRadioButton(new AbstractAction("Default") {
            public void actionPerformed(ActionEvent actionEvent) {
                preview.setForeground(Color.BLACK);
                theColor.set(null);
            }
        });
        if (defaultColor != null)
            preview.setForeground(defaultColor);
        defaultColorButton.setSelected(defaultColor == null);
        colorPanel.add(defaultColorButton);

        final JRadioButton userColorButton = new JRadioButton();
        userColorButton.setAction(new AbstractAction("Choose...") {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!userColorButton.isSelected()) {
                    preview.setForeground(Color.BLACK);
                    theColor.set(null);
                    preview.repaint();
                } else {
                    Color color = JColorChooser.showDialog(dialog, "Choose Font Color", preview.getForeground());
                    if (color != null) {
                        preview.setForeground(color);
                        theColor.set(color);
                        preview.repaint();
                    }
                }
            }
        });
        colorPanel.add(userColorButton);
        userColorButton.setSelected(defaultColor != null);

        ButtonGroup group = new ButtonGroup();
        group.add(defaultColorButton);
        group.add(userColorButton);

        middlePanel.add(colorPanel, BorderLayout.SOUTH);

        mainPanel.add(middlePanel, BorderLayout.CENTER);

        // bottom panel:
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

        bottom.add(Box.createHorizontalGlue());

        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent actionEvent) {
                result = null;
                dialog.setVisible(false);
            }
        });
        bottom.add(cancelButton);
        dialog.getRootPane().setDefaultButton(cancelButton);

        bottom.add(new JButton(new AbstractAction("Default") {
            public void actionPerformed(ActionEvent actionEvent) {
                Font font = Font.decode(null);
                boldCBox.setSelected(font.isBold());
                italicCBox.setSelected(font.isItalic());
                fontNames.setSelectedItem(font.getFamily());
                fontSizes.setSelectedItem("" + font.getSize());
            }
        }));

        bottom.add(new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent actionEvent) {
                String name = fontNames.getSelectedItem().toString().trim();
                int size = Basic.parseInt(fontSizes.getSelectedItem().toString().trim());
                if (name != null && size > 0) {
                    result = new Pair<>(getCurrentFont(fontNames, fontSizes, boldCBox, italicCBox), theColor.get());
                }
                dialog.setVisible(false);
            }
        }));

        mainPanel.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
        return result;
    }

    static private Font getCurrentFont(JComboBox fontNames, JComboBox fontSizes, JCheckBox boldCBox, JCheckBox italicCBox) {
        String name = fontNames.getSelectedItem().toString().trim();
        int size = Basic.parseInt(fontSizes.getSelectedItem().toString().trim());
        if (name != null && size > 0) {
            int style = 0;
            if (boldCBox.isSelected())
                style |= Font.BOLD;
            if (italicCBox.isSelected())
                style |= Font.ITALIC;
            return new Font(name, style, size);
        }
        return Font.decode(null);
    }

    static private JComboBox makeFontNames() {
        JComboBox box = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        box.setMinimumSize(box.getPreferredSize());
        return box;
    }

    static private JComboBox makeFontSizes() {
        Object[] possibleValues = {"8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "32", "36", "40", "44"};
        JComboBox box = new JComboBox(possibleValues);
        box.setEditable(true);
        box.setMinimumSize(box.getPreferredSize());
        return box;
    }

}
