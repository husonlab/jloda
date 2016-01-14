/**
 * SearchActions.java 
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
package jloda.gui.find;

import jloda.gui.ChooseFileDialog;
import jloda.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * actions for find and find/replace dialogs
 * Daniel Huson, 7.2008
 */
public class SearchActions {
    final static String CBOX = "cbox";
    final static String CRITICAL = "critical";

    final static String RADIOBUTTON = "rb";


    private final SearchManager searchManager;
    private final List<AbstractAction> all;

    /**
     * constructor
     *
     * @param searchManager
     */
    SearchActions(SearchManager searchManager) {
        this.searchManager = searchManager;
        this.all = new LinkedList<>();
    }

    /**
     * enable and disable critical actions
     *
     * @param enable
     */
    public void setEnableCritical(boolean enable) {
        for (AbstractAction action : all) {
            action.setEnabled(enable);
        }
        if (enable)
            updateEnableState();
    }

    /**
     * update the enable state
     */
    public void updateEnableState() {

        if (searchManager.getSearcher() instanceof EmptySearcher) {
            for (AbstractAction action : all)
                action.setEnabled(false);
            return;
        }

        if (caseSensitiveOption != null)
            ((JCheckBox) caseSensitiveOption.getValue(CBOX)).setSelected(searchManager.isCaseSensitiveOption());
        if (wholeWordsOption != null)
            ((JCheckBox) wholeWordsOption.getValue(CBOX)).setSelected(searchManager.isWholeWordsOnlyOption());
        if (regularExpressionOption != null)
            ((JCheckBox) regularExpressionOption.getValue(CBOX)).setSelected(searchManager.isRegularExpressionsOption());
        if (forwardDirection != null)
            ((JRadioButton) forwardDirection.getValue(RADIOBUTTON)).setSelected(searchManager.isForwardDirection());
        if (backwardDirection != null)
            ((JRadioButton) backwardDirection.getValue(RADIOBUTTON)).setSelected(!searchManager.isForwardDirection());
        if (selectionScope != null) {
            ((JRadioButton) selectionScope.getValue(RADIOBUTTON)).setEnabled(searchManager.getSearcher().isSelectionFindable());
            if (!searchManager.getSearcher().isSelectionFindable())
                searchManager.setGlobalScope(true);
        }
        if (globalScope != null)
            ((JRadioButton) globalScope.getValue(RADIOBUTTON)).setSelected(searchManager.isGlobalScope());
        if (selectionScope != null)
            ((JRadioButton) selectionScope.getValue(RADIOBUTTON)).setSelected(!searchManager.isGlobalScope());

        if (findAll != null)
            findAll.setEnabled(searchManager.getSearcher().canFindAll());   // can find all in text

        if (findFromFile != null)
            findFromFile.setEnabled(searchManager.getSearcher().canFindAll());   // can find all in text

        if (findAndReplace != null)
            findAndReplace.setEnabled(searchManager.isAllowReplace());
        if (replaceAll != null)
            replaceAll.setEnabled(searchManager.isAllowReplace());
        if (findAndReplace != null)
            findAndReplace.setEnabled(searchManager.isAllowReplace());
        if (findAndReplace != null)
            findAndReplace.setEnabled(searchManager.isAllowReplace());
    }

    AbstractAction caseSensitiveOption;

