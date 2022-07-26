/*
 * SearchManager.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.find;

import javafx.application.Platform;
import javafx.beans.property.*;
import jloda.fx.util.AService;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.window.NotificationManager;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * search manager
 * Daniel Huson, 1.2018
 */
public class SearchManager {
    private final AService<Integer> service = new AService<>();

    private final ObjectProperty<ISearcher> searcher = new SimpleObjectProperty<>();

    private final BooleanProperty disabled = new SimpleBooleanProperty(true);

    private final BooleanProperty caseSensitiveOption = new SimpleBooleanProperty(false);
    private final BooleanProperty wholeWordsOnlyOption = new SimpleBooleanProperty(false);
    private final BooleanProperty regularExpressionsOption = new SimpleBooleanProperty(false);

    private final BooleanProperty forwardDirection = new SimpleBooleanProperty(true);
    private final BooleanProperty globalScope = new SimpleBooleanProperty(true);

    private final StringProperty searchText = new SimpleStringProperty();
    private final StringProperty replaceText = new SimpleStringProperty();

    private final StringProperty message = new SimpleStringProperty();

    private final BooleanProperty canFindAll = new SimpleBooleanProperty(false);

    public final BooleanProperty canFindGlobally = new SimpleBooleanProperty(false);
    public final BooleanProperty canReplaceInSelection = new SimpleBooleanProperty(false);

    /**
     * constructor
     */
    public SearchManager() {
        disabled.bind(searcher.isNull());

        service.setOnScheduled(e -> message.set(""));

        service.setOnFailed(e -> System.err.println("Search failed: " + service.getException()));

        // any change clears the message:
        caseSensitiveOption.addListener(c -> message.set(""));
        wholeWordsOnlyOption.addListener(c -> message.set(""));
        regularExpressionsOption.addListener(c -> message.set(""));
        forwardDirection.addListener(c -> message.set(""));
        globalScope.addListener(c -> message.set(""));
        searchText.addListener(c -> message.set(""));
        replaceText.addListener(c -> message.set(""));

        searcherProperty().addListener((c, o, n) -> {
            if (n instanceof ITextSearcher || n instanceof TableViewSearcher)
                service.setExecutor(Platform::runLater); // must run text searchers in JavaFX application thread
            else if (n instanceof IObjectSearcher) // run other searchers in external thread
                service.setExecutor(ProgramExecutorService.getInstance());
            canFindAll.set(n.canFindAll());
            canReplaceInSelection.bind(n.isSelectionFindable());
            canFindGlobally.bind(n.isGlobalFindable());
        });
    }

    /**
     * erase the current selection
     */
    public void doUnselectAll() {
        searcher.get().selectAll(false);
    }

    /**
     * find the first occurrence
     */
    public void findFirst() {
        service.setCallable(() -> doFindFirst(service.getProgressListener()) ? 1 : 0);
        service.setOnSucceeded(e -> {
            message.set(service.getValue() > 0 ? "Found" : "No matches");
            if (service.getValue() > 0)
                getSearcher().updateView();
        });
        service.restart();
    }

    /**
     * find the first next
     */
    public void findNext() {
        service.setCallable(() -> doFindNext(service.getProgressListener()) ? 1 : 0);
        service.setOnSucceeded(e -> {
            message.set(service.getValue() > 0 ? "Found" : "No matches");
            if (service.getValue() > 0)
                getSearcher().updateView();
        });
        service.restart();
    }

    /**
     * find all
     */
    public void findAll() {
        service.setCallable(() -> doFindAll(service.getProgressListener()));

        service.setOnSucceeded(e -> {
            message.set(service.getValue() > 0 ? "Found " + service.getValue() : "No matches");
            if (service.getValue() > 0)
                getSearcher().updateView();
        });
        service.restart();
    }

    /**
     * replace and find
     */
    public void findAndReplace() {
        if (!isDisabled()) {
            service.setCallable(() -> doFindAndReplace(service.getProgressListener()) ? 1 : 0);
            if (searcher.get() instanceof Searcher aSearcher) {
                service.setOnScheduled(e -> aSearcher.startReplace());
            }
            service.setOnSucceeded(e -> {
                message.set(service.getValue() > 0 ? "Replaced" : "No matches");
                if (service.getValue() > 0)
                    getSearcher().updateView();
                if (searcher.get() instanceof Searcher aSearcher) {
                    aSearcher.endReplace();
                }
            });
            service.restart();
        }
    }

    /**
     * replace all
     */
    public void replaceAll() {
        if (!isDisabled()) {
            service.setCallable(() -> doReplaceAll(service.getProgressListener()));
            if (searcher.get() instanceof Searcher aSearcher) {
                service.setOnScheduled(e -> aSearcher.startReplace());
            }
            service.setOnSucceeded(e -> {
                message.set(service.getValue() > 0 ? "Replaced " + service.getValue() : "No matches");
                if (service.getValue() > 0)
                    getSearcher().updateView();
                if (searcher.get() instanceof Searcher aSearcher) {
                    aSearcher.endReplace();
                }
            });
            service.restart();
        }
    }

