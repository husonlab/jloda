/*
 *  MyTableViewSearcher.java Copyright (C) 2019 Daniel H. Huson
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

package jloda.fx.control.table;

import javafx.beans.property.*;
import javafx.scene.control.MultipleSelectionModel;
import jloda.fx.find.IObjectSearcher;

public class MyTableViewSearcher implements IObjectSearcher<String> {
    private final MyTableView tableView;
    private int row = -1;
    private int col = -1;

    private final BooleanProperty hasData = new SimpleBooleanProperty(false);

    public MyTableViewSearcher(MyTableView tableView) {
        this.tableView = tableView;
        hasData.bind(tableView.rowCountProperty().isNotEqualTo(0).or(tableView.colCountProperty().isNotEqualTo(0)));
    }

    @Override
    public boolean gotoFirst() {
        row = 0;
        col = 0;
        return isCurrentSet();
    }

    @Override
    public boolean gotoNext() {
        if (row < tableView.getRowCount() - 1)
            row++;
        else {
            row = 0;
            col++;
        }
        return isCurrentSet();
    }

    @Override
    public boolean gotoLast() {
        row = tableView.getRowCount() - 1;
        col = tableView.getColCount() - 1;
        return isCurrentSet();
    }

    @Override
    public boolean gotoPrevious() {
        if (row > 0)
            row--;
        else {
            row = tableView.getRowCount() - 1;
            col--;
        }
        return isCurrentSet();
    }

    @Override
    public boolean isCurrentSet() {
        return row >= 0 && row < tableView.getRowCount() && col >= 0 && col < tableView.getColCount();
    }

    @Override
    public boolean isCurrentSelected() {
        return tableView.isSelected(row, col);
    }

    @Override
    public void setCurrentSelected(boolean select) {
        tableView.selectCell(row, col, select);

    }

    @Override
    public String getCurrentLabel() {
        return tableView.getValue(row, col);
    }

    @Override
    public void setCurrentLabel(String newLabel) {
        tableView.setValue(row, col, newLabel);

    }

    @Override
    public int numberOfObjects() {
        return tableView.getRowCount() * tableView.getColCount();
    }

    @Override
    public ReadOnlyObjectProperty<String> foundProperty() {
        return null;
    }

    @Override
    public MultipleSelectionModel<String> getSelectionModel() {
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
        tableView.selectAll(select);

    }
}
