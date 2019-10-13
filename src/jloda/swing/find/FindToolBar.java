/*
 * FindToolBar.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.swing.find;

import jloda.swing.director.IDirectableViewer;
import jloda.swing.director.IDirector;
import jloda.swing.director.IViewerWithFindToolBar;
import jloda.swing.util.BasicSwing;
import jloda.swing.util.RememberingComboBox;
import jloda.swing.util.ResourceManager;
import jloda.util.ProgramProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * find and replace window
 * Daniel Huson, 7.2008
 */
public class FindToolBar extends JPanel implements IFindDialog {
    private final SearchManager searchManager;
    private final IViewerWithFindToolBar viewer;

    private final RememberingComboBox findCBox;
    private final RememberingComboBox replaceCBox;

    private final JToolBar findToolBar;
    private final JToolBar replaceToolBar;

    private boolean showReplaceBar;

    final static private Color LIGHT_RED = new Color(255, 200, 200);
    final static private Color LIGHT_GREEN = new Color(200, 255, 200);

    private final JLabel messageLabel;
    private final JComboBox targetCBox = new JComboBox();
    private final SearchActions actions;

    private final Map<Component, ISearcher> parent2active = new HashMap<>();   // keeps a mapping of windows to active searcher

    private JFrame frame;

    private boolean enabled = true;
    private boolean closing = false;

    /**
     * constructor
     *
     * @param searchManager
     * @param viewer
     * @param actions
     * @param additionalButtons
     */
    public FindToolBar(SearchManager searchManager, IViewerWithFindToolBar viewer, SearchActions actions, Collection<AbstractButton> additionalButtons) {
        this(searchManager, viewer, actions, false, additionalButtons);
    }

    /**
     * constructor
     *
     * @param searchManager
     * @param viewer
     * @param actions
     * @param showReplaceBar
     * @param additionalButtons
     */
    public FindToolBar(SearchManager searchManager, IViewerWithFindToolBar viewer, SearchActions actions, boolean showReplaceBar,
                       Collection<AbstractButton> additionalButtons) {
        this.searchManager = searchManager;
        this.viewer = viewer;
        this.frame = viewer.getFrame();
        this.actions = actions;
        this.showReplaceBar = showReplaceBar;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEtchedBorder());

        findToolBar = new JToolBar();
        findToolBar.setFloatable(false);
        findToolBar.setRollover(true);
        add(findToolBar);

        findCBox = new RememberingComboBox();
        findCBox.addItemsFromString(ProgramProperties.get("FindString." + viewer.getClassName(), ""), "%%%");

        messageLabel = new JLabel();
        messageLabel.setForeground(Color.DARK_GRAY);
        setupFind(additionalButtons);

        replaceToolBar = new JToolBar();
        replaceToolBar.setFloatable(false);
        replaceToolBar.setRollover(true);

        replaceCBox = new RememberingComboBox();
        replaceCBox.addItemsFromString(ProgramProperties.get("ReplaceString." + viewer.getClassName(), ""), "%%%");
        setupReplace();

