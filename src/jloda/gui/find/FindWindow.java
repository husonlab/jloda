/**
 * FindWindow.java 
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

import jloda.util.ProgramProperties;
import jloda.util.RememberingComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * find and replace window
 * Daniel Huson, 7.2008
 */
public class FindWindow extends JFrame implements IFindDialog {
    final SearchManager searchManager;

    final RememberingComboBox findCBox;
    final RememberingComboBox replaceCBox;

    final JLabel messageLabel;
    final JComboBox targetCBox = new JComboBox();
    final SearchActions actions;

    private final int WIDTH_FIND = 600;
    private final int HEIGHT_FIND = 250;
    private final int HEIGHT_FIND_REPLACE = 330;

    private final Map<Component, ISearcher> parent2active = new HashMap<>();   // keeps a mapping of windows to active searcher


    /**
     * constructor
     *
     * @param parent
     * @param title
     * @param searchManager
     */
    public FindWindow(Component parent, String title, SearchManager searchManager, SearchActions actions) {
        this.searchManager = searchManager;

        this.setLocationRelativeTo(parent);
        this.setTitle(title);
        if (ProgramProperties.getProgramIcon() != null)
            this.setIconImage(ProgramProperties.getProgramIcon().getImage());

        int height = searchManager.getShowReplace() ? HEIGHT_FIND_REPLACE : HEIGHT_FIND;
        this.setSize(WIDTH_FIND, height);

        findCBox = new RememberingComboBox();
        findCBox.setBorder(BorderFactory.createBevelBorder(1));
        findCBox.addItemsFromString(ProgramProperties.get("FindString", ""), "%%%");
        replaceCBox = new RememberingComboBox();
        replaceCBox.setBorder(BorderFactory.createBevelBorder(1));
        replaceCBox.addItemsFromString(ProgramProperties.get("ReplaceString", ""), "%%%");
        messageLabel = new JLabel();
        messageLabel.setForeground(Color.DARK_GRAY);

        this.actions = actions;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                ProgramProperties.put("FindString", findCBox.getItemsAsString(20, "%%%"));
                ProgramProperties.put("ReplaceString", replaceCBox.getItemsAsString(20, "%%%"));
                clearMessage();
                getActions().getClose().actionPerformed(null);
            }
        });
        setupPanel(searchManager.getShowReplace());
        actions.updateEnableState();
    }

    /**
     * gets the frame of this
     *
     * @return frame
     */
    public JFrame getFrame() {
        return this;
    }

    /**
     * setup the panel
     */
    private void setupPanel(boolean showReplace) {
        int preferredHeight = (showReplace ? HEIGHT_FIND_REPLACE : HEIGHT_FIND) + (searchManager.targets.length <= 3 ? 0 :
                (searchManager.targets.length - 3) * 30);
        if (getSize().height != preferredHeight)
            this.setSize((int) this.getSize().getWidth(), HEIGHT_FIND_REPLACE);

        Container main = getContentPane();
        main.removeAll();

        main.setLayout(new BorderLayout());

        // top panel:
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        JPanel findTextPanel = new JPanel(new BorderLayout());
        findTextPanel.add(new JLabel("Text to find: "), BorderLayout.WEST);
        findTextPanel.add(findCBox, BorderLayout.CENTER);
        topPanel.add(findTextPanel);

        JPanel replaceTextPanel = new JPanel(new BorderLayout());
        replaceTextPanel.add(new JLabel("Replace with:"), BorderLayout.WEST);
        replaceTextPanel.add(replaceCBox, BorderLayout.CENTER);
        if (showReplace)
            topPanel.add(replaceTextPanel);

        main.add(topPanel, BorderLayout.NORTH);

        // middle panel:
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));

        JPanel row1 = new JPanel();
        row1.setLayout(new GridLayout(2, 0));

        // options:
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        JCheckBox cbox = new JCheckBox();
        cbox.setAction(actions.getCaseSensitiveOption(cbox));

        optionsPanel.add(cbox);

        cbox = new JCheckBox();
        cbox.setAction(actions.getWholeWordsOption(cbox));
        optionsPanel.add(cbox);

        cbox = new JCheckBox();
        cbox.setAction(actions.getRegularExpressionOption(cbox));
        optionsPanel.add(cbox);

        row1.add(optionsPanel);

        // scope:
        JPanel scopePanel = new JPanel();
        scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
        scopePanel.setBorder(BorderFactory.createTitledBorder("Scope"));

        ButtonGroup scopeButtonGroup = new ButtonGroup();

        JRadioButton globalRB = new JRadioButton();
        scopeButtonGroup.add(globalRB);
        globalRB.setAction(actions.getGlobalScope(globalRB));
        scopePanel.add(globalRB);

        JRadioButton selectionRB = new JRadioButton();
        scopeButtonGroup.add(selectionRB);
        selectionRB.setAction(actions.getSelectionScope(selectionRB));
        scopePanel.add(selectionRB);

        scopePanel.add(Box.createVerticalGlue());
        scopePanel.add(messageLabel);

        row1.add(scopePanel);
        middlePanel.add(row1);

        JPanel row2 = new JPanel();
        row2.setLayout(new GridLayout(2, 0));

        // direction
        JPanel directionPanel = new JPanel();
        directionPanel.setLayout(new BoxLayout(directionPanel, BoxLayout.Y_AXIS));
        directionPanel.setBorder(BorderFactory.createTitledBorder("Direction"));

        ButtonGroup directionButtonGroup = new ButtonGroup();
        JRadioButton forwardRB = new JRadioButton();
        directionButtonGroup.add(forwardRB);
        forwardRB.setAction(actions.getForwardDirection(forwardRB));
        directionPanel.add(forwardRB);

        JRadioButton backwardRB = new JRadioButton();
        directionButtonGroup.add(backwardRB);
        backwardRB.setAction(actions.getBackwardDirection(backwardRB));
        directionPanel.add(backwardRB);

        row2.add(directionPanel);

        // targets
        JPanel targetPanel = new JPanel();
        targetPanel.setLayout(new BoxLayout(targetPanel, BoxLayout.Y_AXIS));
        targetPanel.setBorder(BorderFactory.createTitledBorder("Target"));
        targetCBox.setEditable(false);
        updateTargets();
        targetPanel.add(targetCBox);

        row2.add(targetPanel);
        middlePanel.add(row2);
        middlePanel.add(Box.createVerticalGlue());

        main.add(middlePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(showReplace ? 2 : 1, 0));
        if (showReplace) {
            JPanel replacePanel = new JPanel();
            replacePanel.setLayout(new BoxLayout(replacePanel, BoxLayout.X_AXIS));

            replacePanel.add(new JButton(actions.getFindAndReplace()));
            replacePanel.add(new JButton(actions.getReplaceAll()));
            bottomPanel.add(replacePanel);
        }

        JPanel findPanel = new JPanel();
        findPanel.setBorder(BorderFactory.createEtchedBorder());
        findPanel.setLayout(new BoxLayout(findPanel, BoxLayout.X_AXIS));
        findPanel.add(new JButton(actions.getClose()));
        findPanel.add(Box.createHorizontalGlue());

        findPanel.add(new JButton(actions.getFindAll()));
        findPanel.add(new JButton(actions.getUnselectAll()));
        findPanel.add(new JButton(actions.getFindFromFile()));

        findPanel.add(Box.createHorizontalGlue());
        findPanel.add(new JButton(actions.getFindFirst()));

        JButton nextButton = new JButton(actions.getFindNext());
        findPanel.add(nextButton);
        rootPane.setDefaultButton(nextButton);
        bottomPanel.add(findPanel);

        main.add(bottomPanel, BorderLayout.SOUTH);

        main.validate();
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

        targetCBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    ISearcher searcher = ((SearcherItem) event.getItem()).getSearcher();
                    searchManager.setSearcher(searcher);
                    if (searcher.getParent() != null)
                        parent2active.put(searcher.getParent(), searcher);
                }
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

    class SearcherItem extends JButton {
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
        messageLabel.setText(message);
    }

    /**
     * clears the message
     */
    public void clearMessage() {
        messageLabel.setText("");
    }

    public String getFindText() {
        return findCBox.getCurrentText(true);
    }

    public String getReplaceText() {
        return replaceCBox.getCurrentText(true);
    }


}
