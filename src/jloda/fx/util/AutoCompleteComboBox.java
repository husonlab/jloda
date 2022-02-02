/*
 *  AutoCompleteComboBox.java Copyright (C) third party
 */

package jloda.fx.util;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * setup auto complete for combobox
 * Based on: https://gist.github.com/yelken
 */
public class AutoCompleteComboBox {
	/**
	 * installs autocomplete on a combobox
	 * todo: this uses a copy of the original elements of the combobox, so subsequent changes to the list are lost
	 */
	public static <T> void install(ComboBox<T> comboBox) {
		comboBox.setEditable(true);
		comboBox.setOnKeyPressed(t -> comboBox.hide());
		comboBox.setOnKeyReleased(createKeyReleasedHandler(comboBox));
	}

	/**
	 * creates a handler for key release events
	 *
	 * @return the handler
	 */
	private static <T> EventHandler<KeyEvent> createKeyReleasedHandler(ComboBox<T> comboBox) {
		return new EventHandler<>() {
			private final ObservableList<T> items = FXCollections.observableArrayList(comboBox.getItems());
			private int caretPos;
			private boolean moveCaretToPos = false;

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.UP) {
					caretPos = -1;
					moveCaret(comboBox.getEditor().getText().length());
					return;
				} else if (event.getCode() == KeyCode.DOWN) {
					if (!comboBox.isShowing()) {
						comboBox.show();
						caretPos = -1;
						moveCaret(comboBox.getEditor().getText().length());
					} else {
						var pos = comboBox.getItems().indexOf(comboBox.getValue());
						if (pos == -1)
							comboBox.setValue(comboBox.getItems().get(0));
					}
					return;
				} else if (event.getCode() == KeyCode.BACK_SPACE) {
					moveCaretToPos = true;
					caretPos = comboBox.getEditor().getCaretPosition();
				} else if (event.getCode() == KeyCode.DELETE) {
					moveCaretToPos = true;
					caretPos = comboBox.getEditor().getCaretPosition();
				}

				if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
					|| event.isControlDown() || event.getCode() == KeyCode.HOME
					|| event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
					return;
				}

				if ((comboBox.getValue() != null && comboBox.getEditor().getText().equals(comboBox.getValue().toString())))
					Platform.runLater(comboBox::hide);

				ObservableList<T> list = FXCollections.observableArrayList();
				for (var item : items) {
					if (item.toString().toLowerCase().startsWith(comboBox.getEditor().getText().toLowerCase())) {
						list.add(item);
					}
				}
				var text = comboBox.getEditor().getText();
				comboBox.setItems(list);
				comboBox.getEditor().setText(text);
				if (!moveCaretToPos) {
					caretPos = -1;
				}
				moveCaret(text.length());
				if (!list.isEmpty()) {
					comboBox.show();
				}
			}

			private void moveCaret(int textLength) {
				if (caretPos == -1) {
					comboBox.getEditor().positionCaret(textLength);
				} else {
					comboBox.getEditor().positionCaret(caretPos);
				}
				moveCaretToPos = false;
			}
		};
	}
}
