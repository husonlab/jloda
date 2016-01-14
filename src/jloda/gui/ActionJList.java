/**
 * ActionJList.java 
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
