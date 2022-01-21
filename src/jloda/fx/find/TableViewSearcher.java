/*
 * TableViewSearcher.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.TableView;

import java.util.function.Function;

/**
 * searcher for my table view
 * Daniel Huson, 2019
 */
public class TableViewSearcher<S> implements IObjectSearcher<String> {
    private final TableView<S> tableView;
    private int row = -1;
    private int col = -1;

    private final BooleanProperty hasData = new SimpleBooleanProperty(false);

    public TableViewSearcher(TableView<S> tableView) {
        this.tableView = tableView;
        hasData.bind(Bindings.isNotEmpty(tableView.getItems()));
    }

    @Override
    public boolean gotoFirst() {
        row = 0;
        col = 0;
        return isCurrentSet();
    }

    @Override
    public boolean gotoNext() {
        if (tableView.getSelectionModel().isCellSelectionEnabled()) {
            if (col < getColCount() - 1) {
                col++;
            } else {
                row++;
                col = 0;
            }
        } else
            row++;
        return isCurrentSet();
    }

    @Override
    public boolean gotoLast() {
        row = getRowCount() - 1;
        col = getColCount() - 1;
        return isCurrentSet();
    }

    @Override
    public boolean gotoPrevious() {
        if (tableView.getSelectionModel().isCellSelectionEnabled()) {
            if (col > 0) {
                col--;
            } else {
                col = getColCount() - 1;
                row--;
            }
        } else
            row--;
        return isCurrentSet();
    }

    @Override
    public boolean isCurrentSet() {
        return row >= 0 && row < getRowCount() && col >= 0 && col < getColCount();
    }

    @Override
    public boolean isCurrentSelected() {
        if (tableView.getSelectionModel().isCellSelectionEnabled()) {
            return isCurrentSet() && tableView.getSelectionModel().isSelected(row, tableView.getColumns().get(col));
        } else
            return isCurrentSet() && tableView.getSelectionModel().isSelected(row);
    }

    @Override
    public void setCurrentSelected(boolean select) {
        if (tableView.getSelectionModel().isCellSelectionEnabled()) {
            if (select)
                tableView.getSelectionModel().select(row, tableView.getColumns().get(col));
            else
                tableView.getSelectionModel().clearSelection(row, tableView.getColumns().get(col));
        } else {
            if (select)
                tableView.getSelectionModel().select(row);
            else
                tableView.getSelectionModel().clearSelection(row);
        }
    }

    @Override
    public String getCurrentLabel() {
        if (tableView.getSelectionModel().isCellSelectionEnabled())
            return tableView.getColumns().get(col).getCellObservableValue(tableView.getItems().get(row)).getValue().toString();
        else
            return tableView.getItems().get(row).toString();
    }

    @Override
    public Function<String, String> getPrepareTextForReplaceFunction() {
        return null;
    }

    @Override
    public void setCurrentLabel(String newLabel) {
    }

    @Override
    public int numberOfObjects() {
        if (tableView.getSelectionModel().isCellSelectionEnabled())
            return getRowCount() * getColCount();
        else
            return getRowCount();
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
        tableView.requestFocus();
    }

    @Override
    public boolean canFindAll() {
        return true;
    }

    @Override
    public void selectAll(boolean select) {
        if (select)
            tableView.getSelectionModel().selectAll();
        else
            tableView.getSelectionModel().clearSelection();

    }

    private int getRowCount() {
        return tableView.getItems().size();
    }

    private int getColCount() {
        return tableView.getColumns().size();
    }
}
