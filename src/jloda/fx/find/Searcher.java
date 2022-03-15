/*
 * Searcher.java Copyright (C) 2022 Daniel H. Huson
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
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * a searcher
 * Daniel Huson, 4.2019
 *
 * @param <T>
 */
public class Searcher<T> implements IObjectSearcher<T> {
    private final ObservableList<T> items;
    private final Function<Integer, Boolean> isSelectedFunction;
    private final BiConsumer<Integer, Boolean> selectCallback;
    private final Function<Integer, String> textGetter;
    private final Function<String, String> prepareTextForReplaceFunction;

    private final BiConsumer<Integer, String> textSetter;
    private final Runnable startReplaceCallback;
    private final Runnable endReplaceCallback;

    private final StringProperty name = new SimpleStringProperty("Searcher");

    private final ObjectProperty<T> found = new SimpleObjectProperty<>();

    private final BooleanProperty globalFindable = new SimpleBooleanProperty(false);

    private final BooleanProperty selectionFindable = new SimpleBooleanProperty(false);

    private int current = 0;

    /**
     * constructor
     *
     * @param items              the lists of items
     * @param isSelectedFunction function that returns selection state for a given item index
     * @param selectCallback     callback for changing the selection state for a given item index
     * @param selectionMode      desired selection mode
     * @param textGetter         gets text for current item
     * @param textSetter         sets text for current item
     */
    public Searcher(ObservableList<T> items, Function<Integer, Boolean> isSelectedFunction, BiConsumer<Integer, Boolean> selectCallback,
                    ObjectProperty<SelectionMode> selectionMode, Function<Integer, String> textGetter,
                    Function<String, String> prepareTextForReplaceFunction,
                    BiConsumer<Integer, String> textSetter, Runnable startReplaceCallback, Runnable endReplace) {
        this.isSelectedFunction = isSelectedFunction;
        this.selectCallback = selectCallback;
        this.items = items;
        this.textGetter = textGetter;
        this.prepareTextForReplaceFunction = prepareTextForReplaceFunction;
        this.textSetter = textSetter;
        this.startReplaceCallback = startReplaceCallback;
        this.endReplaceCallback = endReplace;

        globalFindable.bind(selectionMode.isEqualTo(SelectionMode.MULTIPLE).and(Bindings.size(items).greaterThan(0)));
    }

    @Override
    public boolean gotoFirst() {
        if (items.size() > 0) {
            current = 0;
            return true;
        } else {
            current = -1;
            return false;
        }
    }

    @Override
    public boolean gotoNext() {
        if (current + 1 < items.size()) {
            current++;
            return true;
        } else {
            current = -1;
            return false;
        }
    }

    @Override
    public boolean gotoLast() {
        if (items.size() > 0) {
            current = items.size() - 1;
            return true;
        } else {
            current = -1;
            return false;
        }
    }

    @Override
    public boolean gotoPrevious() {
        if (items.size() > 0 && current > 0) {
            current--;
            return true;
        } else {
            current = -1;
            return false;
        }
    }

    @Override
    public boolean isCurrentSet() {
        return current != -1;
    }

    @Override
    public boolean isCurrentSelected() {
        return isCurrentSet() && isSelectedFunction.apply(current);
    }

    @Override
    public void setCurrentSelected(boolean select) {
        if (isCurrentSet()) {
            var index = current;
            Platform.runLater(() -> {
                selectCallback.accept(index, select);
                found.set(items.get(index));
            });
        }
    }

    @Override
    public String getCurrentLabel() {
        return textGetter.apply(current);
    }

    @Override
    public Function<String, String> getPrepareTextForReplaceFunction() {
        return prepareTextForReplaceFunction;
    }

    @Override
    public void setCurrentLabel(String newLabel) {
        if (textSetter != null && isCurrentSet()) {
            var index = current;
            Platform.runLater(() -> textSetter.accept(index, newLabel));
        }
    }

    @Override
    public int numberOfObjects() {
        return items.size();
    }

    @Override
    public ReadOnlyObjectProperty<T> foundProperty() {
        return found;
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public ReadOnlyBooleanProperty isGlobalFindable() {
        return globalFindable;
    }

    @Override
    public ReadOnlyBooleanProperty isSelectionFindable() {
        return selectionFindable;
    }

    /**
     * run this in the FX thread to update selections
     */
    @Override
    public void updateView() {
     }

    @Override
    public boolean canFindAll() {
        return true;
    }

    @Override
    public void selectAll(boolean select) {
        Platform.runLater(() -> {
            for (int t = 0; t < items.size(); t++) {
                selectCallback.accept(t, select);
            }
        });
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public BooleanProperty selectionFindableProperty() {
        return selectionFindable;
    }

    public void setSelectionFindable(boolean selectionFindable) {
        this.selectionFindable.set(selectionFindable);
    }

    public void startReplace() {
        if (startReplaceCallback != null)
            startReplaceCallback.run();
    }

    public void endReplace() {
        if (endReplaceCallback != null)
            endReplaceCallback.run();
    }
}
