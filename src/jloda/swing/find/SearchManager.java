/*
 * SearchManager.java Copyright (C) 2019. Daniel H. Huson
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

import jloda.swing.commands.CommandManager;
import jloda.swing.director.IDirectableViewer;
import jloda.swing.director.IDirector;
import jloda.swing.director.IViewerWithFindToolBar;
import jloda.swing.util.Alert;
import jloda.swing.util.Message;
import jloda.swing.util.ProgressDialog;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import jloda.util.ProgressSilent;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * contains the logic to find and replace strings
 * Daniel Huson, 7.2008
 */
public class SearchManager implements IDirectableViewer {
    private IDirector dir;
    private boolean caseSensitiveOption = false;
    private boolean wholeWordsOnlyOption = false;
    private boolean regularExpressionsOption = false;
    private boolean forwardDirection = true;
    private boolean globalScope = true;

    private Thread worker = null;

    private boolean equateUnderscoreWithSpace = false;

    private boolean isLocked = false;

    ISearcher[] targets;
    private ISearcher searcher;

    final IFindDialog findDialog;

    final Set<String> disabledSearchers = new HashSet<>();

    static SearchManager instance;

    private boolean showReplace;
    private boolean allowReplace = true;

    /**
     * constructor. This does not create a single instance.
     *
     * @param dir
     * @param viewer
     * @param target
     * @param showReplace
     * @param createToolBar
     */
    public SearchManager(IDirector dir, IViewerWithFindToolBar viewer, ISearcher target, boolean showReplace, boolean createToolBar) {
        if (!createToolBar) {
            if (getInstance() != null)
                new Alert("Internal error, multiple instances of SearchManager");
            else
                setInstance(this);
        }
        this.dir = dir;
        this.targets = new ISearcher[]{target};
        this.showReplace = showReplace;
        searcher = targets[0];
        if (createToolBar)
            findDialog = new FindToolBar(this, viewer, new SearchActions(this), showReplace, target.getAdditionalButtons());
        else
            findDialog = new FindWindow(searcher.getParent(), "", this, new SearchActions(this));
    }

    /**
     * if constructor was instructed to create a tool bar, returns the tool bar, else returns null
     *
     * @return find toolbar or nul
     */
    public FindToolBar getFindDialogAsToolBar() {
        if (findDialog instanceof FindToolBar)
            return (FindToolBar) findDialog;
        else
            return null;
    }


    /**
     * constructor
     *
     * @param dir
     * @param title
     * @param targets
     * @param showReplace
     */
    public SearchManager(IDirector dir, String title, ISearcher[] targets, boolean showReplace) {
        if (getInstance() != null)
            new Alert("Internal error, multiple instances of SearchManager");
        else
            setInstance(this);
        this.dir = dir;
        this.targets = targets;
        this.showReplace = showReplace;
        searcher = targets[0];
        findDialog = new FindWindow(searcher.getParent(), title, this, new SearchActions(this));
    }

    /**
     * constructor
     *
     * @param title
     * @param targets
     * @param showReplace
     */
    public SearchManager(String title, ISearcher[] targets, boolean showReplace) {
        if (getInstance() != null)
            new Alert("Internal error, multiple instances of SearchManager");
        else
            setInstance(this);
        this.dir = null;
        this.targets = targets;
        this.showReplace = showReplace;
        searcher = targets[0];
        findDialog = new FindWindow(searcher.getParent(), title, this, new SearchActions(this));
    }

    /**
     * constructor for non-gui version. Doesn't set instance!
     *
     * @param targets
     */
    public SearchManager(ISearcher[] targets) {
        this.dir = null;
        this.targets = targets;
        this.showReplace = false;
        searcher = targets[0];
        findDialog = null;
    }

    private String searchText = null;
    private String replaceText = null;

    public boolean isCaseSensitiveOption() {
        return caseSensitiveOption;
    }

    public void setCaseSensitiveOption(boolean caseSensitiveOption) {
        this.caseSensitiveOption = caseSensitiveOption;
    }

    public boolean isWholeWordsOnlyOption() {
        return wholeWordsOnlyOption;
    }

    public void setWholeWordsOnlyOption(boolean wholeWordsOnlyOption) {
        this.wholeWordsOnlyOption = wholeWordsOnlyOption;
    }

