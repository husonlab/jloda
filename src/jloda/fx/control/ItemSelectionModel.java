package jloda.fx.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 * simple item-based selection  model
 *
 * @param <T> Daniel Huson, 2015
 */
public class ItemSelectionModel<T> {
    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private final ObservableSet<T> selectedItemSet = FXCollections.observableSet();
    private final IntegerProperty size = new SimpleIntegerProperty(0);

    public ItemSelectionModel() {
        size.bind(Bindings.size(selectedItemSet));
        selectedItemSet.addListener((SetChangeListener<T>) c -> {
            if (c.wasRemoved())
                selectedItems.remove(c.getElementRemoved());
            else if (c.wasAdded())
                selectedItems.add(c.getElementAdded());
        });
    }

    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }

    public void clearAndSelect(T item) {
        clearSelection();
        select(item);
    }

    public void select(T item) {
        selectedItemSet.add(item);
    }

    public void clearSelection(T item) {
        selectedItemSet.remove(item);
    }

    public void clearSelection() {
        selectedItemSet.clear();
    }

    public boolean isSelected(T item) {
        return selectedItemSet.contains(item);
    }

    public boolean isEmpty() {
        return selectedItemSet.isEmpty();
    }

    public int size() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size;
    }
}