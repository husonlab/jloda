/*
 *  MyTableView.java Copyright (C) 2019 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.converter.DefaultStringConverter;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.util.Basic;
import jloda.util.BitSetUtils;
import jloda.util.Triplet;

import java.util.*;
import java.util.function.Function;

/**
 * string table
 * Daniel Huson, 5.2019
 */
public class MyTableView extends Pane {
    private final ListView<String> rowHeaderView;
    private boolean rowHeaderSortDescending = false;
    private final TableView<MyTableRow> tableView;

    private final ObservableMap<String, Node> rowGraphicMap = FXCollections.observableHashMap();

    private final IntegerProperty rowCount = new SimpleIntegerProperty(0);
    private final IntegerProperty colCount = new SimpleIntegerProperty(0);

    private final IntegerProperty countSelectedRows = new SimpleIntegerProperty(0);
    private final IntegerProperty countSelectedCols = new SimpleIntegerProperty(0);
    private final BooleanProperty editable = new SimpleBooleanProperty(false);

    private final BooleanProperty allowRenameRow = new SimpleBooleanProperty(false);
    private final BooleanProperty allowDeleteRow = new SimpleBooleanProperty(false);
    private final BooleanProperty allAddRow = new SimpleBooleanProperty(false);
    private final BooleanProperty allowReorderRow = new SimpleBooleanProperty(false);

    private final BooleanProperty allowRenameCol = new SimpleBooleanProperty(false);
    private final BooleanProperty allowDeleteCol = new SimpleBooleanProperty(false);
    private final BooleanProperty allowAddCol = new SimpleBooleanProperty(false);

    private final ObservableSet<String> unrenameableCols = FXCollections.observableSet();
    private final ObservableSet<String> undeleteableCols = FXCollections.observableSet();

    private final StringProperty defaultNewCellValue = new SimpleStringProperty("?");

    private Function<Collection<String>, Collection<MenuItem>> additionRowHeaderMenuItems;
    private Function<String, Collection<MenuItem>> additionColHeaderMenuItems;

    private int updatePauseLevel = 0; // if level larger than 0 then updating is paused
    private final LongProperty update = new SimpleLongProperty(0);

    private final Image dragImage;

