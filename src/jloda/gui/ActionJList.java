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

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

/*
** sends ACTION_PERFORMED event for double-click
** and ENTER key
*/

public class ActionJList<E> extends JList<E> {
    ActionListener al;

    public ActionJList(ListModel<E> dataModel) {
        super(dataModel);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (al == null) return;
                final List<E> selectedValuesList = getSelectedValuesList();
                if (selectedValuesList.size() != 1) return;
                if (me.getClickCount() == 2) {
                    //System.out.println("Sending ACTION_PERFORMED to ActionListener");
                    al.actionPerformed(new ActionEvent(this,
                            ActionEvent.ACTION_PERFORMED,
                            selectedValuesList.get(0).toString()));
                    me.consume();
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (al == null) return;
                final List<E> selectedValuesList = getSelectedValuesList();
                if (selectedValuesList.size() != 1) return;
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    //System.out.println("Sending ACTION_PERFORMED to ActionListener");
                    al.actionPerformed(new ActionEvent(this,
                            ActionEvent.ACTION_PERFORMED,
                            selectedValuesList.get(0).toString()));
                    ke.consume();
                }
            }
        });
        //this.setSelectedIndex(0); //All are selected when we open the pane
        this.setSelectedIndex(-1); //None are selected when we open the pane
    }

    public void addActionListener(ActionListener al) {
        this.al = al;
    }
}