    public AbstractAction getCaseSensitiveOption(final JCheckBox cbox) {

        AbstractAction action = caseSensitiveOption;
        if (action != null) {
            action.putValue(CBOX, cbox);
            return action;
        }
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.setCaseSensitiveOption(cbox.isSelected());
            }
        };
        action.putValue(CBOX, cbox);
        action.putValue(AbstractAction.NAME, "Case sensitive");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Do not match upper and lower case letters");
        all.add(action);
        return caseSensitiveOption = action;
    }

    AbstractAction wholeWordsOption;

    public AbstractAction getWholeWordsOption(final JCheckBox cbox) {
        AbstractAction action = wholeWordsOption;
        if (action != null) {
            action.putValue(CBOX, cbox);
            return action;
        }
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.setWholeWordsOnlyOption(cbox.isSelected());

            }
        };
        action.putValue(CBOX, cbox);

        action.putValue(AbstractAction.NAME, "Whole words only");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Match whole words only");
        all.add(action);
        return wholeWordsOption = action;
    }

    AbstractAction regularExpressionOption;

    public AbstractAction getRegularExpressionOption(final JCheckBox cbox) {
        AbstractAction action = regularExpressionOption;
        if (action != null) {
            action.putValue(CBOX, cbox);
            return action;
        }
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.setRegularExpressionsOption(cbox.isSelected());

            }
        };
        action.putValue(CBOX, cbox);

        action.putValue(AbstractAction.NAME, "Regular expression");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Find using Java regular expression");

        all.add(action);
        return regularExpressionOption = action;
    }

    AbstractAction forwardDirection;

    public AbstractAction getForwardDirection(final JRadioButton rb) {
        AbstractAction action = forwardDirection;
        if (action != null) {
            action.putValue(RADIOBUTTON, rb);
            return action;
        }
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.setForwardDirection(true);
            }
        };
        action.putValue(RADIOBUTTON, rb);

        action.putValue(AbstractAction.NAME, "Forward");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Search in forward direction");

        all.add(action);
        return forwardDirection = action;
    }

    AbstractAction backwardDirection;

    public AbstractAction getBackwardDirection(final JRadioButton rb) {
        AbstractAction action = backwardDirection;
        if (action != null) {
            action.putValue(RADIOBUTTON, rb);
            return action;
        }
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.setForwardDirection(false);
            }
        };
        action.putValue(RADIOBUTTON, rb);

        action.putValue(AbstractAction.NAME, "Backward");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Search in backward direction");

        all.add(action);
        return backwardDirection = action;
    }

    AbstractAction globalScope;

    public AbstractAction getGlobalScope(final JRadioButton rb) {
        AbstractAction action = globalScope;
        if (action != null) {
            action.putValue(RADIOBUTTON, rb);
            return action;
        }
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.setGlobalScope(true);
            }
        };
        action.putValue(RADIOBUTTON, rb);
        action.putValue(AbstractAction.NAME, "Global");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Search globally");
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return globalScope = action;
    }

    AbstractAction selectionScope;

    public AbstractAction getSelectionScope(final JRadioButton rb) {
        AbstractAction action = selectionScope;
        if (action != null) {
            action.putValue(RADIOBUTTON, rb);
            return action;
        }
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.setGlobalScope(false);
            }
        };
        action.putValue(RADIOBUTTON, rb);
        action.putValue(AbstractAction.NAME, "Selection");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Search only in selection");
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return selectionScope = action;
    }

    private AbstractAction close;

    public AbstractAction getClose() {
        AbstractAction action = close;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.getFrame().setVisible(false);
            }
        };
        action.putValue(AbstractAction.NAME, "Close");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Close the window");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("Close16.gif"));
        all.add(action);
        return close = action;
    }

    AbstractAction findFirst;

    public AbstractAction getFindFirst() {
        AbstractAction action = findFirst;
        if (action != null) return action;
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                flushStrings();
                searchManager.applyFindFirst();
            }
        };

        action.putValue(AbstractAction.NAME, "First");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Find first occurrence");
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return findFirst = action;
    }

    AbstractAction findNext;

    public AbstractAction getFindNext() {
        AbstractAction action = findNext;
        if (action != null) return action;
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                flushStrings();
                searchManager.applyFindNext();
            }
        };

        action.putValue(AbstractAction.NAME, "Next");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Find next occurrence");
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return findNext = action;
    }

    AbstractAction findAll;

    public AbstractAction getFindAll() {
        AbstractAction action = findAll;
        if (action != null) return action;
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                flushStrings();
                searchManager.applyFindAll();
            }
        };
        action.putValue(AbstractAction.NAME, "Find All");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Find all occurrences");
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return findAll = action;
    }


    private AbstractAction findFromFile;

    public AbstractAction getFindFromFile() {
        AbstractAction action = findFromFile;
        if (action != null) return action;

        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                File lastFile = ProgramProperties.getFile("FindFile");

                File file = ChooseFileDialog.chooseFileToOpen(searchManager.findDialog.getFrame(), lastFile, new TextFileFilter(), new TextFileFilter(), event, "Open file containing search terms");
                if (file != null) {
                    try {
                        searchManager.findFromFile(file);
                    } catch (IOException e) {
                        new Alert(searchManager.findDialog.getFrame(), "Find from file failed: " + e.getMessage());
                        Basic.caught(e);
                    }
                    ProgramProperties.put("FindFile", file);
                }
            }
        };
        action.putValue(AbstractAction.NAME, "From File...");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Process each line of a file as a find query");
        action.putValue(CRITICAL, Boolean.TRUE);
        action.putValue(AbstractAction.SMALL_ICON, ResourceManager.getIcon("sun/toolbarButtonGraphics/general/Open16.gif"));
        all.add(action);
        return findFromFile = action;
    }


    AbstractAction findAndReplace;

    public AbstractAction getFindAndReplace() {
        AbstractAction action = findAndReplace;
        if (action != null) return action;
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                flushStrings();
                searchManager.applyFindAndReplace();
            }
        };

        action.putValue(AbstractAction.NAME, "Replace");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Find and replace next occurrence");
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return findAndReplace = action;
    }

    AbstractAction replaceAll;

    public AbstractAction getReplaceAll() {
        AbstractAction action = replaceAll;
        if (action != null) return action;
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                flushStrings();
                searchManager.applyReplaceAll();
            }
        };

        action.putValue(AbstractAction.NAME, "Replace All");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Replace all occurrence");
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return replaceAll = action;
    }

    AbstractAction unselectAll;

    public AbstractAction getUnselectAll() {
        AbstractAction action = unselectAll;
        if (action != null) return action;
        action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                searchManager.applyUnselectAll();
            }
        };

        action.putValue(AbstractAction.NAME, "Unselect All");
        action.putValue(AbstractAction.SHORT_DESCRIPTION, "Unselect all currently selected objects");
        action.putValue(CRITICAL, Boolean.TRUE);
        all.add(action);
        return unselectAll = action;
    }


    private void flushStrings() {
        searchManager.setSearchText(searchManager.findDialog.getFindText());
        searchManager.setReplaceText(searchManager.findDialog.getReplaceText());

    }

    public List<AbstractAction> getAll() {
        return all;
    }
}
