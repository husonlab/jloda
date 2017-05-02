/**
 * StatusBar.java
 * Copyright (C) 2017 Daniel H. Huson
 * <p>
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.gui;

import jloda.util.Basic;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * StatusBar for windows
 *
 * @author Daniel Huson, 1.2011, 4.2017
 */
public class StatusBar extends JPanel {
    private final JTextField text1 = new JTextField();
    private final JPanel panel1 = new JPanel();
    private final ArrayList<JTextField> text2items = new ArrayList<>();
    private final JPanel panel2 = new JPanel();
    private final JLabel text3 = new JLabel();
    private final JPanel panel3 = new JPanel();

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
        panel1.setLayout(new WrapLayout(FlowLayout.LEFT, 4, 2));
        panel2.setLayout(new WrapLayout(FlowLayout.LEFT, 4, 2));
        panel3.setLayout(new WrapLayout(FlowLayout.RIGHT, 4, 2));

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEtchedBorder());

        text1.setFont(new Font("Dialog", Font.PLAIN, 10));
        text1.setEditable(false);
        text1.setBorder(null);
        text1.setBackground(panel1.getBackground());
        //text1.setFocusable(false);
        // text2.setFocusable(false);
        text3.setFont(new Font("Dialog", Font.PLAIN, 10));
        text3.setText(Basic.getMemoryUsageString(100));
        text3.setFocusable(false);

        panel1.add(text1);

        if (showMemoryUsage) {
            setText3("------------");
            panel3.add(text3);
            panel3.setToolTipText("Memory usage");
            panel3.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

            changeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setText3(changeEvent.getSource().toString());
                }
            };
            MemoryUsageManager.addChangeListener(changeListener);
        } else {
            changeListener = null;
        }

        add(panel1, BorderLayout.WEST);
        add(panel2, BorderLayout.CENTER);
        add(panel3, BorderLayout.EAST);
    }

    /**
     * set the text directly
     *
     * @param text
     */
    public void setText1(final String text) {
        text1.setText(text + "     ");
        revalidate();
        repaint();

    }

    public String getText1() {
        return text1.getText().trim();
    }

    /**
     * set the text directly
     *
     * @param text
     */
    public void setText2(final String text) {
        text2items.clear();
        panel2.removeAll();

        final String[] tokens = text.split("\\s+");
        for (String label : tokens) {
            final JTextField textField = new JTextField();
            textField.setFont(new Font("Dialog", Font.PLAIN, 10));
            textField.setBorder(null);
            textField.setEditable(false);
            textField.setText(label);
            textField.setBackground(panel2.getBackground());
            text2items.add(textField);
            panel2.add(textField);
        }

        revalidate();
        repaint();
    }

    public String getText2() {
        final StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (JTextField textField : text2items) {
            if (first)
                first = false;
            else
                buf.append(" ");
            buf.append(textField.getText());
        }
        return buf.toString();
    }

    public void setExternalPanel1(final JComponent externalPanel, final boolean visible) {
        panel1.removeAll();
        if (visible)
            panel1.add(externalPanel);
        else
            panel1.add(text1);
        revalidate();
        repaint();
    }

    public void setComponent2(final JComponent externalPanel, final boolean visible) {
        panel2.removeAll();
        if (visible)
            panel2.add(externalPanel);
        else {
            for (JTextField textField : text2items) {
                panel2.add(textField);
            }
        }
        revalidate();
        repaint();

    }

    /**
     * set the text3 directly
     *
     * @param text
     */
    public void setText3(final String text) {
        text3.setText(text + " ");
        revalidate();
        repaint();
    }

    public String getText3() {
        return text3.getText().trim();
    }

    public void setToolTipText(String toolTipText) {
        panel2.setToolTipText(toolTipText);
    }
}
