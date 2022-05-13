/*
 *  SliderHistogramView.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;

/**
 * slider histogram view
 * Daniel Huson, 5.2022
 */
public class SliderHistogramView {
	public SliderHistogramView(Stage owner, Modality modality, double screenX, double screenY, String title, ObservableList<Double> values, DoubleProperty threshold, ReadOnlyDoubleProperty minValue, ReadOnlyDoubleProperty maxValue) {
		var loader = new ExtendedFXMLLoader<SliderHistogramController>(SliderHistogramController.class);
		var controller = loader.getController();

		var stage = new Stage();
		stage.initOwner(owner);
		stage.initModality(modality);
		stage.setTitle(title);
		stage.setScene(new Scene(loader.getRoot()));
		stage.sizeToScene();
		stage.setX(screenX);
		stage.setY(screenY);
		//stage.setAlwaysOnTop(true);
		stage.show();

		var presenter = new SliderHistogramPresenter(stage, controller, values, threshold, minValue, maxValue);
	}
}
