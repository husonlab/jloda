/*
 *  MenuMnemonics.java Copyright (C) 2019 Daniel H. Huson
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

package jloda.swing.window;

import javax.swing.*;
import java.util.BitSet;

/**
 * add mnemonics to menu, preserving any already set and fixing any broken ones
 * Daniel Huson , 6.2005
 */
public class MenuMnemonics {
    /**
     * set the mnemonic for all items of a menu
     *
     * @param menu
     */
    public static void setMnemonics(JMenu menu) {
        if (menu.getMnemonic() == 0) {
            int menuMnemonic = menu.getText().charAt(0);
            menu.setMnemonic(menuMnemonic);
        }
        final BitSet seen = new BitSet();

        // use all mnemonics already set:
        for (int itemNumber = 0; itemNumber < menu.getItemCount(); itemNumber++) {
            if (menu.getItem(itemNumber) != null) {
                final String text = menu.getItem(itemNumber).getText();
                if (text != null) {
                    final int m = Character.toLowerCase(menu.getItem(itemNumber).getMnemonic());
                    if (m != 0) {
                        if (!seen.get(m)) {
                            seen.set(m);
                            menu.getItem(itemNumber).setMnemonic(m);
                        } else {
                            menu.getItem(itemNumber).setMnemonic(0);
                        }
                    }
                }
            }
        }
        // add new mnemonics
        for (int itemNumber = 0; itemNumber < menu.getItemCount(); itemNumber++) {
            if (menu.getItem(itemNumber) != null) {
                final String text = menu.getItem(itemNumber).getText();
                if (text != null) {
                    final JMenu subMenu;
                    if (menu.getItem(itemNumber) instanceof JMenu)
                        subMenu = (JMenu) menu.getItem(itemNumber);
                    else
                        subMenu = null;
                    final int m = Character.toLowerCase(menu.getItem(itemNumber).getMnemonic());
                    if (m == 0) // not set
                    {
                        for (int pos = 0; pos < text.length(); pos++) {
                            final int letter = Character.toLowerCase(text.charAt(pos));
                            if (Character.isLetter(letter)) {
                                if (!seen.get(letter)) {
                                    menu.getItem(itemNumber).setMnemonic(letter);
                                    seen.set(letter);
                                    if (subMenu !=null) {
                                        subMenu.setMnemonic(letter);
                                    }
                                    break; // found a usable letter
                                }
                            }
                        }
                    }
                    if (subMenu != null)
                        setMnemonics(subMenu);
                }
            }
        }
    }
}
