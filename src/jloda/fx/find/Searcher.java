/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package jloda.fx.find;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import jloda.fx.control.AnotherMultipleSelectionModel;

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
    private final AnotherMultipleSelectionModel<T> selectionModel;
    private final Function<T, String> textGetter;
    private final BiConsumer<T, String> textSetter;
    private final StringProperty name = new SimpleStringProperty("Searcher");

    private final BooleanProperty globalFindable = new SimpleBooleanProperty(false);

    private final BooleanProperty selectionFindable = new SimpleBooleanProperty(false);

    private int current = 0;

    /**
     * constructor
     *
     * @param selectionModel
     * @param textGetter
     * @param textSetter
     */
    public Searcher(AnotherMultipleSelectionModel<T> selectionModel, Function<T, String> textGetter, BiConsumer<T, String> textSetter) {
        this.selectionModel = selectionModel;
        this.items = selectionModel.getItems();
        this.textGetter = textGetter;
        this.textSetter = textSetter;

        globalFindable.bind(selectionModel.selectionModeProperty().isEqualTo(SelectionMode.MULTIPLE).and(Bindings.size(items).greaterThan(0)));

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
        return isCurrentSet() && selectionModel.isSelected(current);
    }

    @Override
    public void setCurrentSelected(boolean select) {
        selectionModel.select(current);
    }

    @Override
    public String getCurrentLabel() {
        return textGetter.apply(items.get(current));
    }

    @Override
    public void setCurrentLabel(String newLabel) {
        if (textSetter != null)
            textSetter.accept(items.get(current), newLabel);

    }

    @Override
    public int numberOfObjects() {
        return items.size();
    }

    @Override
    public ReadOnlyObjectProperty<T> foundProperty() {
        return null;
    }

    @Override
    public MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel;
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

    @Override
    public void updateView() {

    }

    @Override
    public boolean canFindAll() {
        return items.size() > 0 && selectionModel.getSelectedItems().size() < items.size();
    }

    @Override
    public void selectAll(boolean select) {
        selectionModel.selectAll();

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
}
