/*
 *  MyTable.java Copyright (C) 2019 Daniel H. Huson
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


import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MyTable {
    private final ObservableList<String> rowNames = FXCollections.observableArrayList();
    private final ObservableList<String> colNames = FXCollections.observableArrayList();

    private final ObservableMap<String, ArrayList<String>> columns = FXCollections.observableHashMap();

    public MyTable() {
        colNames.addListener((ListChangeListener<String>) (e) -> {
            while (e.next()) {
                for (String name : e.getAddedSubList()) {
                    columns.put(name, newColumn(numberOfRows()));
                }
                for (String name : e.getRemoved()) {
                    columns.remove(name);
                }
            }
        });

        rowNames.addListener((ListChangeListener<String>) (e) -> {
            while (e.next()) {
                if (e.wasAdded()) {
                    for (String colName : colNames) {
                        final ArrayList<String> col = columns.get(colName);
                        col.addAll(e.getFrom(), newColumn(e.getAddedSize()));
                    }
                } else if (e.wasRemoved()) {
                    for (String colName : colNames) {
                        final ArrayList<String> col = columns.get(colName);
                        for (int i = 0; i < e.getRemovedSize(); i++)
                            col.remove(e.getRemoved());
                    }
                } else if (e.wasPermutated()) {
                    for (String colName : colNames) {
                        final ArrayList<String> col = columns.get(colName);
                        for (int oldIndex = e.getFrom(); oldIndex < e.getTo(); oldIndex++) {
                            final int newIndex = e.getPermutation(oldIndex);
                            final String tmp = col.get(oldIndex);
                            col.set(oldIndex, col.get(newIndex));
                            col.set(newIndex, tmp);
                        }
                    }
                }
            }
        });
    }

    public void clear() {
        rowNames.clear();
        colNames.clear();
    }

    public void createColumn(int pos, String name) throws IllegalArgumentException {
        if (colNames.contains(name))
            throw new IllegalArgumentException("Column name already present");
        if (pos < colNames.size())
            colNames.add(pos, name);
        else
            colNames.add(name);

    }

    public void createColumn(String name, String... values) {
        createColumn(Integer.MAX_VALUE, name, values);
    }

    public void createColumn(int pos, String name, Comparable... values) {
        createColumn(pos, name);
        if (values.length > 0) {
            if (values.length != numberOfRows())
                throw new IllegalArgumentException("Wrong number of values");
            int count = 0;
            for (String rowName : rowNames) {
                set(rowName, name, values[count++].toString());
            }
        }
    }


    public void createRow(int pos, String name) {
        if (pos < rowNames.size())
            rowNames.add(pos, name);
        else
            rowNames.add(name);
    }

    public void createRow(String name, Comparable... values) {
        createRow(Integer.MAX_VALUE, name);
        if (values.length > 0) {
            if (values.length != numberOfCols())
                throw new IllegalArgumentException("Wrong number of values");
            int count = 0;
            for (String colName : colNames) {
                set(name, colName, values[count++].toString());
            }
        }
    }

    public void createRowsAndColumns(Collection<String> rowNames, Collection<String> colNames) {
        for (String name : rowNames) {
            createRow(name);
        }
        for (String name : colNames) {
            createColumn(name);
        }
    }

    public String get(String rowName, String colName) {
        return getColumn(colName).get(rowNames.indexOf(rowName));
    }

    public String get(int row, int col) {
        return getColumn(colNames.get(col)).get(row);
    }

    public void set(String rowName, String colName, String value) {
        getColumn(colName).set(rowNames.indexOf(rowName), value);
    }

    public void set(int row, int col, String value) {
        getColumn(colNames.get(col)).set(row, value);
    }

    public ArrayList<String> getColumn(String colName) {
        return columns.get(colName);
    }

    public ArrayList<String> getRow(String rowName) {
        final int pos = rowNames.indexOf(rowName);
        if (pos == -1)
            throw new IllegalArgumentException("Row not found");
        final ArrayList<String> array = new ArrayList<>(numberOfCols());
        for (String colName : colNames) {
            array.add(getColumn(colName).get(pos));
        }
        return array;
    }

    public ObservableList<String> getRowNames() {
        return new ReadOnlyListWrapper<>(rowNames);
    }

    public ObservableList<String> getColNames() {
        return new ReadOnlyListWrapper<>(colNames);
    }

    public int numberOfRows() {
        return rowNames.size();
    }

    public int numberOfCols() {
        return colNames.size();
    }

    public void changeColumns(Collection<String> changedCols) {
        final Map<String, ArrayList<String>> saveMap = new HashMap<>();
        for (String colName : changedCols) {
            final ArrayList<String> col = getColumn(colName);
            if (col == null)
                throw new IllegalArgumentException("Col not found");
            saveMap.put(colName, col);
        }
        colNames.setAll(changedCols);
        for (String col : changedCols) {
            columns.put(col, saveMap.get(col));
        }
    }

    public void changeRows(Collection<String> changedRows) {
        final Map<String, ArrayList<String>> saveMap = new HashMap<>();
        for (String rowName : changedRows) {
            final ArrayList<String> row = getRow(rowName);
            if (row == null)
                throw new IllegalArgumentException("Row not found");
            saveMap.put(rowName, row);
        }
        rowNames.clear();
        for (String rowName : changedRows) {
            createRow(rowName, saveMap.get(rowName).toArray(new String[0]));
        }
    }

    private static ArrayList<String> newColumn(int size) {
        final ArrayList<String> row = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            row.add(null);
        }
        return row;
    }

    public String getColName(int index) {
        return colNames.get(index);
    }

    public void deleteColumn(String colName) {
        colNames.remove(colName);
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();

        buf.append("#table");
        for (String colName : colNames) {
            buf.append("\t").append(colName);
        }
        buf.append("\n");
        for (String rowName : rowNames) {
            buf.append(rowName);
            for (String colName : colNames) {
                buf.append("\t");
                buf.append(get(rowName, colName));
            }
            buf.append("\n");
        }
        return buf.toString();
    }
}
