/*
 * ListViewSearcher.java Copyright (C) 2022 Daniel H. Huson
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
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;

import java.util.function.Function;

/**
 * searcher for list view
 * Daniel Huson, 2.2020
 */
public class ListViewSearcher<S> implements IObjectSearcher<String> {
    private final ListView<S> listView;
    private int pos = -1;

    private final BooleanProperty hasData = new SimpleBooleanProperty(false);

    private LabelSetter labelSetter = createDefaultLabelSetter();

    public ListViewSearcher(ListView<S> listView) {
        this.listView = listView;
        hasData.bind(Bindings.isNotEmpty(listView.getItems()));
    }

    @Override
    public boolean gotoFirst() {
        pos = 0;
        return isCurrentSet();
    }

    @Override
    public boolean gotoNext() {
        pos++;
        return isCurrentSet();
    }

    @Override
    public boolean gotoLast() {
        pos = size() - 1;
        return isCurrentSet();
    }

    @Override
    public boolean gotoPrevious() {
        pos--;
        return isCurrentSet();
    }

    @Override
    public boolean isCurrentSet() {
        return pos >= 0 && pos < size();
    }

    @Override
    public boolean isCurrentSelected() {
        return isCurrentSet() && listView.getSelectionModel().isSelected(pos);
    }

    @Override
    public void setCurrentSelected(boolean select) {
        if (isCurrentSet()) {
            final int which = pos;
            final Runnable runnable = () -> {
                if (select)
                    listView.getSelectionModel().select(which);
                else
                    listView.getSelectionModel().clearSelection(which);
            };
            if (Platform.isFxApplicationThread())
                runnable.run();
            else
                Platform.runLater(runnable);
        }
    }

    @Override
    public String getCurrentLabel() {
        return listView.getItems().get(pos).toString();
    }

    @Override
    public Function<String, String> getPrepareTextForReplaceFunction() {
        return null;
    }

    @Override
    public void setCurrentLabel(String newLabel) {
        if (isCurrentSet()) {
            final int which = pos;
            if (Platform.isFxApplicationThread())
                labelSetter.apply(listView, which, newLabel);
            else
                Platform.runLater(() -> labelSetter.apply(listView, which, newLabel));
        }
    }

    @Override
    public int numberOfObjects() {
        return size();
    }

    @Override
    public ReadOnlyObjectProperty<String> foundProperty() {
        return null;
    }

    @Override
    public String getName() {
        return "table";
    }

    @Override
    public ReadOnlyBooleanProperty isGlobalFindable() {
        return hasData;
    }

    @Override
    public ReadOnlyBooleanProperty isSelectionFindable() {
        return new ReadOnlyBooleanWrapper(false);
    }

    @Override
    public void updateView() {
        listView.requestFocus();
    }

    @Override
    public boolean canFindAll() {
        return true;
    }

    @Override
    public void selectAll(boolean select) {
        final Runnable runnable = () -> {
            if (select)
                listView.getSelectionModel().selectAll();
            else
                listView.getSelectionModel().clearSelection();
        };
        if (Platform.isFxApplicationThread())
            runnable.run();
        else
            Platform.runLater(runnable);

    }

    private int size() {
        return listView.getItems().size();
    }

    public interface LabelSetter {
        void apply(ListView listView, int which, String newLabel);
    }

    public LabelSetter getLabelSetter() {
        return labelSetter;
    }

    public void setLabelSetter(LabelSetter labelSetter) {
        this.labelSetter = labelSetter;
    }

    public LabelSetter createDefaultLabelSetter() {
        return (listView, which, newLabel) -> {
            if (listView.getItems().get(which) instanceof String)
                listView.getItems().set(which, newLabel);
            else if (listView.getItems().get(which) instanceof Labeled) {
                ((Labeled) listView.getItems().get(which)).setText(newLabel);
            }
        };
    }
}