    public boolean isRegularExpressionsOption() {
        return regularExpressionsOption;
    }

    public void setRegularExpressionsOption(boolean regularExpressionsOption) {
        this.regularExpressionsOption = regularExpressionsOption;
    }

    public boolean isForwardDirection() {
        return forwardDirection;
    }

    public void setForwardDirection(boolean forwardDirection) {
        this.forwardDirection = forwardDirection;
    }

    public boolean isGlobalScope() {
        return globalScope;
    }

    public void setGlobalScope(boolean globalScope) {
        this.globalScope = globalScope;
    }

    public void setSearcher(ISearcher searcher) {
        this.searcher = searcher;
        findDialog.getActions().setEnableCritical(!disabledSearchers.contains(getSearcher().getName())); // turn off stuff is this search is disabled
    }

    public ISearcher getSearcher() {
        return searcher;
    }

    /**
     * replace current or next occurrence of the query string
     */
    public void applyFindAndReplace() {
        if (isCommandLineMode()) {
            final boolean found = doFindAndReplace(new ProgressSilent());
            System.err.println(found ? "Replaced" : "No replacements");
        } else {
            findDialog.clearMessage();
            if (worker == null || !worker.isAlive()) {
                worker = new Thread(() -> {
                    notifyLockUserInput();
                    final boolean found = doFindAndReplace(new ProgressDialog("Search", "Find and replace", searcher.getParent()));
                    SwingUtilities.invokeLater(() -> findDialog.setMessage(found ? "Replaced" : "No replacements"));
                    notifyUnlockUserInput();
                });
                worker.setPriority(Thread.currentThread().getPriority() - 1);
                worker.start();
            }
        }
    }

