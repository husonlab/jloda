/*
 * FlowView.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.control;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * simple virtualized flow view based on list view
 *
 * @param <T> Daniel Huson, 8.2021
 */
public class FlowView<T> extends Pane {
	private final ObservableList<T> items = FXCollections.observableArrayList();
	private final IntegerProperty size = new SimpleIntegerProperty(0);
	private final Function<T, Double> widthSupplier;

	private final ListView<List<T>> listView = new ListView<>();
	private final DoubleProperty Vgap = new SimpleDoubleProperty(0);
	private final DoubleProperty Hgap = new SimpleDoubleProperty(0);

	private final static Background emptyBackground = new Background(new BackgroundFill(Color.TRANSPARENT, null, null));

	private final Executor executor = Executors.newSingleThreadExecutor();
	private final AtomicBoolean isCollectingUpdates = new AtomicBoolean(false);

	/**
	 * construct a flow view
	 *
	 * @param nodeSupplier  supplies nodes for items on demand
	 * @param widthSupplier supplies widths for items on demand
	 */
	public FlowView(Function<T, Node> nodeSupplier, Function<T, Double> widthSupplier) {
		this.widthSupplier = widthSupplier;

		size.bind(Bindings.size(items));

		listView.prefWidthProperty().bind(widthProperty());
		listView.prefHeightProperty().bind(heightProperty());
		listView.setOrientation(Orientation.VERTICAL);

		listView.setStyle("-fx-background-color: transparent;");

		getChildren().add(listView);
		listView.setSelectionModel(new EmptyMultipleSelectionModel<>());
		listView.setFocusTraversable(false);

		listView.setCellFactory(v -> new ListCell<>() {
			{
				setStyle(String.format("-fx-padding: %.1fpx 2px 0px 2px; -fx-background-color: transparent;", 0.25 * getVgap()));
			}

			@Override
			protected void updateItem(List<T> block, boolean empty) {
				super.updateItem(block, empty);
				super.setBackground(emptyBackground);
				if (empty || block == null) {
					setText(null);
					setGraphic(null);
				} else {
					final var flowPane = new FlowPane();
					flowPane.setHgap(getHgap());
					flowPane.setBackground(emptyBackground);

					flowPane.setOnKeyPressed((e) -> {
					});
					flowPane.setOnKeyReleased((e) -> {
					});

					for (var item : block) {
						flowPane.getChildren().add(nodeSupplier.apply(item));
					}
					flowPane.setUserData(block);
					setGraphic(flowPane);
				}
			}
		});

		InvalidationListener invalidationListener = c -> {
			if (isCollectingUpdates.compareAndSet(false, true)) {
				executor.execute(() -> {
					try {
						Thread.sleep(200);
					} catch (InterruptedException ignored) {
					} finally {
						update();
						isCollectingUpdates.set(false);
					}
				});
			}
		};
		items.addListener(invalidationListener);
		widthProperty().addListener(invalidationListener);
	}

	public synchronized void update() {
		var row = new ArrayList<T>();
		var width = 5d;

		Platform.runLater(() -> listView.getItems().clear());

		try {
			for (T item : items) {
				var itemWidth = widthSupplier.apply(item);
				if (itemWidth == -1.0) { // item to be placed on line of its own
					if (row.size() > 0) {
						var rowRef = row;
						Platform.runLater(() -> listView.getItems().add(rowRef));
						row = new ArrayList<>();
					}
					Platform.runLater(() -> {
						if (item != null)
							listView.getItems().add(List.of(item));
					});
					width = 5d;
				} else {
					if (row.size() > 0 && width + getHgap() + 5 + itemWidth >= listView.getWidth() - 20) {
						var rowRef = row;
						Platform.runLater(() -> listView.getItems().add(rowRef));
						row = new ArrayList<>();
						width = 5d;
					}
					row.add(item);
					width += getHgap() + 5 + itemWidth;
				}
			}
			if (row.size() > 0) {
				var rowRef = row;
				Platform.runLater(() -> listView.getItems().add(rowRef));
			}
		} catch (ConcurrentModificationException ignored) {
		}
		Platform.runLater(listView::layout);
	}

	public int size() {
		return size.get();
	}

	public ReadOnlyIntegerProperty sizeProperty() {
		return size;
	}

	public ObservableList<T> getItems() {
		return items;
	}

	public double getVgap() {
		return Vgap.get();
	}

	public DoubleProperty vgapProperty() {
		return Vgap;
	}

	public void setVgap(double vgap) {
		this.Vgap.set(vgap);
	}

	public double getHgap() {
		return Hgap.get();
	}

	public DoubleProperty hgapProperty() {
		return Hgap;
	}

	public void setHgap(double hgap) {
		this.Hgap.set(hgap);
	}

	public ListView<List<T>> getListView() {
		return listView;
	}
}