    public MyTableView() {
        rowHeaderView = new ListView<>();
        rowHeaderView.setPrefWidth(200);
        rowHeaderView.setSelectionModel(new AMultipleSelectionModel<>());
        rowHeaderView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        rowHeaderView.setFocusTraversable(false);

        setupDragAndDrop();

        tableView = new TableView<>();
        tableView.setEditable(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getStylesheets().add("jloda/fx/control/table/mytable.css");

        rowCount.bind(Bindings.size(tableView.getItems()));
        colCount.bind(Bindings.size(tableView.getColumns()));

        rowHeaderView.getStylesheets().add("jloda/fx/control/table/mytable.css");
        rowHeaderView.setCellFactory(t -> {
            final ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            rowGraphicMap.addListener((InvalidationListener) (e) -> cell.setGraphic(rowGraphicMap.get(cell.getText())));
            cell.textProperty().addListener((c, o, n) -> cell.setGraphic(rowGraphicMap.get(n)));

            final MenuItem selectMenuItem = new MenuItem("Select All Values");
            selectMenuItem.setOnAction((e) -> {
                for (String rowName : rowHeaderView.getSelectionModel().getSelectedItems()) {
                    selectRow(rowName, true);
                }
                tableView.requestFocus();
            });

            final MenuItem sortMenuItem = new MenuItem("Sort Rows");
            sortMenuItem.setOnAction((e) -> {
                if (rowHeaderSortDescending)
                    rowHeaderView.getItems().sort((a, b) -> -a.compareTo(b));
                else
                    rowHeaderView.getItems().sort(String::compareTo);
                rowHeaderSortDescending = !rowHeaderSortDescending;

                final Map<String, MyTableRow> map = new HashMap<>();
                for (String rowName : getRowNames()) {
                    map.put(rowName, getRow(rowName));
                }
                final ArrayList<MyTableRow> list = new ArrayList<>(map.size());
                for (String rowName : rowHeaderView.getItems()) {
                    list.add(map.get(rowName));
                }
                tableView.getItems().setAll(list);

                tableView.requestFocus();
            });

            final ContextMenu contextMenu = new ContextMenu(selectMenuItem, sortMenuItem, new SeparatorMenuItem());

            final MenuItem addRowMenuItem = new MenuItem("Add Row...");
            addRowMenuItem.setOnAction((e) -> {
                TextInputDialog dialog = new TextInputDialog("row");
                dialog.setTitle("New row");
                dialog.setHeaderText("Enter row name:");

                final Optional<String> result = dialog.showAndWait();
                if (result.isPresent() && rowHeaderView.getSelectionModel().getSelectedItems().size() == 1) {
                    final String newName = Basic.getUniqueName(result.get().trim(), getRowNames());

                    final MyTableRow newRow = new MyTableRow(newName);
                    final int pos = getRowIndex(rowHeaderView.getSelectionModel().getSelectedItems().get(0));
                    if (pos >= 0 && pos < tableView.getItems().size())
                        tableView.getItems().add(pos, newRow);
                    else
                        tableView.getItems().add(newRow);
                }
            });
            addRowMenuItem.disableProperty().bind(Bindings.size(rowHeaderView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));

            final MenuItem renameMenuItem = new MenuItem("Rename Row...");
            renameMenuItem.setOnAction((e) -> {
                final String oldName = cell.getText();
                TextInputDialog dialog = new TextInputDialog(oldName);
                dialog.setTitle("New Row Name");
                dialog.setHeaderText("Enter new row name:");

                final Optional<String> result = dialog.showAndWait();
                if (result.isPresent() && rowHeaderView.getSelectionModel().getSelectedItems().size() == 1) {
                    if (!result.get().equals(oldName)) {
                        final String newName = Basic.getUniqueName(result.get().trim(), getRowNames());
                        renameRow(oldName, newName);
                    }
                }
            });
            renameMenuItem.disableProperty().bind(Bindings.size(rowHeaderView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));

            final MenuItem deleteRowMenuItem = new MenuItem("Delete Row(s)");
            deleteRowMenuItem.setOnAction((e) -> deleteRows(new ArrayList<>(rowHeaderView.getSelectionModel().getSelectedItems())));
            deleteRowMenuItem.disableProperty().bind(Bindings.size(rowHeaderView.getSelectionModel().getSelectedItems()).isEqualTo(0));

            final ArrayList<MenuItem> originalMenuItems = new ArrayList<>(contextMenu.getItems());

            contextMenu.setOnShowing((e) -> {
                contextMenu.getItems().setAll(originalMenuItems);
                if (getAllAddRow())
                    contextMenu.getItems().add(addRowMenuItem);
                if (isAllowRenameRow())
                    contextMenu.getItems().add(renameMenuItem);
                if (isAllowDeleteRow())
                    contextMenu.getItems().add(deleteRowMenuItem);
                if (getAdditionRowHeaderMenuItems() != null) {
                    if (!(contextMenu.getItems().get(contextMenu.getItems().size() - 1) instanceof SeparatorMenuItem))
                        contextMenu.getItems().add(new SeparatorMenuItem());
                    contextMenu.getItems().addAll(getAdditionRowHeaderMenuItems().apply(new ArrayList<>(getSelectedRows())));
                }
            });

            cell.emptyProperty().addListener((c, o, n) -> {
                if (n) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });

        final HBox hbox = new HBox();

        VBox.setVgrow(rowHeaderView, Priority.ALWAYS);
        final ToolBar pane = new ToolBar();
        pane.setMinHeight(26);
        pane.setMaxHeight(26);
        pane.prefWidthProperty().bind(rowHeaderView.widthProperty());
        pane.setOnMouseClicked((e) -> tableView.getSelectionModel().clearSelection());
        final VBox leftVBox = new VBox(pane, rowHeaderView);

        HBox.setHgrow(leftVBox, Priority.SOMETIMES);
        HBox.setHgrow(tableView, Priority.ALWAYS);
        hbox.getChildren().setAll(leftVBox, tableView);

        hbox.prefWidthProperty().bind(widthProperty());
        hbox.prefHeightProperty().bind(heightProperty());

        this.getChildren().add(hbox);

        tableView.skinProperty().addListener((c, o, n) -> {
            ScrollBar mainTableVerticalScrollBar = (ScrollBar) tableView.lookup(".scroll-bar:vertical");
            ScrollBar rowHeaderScrollBar = (ScrollBar) rowHeaderView.lookup(".scroll-bar");
            if (mainTableVerticalScrollBar != null && rowHeaderScrollBar != null) {
                rowHeaderScrollBar.valueProperty().bindBidirectional(mainTableVerticalScrollBar.valueProperty());
            }
        });

        tableView.getItems().addListener((InvalidationListener) e -> {
            pausePostingUpdates();
            try {
                final ArrayList<String> order = new ArrayList<>(tableView.getItems().size());
                for (MyTableRow row : tableView.getItems())
                    order.add(row.getRowName());
                ((AMultipleSelectionModel<String>) rowHeaderView.getSelectionModel()).setItems(order);
                rowHeaderView.getItems().setAll(order);
            } finally {
                resumePostingUpdates();
            }
        });

        tableView.getColumns().addListener((InvalidationListener) e -> postUpdate());

        tableView.getSelectionModel().getSelectedCells().addListener((InvalidationListener) (e) -> {
            final BitSet selectedRows = new BitSet();
            final BitSet selectedCols = new BitSet();

            for (TableColumn<MyTableRow, ?> column : tableView.getColumns()) {
                column.getStyleClass().remove("selected");
            }

            for (TablePosition pos : tableView.getSelectionModel().getSelectedCells()) {
                selectedRows.set(pos.getRow());
                selectedCols.set(pos.getColumn());
                if (pos.getColumn() < tableView.getColumns().size()) {
                    TableColumn<MyTableRow, ?> column = getCol(pos.getColumn());
                    column.getStyleClass().add("selected");
                }
            }
            countSelectedRows.set(selectedRows.cardinality());
            countSelectedCols.set(selectedCols.cardinality());

            rowHeaderView.getSelectionModel().clearSelection();
            for (int index : BitSetUtils.members(selectedRows)) {
                Platform.runLater(() -> rowHeaderView.getSelectionModel().select(getRowName(index)));
            }

        });

        dragImage = createRectangleImage();
    }

    public void pausePostingUpdates() {
        updatePauseLevel++;
    }

    public void resumePostingUpdates() {
        if (updatePauseLevel > 0) {
            updatePauseLevel--;
            if (updatePauseLevel == 0)
                postUpdate();
        }
    }

    private void postUpdate() {
        if (updatePauseLevel == 0)
            update.set(update.get() + 1);
    }

    public void renameRow(String oldName, String newName) {
        final int index = getRowIndex(oldName);
        final MyTableRow tableRow = getRow(oldName);
        if (index != -1 && tableRow != null) {
            newName = Basic.getUniqueName(newName.trim(), getRowNames());
            rowHeaderView.getItems().set(index, newName);
            tableRow.setRowName(newName);
            postUpdate();
        }
    }

    public void renameCol(String oldName, String newName) {
        final TableColumn col = getCol(oldName);
        if (col != null) {
            newName = Basic.getUniqueName(newName.trim(), getColNames());
            col.setText(newName);
            for (MyTableRow row : tableView.getItems()) {
                row.renameCol(oldName, newName);
            }
            postUpdate();
        }
    }


    public ArrayList<String> getColNames() {
        final ArrayList<String> list = new ArrayList<>();
        for (TableColumn column : tableView.getColumns()) {
            list.add(column.getText());
        }
        return list;
    }

    public ArrayList<String> getRowNames() {
        final ArrayList<String> list = new ArrayList<>();
        for (MyTableRow tableRow : tableView.getItems()) {
            list.add(tableRow.getRowName());
        }
        return list;
    }

    private TableColumn<MyTableRow, String> createTableCol(String colName) {
        final TableColumn<MyTableRow, String> tableColumn = new TableColumn<>(colName);

        tableColumn.setSortable(false);

        tableColumn.setCellValueFactory(p -> p.getValue().valueProperty(tableColumn.getText()));

        tableColumn.setCellFactory(param -> {
            final TextFieldTableCell<MyTableRow, String> cell = new TextFieldTableCell<>(new DefaultStringConverter());
            cell.editableProperty().bind(editable);
            return cell;
        });

        tableColumn.setOnEditCommit(t -> {
                    final String oldValue = t.getTableView().getItems().get(t.getTablePosition().getRow()).getValue(tableColumn.getText());
                    final String newValue = t.getNewValue();
                    if (!newValue.equals(oldValue)) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).valueProperty(tableColumn.getText()).set(newValue);
                        Platform.runLater(this::postUpdate);
                    }
                    Platform.runLater(() -> {
                        tableView.requestFocus();
                        final int row = t.getTablePosition().getRow();
                        tableView.getSelectionModel().clearAndSelect(row < tableView.getItems().size() ? row + 1 : row, t.getTableColumn());
                    });
                }
        );

        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem selectMenuItem = new MenuItem("Select All Values");
        selectMenuItem.setOnAction((e) -> selectCol(tableColumn, true));

        final MenuItem sortMenuItem = new MenuItem("Sort By Column");
        sortMenuItem.setOnAction((e) -> sortByCol(tableColumn.getText(), tableColumn.getSortType() == TableColumn.SortType.DESCENDING ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING));

        contextMenu.getItems().addAll(selectMenuItem, sortMenuItem, new SeparatorMenuItem());

        final MenuItem addColumnMenuItem = new MenuItem("Add Column...");
        addColumnMenuItem.setOnAction((e) -> {
            TextInputDialog dialog = new TextInputDialog("col");
            dialog.setTitle("New Column");
            dialog.setHeaderText("Enter new column name:");

            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                final int pos = tableView.getColumns().indexOf(tableColumn);
                addCol(pos, result.get().trim());
            }
        });

        final MenuItem renameMenuItem = new MenuItem("Rename Column...");
        renameMenuItem.setOnAction((e) -> {
            TextInputDialog dialog = new TextInputDialog(tableColumn.getText());
            dialog.setTitle("Rename Column");
            dialog.setHeaderText("Enter new column name:");

            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                if (!result.get().equals(tableColumn.getText())) {
                    final String newName = Basic.getUniqueName(result.get().trim(), getColNames());
                    renameCol(tableColumn.getText(), newName);
                }
            }
        });