    /**
     * find the first occurrence of the query
     */
    private boolean doFindFirst(ProgressListener progress) throws CanceledException {
        if (isDisabled())
            return false;

        boolean changed = false;
		try {
			getSearcher().selectAll(false);
			if (getSearcher() instanceof IObjectSearcher searcher) {
				var ok = isForwardDirection() ? searcher.gotoFirst() : searcher.gotoLast();

				final var regexp = prepareRegularExpression(getSearchText());
				final var pattern = Pattern.compile(regexp);

				progress.setMaximum(searcher.numberOfObjects());
				progress.setProgress(0);

				while (ok) {
					if (isGlobalScope() || searcher.isCurrentSelected()) {
						var label = searcher.getCurrentLabel();
						if (label == null)
							label = "";
						if (matches(pattern, label)) {
							searcher.setCurrentSelected(true);
							changed = true;
							break;
						}
					}
					ok = isForwardDirection() ? searcher.gotoNext() : searcher.gotoPrevious();
					progress.incrementProgress();
				}
			} else if (getSearcher() instanceof ITextSearcher searcher) {
				searcher.setGlobalScope(isGlobalScope());
				final var regexp = prepareRegularExpression(getSearchText());
				changed = searcher.findFirst(regexp);
			}
		} catch (PatternSyntaxException ex) {
			NotificationManager.showError("Syntax error: " + ex.getMessage());
		}
        return changed;
    }

    /**
     * find the next occurrence of the query
     */
    private boolean doFindNext(ProgressListener progressListener) throws CanceledException {
		if (isDisabled())
			return false;

		boolean changed = false;
		try {
			if (getSearcher() instanceof IObjectSearcher searcher) {
				boolean ok = isForwardDirection() ? searcher.gotoNext() : searcher.gotoPrevious();

				progressListener.setMaximum(-1);

				final var regexp = prepareRegularExpression(getSearchText());
				final var pattern = Pattern.compile(regexp);
				while (ok) {
					if (isGlobalScope() || searcher.isCurrentSelected()) {
						String label = searcher.getCurrentLabel();
						if (label == null)
							label = "";
						if (matches(pattern, label)) {
							searcher.setCurrentSelected(true);
							changed = true;
							break;
						}
					}
					ok = isForwardDirection() ? searcher.gotoNext() : searcher.gotoPrevious();
					progressListener.checkForCancel();
				}
			} else if (getSearcher() instanceof ITextSearcher searcher) {
				searcher.setGlobalScope(isGlobalScope());

				final var regexp = prepareRegularExpression(getSearchText());
				if (isForwardDirection()) {
					changed = searcher.findNext(regexp);
				} else
					changed = searcher.findPrevious(regexp);
			}
		} catch (PatternSyntaxException ex) {
			NotificationManager.showError("Syntax error: " + ex.getMessage());
		}
        return changed;
    }

    /**
     * select all occurrences of the query string
     */
    private int doFindAll(ProgressListener progressListener) throws CanceledException {
		if (isDisabled())
			return 0;

		int count = 0;
		try {
			if (getSearcher() instanceof IObjectSearcher searcher) {
				boolean ok = searcher.gotoFirst();

				progressListener.setMaximum(searcher.numberOfObjects());

				final var regexp = prepareRegularExpression(getSearchText());
				final var pattern = Pattern.compile(regexp);
				while (ok) {
					if (isGlobalScope() || searcher.isCurrentSelected()) {
						String label = searcher.getCurrentLabel();
						if (label == null)
							label = "";
						var select = matches(pattern, label);
						if (select) {
							searcher.setCurrentSelected(true);
							count++;
						}
					}
					ok = searcher.gotoNext();
					progressListener.incrementProgress();
				}
			} else if (getSearcher() instanceof ITextSearcher searcher) {
				searcher.setGlobalScope(isGlobalScope());
				final var regexp = prepareRegularExpression(getSearchText());

				count = searcher.findAll(regexp);
			}
		} catch (PatternSyntaxException ex) {
			NotificationManager.showError("Syntax error: " + ex.getMessage());
		}
        return count;
    }

    /**
     * replace current or next occurrence of the query string
     */
    private boolean doFindAndReplace(ProgressListener progressListener) throws CanceledException {
		if (isDisabled())
			return false;

		var changed = false;
		try {
			if (getSearcher() instanceof IObjectSearcher<?> searcher) {
				progressListener.setMaximum(-1);

				var ok = searcher.isCurrentSet();
				if (!ok)
					ok = isForwardDirection() ? searcher.gotoFirst() : searcher.gotoLast();

				final var regexp = prepareRegularExpression(getSearchText());
				final var pattern = Pattern.compile(regexp);

				while (ok) {
					if (isGlobalScope() || searcher.isCurrentSelected()) {
						var label = searcher.getCurrentLabel();
						if (label == null)
							label = "";
						if (searcher.getPrepareTextForReplaceFunction() != null)
							label = searcher.getPrepareTextForReplaceFunction().apply(label);
						var replace = getReplacement(pattern, getReplaceText(), label);
						if (replace != null && !label.equals(replace)) {
							searcher.setCurrentSelected(true);
							searcher.setCurrentLabel(replace);
							changed = true;
							break;
						}
					}

					ok = isForwardDirection() ? searcher.gotoNext() : searcher.gotoPrevious();
					progressListener.checkForCancel();
				}
			} else if (getSearcher() instanceof ITextSearcher searcher) {
				searcher.setGlobalScope(isGlobalScope());

				final var regexp = prepareRegularExpression(getSearchText());
				changed = searcher.replaceNext(regexp, getReplaceText());
			}
		} catch (PatternSyntaxException ex) {
			NotificationManager.showError("Syntax error: " + ex.getMessage());
		}
        return changed;
    }