        if (showReplaceBar)
            add(replaceToolBar);

        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent mouseEvent) {
                if (frame != null)
                    frame.requestFocusInWindow();
            }

            public void mouseEntered(MouseEvent mouseEvent) {
            }
        });
        actions.updateEnableState();
    }

    /**
     * setup the panel
     */
    private void setupFind(Collection<AbstractButton> additionalButtons) {
        findCBox.setMinimumSize(new Dimension(200, 20));
        findCBox.setMaximumSize(new Dimension(200, 20));
        findCBox.setPreferredSize(new Dimension(200, 20));
        BasicSwing.changeFontSize(findCBox, 10);
        findToolBar.add(findCBox);

        findCBox.getEditor().addActionListener(arg0 -> {
            String word = findCBox.getEditor().getItem().toString().trim();
            if (word.length() > 0) {
                findCBox.setSelectedItem(word);
                findCBox.getCurrentText(true);
                searchManager.setSearchText(word);
                searchManager.applyFindFirst();
            }
        });

        JButton button = new JButton(actions.getFindFirst());
        BasicSwing.changeFontSize(button, 10);
        findToolBar.add(button);
        button = new JButton(actions.getFindNext());
        BasicSwing.changeFontSize(button, 10);
        findToolBar.add(button);

        button = new JButton(actions.getFindAll());
        button.setText("All");
        BasicSwing.changeFontSize(button, 10);
        findToolBar.add(button);

        button = new JButton(actions.getFindFromFile());
        button.setText("");
        addButton(button, findToolBar);

        findToolBar.addSeparator(new Dimension(5, 10));

        JCheckBox cbox = new JCheckBox();
        cbox.setAction(actions.getCaseSensitiveOption(cbox));
        addButton(cbox, findToolBar);

        cbox = new JCheckBox();
        cbox.setAction(actions.getWholeWordsOption(cbox));
        addButton(cbox, findToolBar);

        cbox = new JCheckBox();
        cbox.setAction(actions.getRegularExpressionOption(cbox));
        cbox.setText("Regex");
        addButton(cbox, findToolBar);

        findToolBar.addSeparator(new Dimension(5, 10));


        if (additionalButtons != null && additionalButtons.size() > 0) {
            for (AbstractButton but : additionalButtons) {
                addButton(but, findToolBar);
            }
            findToolBar.addSeparator(new Dimension(5, 10));
        }

        findToolBar.add(Box.createHorizontalStrut(10));

        BasicSwing.changeFontSize(messageLabel, 10);
        messageLabel.setMinimumSize(new Dimension(200, 20));
        messageLabel.setMaximumSize(new Dimension(200, 20));
        messageLabel.setPreferredSize(new Dimension(100, 20));
        findToolBar.add(messageLabel);
        findToolBar.add(Box.createHorizontalGlue());

        JButton done = new JButton(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                FindToolBar.this.setClosing(true);
                if (viewer instanceof IDirectableViewer)
                    ((IDirectableViewer) viewer).updateView(IDirector.ENABLE_STATE);
            }
        });
        done.setIcon(ResourceManager.getIcon("CloseToolBar16.gif"));
        done.setToolTipText("Close find toolbar");
        addButton(done, findToolBar);

        findToolBar.validate();
    }

    /**
     * setup the panel
     */
    private void setupReplace() {
        replaceCBox.setMinimumSize(new Dimension(200, 20));
        replaceCBox.setMaximumSize(new Dimension(200, 20));
        replaceCBox.setPreferredSize(new Dimension(200, 20));
        BasicSwing.changeFontSize(replaceCBox, 10);
        replaceToolBar.add(replaceCBox);

        replaceCBox.getEditor().addActionListener(arg0 -> {
            String word = replaceCBox.getEditor().getItem().toString().trim();
            if (word.length() > 0) {
                searchManager.setReplaceText(word);
                replaceCBox.setSelectedItem(word);
                replaceCBox.getCurrentText(true);
            }
        });

        JButton button = new JButton(actions.getFindAndReplace());
        BasicSwing.changeFontSize(button, 10);
        replaceToolBar.add(button);
        button = new JButton(actions.getReplaceAll());
        BasicSwing.changeFontSize(button, 10);
        replaceToolBar.add(button);

        replaceToolBar.addSeparator(new Dimension(5, 10));

        ButtonGroup group = new ButtonGroup();
        JRadioButton globalButton = new JRadioButton();
        group.add(globalButton);
        globalButton.setAction(actions.getGlobalScope(globalButton));
        addButton(globalButton, replaceToolBar);
        JRadioButton selectionButton = new JRadioButton();
        selectionButton.setAction(actions.getSelectionScope(selectionButton));
        group.add(selectionButton);
        addButton(selectionButton, replaceToolBar);

        replaceToolBar.add(Box.createHorizontalGlue());
        replaceToolBar.validate();
    }

    private void addButton(AbstractButton button, JToolBar toolBar) {
        BasicSwing.changeFontSize(button, 10);
        button.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        toolBar.add(button);
    }

    /**
     * show or hide the replace bar
     *
     * @param showReplaceBar
     */
    public void setShowReplaceBar(boolean showReplaceBar) {
        if (this.showReplaceBar != showReplaceBar) {
            removeAll();
            add(findToolBar);
            if (showReplaceBar)
                add(replaceToolBar);
            revalidate();
            this.showReplaceBar = showReplaceBar;
        }
    }

    /**
     * is the replace bar showing?
     *
     * @return true if replace bar showing
     */
    public boolean isShowReplaceBar() {
        return showReplaceBar;
    }

    /**
     * update the targets cbox
     */
    public void updateTargets() {
        parent2active.clear();

        targetCBox.removeAllItems();

        for (int i = 0; i < searchManager.targets.length; i++) {
            ISearcher searcher = searchManager.targets[i];
            targetCBox.addItem(new SearcherItem(searcher));
            if (searcher.getParent() != null && parent2active.get(searcher.getParent()) == null)
                parent2active.put(searcher.getParent(), searcher);
        }

        targetCBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                ISearcher searcher = ((SearcherItem) event.getItem()).getSearcher();
                searchManager.setSearcher(searcher);
                if (searcher.getParent() != null)
                    parent2active.put(searcher.getParent(), searcher);
            }
        });
    }

    /**
     * update the target selection
     *
     * @param name named search target
     */
    public boolean selectTarget(String name) {
        for (int i = 0; i < searchManager.targets.length; i++) {
            SearcherItem item = (SearcherItem) targetCBox.getItemAt(i);
            if (item.toString().equals(name)) {
                targetCBox.setSelectedIndex(i);
                return true;
            }
        }
        for (int i = 0; i < searchManager.targets.length; i++) {
            SearcherItem item = (SearcherItem) targetCBox.getItemAt(i);
            if (item.toString().equalsIgnoreCase(name)) {
                targetCBox.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    /**
     * when activating a window, call this method to revert to the last used searcher for this window
     *
     * @param parent
     */
    public void chooseTargetForFrame(Component parent) {
        if (parent != null) {
            ISearcher searcher = parent2active.get(parent);
            if (searcher != null)
                selectTarget(searcher.getName());
        }
    }

    /**
     * indicates that user has pressed close button
     *
     * @return true, if closing
     */
    public boolean isClosing() {
        return closing;
    }

    /**
     * once closed, client show set closing to false
     *
     * @param closing
     */
    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    static class SearcherItem extends JButton {
        final ISearcher searcher;

        SearcherItem(ISearcher searcher) {
            setText(searcher.getName());
            this.searcher = searcher;
        }

        public String toString() {
            return searcher.getName();
        }

        public ISearcher getSearcher() {
            return searcher;
        }
    }

    public SearchActions getActions() {
        return actions;
    }

    /**
     * sets the message
     *
     * @param message
     */
    public void setMessage(String message) {
        if (message.contains("No matches") || message.contains("Found: 0"))
            findCBox.setBackground(LIGHT_RED);
        else if (message.contains("Found"))
            findCBox.setBackground(LIGHT_GREEN);
        else
            findCBox.setBackground(Color.WHITE);

        if (message.contains("No replacements") || message.contains("Replacements: 0"))
            replaceCBox.setBackground(LIGHT_RED);
        else if (message.contains("Replace"))
            replaceCBox.setBackground(LIGHT_GREEN);
        else
            replaceCBox.setBackground(Color.WHITE);
        messageLabel.setText(message);
    }

    /**
     * clears the message
     */
    public void clearMessage() {
        findCBox.setBackground(Color.WHITE);
        replaceCBox.setBackground(Color.WHITE);
        messageLabel.setText("");
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * call when window is closed to remember find strings
     */
    public void close() {
        ProgramProperties.put("FindString." + viewer.getClassName(), findCBox.getItemsAsString(20, "%%%"));
        ProgramProperties.put("ReplaceString." + viewer.getClassName(), replaceCBox.getItemsAsString(20, "%%%"));
        clearMessage();
    }

    public String getFindText() {
        if (frame != null)
            frame.requestFocusInWindow();
        return findCBox.getCurrentText(true);
    }

    public String getReplaceText() {
        return replaceCBox.getCurrentText(true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            findCBox.requestFocusInWindow(); // grab focus
    }

    public void setEnableCritical(boolean enableCritical) {
        if (isEnabled())
            actions.setEnableCritical(enableCritical);
    }
}
