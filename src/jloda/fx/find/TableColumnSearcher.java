/*
 * ListViewSearcher.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.fx.find;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.function.BiConsumer;

/**
 * searcher for table column of strings
 * Daniel Huson, 11.2021
 */
public class TableColumnSearcher<S> implements IObjectSearcher<String> {
    private final TableView<S> tableView;
    private final TableColumn<S, String> tableColumn;
    private int pos = -1;

    private final BooleanProperty hasData = new SimpleBooleanProperty(false);

    private final BiConsumer<S, String> labelSetter;

    public TableColumnSearcher(TableColumn<S, String> tableColumn, BiConsumer<S, String> labelSetter) {
        this.tableView = tableColumn.getTableView();
        this.tableColumn = tableColumn;
        this.labelSetter = labelSetter;
        hasData.bind(Bindings.isNotEmpty(tableColumn.getTableView().getItems()));
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
        return isCurrentSet() && tableView.getSelectionModel().isSelected(pos);
    }

    @Override
    public void setCurrentSelected(boolean select) {
        if (isCurrentSelected()) {
            final int which = pos;
            final Runnable runnable = () -> {
                if (select)
                    tableView.getSelectionModel().select(which);
                else
                    tableView.getSelectionModel().clearSelection(which);
            };
            if (Platform.isFxApplicationThread())
                runnable.run();
            else
                Platform.runLater(runnable);
        }
    }

    @Override
    public String getCurrentLabel() {
        return tableColumn.getCellData(pos);
    }

    @Override
    public void setCurrentLabel(String newLabel) {
        if (isCurrentSet() && labelSetter != null) {
            final int which = pos;
            if (Platform.isFxApplicationThread())
                labelSetter.accept(tableView.getItems().get(which), newLabel);
            else
                Platform.runLater(() -> labelSetter.accept(tableView.getItems().get(which), newLabel));
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
        tableColumn.getTableView().requestFocus();
    }

    @Override
    public boolean canFindAll() {
        return tableView.getSelectionModel().getSelectionMode() == SelectionMode.MULTIPLE;
    }

    @Override
    public void selectAll(boolean select) {
        final Runnable runnable = () -> {
            if (select)
                tableView.getSelectionModel().selectAll();
            else
                tableView.getSelectionModel().clearSelection();
        };
        if (Platform.isFxApplicationThread())
            runnable.run();
        else
            Platform.runLater(runnable);

    }

    private int size() {
        return tableView.getItems().size();
    }

}
