package jloda.fx.control.table;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class MyTextFieldTableCell<S, T> extends TableCell<S, T> {
    private TextField textField;
    private ObjectProperty<StringConverter<T>> converter;
    
    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(StringConverter<T> var0) {
        return (var1) -> {
            return new javafx.scene.control.cell.TextFieldTableCell(var0);
        };
    }

    public MyTextFieldTableCell() {
        this((StringConverter)null);
    }

    public MyTextFieldTableCell(StringConverter<T> var1) {
        this.converter = new SimpleObjectProperty(this, "converter");
        this.getStyleClass().add("text-field-table-cell");
        this.setConverter(var1);
    }

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return this.converter;
    }

    public final void setConverter(StringConverter<T> var1) {
        this.converterProperty().set(var1);
    }

    public final StringConverter<T> getConverter() {
        return (StringConverter)this.converterProperty().get();
    }

    public void startEdit() {
        if (this.isEditable() && this.getTableView().isEditable() && this.getTableColumn().isEditable()) {
            super.startEdit();
            if (this.isEditing()) {
                if (this.textField == null) {
                    this.textField = createTextField(this, this.getConverter());
                }

                startEdit(this, this.getConverter(), (HBox)null, (Node)null, this.textField);
            }

        }
    }

    static <T> void startEdit(Cell<T> var0, StringConverter<T> var1, HBox var2, Node var3, TextField var4) {
        if (var4 != null) {
            var4.setText(getItemText(var0, var1));
        }

        var0.setText((String)null);
        if (var3 != null) {
            var2.getChildren().setAll(new Node[]{var3, var4});
            var0.setGraphic(var2);
        } else {
            var0.setGraphic(var4);
        }

        var4.selectAll();
        var4.requestFocus();
    }

    public void cancelEdit() {
        super.cancelEdit();
        cancelEdit(this, this.getConverter(), (Node)null);
    }

    public void updateItem(T var1, boolean var2) {
        super.updateItem(var1, var2);
        updateItem(this, this.getConverter(), (HBox)null, (Node)null, this.textField);
    }

    static <T> void cancelEdit(Cell<T> var0, StringConverter<T> var1, Node var2) {
        var0.setText(getItemText(var0, var1));
        var0.setGraphic(var2);
    }
    static <T> TextField createTextField(Cell<T> var0, StringConverter<T> var1) {
        TextField var2 = new TextField(getItemText(var0, var1));
        var2.setOnAction((var3) -> {
            if (var1 == null) {
                throw new IllegalStateException("Attempting to convert text input into Object, but provided StringConverter is null. Be sure to set a StringConverter in your cell factory.");
            } else {
                var0.commitEdit(var1.fromString(var2.getText()));
                var3.consume();
            }
        });
        var2.setOnKeyReleased((var1x) -> {
            if (var1x.getCode() == KeyCode.ESCAPE) {
                var0.cancelEdit();
                var1x.consume();
            }

        });
        return var2;
    }

    private static <T> String getItemText(Cell<T> var0, StringConverter<T> var1) {
        return var1 == null ? (var0.getItem() == null ? "" : var0.getItem().toString()) : var1.toString(var0.getItem());
    }

    static <T> void updateItem(Cell<T> var0, StringConverter<T> var1, HBox var2, Node var3, TextField var4) {
        if (var0.isEmpty()) {
            var0.setText((String)null);
            var0.setGraphic((Node)null);
        } else if (var0.isEditing()) {
            if (var4 != null) {
                var4.setText(getItemText(var0, var1));
            }

            var0.setText((String)null);
            if (var3 != null) {
                var2.getChildren().setAll(new Node[]{var3, var4});
                var0.setGraphic(var2);
            } else {
                var0.setGraphic(var4);
            }
        } else {
            var0.setText(getItemText(var0, var1));
            var0.setGraphic(var3);
        }

    }

    public TextField getTextField() {
        return textField;
    }
}