/*
 * StatusBar.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.util;

import jloda.util.ProgramProperties;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * StatusBar for windows
 *
 * @author Daniel Huson, 1.2011
 */
public class StatusBar extends JPanel {
    private final JTextArea text1 = new JTextArea();
    private final JPanel panel1 = new JPanel();
    private final JTextArea text2 = new JTextArea();
    private final JPanel panel2 = new JPanel();
    private final JLabel text3 = new JLabel();
    private final JSplitPane splitPane1;
    private final JSplitPane splitPane2;

    private final ChangeListener changeListener;

    /**
     * Constructor for the status bar of the window
     */
    public StatusBar() {
        this(true);
    }

    /**
     * Constructor for the status bar of the window
     */
    public StatusBar(boolean showMemoryUsage) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEtchedBorder());

        text1.setFont(new Font("Dialog", Font.PLAIN, 10));
        text1.setEditable(false);
        text1.setBackground(text3.getBackground());
        //text1.setFocusable(false);
        text2.setFont(new Font("Dialog", Font.PLAIN, 10));
        text2.setEditable(false);
        text2.setBackground(text3.getBackground());
        // text2.setFocusable(false);
        text3.setFont(new Font("Dialog", Font.PLAIN, 10));
        text3.setText(BasicSwing.getMemoryUsageString(100));
        text3.setFocusable(false);

        panel1.add(text1);

        panel2.add(text2);

        splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panel2);
        splitPane1.setBorder(BorderFactory.createEmptyBorder());
        splitPane1.setResizeWeight(0);

        if (ProgramProperties.isMacOS())
            splitPane1.setDividerSize(10);
        else splitPane1.setDividerSize(1);

        JPanel text3Panel = new JPanel();
        splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane1, text3Panel);
        splitPane2.setBorder(BorderFactory.createEmptyBorder());
        splitPane2.setResizeWeight(1);
        if (ProgramProperties.isMacOS())
            splitPane2.setDividerSize(10);
        else splitPane2.setDividerSize(1);

        this.add(splitPane2, BorderLayout.CENTER);

        if (showMemoryUsage) {
            changeListener=changeEvent -> setText3(changeEvent.getSource().toString());
            SwingUtilities.invokeLater(() -> {
                setText3("------------");
                text3Panel.add(text3);
                this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
                MemoryUsageManager.addChangeListener(changeListener);
            });
        }
        else
            changeListener=null;
    }

    /**
     * set the text directly
     *
     * @param text
     */
    public void setText1(String text) {
        this.text1.setText(text);
        splitPane1.resetToPreferredSizes();
        splitPane2.resetToPreferredSizes();
    }

    public String getText1() {
        return text1.getText().trim();
    }

    /**
     * set the text directly
     *
     * @param text
     */
    public void setText2(String text) {
        this.text2.setText(text + "   ");
        splitPane1.resetToPreferredSizes();
        splitPane2.resetToPreferredSizes();
    }

    public String getText2() {
        return text2.getText().trim();
    }

    public void setExternalPanel1(JComponent externalPanel, boolean visible) {
        panel1.removeAll();
        if (visible)
            panel1.add(externalPanel);
        else
            panel1.add(text1);
        splitPane1.resetToPreferredSizes();
        splitPane2.resetToPreferredSizes();
        panel1.repaint();
    }

    public void setComponent2(JComponent externalPanel, boolean visible) {
        panel2.removeAll();
        if (visible)
            panel2.add(externalPanel);
        else
            panel2.add(text2);
        splitPane1.resetToPreferredSizes();
        splitPane2.resetToPreferredSizes();
        panel2.repaint();
    }

    /**
     * set the text3 directly
     *
     * @param text3
     */
    public void setText3(String text3) {
        this.text3.setText(text3 + " ");
        if (splitPane1 != null)
            splitPane1.resetToPreferredSizes();
        if (splitPane2 != null)
            splitPane2.resetToPreferredSizes();
    }

    public String getText3() {
        return text3.getText().trim();
    }

    public void setToolTipText(String toolTipText) {
        panel1.setToolTipText(toolTipText);
        text1.setToolTipText(toolTipText);
        panel2.setToolTipText(toolTipText);
        text2.setToolTipText(toolTipText);
        text3.setToolTipText(toolTipText);
    }
}