    /**
     * replace current or next occurrence of the query string
     */
    private boolean doFindAndReplace(ProgressListener progressListener) {
        boolean changed = false;
        try {
            if (searcher instanceof IObjectSearcher) {
                IObjectSearcher oSearcher = (IObjectSearcher) searcher;

                progressListener.setMaximum(-1);

                boolean ok = oSearcher.isCurrentSet();
                if (!ok)
                    ok = isForwardDirection() ? oSearcher.gotoFirst() : oSearcher.gotoLast();

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                final Pattern pattern = Pattern.compile(regexp);

                try (progressListener) {
                    while (ok) {
                        if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                            String label = oSearcher.getCurrentLabel();
                            if (equateUnderscoreWithSpace)
                                label = label.replaceAll("_", " ");
                            if (label == null)
                                label = "";
                            String replace = getReplacement(pattern, replaceText, label);
                            if (replace != null && !label.equals(replace)) {
                                oSearcher.setCurrentSelected(true);
                                oSearcher.setCurrentLabel(replace);
                                changed = true;
                                break;
                            }
                        }

                        ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();
                        progressListener.checkForCancel();

                    }
                } catch (CanceledException e) {
                    System.err.println("Search canceled");
                }
            } else if (searcher instanceof ITextSearcher) {
                ITextSearcher tSearcher = (ITextSearcher) searcher;
                tSearcher.setGlobalScope(isGlobalScope());

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                changed = tSearcher.replaceNext(regexp, replaceText);
            }
        } catch (Exception ex) {
            new Alert(findDialog.getFrame(), "Error: " + ex);
        }
        if (changed) {
            searcher.updateView();
        }
        return changed;
    }

    /**
     * erase the current selection
     */
    public void applyUnselectAll() {
        findDialog.clearMessage();
        searcher.selectAll(false);
    }

    /**
     * find the first occurrence of the query
     */
    public void applyFindFirst() {
        if (isCommandLineMode()) {
            boolean found = doFindFirst();
            System.err.println(found ? "found" : "no matches");
        } else {
            findDialog.clearMessage();
            if (worker == null || !worker.isAlive()) {
                worker = new Thread(() -> {
                    notifyLockUserInput();
                    final boolean found = doFindFirst();

                    SwingUtilities.invokeLater(() -> findDialog.setMessage(found ? "Found" : "No matches"));
                    notifyUnlockUserInput();
                });
                worker.setPriority(Thread.currentThread().getPriority() - 1);
                worker.start();
            }
        }
    }

    /**
     * find the first occurrence of the query
     */
    private boolean doFindFirst() {
        boolean changed = false;
        try {
            searcher.selectAll(false);
            if (searcher instanceof IObjectSearcher) {
                IObjectSearcher oSearcher = (IObjectSearcher) searcher;
                boolean ok = isForwardDirection() ? oSearcher.gotoFirst() : oSearcher.gotoLast();

                ProgressListener progressListener = (searcher.getParent() != null ?
                        (new ProgressDialog("Search", "Find first", searcher.getParent())) : new ProgressSilent());
                progressListener.setMaximum(oSearcher.numberOfObjects());

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                final Pattern pattern = Pattern.compile(regexp);

                try {
                    while (ok) {
                        if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                            String label = oSearcher.getCurrentLabel();
                            if (label == null)
                                label = "";
                            if (equateUnderscoreWithSpace)
                                label = label.replaceAll("_", " ");
                            if (matches(pattern, label)) {
                                oSearcher.setCurrentSelected(true);
                                changed = true;
                                break;
                            }
                        }
                        ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();
                        progressListener.incrementProgress();
                    }
                } catch (CanceledException e) {
                    System.err.println("Search canceled");
                } finally {
                    progressListener.close();
                }
            } else if (searcher instanceof ITextSearcher) {
                ITextSearcher tSearcher = (ITextSearcher) searcher;
                tSearcher.setGlobalScope(isGlobalScope());

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                changed = tSearcher.findFirst(regexp);
            }
        } catch (Exception ex) {
            new Alert(findDialog.getFrame(), "Error: " + ex);
        }
        if (changed)
            searcher.updateView();
        return changed;
    }

    /**
     * find the next occurrence of the query
     */
    public void applyFindNext() {
        if (isCommandLineMode()) {
            final boolean found = doFindNext(new ProgressSilent());
            System.err.println(found ? "found" : "no matches");
        } else {
            findDialog.clearMessage();
            if (worker == null || !worker.isAlive()) {
                worker = new Thread(() -> {
                    notifyLockUserInput();
                    final boolean found = doFindNext(new ProgressDialog("Search", "Find next", searcher.getParent()));

                    SwingUtilities.invokeLater(() -> findDialog.setMessage(found ? "Found" : "No matches"));
                    notifyUnlockUserInput();
                });
                worker.setPriority(Thread.currentThread().getPriority() - 1);
                worker.start();
            }
        }
    }

    /**
     * find the next occurrence of the query
     */
    private boolean doFindNext(ProgressListener progressListener) {
        boolean changed = false;
        try {
            if (searcher instanceof IObjectSearcher) {
                IObjectSearcher oSearcher = (IObjectSearcher) searcher;
                boolean ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();

                progressListener.setMaximum(-1);

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                final Pattern pattern = Pattern.compile(regexp);
                try (progressListener) {
                    while (ok) {
                        if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                            String label = oSearcher.getCurrentLabel();
                            if (label == null)
                                label = "";
                            if (equateUnderscoreWithSpace)
                                label = label.replaceAll("_", " ");
                            if (matches(pattern, label)) {
                                oSearcher.setCurrentSelected(true);
                                changed = true;
                                break;
                            }
                        }
                        ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();
                        progressListener.checkForCancel();
                    }
                } catch (CanceledException e) {
                    System.err.println("Search canceled");
                }
            } else if (searcher instanceof ITextSearcher) {
                ITextSearcher tSearcher = (ITextSearcher) searcher;
                tSearcher.setGlobalScope(isGlobalScope());

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                if (isForwardDirection()) {
                    changed = tSearcher.findNext(regexp);
                } else
                    changed = tSearcher.findPrevious(regexp);
            }
        } catch (Exception ex) {
            new Alert(findDialog.getFrame(), "Error: " + ex);
        }
        if (changed)
            searcher.updateView();
        return changed;
    }


    /**
     * select all occurrences of the query string
     */
    public void applyFindAll() {
        if (isCommandLineMode()) {
            final int found = Math.abs(doFindAll(new ProgressSilent()));
            System.err.println("Found: " + found);
        } else {
            findDialog.clearMessage();
            if (worker == null || !worker.isAlive()) {
                worker = new Thread(() -> {
                    notifyLockUserInput();
                    ProgressListener progressListener = new ProgressDialog("Search", "Find all", searcher.getParent());
                    int found = doFindAll(progressListener);
                    if (found == Integer.MIN_VALUE)
                        found = 0;
                    final int finalFound = Math.abs(found);
                    progressListener.close();
                    SwingUtilities.invokeLater(() -> findDialog.setMessage("Found: " + finalFound));
                    notifyUnlockUserInput();
                });
                worker.setPriority(Thread.currentThread().getPriority() - 1);
                worker.start();
            }
        }
    }

    /**
     * select all occurrences of the query string
     */
    private int doFindAll(ProgressListener progressListener) {
        boolean changed = false;
        int count = 0;
        boolean canceled = false;
        try {
            if (searcher instanceof IObjectSearcher) {
                IObjectSearcher oSearcher = (IObjectSearcher) searcher;
                boolean ok = oSearcher.gotoFirst();

                progressListener.setMaximum(oSearcher.numberOfObjects());

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                final Pattern pattern = Pattern.compile(regexp);
                try {
                    while (ok) {
                        if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                            String label = oSearcher.getCurrentLabel();
                            if (label == null)
                                label = "";
                            if (equateUnderscoreWithSpace)
                                label = label.replaceAll("_", " ");
                            boolean select = matches(pattern, label);
                            if (select) {
                                if (!oSearcher.isCurrentSelected()) {
                                    changed = true;
                                }
                                oSearcher.setCurrentSelected(true);
                                count++;
                            }
                        }
                        ok = oSearcher.gotoNext();
                        progressListener.incrementProgress();
                    }
                } catch (CanceledException e) {
                    System.err.println("Search canceled");
                    canceled = true;
                }
            } else if (searcher instanceof ITextSearcher) {
                ITextSearcher tSearcher = (ITextSearcher) searcher;
                tSearcher.setGlobalScope(isGlobalScope());

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                count = tSearcher.findAll(regexp);
                if (count > 0)
                    changed = true;
            }
        } catch (Exception ex) {
            new Alert(findDialog.getFrame(), "Error: " + ex);
        }
        if (changed)
            searcher.updateView();
        if (canceled)
            return count > 0 ? -count : Integer.MIN_VALUE; // negative count to indicate that this was canceled
        else
            return count;
    }

    /**
     * find all strings present in the given file
     *
     * @param file
     */
    public void findFromFile(final File file) throws IOException {
        if (isCommandLineMode()) {
            {
                int count = 0;
                if (file != null && file.exists()) {
                    BufferedReader r = new BufferedReader(new FileReader(file));
                    String aLine;
                    while ((aLine = r.readLine()) != null) {
                        aLine = aLine.trim();
                        if (aLine.length() > 0 && !aLine.startsWith("#")) {
                            System.err.println("find and select: " + aLine);
                            setSearchText(aLine);
                            int found = doFindAll(new ProgressSilent());
                            boolean canceled = (found < 0);
                            count += Math.abs(found);
                            if (canceled)
                                break;
                        }
                    }
                    if (count > 0)
                        searcher.updateView();
                }
            }
        } else {
            findDialog.clearMessage();
            if (worker == null || !worker.isAlive()) {
                worker = new Thread(() -> {
                    notifyLockUserInput();
                    int count = 0;

                    try {

                        if (file != null && file.exists()) {
                            BufferedReader r = new BufferedReader(new FileReader(file));

                            ProgressListener progressListener = new ProgressDialog("Search", "Find all", searcher.getParent());
                            String aLine;
                            while ((aLine = r.readLine()) != null) {
                                aLine = aLine.trim();
                                if (aLine.length() > 0 && !aLine.startsWith("#")) {
                                    System.err.println("find and select: " + aLine);
                                    setSearchText(aLine);
                                    int found = doFindAll(progressListener);
                                    boolean canceled = (found < 0);
                                    if (found != Integer.MIN_VALUE)
                                        count += Math.abs(found);
                                    if (canceled)
                                        break;
                                }
                            }
                            progressListener.close();
                        }
                    } catch (Exception ex) {
                        Basic.caught(ex);
                    }
                    final int finalCount = Math.abs(count);
                    SwingUtilities.invokeLater(() -> new Message(findDialog.getFrame(), "Matches: " + finalCount, 150, 100));
                    notifyUnlockUserInput();
                });
                worker.setPriority(Thread.currentThread().getPriority() - 1);
                worker.start();
            }
        }
    }

    /**
     * replace all occurrences of the query string
     */
    public void applyReplaceAll() {
        if (isCommandLineMode()) {
            final int found = doReplaceAll();
            System.err.println("Replacements: " + found);
        } else {
            findDialog.clearMessage();
            if (worker == null || !worker.isAlive()) {
                worker = new Thread(() -> {
                    notifyLockUserInput();
                    final int found = doReplaceAll();
                    SwingUtilities.invokeLater(() -> findDialog.setMessage("Replacements: " + found));
                    notifyUnlockUserInput();
                });
                worker.setPriority(Thread.currentThread().getPriority() - 1);
                worker.start();
            }
        }
    }

    /**
     * replace all occurrences of the query string
     */
    private int doReplaceAll() {
        int count = 0;
        boolean changed = false;

        try {
            if (searcher instanceof IObjectSearcher) {
                IObjectSearcher oSearcher = (IObjectSearcher) searcher;
                boolean ok = isForwardDirection() ? oSearcher.gotoFirst() : oSearcher.gotoLast();

                ProgressListener progressListener = (searcher.getParent() != null ?
                        (new ProgressDialog("Search", "Replace all", searcher.getParent())) : new ProgressSilent());
                progressListener.setMaximum(oSearcher.numberOfObjects());

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                final Pattern pattern = Pattern.compile(regexp);

                try {
                    while (ok) {
                        if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                            String label = oSearcher.getCurrentLabel();
                            if (label == null)
                                label = "";
                            if (equateUnderscoreWithSpace)
                                label = label.replaceAll("_", " ");
                            String replace = getReplacement(pattern, replaceText, label);
                            if (replace != null && !replace.equals(label)) {
                                oSearcher.setCurrentSelected(true);
                                oSearcher.setCurrentLabel(replace);
                                changed = true;
                                count++;
                            }
                        }
                        ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();
                        progressListener.incrementProgress();
                    }
                } catch (CanceledException e) {
                    System.err.println("Search canceled");
                } finally {
                    progressListener.close();
                }
            } else if (searcher instanceof ITextSearcher) {
                ITextSearcher tSearcher = (ITextSearcher) searcher;
                tSearcher.setGlobalScope(isGlobalScope());

                final String regexp = prepareRegularExpression(equateUnderscoreWithSpace ? searchText.replaceAll("_", " ") : searchText);
                count = tSearcher.replaceAll(regexp, replaceText, !isGlobalScope());
                if (count > 0)
                    changed = true;
            }
            if (changed) {
                searcher.updateView();
            }
        } catch (Exception ex) {
            new Alert(findDialog.getFrame(), "Error: " + ex);
        }
        return count;
    }

    /**
     * does label match pattern?
     *
     * @param pattern
     * @param label
     * @return true, if match
     */
    private boolean matches(Pattern pattern, String label) {
        if (label == null)
            label = "";
        Matcher matcher = pattern.matcher(label);
        return matcher.find();
    }

    /**
     * determines whether pattern matches label.
     *
     * @param pattern
     * @param replacement
     * @param label
     * @return result of replacing query by replace string in label
     */
    private String getReplacement(Pattern pattern, String replacement, String label) {
        if (label == null)
            label = "";
        if (replacement == null)
            replacement = "";

        Matcher matcher = pattern.matcher(label);
        return matcher.replaceAll(replacement);
    }

    /**
     * prepares the regular expression that reflects the chosen find options
     *
     * @param query
     * @return regular expression
     */
    private String prepareRegularExpression(String query) {
        if (query == null)
            query = "";

        String regexp = "" + query; //Copy the search string over.

        /* Reg expression or not? If not regular expression, we need to surround the above
        with quote literals: \Q expression \E just in case there are some regexp characters
        already there. Note - this will fail if string already contains \E or \Q !!!!!!! */
        if (!isRegularExpressionsOption()) {
            if (regexp.contains("\\E"))
                throw new PatternSyntaxException("Illegal character ''\\'' in search string", query, -1);
            // TODO: this doesn't seem to work here, perhaps needs 1.5?
            regexp = '\\' + "Q" + regexp + '\\' + "E";
        }

        if (isWholeWordsOnlyOption())
            regexp = "\\b" + regexp + "\\b";

        /* Check if case insensitive - if it is, then append (?i) before string */
        if (!isCaseSensitiveOption())
            regexp = "(?i)" + regexp;

        //System.err.println(regexp);
        return regexp;
    }

    /**
     * the current query string
     *
     * @return query
     */
    public String getSearchText() {
        return searchText;
    }

    /**
     * set the current query string
     *
     * @param searchText
     */
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    /**
     * get the current replacement string
     *
     * @return replacement
     */
    public String getReplaceText() {
        return replaceText;
    }

    /**
     * set the current replacement string
     *
     * @param replaceText
     */
    public void setReplaceText(String replaceText) {
        this.replaceText = replaceText;
    }

    /**
     * return the frame associated with the viewer
     *
     * @return frame
     */
    public JFrame getFrame() {
        return findDialog.getFrame();
    }

    /**
     * gets the title
     *
     * @return title
     */
    public String getTitle() {
        return findDialog.getFrame().getTitle();
    }

    /**
     * is viewer uptodate?
     *
     * @return uptodate
     */
    public boolean isUptoDate() {
        return true;
    }

    /**
     * ask tree to destroy itself
     */
    public void destroyView() throws CanceledException {
        // because the searchmanager is directed by all documents, don't want it to close when one document is closed
        //searchWindow.getFrame().setVisible(false);
    }

    /**
     * ask tree to prevent user input
     */
    public void lockUserInput() {
        isLocked = true;
        if (findDialog != null)
            findDialog.getActions().setEnableCritical(false);
    }

    public boolean isLocked() {
        return isLocked;
    }

    /**
     * set uptodate state
     *
     * @param flag
     */
    public void setUptoDate(boolean flag) {
    }

    /**
     * ask tree to allow user input
     */
    public void unlockUserInput() {
        if (isLocked) {
            isLocked = false;
            if (findDialog != null)
                findDialog.getActions().setEnableCritical(true);
        }
    }

    /**
     * ask tree to update itself. This is method is wrapped into a runnable object
     * and put in the swing event queue to avoid concurrent modifications.
     *
     * @param what what should be updated? Possible values: Director.ALL or Director.TITLE
     */
    public void updateView(String what) {
        if (findDialog != null) {
            findDialog.getActions().updateEnableState();
            if (disabledSearchers.contains(getSearcher().getName()))
                findDialog.getActions().setEnableCritical(false); // turn off stuff is this search is disabled
        }
    }

    /**
     * chooses the current searcher by name
     *
     * @param name
     * @return true, if found
     */
    public boolean chooseSearcher(String name) {
        if (findDialog != null)
            return findDialog.selectTarget(name);
        else  // need this in command-line mode:
        {
            for (ISearcher target : targets) {
                if (target.getName().equalsIgnoreCase(name)) {
                    searcher = target;
                    updateView(IDirector.ALL);
                    return true;
                }
            }
            // if no exact match, use prefix
            for (ISearcher target : targets) {
                if (target.getName().toLowerCase().startsWith(name.toLowerCase())) {
                    searcher = target;
                    updateView(IDirector.ALL);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * replace the set of searchers by a new set
     *
     * @param searchers
     * @param showReplace
     */
    public void replaceSearchers(IDirector dir, ISearcher[] searchers, boolean showReplace) {
        this.dir = dir;
        if (searchers != this.targets) {
            this.targets = searchers;
            searcher = searchers[0];
            if (findDialog != null) {
                findDialog.updateTargets();
                updateView(IDirector.ALL);
            }
        }
    }

    /**
     * enable or disable the named searcher
     *
     * @param searcherName
     * @param enable
     */
    public void setEnabled(String searcherName, boolean enable) {
        if (enable)
            disabledSearchers.remove(searcherName);
        else {
            disabledSearchers.add(searcherName);
            if (searcher.getName().equals(searcherName)) {
                for (ISearcher target : targets) {
                    if (!disabledSearchers.contains(target.getName())) {
                        searcher = target;
                        break;
                    }
                }
            }
        }
        updateView(IDirector.ALL);
    }

    /**
     * is named searcher currently enabled?
     *
     * @param searcherName
     * @return true, if enabled
     */
    public boolean isEnabled(String searcherName) {
        return !disabledSearchers.contains(searcherName);
    }

    /**
     * is replace part of dialog showning?
     *
     * @return true, if replace showing
     */
    public boolean getShowReplace() {
        return showReplace;
    }

    /**
     * show or hide replace dialog
     *
     * @param showReplace
     */
    public void setShowReplace(boolean showReplace) {
        if (showReplace != this.showReplace) {
            this.showReplace = showReplace;
            setAllowReplace(showReplace);
            if (findDialog != null)
                findDialog.updateTargets();
        }
    }

    /**
     * run a find. This is used in the command line version of a program
     *
     * @param searchText
     * @param target
     * @param all
     * @param regularExpression
     * @param wholeWord
     * @param caseSensitive
     */
    public void runFind(String searchText, String target, boolean all, boolean regularExpression, boolean wholeWord, boolean caseSensitive) {
        chooseSearcher(target);
        setSearchText(searchText);
        setRegularExpressionsOption(regularExpression);
        setWholeWordsOnlyOption(wholeWord);
        setCaseSensitiveOption(caseSensitive);
        if (!all)
            doFindNext(new ProgressSilent());
        else
            applyFindAll();
    }

    /**
     * run a find. This is used in the command line version of a program
     *
     * @param searchFile
     * @param target
     * @param regularExpression
     * @param wholeWord
     * @param caseSensitive
     */
    public void runFindFromFile(File searchFile, String target, boolean regularExpression, boolean wholeWord, boolean caseSensitive) throws IOException {
        chooseSearcher(target);
        setRegularExpressionsOption(regularExpression);
        setWholeWordsOnlyOption(wholeWord);
        setCaseSensitiveOption(caseSensitive);
        findFromFile(searchFile);
    }

    /**
     * run a find and replace. This is used in the command line version of a program
     *
     * @param searchText
     * @param replaceText
     * @param target
     * @param all
     * @param regularExpression
     * @param wholeWord
     * @param caseSensitive
     */
    public boolean runFindReplace(String searchText, String replaceText, String target, boolean all, boolean regularExpression, boolean wholeWord, boolean caseSensitive) {
        chooseSearcher(target);
        setSearchText(searchText);
        setReplaceText(replaceText);
        setRegularExpressionsOption(regularExpression);
        setWholeWordsOnlyOption(wholeWord);
        setCaseSensitiveOption(caseSensitive);

        if (!all) {
            return doFindAndReplace(new ProgressSilent());
        } else {
            return doReplaceAll() > 0;
        }
    }

    /**
     * gets names of all targets
     *
     * @return names
     */
    public String[] getTargetNames() {
        String[] names = new String[targets.length];

        for (int i = 0; i < names.length; i++)
            names[i] = targets[i].getName();
        return names;
    }

    /**
     * equate an underscore in a label with a space in the query?
     *
     * @return true, if set
     */
    public boolean isEquateUnderscoreWithSpace() {
        return equateUnderscoreWithSpace;
    }

    /**
     * equate an underscore in a label with a space in the query?
     *
     * @param equateUnderscoreWithSpace
     */
    public void setEquateUnderscoreWithSpace(boolean equateUnderscoreWithSpace) {
        this.equateUnderscoreWithSpace = equateUnderscoreWithSpace;
    }

    /**
     * gets the instance of the search manager.
     * This will return null, if not yet set
     *
     * @return instance of search manager
     */
    public static SearchManager getInstance() {
        return instance;
    }

    /**
     * sets the instance of the search manager
     *
     * @param instance
     */
    public static void setInstance(SearchManager instance) {
        SearchManager.instance = instance;
    }


    /**
     * notifies the director to lock user input
     */
    private void notifyLockUserInput() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                if (dir != null)
                    dir.notifyLockInput();
            });
        } catch (Exception e) {
        }
    }

    /**
     * notifies the director to unlock user input
     */
    private void notifyUnlockUserInput() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                if (dir != null)
                    dir.notifyUnlockInput();
            });
        } catch (Exception e) {
        }
    }

    private boolean isCommandLineMode() {
        return findDialog == null;
    }

    public void chooseTargetForFrame(Component parent) {
        if (!isLocked && findDialog != null)
            findDialog.chooseTargetForFrame(parent);
    }

    public boolean isAllowReplace() {
        return allowReplace;
    }

    public void setAllowReplace(boolean allowReplace) {
        this.allowReplace = allowReplace;
    }

    public CommandManager getCommandManager() {
        return null;
    }

    public IDirector getDir() {
        return dir;
    }

    /**
     * get the name of the class
     *
     * @return class name
     */
    @Override
    public String getClassName() {
        return "SearchManager";
    }
}
