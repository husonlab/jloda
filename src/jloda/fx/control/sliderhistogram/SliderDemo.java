/*
 *  SliderDemo.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.control.sliderhistogram;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Random;

public class SliderDemo extends Application {
	@Override
	public void start(Stage stage) throws Exception {

		ObservableList<Double> list = FXCollections.observableArrayList();

		var random = new Random();
		for (var i = 0; i < 10000; i++) {
			list.add(random.nextDouble());
		}

		var threshold = new SimpleDoubleProperty(0.5);

		var button = new Button("DEMO");
		button.setOnAction(e -> new SliderHistogramView(stage, Modality.WINDOW_MODAL, stage.getX() + 20, stage.getY() + 20,
				"Demo", list, threshold, new SimpleDoubleProperty(0), new SimpleDoubleProperty(1)));

		var label1 = new Label(String.valueOf(threshold.get()));
		label1.textProperty().bind(Bindings.createObjectBinding(() -> String.valueOf(threshold.get()), threshold));

		var root = new BorderPane();
		root.setLeft(button);
		root.setBottom(label1);
		root.setStyle("-fx-font-size: 16;");

		var slider = new SliderHistogramView(list, threshold, new SimpleDoubleProperty(0), new SimpleDoubleProperty(1));
		root.setCenter(slider.getRoot());

		stage.setScene(new Scene(root, 300, 200));
		stage.show();
	}
}
