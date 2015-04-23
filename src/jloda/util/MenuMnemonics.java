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

package jloda.util;

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
