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

package jloda.gui;

import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * show a message window
 *
 * @author huson
 *         Date: 23-Feb-2004
 */
public class Message {
    public static final Color PALE_YELLOW = new Color(252, 232, 131, 100);

    /**
     * create an message window with the given message and display it
     *
     * @param message
     */
    public Message(String message) {
        this(null, message);
    }

    /**
     * create an message window with the given message and display it
     *
     * @param parent  parent window
     * @param message
     */
    public Message(Component parent, final String message) {
        this(parent, message, 400, 200, null);
    }

    /**
     * create an message window with the given message and display it
     *
     * @param parent  parent window
     * @param message
     */
    public Message(Component parent, final String message, final String title) {
        this(parent, message, 400, 200, title);
    }

    /**
     * create an message window with the given message and display it
     *
     * @param parent  parent window
     * @param message
     */
    public Message(Component parent, final String message, int width, int height) {
        this(parent, message, width, height, null);
    }


    /**
     * create an message window with the given message and display it
     *
     * @param parent  parent window
     * @param message
     */
    public Message(Component parent, final String message, int width, int height, final String title) {
        if (ProgramProperties.isUseGUI()) {
            String label;
            if (title == null) {
                if (ProgramProperties.getProgramName() != null)
                    label = "Message - " + ProgramProperties.getProgramName();
                else
                    label = "Message";
            } else
                label = title + " - " + ProgramProperties.getProgramName();
            new MessageDialog(parent, message, label, width, height);
            //new MessageBox((JFrame)parent,message,label,width,height);
        } else
            System.err.println("Message - " + message);
    }

}

class MessageDialog extends JDialog {
    MessageDialog(Component parent, String message, String title, int width, int height) {
        super();
        // setIconImage(ProgramProperties.getProgramIcon().getImage());
        setModal(true);
        setTitle(title);
        setSize(width, height);
        setLocationRelativeTo(parent);
        Container main = getContentPane();
        main.setLayout(new BorderLayout());
        JPanel middle = new JPanel();
        middle.setLayout(new BorderLayout());
        middle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JEditorPane text;
        if (message.startsWith("<html>")) {
            text = new JEditorPane("text/html", message);
        } else {
            text = new JEditorPane();
            text.setText(message);
        }
        text.setEditable(false);
        //text.setWrapStyleWord(true);
        //text.setLineWrap(true);
        text.setBackground(main.getBackground());
        middle.add(new JScrollPane(text), BorderLayout.CENTER);
        main.add(middle, BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        //bottom.setBorder(BorderFactory.createEtchedBorder()); 
        JButton closeButton = new JButton(getCloseAction());
        bottom.add(closeButton, BorderLayout.EAST);
        rootPane.setDefaultButton(closeButton);

        main.add(bottom, BorderLayout.SOUTH);
        text.setCaretPosition(0);
        setVisible(true);
    }

    public AbstractAction getCloseAction() {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                MessageDialog.this.dispose();
            }
        };
        action.putValue(AbstractAction.NAME, "Close");
        return action;
    }
}

class MessageBox extends Window {
    MessageBox(JFrame parent, String message, String title, int width, int height) {
        super(parent);
        setBackground(Message.PALE_YELLOW);

        // setIconImage(ProgramProperties.getProgramIcon().getImage());
        int x = width;
        int y = parent.getHeight() - height;

        setSize(width, height);
        //setLocationRelativeTo(parent);
        setLocation(x, y);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Message.PALE_YELLOW);
        add(panel);
        JPanel middle = new JPanel();
        middle.setBackground(panel.getBackground());
        middle.setLayout(new BorderLayout());
        middle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JEditorPane text;
        if (message.startsWith("<html>")) {
            text = new JEditorPane("text/html", message);
        } else {
            text = new JEditorPane();
            text.setText(message);
        }
        text.setEditable(false);
        //text.setWrapStyleWord(true);
        //text.setLineWrap(true);
        text.setBackground(panel.getBackground());
        JScrollPane scrollPane = new JScrollPane(text);
        scrollPane.setBackground(panel.getBackground());
        middle.add(scrollPane, BorderLayout.CENTER);
        panel.add(middle, BorderLayout.CENTER);
        JPanel top = new JPanel();
        top.setBackground(panel.getBackground());
        top.setLayout(new BorderLayout());
        //bottom.setBorder(BorderFactory.createEtchedBorder());
        JButton closeButton = new JButton(getCloseAction());
        closeButton.setBorder(null);
        closeButton.setBackground(panel.getBackground());
        top.add(closeButton, BorderLayout.WEST);

        panel.add(top, BorderLayout.NORTH);
        text.setCaretPosition(0);
        setVisible(true);
    }

    public AbstractAction getCloseAction() {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                MessageBox.this.dispose();
            }
        };
        //action.putValue(AbstractAction.NAME, "Close");
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("Close16.gif"));
        return action;
    }
}