        final MenuItem deleteItem = new MenuItem(("Delete Column"));
        deleteItem.setOnAction((e) -> deleteCol(tableColumn.getText()));

        final ArrayList<MenuItem> originalMenuItems = new ArrayList<>(contextMenu.getItems());

        contextMenu.setOnShowing((e) -> {
            contextMenu.getItems().setAll(originalMenuItems);
            if (getAllowAddCol())
                contextMenu.getItems().add(addColumnMenuItem);
            if (isAllowRenameCol() && !getUnrenameableCols().contains(tableColumn.getText()))
                contextMenu.getItems().add(renameMenuItem);
            if (isAllowDeleteCol() && !getUndeleteableCols().contains(tableColumn.getText()))
                contextMenu.getItems().add(deleteItem);
            if (getAdditionColHeaderMenuItems() != null) {
                if (!(contextMenu.getItems().get(contextMenu.getItems().size() - 1) instanceof SeparatorMenuItem))
                    contextMenu.getItems().add(new SeparatorMenuItem());
                contextMenu.getItems().addAll(getAdditionColHeaderMenuItems().apply(tableColumn.getText()));
            }
            for (MenuItem item : contextMenu.getItems()) {
                item.setStyle("-fx-text-fill: black");
            }
        });

        tableColumn.setContextMenu(contextMenu);
        return tableColumn;
    }

    public void deleteRow(String rowName) {
        tableView.getItems().remove(getRow(rowName));
    }

    public void deleteRows(Collection<String> rowNames) {
        pausePostingUpdates();
        try {
            for (String row : rowNames) {
                tableView.getItems().remove(getRow(row));
            }
        } finally {
            resumePostingUpdates();
        }
    }

    public void deleteCol(String colName) {
        pausePostingUpdates();
        try {
            tableView.getColumns().remove(getCol(colName));
            for (MyTableRow row : tableView.getItems()) {
                row.colValueMap.remove(colName);
            }
        } finally {
            resumePostingUpdates();
        }
    }

    public void deleteCols(Collection<String> colNames) {
        pausePostingUpdates();
        try {
            for (String colName : colNames) {
                tableView.getColumns().remove(getCol(colName));
                for (MyTableRow row : tableView.getItems()) {
                    row.colValueMap.remove(colName);
                }
            }
        } finally {
            resumePostingUpdates();
        }
    }

    public void sortByCol(String colName, TableColumn.SortType sortType) {
        pausePostingUpdates();
        try {
            final TableColumn<MyTableRow, ?> tableColumn = getCol(colName);
            tableView.getSelectionModel().clearSelection();
            final ArrayList<MyTableRow> list = new ArrayList<>(tableView.getItems());
            list.sort(new ColumnComparator(colName, tableColumn.getSortType(), list));
            tableColumn.setSortType(sortType);
            tableView.getItems().setAll(list);
            tableView.requestFocus();
            tableView.getSelectionModel().select(0, tableColumn);
        } finally {
            resumePostingUpdates();
        }
    }

    public TableColumn<MyTableRow, ?> getCol(String colName) {
        for (TableColumn<MyTableRow, ?> column : tableView.getColumns()) {
            if (column.getText().equals(colName))
                return column;
        }
        return null;
    }

    public TableColumn<MyTableRow, ?> getCol(int index) {
        return tableView.getColumns().get(index);
    }

    public String getColName(int index) {
        return tableView.getColumns().get(index).getText();
    }

    public int getColIndex(String name) {
        for (int index = 0; index < getColCount(); index++) {
            if (tableView.getColumns().get(index).getText().equalsIgnoreCase(name))
                return index;
        }
        return -1;
    }

    public String getRowName(int index) {
        return tableView.getItems().get(index).getRowName();
    }

    public int getRowIndex(String rowName) {
        int count = 0;
        for (MyTableRow row : tableView.getItems()) {
            if (row.getRowName().equals(rowName))
                return count;
            count++;
        }
        return -1;
    }

    private MyTableRow getRow(String rowName) {
        for (MyTableRow row : tableView.getItems()) {
            if (row.getRowName().equals(rowName))
                return row;
        }
        return null;
    }

    public void selectByValue(String colName, String value) {
        final int col = getColIndex(colName);
        for (int row = 0; row < getRowCount(); row++) {
            if (value.equals(getValue(row, col))) {
                selectCell(row, col, true);
            }
        }
    }

    public void selectCol(TableColumn<MyTableRow, ?> column, boolean select) {
        for (int row = 0; row < tableView.getItems().size(); row++) {
            if (select)
                tableView.getSelectionModel().select(row, column);
            else
                tableView.getSelectionModel().clearSelection(row, column);
        }
    }

    public void selectCol(String colName, boolean select) {
        selectCol(getCol(colName), select);
    }

    public void selectRow(String rowName, boolean select) {
        selectRow(getRowIndex(rowName), select);
    }


    public void selectRowHeader(String rowName, boolean select) {
        final int index = getRowIndex(rowName);
        if (index >= 0 && index < rowHeaderView.getItems().size()) {
            if (select)
                rowHeaderView.getSelectionModel().select(index);
            else
                rowHeaderView.getSelectionModel().clearSelection(index);
        }
    }

    public void selectRowHeaders(Collection<String> rowNames, boolean select) {
        for (String row : rowNames) {
            selectRowHeader(row, select);
        }
    }

    public void selectRow(int row, boolean select) {
        for (TableColumn<MyTableRow, ?> column : tableView.getColumns())
            if (select)
                tableView.getSelectionModel().select(row, column);
            else
                tableView.getSelectionModel().clearSelection(row, column);
    }

    public void selectRows(Collection<String> rowNames, boolean select) {
        for (String row : rowNames) {
            selectRow(row, select);
        }
    }

    public void selectCell(int rowId, int colId, boolean select) {
        if (select)
            tableView.getSelectionModel().select(rowId, tableView.getColumns().get(colId));
        else
            tableView.getSelectionModel().clearSelection(rowId, tableView.getColumns().get(colId));
    }

    public boolean isSelected(int rowId, int colId) {
        return tableView.getSelectionModel().isSelected(rowId, tableView.getColumns().get(colId));
    }

    public String getASelectedCol() {
        final int col = getASelectedColIndex();
        if (col != -1)
            return getColName(col);
        else
            return null;
    }

    public int getASelectedColIndex() {
        if (tableView.getSelectionModel().getSelectedCells().size() > 0)
            return tableView.getSelectionModel().getSelectedCells().get(0).getColumn();
        else
            return -1;
    }

    public void selectAll(boolean select) {
        if (select)
            tableView.getSelectionModel().selectAll();
        else
            tableView.getSelectionModel().clearSelection();
    }

    public ObservableList<String> getSelectedRows() {
        return new ReadOnlyListWrapper<>(rowHeaderView.getSelectionModel().getSelectedItems());
    }

    public ArrayList<Integer> getSelectedRowIndices() {
        final ArrayList<Integer> list = new ArrayList<>();
        try {
            for (String rowName : getSelectedRows()) {
                list.add(getRowIndex(rowName));
            }
        } catch (ConcurrentModificationException ex) {
            // don't know why this happens
        }
        return list;
    }

    public ArrayList<String> getSelectedCols() {
        final BitSet cols = new BitSet();
        try {
            for (TablePosition position : getSelectedCells()) {
                cols.set(position.getColumn());
            }
        } catch (IndexOutOfBoundsException ex) {
        }
        final ArrayList<String> list = new ArrayList<>(cols.cardinality());
        for (String colName : getColNames()) {
            if (cols.get(getColIndex(colName)))
                list.add(colName);
        }
        return list;
    }

    public int getCountSelectedRows() {
        return countSelectedRows.get();
    }

    public ReadOnlyIntegerProperty countSelectedRowsProperty() {
        return countSelectedRows;
    }


    public int getCountSelectedCols() {
        return countSelectedCols.get();
    }

    public ReadOnlyIntegerProperty countSelectedColsProperty() {
        return countSelectedCols;
    }


    public TableView.TableViewSelectionModel<MyTableRow> getSelectionModel() {
        return tableView.getSelectionModel();
    }

    public ObservableList<TablePosition> getSelectedCells() {
        return tableView.getSelectionModel().getSelectedCells();
    }

    public String getValue(int rowId, int colId) {
        return tableView.getItems().get(rowId).getValue(getColName(colId));
    }

    public String getValue(String rowName, String colName) {
        return tableView.getItems().get(getRowIndex(rowName)).getValue(colName);
    }

    public void setValue(int rowId, int colId, String value) {
        tableView.getItems().get(rowId).valueProperty(getColNames().get((colId))).set(value);
        postUpdate();
    }

    public void setValue(String rowName, String colName, String value) {
        tableView.getItems().get(getRowIndex(rowName)).valueProperty(colName).set(value);
        postUpdate();
    }

    public boolean isAllowDeleteCol() {
        return allowDeleteCol.get();
    }

    public BooleanProperty allowDeleteColProperty() {
        return allowDeleteCol;
    }

    public void setAllowDeleteCol(boolean allowDeleteCol) {
        this.allowDeleteCol.set(allowDeleteCol);
    }

    public boolean getAllowAddCol() {
        return allowAddCol.get();
    }

    public BooleanProperty allowAddColProperty() {
        return allowAddCol;
    }

    public String getDefaultNewCellValue() {
        return defaultNewCellValue.get();
    }

    public StringProperty defaultNewCellValueProperty() {
        return defaultNewCellValue;
    }

    public void setDefaultNewCellValue(String defaultNewCellValue) {
        this.defaultNewCellValue.set(defaultNewCellValue);
    }

    public void setAllowAddCol(boolean allowAddCol) {
        this.allowAddCol.set(allowAddCol);
    }

    public boolean isAllowDeleteRow() {
        return allowDeleteRow.get();
    }

    public BooleanProperty allowDeleteRowProperty() {
        return allowDeleteRow;
    }

    public void setAllowDeleteRow(boolean allowDeleteRow) {
        this.allowDeleteRow.set(allowDeleteRow);
    }

    public boolean getAllAddRow() {
        return allAddRow.get();
    }

    public BooleanProperty allAddRowProperty() {
        return allAddRow;
    }

    public void setAllAddRow(boolean allAddRow) {
        this.allAddRow.set(allAddRow);
    }

    public boolean isAllowRenameRow() {
        return allowRenameRow.get();
    }

    public BooleanProperty allowRenameRowProperty() {
        return allowRenameRow;
    }

    public void setAllowRenameRow(boolean allowRenameRow) {
        this.allowRenameRow.set(allowRenameRow);
    }

    public boolean getAllowReorderRow() {
        return allowReorderRow.get();
    }

    public BooleanProperty allowReorderRowProperty() {
        return allowReorderRow;
    }

    public void setAllowReorderRow(boolean allowReorderRow) {
        this.allowReorderRow.set(allowReorderRow);
    }

    public boolean isAllowRenameCol() {
        return allowRenameCol.get();
    }

    public BooleanProperty allowRenameColProperty() {
        return allowRenameCol;
    }

    public void setAllowRenameCol(boolean allowRenameCol) {
        this.allowRenameCol.set(allowRenameCol);
    }

    public Function<Collection<String>, Collection<MenuItem>> getAdditionRowHeaderMenuItems() {
        return additionRowHeaderMenuItems;
    }

    public void setAdditionRowHeaderMenuItems(Function<Collection<String>, Collection<MenuItem>> additionRowHeaderMenuItems) {
        this.additionRowHeaderMenuItems = additionRowHeaderMenuItems;
    }

    public Function<String, Collection<MenuItem>> getAdditionColHeaderMenuItems() {
        return additionColHeaderMenuItems;
    }

    public void setAdditionColHeaderMenuItems(Function<String, Collection<MenuItem>> additionColHeaderMenuItems) {
        this.additionColHeaderMenuItems = additionColHeaderMenuItems;
    }

    public int getRowCount() {
        return rowCount.get();
    }

    public ReadOnlyIntegerProperty rowCountProperty() {
        return rowCount;
    }

    public int getColCount() {
        return colCount.get();
    }

    public ReadOnlyIntegerProperty colCountProperty() {
        return colCount;
    }

    private void setupDragAndDrop() {
        rowHeaderView.setOnDragDetected(e -> {
            if (getAllowReorderRow()) {
                pausePostingUpdates();

                final PickResult pickResult = e.getPickResult();
                if (pickResult != null && (pickResult.getIntersectedNode() instanceof ListCell && ((ListCell) pickResult.getIntersectedNode()).getItem() != null
                        || pickResult.getIntersectedNode() instanceof Text)) {
                    final Dragboard dragboard = rowHeaderView.startDragAndDrop(TransferMode.MOVE);
                    final ClipboardContent content = new ClipboardContent();
                    content.put(DataFormat.PLAIN_TEXT, Basic.toString(new ArrayList<>(rowHeaderView.getSelectionModel().getSelectedItems()), "\n"));
                    dragboard.setDragView(dragImage);

                    content.putString(Basic.toString(rowHeaderView.getSelectionModel().getSelectedItems(), "\n"));
                    dragboard.setContent(content);

                    tableView.getSelectionModel().clearSelection();
                    for (String row : rowHeaderView.getSelectionModel().getSelectedItems())
                        selectRow(row, true);
                    tableView.requestFocus();
                }
                e.consume();
            }
        });

        rowHeaderView.setOnDragOver(e -> {
            if (getAllowReorderRow()) {
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            }
        });

        rowHeaderView.setOnDragDropped(e -> {
            if (getAllowReorderRow()) {
                final List<String> list = Basic.toList(e.getDragboard().getContent(DataFormat.PLAIN_TEXT).toString());

                String hitRowName = null;
                {
                    final PickResult pickResult = e.getPickResult();
                    if (pickResult != null && (pickResult.getIntersectedNode() instanceof ListCell && ((ListCell) pickResult.getIntersectedNode()).getItem() != null
                            || pickResult.getIntersectedNode() instanceof Text)) {
                        final String name;

                        if (pickResult.getIntersectedNode() instanceof ListCell)
                            name = ((ListCell) pickResult.getIntersectedNode()).getText();
                        else
                            name = ((Text) pickResult.getIntersectedNode()).getText();

                        for (String item : rowHeaderView.getItems()) {
                            if (item.equals(name)) {
                                hitRowName = name;
                                break;
                            }
                        }
                    }
                }

                if (hitRowName != null && !list.contains(hitRowName)) {
                    rowHeaderView.getItems().removeAll(list);
                    rowHeaderView.getItems().addAll(rowHeaderView.getItems().indexOf(hitRowName), list);

                    final ArrayList<MyTableRow> sorted = new ArrayList<>(tableView.getItems().size());
                    for (String rowName : rowHeaderView.getItems()) {
                        sorted.add(getRow(rowName));
                    }
                    tableView.getItems().setAll(sorted);

                    tableView.getSelectionModel().clearSelection();
                    for (String row : list)
                        selectRow(row, true);
                    tableView.requestFocus();
                }

                e.setDropCompleted(true);
                e.consume();
            }
        });

        setOnDragDone((e) -> {
            if (getAllowReorderRow()) {
                resumePostingUpdates();
                e.consume();
            }
        });
    }

    public void setRowGraphic(String rowName, Node node) {
        rowGraphicMap.put(rowName, node);
    }

    public Node getRowGraph(String rowName) {
        return rowGraphicMap.get(rowName);
    }

    public void clearRowGraphic(String rowName) {
        rowGraphicMap.remove(rowName);
    }

    public void clearRowGraphics() {
        rowGraphicMap.clear();
    }

    public boolean isEditable() {
        return editable.get();
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    public ObservableSet<String> getUnrenameableCols() {
        return unrenameableCols;
    }

    public ObservableSet<String> getUndeleteableCols() {
        return undeleteableCols;
    }

    private static Image createRectangleImage() {
        final Rectangle rectangle = new Rectangle(64, 16);
        rectangle.setFill(Color.LIGHTGRAY);
        rectangle.setStroke(Color.BLACK);
        return rectangle.snapshot(null, null);
    }

    public void copyToClipboard() {
        final StringBuilder buf = new StringBuilder();
        buf.append("Table");
        for (String colName : getSelectedCols())
            buf.append("\t").append(colName);
        buf.append("\n");
        for (int row = 0; row < getRowCount(); row++) {
            boolean addedRowHeader = false;
            for (int col = 0; col < getColCount(); col++) {
                if (isSelected(row, col)) {
                    if (!addedRowHeader) {
                        buf.append(getRowName(row));
                        addedRowHeader = true;
                    }
                    buf.append("\t").append(getValue(row, col));
                }
            }
            if (addedRowHeader)
                buf.append("\n");
        }

        if (buf.length() > 0) {
            final ClipboardContent contents = new ClipboardContent();

            contents.put(DataFormat.PLAIN_TEXT, buf.toString());
            contents.putString(buf.toString());
            Clipboard.getSystemClipboard().setContent(contents);
        }
    }

    public void scrollToRow(String rowName) {
        rowHeaderView.scrollTo(rowName);
    }

    public void scrollToRow(int index) {
        rowHeaderView.scrollTo(index);
    }

    public void addCol(int pos, String colName) {
        pausePostingUpdates();
        try {
            setUserData(-1);
            final String newColName = Basic.getUniqueName(colName, getColNames());
            final TableColumn<MyTableRow, String> newColumn = createTableCol(newColName);
            if (pos >= 0 && pos < getColCount() - 1)
                tableView.getColumns().add(pos + 1, newColumn);
            else
                tableView.getColumns().add(newColumn);
            setUserData(0);
        } finally {
            resumePostingUpdates();
        }
    }

    public void addCol(String colName) {
        addCol(Integer.MAX_VALUE, colName);
    }

    public void addRow(int index, String rowName, Object... values) {
        pausePostingUpdates();
        try {
            final String newRowName = Basic.getUniqueName(rowName, getRowNames());
            final MyTableRow newRow = new MyTableRow(newRowName);
            if (index >= 0 && index < getRowCount() - 1)
                tableView.getItems().add(index, newRow);
            else
                tableView.getItems().add(newRow);

            final int top = Math.min(values.length, getColCount());
            for (int i = 0; i < top; i++) {
                if (values[i] != null)
                    newRow.setValue(getColName(i), values[i].toString());
            }
        } finally {
            resumePostingUpdates();
        }
    }

    public void addRow(String rowName, Object... values) {
        addRow(Integer.MAX_VALUE, rowName, values);
    }

    public void clear() {
        pausePostingUpdates();
        try {
            tableView.getItems().clear();
            tableView.getColumns().clear();
        } finally {
            resumePostingUpdates();
        }
    }

    public void createRowsAndCols(ArrayList<String> rowNames, ArrayList<String> colNames) {
        pausePostingUpdates();
        try {
            tableView.getItems().clear();
            tableView.getColumns().clear();
            for (String colName : colNames) {
                tableView.getColumns().add(createTableCol(colName));
            }
            for (String rowName : rowNames) {
                tableView.getItems().add(new MyTableRow(rowName));
            }
        } finally {
            resumePostingUpdates();
        }
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();

        buf.append("#table");
        for (String colName : getColNames()) {
            buf.append("\t").append(colName);
        }
        buf.append("\n");
        for (String rowName : getRowNames()) {
            buf.append(rowName);
            for (String colName : getColNames()) {
                buf.append("\t");
                buf.append(getValue(rowName, colName));
            }
            buf.append("\n");
        }
        return buf.toString();
    }

    public long getUpdate() {
        return update.get();
    }

    public ReadOnlyLongProperty updateProperty() {
        return update;
    }

    public Triplet<String, String, String> getSingleSelectedCell() {
        if (getSelectedCells().size() == 1) {
            final TablePosition tablePosition = getSelectedCells().get(0);
            return new Triplet<>(getRowName(tablePosition.getRow()), getColName(tablePosition.getColumn()), getValue(tablePosition.getRow(), tablePosition.getColumn()));
        } else
            return null;
    }

    public void swapRows(int pos1, int pos2) {
        if (pos1 != pos2 && pos1 >= 0 && pos1 < getRowCount() && pos2 >= 0 && pos2 < getRowCount()) {
            pausePostingUpdates();
            try {
                final int minPos = Math.min(pos1, pos2);
                final int maxPos = Math.max(pos1, pos2);
                System.err.println("Swapping: " + minPos + " " + maxPos);
                final MyTableRow minRow = tableView.getItems().get(minPos);
                final MyTableRow maxRow = tableView.getItems().remove(maxPos);
                tableView.getItems().set(minPos, maxRow);
                if (maxPos < getRowCount())
                    tableView.getItems().add(maxPos, minRow);
                else
                    tableView.getItems().add(minRow);
            } finally {
                resumePostingUpdates();
            }
        }
    }

    public ArrayList<String> sortInSameOrderAsRows(Collection<String> samples) {
        final ArrayList<String> sorted = new ArrayList<>(samples.size());
        for (String row : getRowNames()) {
            if (samples.contains(row))
                sorted.add(row);
        }
        return sorted;
    }

    private class ColumnComparator implements Comparator<MyTableRow> {
        private final String colName;
        private final TableColumn.SortType sortType;
        private boolean sortAsNumbers;

        public ColumnComparator(String colName, TableColumn.SortType sortType, Collection<MyTableRow> rows) {
            this.colName = colName;
            this.sortType = sortType;
            sortAsNumbers = true;
            for (MyTableRow row : rows) {
                if (!Basic.isDouble(row.getValue(colName))) {
                    sortAsNumbers = false;
                    break;
                }
            }
        }

        @Override
        public int compare(MyTableRow a, MyTableRow b) {
            if (sortAsNumbers)
                return (sortType == TableColumn.SortType.ASCENDING ? 1 : -1) * Double.compare(Basic.parseDouble(a.getValue(colName)), Basic.parseDouble(b.getValue(colName)));
            else
                return (sortType == TableColumn.SortType.ASCENDING ? 1 : -1) * a.getValue(colName).compareTo(b.getValue(colName));
        }
    }

    public class MyTableRow {
        private String rowName;
        private final Map<String, StringProperty> colValueMap = new HashMap<>();

        MyTableRow(String rowName) {
            this.rowName = rowName;
        }

        public String getRowName() {
            return rowName;
        }

        void setRowName(String name) {
            rowName = name;
        }

        public StringProperty valueProperty(String colName) {
            StringProperty valueProperty = colValueMap.get(colName);
            if (valueProperty == null) {
                valueProperty = new SimpleStringProperty(getDefaultNewCellValue());
                colValueMap.put(colName, valueProperty);
            }
            return valueProperty;
        }

        public String getValue(String colName) {
            return valueProperty(colName).get();
        }

        public void setValue(String colName, String value) {
            valueProperty(colName).set(value);
        }

        void renameCol(String oldName, String newName) {
            if (!newName.equals(oldName)) {
                colValueMap.put(newName, colValueMap.get(oldName));
                colValueMap.remove(oldName);
            }
        }
    }

}
