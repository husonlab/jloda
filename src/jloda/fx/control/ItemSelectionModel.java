package jloda.fx.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * simple item-based selection  model
 *
 * @param <T> Daniel Huson, 2015
 */
public class ItemSelectionModel<T> {
    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private final IntegerProperty size = new SimpleIntegerProperty(0);

    public ItemSelectionModel() {
        size.bind(Bindings.size(selectedItems));
    }

    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }

    public void clearAndSelect(T item) {
        clearSelection();
        select(item);
    }

    public void select(T item) {
        selectedItems.add(item);
    }

    public void clearSelection(T item) {
        selectedItems.remove(item);
    }

    public void clearSelection() {
        selectedItems.clear();
    }

    public boolean isSelected(T item) {
        return selectedItems.contains(item);
    }

    public boolean isEmpty() {
        return selectedItems.isEmpty();
    }

    public int size() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size;
    }
}