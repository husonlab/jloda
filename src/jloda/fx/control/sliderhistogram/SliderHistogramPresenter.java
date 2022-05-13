/*
 *  SliderHistogramPresenter.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import jloda.util.StringUtils;

import java.util.ArrayList;


public class SliderHistogramPresenter {
	private final InvalidationListener invalidationListener;
	private boolean inUpdate = false;

	private final double originalThreshold;

	public SliderHistogramPresenter(Stage stage, SliderHistogramController controller, ObservableList<Double> values, DoubleProperty threshold, ReadOnlyDoubleProperty minValue, ReadOnlyDoubleProperty maxValue) {
		originalThreshold = threshold.get();
		controller.getThresholdSlider().setValue(threshold.get());

		controller.getCancelButton().setOnAction(e -> {
			threshold.set(originalThreshold);
			stage.hide();
		});

		controller.getApplyButton().setOnAction(e -> {
			threshold.set(controller.getThresholdSlider().getValue());
			stage.hide();
		});

		invalidationListener = e -> {
			if (!inUpdate)
				try {
					inUpdate = true;
					var barChart = controller.getValuesBarChart();
					barChart.getData().clear();

					if (values.size() > 0) {
						var currentThreshold = controller.getThresholdSlider().getValue();

						var series = new XYChart.Series<String, Number>();
						barChart.getData().add(series);

						var sorted = new ArrayList<>(values);
						sorted.sort(Double::compare);

						var min = Math.min(minValue.get(), sorted.get(0));
						var max = Math.max(maxValue.get(), sorted.get(sorted.size() - 1));

						var bars = Math.min(100, values.size());
						var bucketWidth = (max - min) / bars;
						var bucketMinValue = min;
						var bucketMaxValue = bucketMinValue + bucketWidth;
						var bucketContentSize = 0;
						var bucketNumber = 0;

						var firstActive = Double.MAX_VALUE;
						var countActive = 0;

						for (var value : sorted) {
							if (value < bucketMaxValue) {
								bucketContentSize++;
							} else {
								series.getData().add(new XYChart.Data<>(StringUtils.removeTrailingZerosAfterDot(bucketMinValue), bucketContentSize));
								if (bucketMinValue < currentThreshold)
									barChart.lookup(".data%d.chart-bar".formatted(bucketNumber++)).setStyle("-fx-bar-fill: gray;");
								else {
									firstActive = Math.min(firstActive, bucketMinValue);
									countActive += bucketContentSize;
									barChart.lookup(".data%d.chart-bar".formatted(bucketNumber++)).setStyle("-fx-bar-fill: green;");
								}
								bucketMinValue = min + bucketNumber * bucketWidth;
								bucketMaxValue = bucketMinValue + bucketWidth;
								bucketContentSize = 1;
							}
						}
						{
							series.getData().add(new XYChart.Data<>(StringUtils.removeTrailingZerosAfterDot(bucketMinValue), bucketContentSize));
							if (bucketMinValue < currentThreshold)
								barChart.lookup(".data%d.chart-bar".formatted(bucketNumber)).setStyle("-fx-bar-fill: gray;");
							else {
								firstActive = Math.min(firstActive, bucketMinValue);
								countActive += bucketContentSize;
								barChart.lookup(".data%d.chart-bar".formatted(bucketNumber)).setStyle("-fx-bar-fill: green;");
							}
						}

						controller.getThresholdSlider().setMin(Math.min(0, min));
						controller.getThresholdSlider().setMax(Math.max(1, max + bucketWidth));

						if (firstActive < Double.MAX_VALUE)
							controller.getThresholdSlider().setValue(firstActive);
						else
							controller.getThresholdSlider().setValue(controller.getThresholdSlider().getMax());
						controller.getReportLabel().setText("%,d (of %,d)".formatted(countActive, values.size()));
					}
				} finally {
					inUpdate = false;
				}
		};

		values.addListener(new WeakInvalidationListener(invalidationListener));
		minValue.addListener(new WeakInvalidationListener(invalidationListener));
		maxValue.addListener(new WeakInvalidationListener(invalidationListener));
		threshold.addListener(new WeakInvalidationListener(invalidationListener));

		controller.getThresholdSlider().valueProperty().addListener(invalidationListener);

		invalidationListener.invalidated(null);
	}
}