    /**
     * replace all occurrences of the query string
     */
    private int doReplaceAll(ProgressListener progressListener) throws CanceledException {
        if (isDisabled())
            return 0;

        int count = 0;

		try {
			if (getSearcher() instanceof IObjectSearcher<?> searcher) {
				var ok = isForwardDirection() ? searcher.gotoFirst() : searcher.gotoLast();
				progressListener.setMaximum(searcher.numberOfObjects());

				final var regexp = prepareRegularExpression(getSearchText());
				final var pattern = Pattern.compile(regexp);

				while (ok) {
					if (isGlobalScope() || searcher.isCurrentSelected()) {
						var label = searcher.getCurrentLabel();
						if (label == null)
							label = "";
						if (searcher.getPrepareTextForReplaceFunction() != null)
							label = searcher.getPrepareTextForReplaceFunction().apply(label);
						var replace = getReplacement(pattern, getReplaceText(), label);
						if (replace != null && !replace.equals(label)) {
							searcher.setCurrentSelected(true);
							searcher.setCurrentLabel(replace);
							count++;
						}
					}
					ok = isForwardDirection() ? searcher.gotoNext() : searcher.gotoPrevious();
					progressListener.incrementProgress();
				}
			} else if (getSearcher() instanceof ITextSearcher searcher) {
				searcher.setGlobalScope(isGlobalScope());

				final var regexp = prepareRegularExpression(getSearchText());
				count = searcher.replaceAll(regexp, getReplaceText(), !isGlobalScope());
			}
		} catch (PatternSyntaxException ex) {
			NotificationManager.showError("Syntax error: " + ex.getMessage());
		}
        return count;
    }

    /**
     * does label match pattern?
     *
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

    public ISearcher getSearcher() {
        return searcher.get();
    }

    public ObjectProperty<ISearcher> searcherProperty() {
        return searcher;
    }

    public void setSearcher(ISearcher searcher) {
        this.searcher.set(searcher);
    }

    public boolean isCaseSensitiveOption() {
        return caseSensitiveOption.get();
    }

    public BooleanProperty caseSensitiveOptionProperty() {
        return caseSensitiveOption;
    }

    public void setCaseSensitiveOption(boolean caseSensitiveOption) {
        this.caseSensitiveOption.set(caseSensitiveOption);
    }

    public boolean isWholeWordsOnlyOption() {
        return wholeWordsOnlyOption.get();
    }

    public BooleanProperty wholeWordsOnlyOptionProperty() {
        return wholeWordsOnlyOption;
    }

    public void setWholeWordsOnlyOption(boolean wholeWordsOnlyOption) {
        this.wholeWordsOnlyOption.set(wholeWordsOnlyOption);
    }

    public boolean isRegularExpressionsOption() {
        return regularExpressionsOption.get();
    }

    public BooleanProperty regularExpressionsOptionProperty() {
        return regularExpressionsOption;
    }

    public void setRegularExpressionsOption(boolean regularExpressionsOption) {
        this.regularExpressionsOption.set(regularExpressionsOption);
    }

    public boolean isForwardDirection() {
        return forwardDirection.get();
    }

    public BooleanProperty forwardDirectionProperty() {
        return forwardDirection;
    }

    public void setForwardDirection(boolean forwardDirection) {
        this.forwardDirection.set(forwardDirection);
    }

    public boolean isGlobalScope() {
        return globalScope.get();
    }

    public BooleanProperty globalScopeProperty() {
        return globalScope;
    }

    public void setGlobalScope(boolean globalScope) {
        this.globalScope.set(globalScope);
    }

    public ReadOnlyBooleanProperty disabledProperty() {
        return disabled;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public String getSearchText() {
        return searchText.get();
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    public String getReplaceText() {
        return replaceText.get();
    }

    public StringProperty replaceTextProperty() {
        return replaceText;
    }

    public void setReplaceText(String replaceText) {
        this.replaceText.set(replaceText);
    }

    public void close() {
        service.cancel();
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public boolean isCanFindAll() {
        return canFindAll.get();
    }

    public ReadOnlyBooleanProperty canFindAllProperty() {
        return canFindAll;
    }

    public void cancel() {
        service.cancel();
    }

    public boolean isCanFindGlobally() {
        return canFindGlobally.get();
    }

    public BooleanProperty canFindGloballyProperty() {
        return canFindGlobally;
    }

    public boolean isCanReplaceInSelection() {
        return canReplaceInSelection.get();
    }

    public BooleanProperty canReplaceInSelectionProperty() {
        return canReplaceInSelection;
    }
}
