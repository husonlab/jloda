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
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import jloda.util.StringUtils;


public class SliderHistogramPresenter {
	private final InvalidationListener invalidationListener;
	private boolean inUpdate = false;

	public SliderHistogramPresenter(SliderHistogramController controller, ObservableList<Double> values, DoubleProperty threshold) {
		controller.getThresholdSlider().setValue(threshold.get());

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

						var min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
						var max = values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
						var bars = Math.min(100, values.size());
						var barWidth = (max - min) / (bars - 1);
						var barContentSize = new double[bars];

						for (var value : values) {
							var bar = (int) ((value - min) / barWidth);
							barContentSize[bar]++;
						}

						var firstActive = 0.0;
						var countActive = 0;

						for (var i = 0; i < bars; i++) {
							var bucketMinValue = min + i * barWidth;
							var count = barContentSize[i];
							series.getData().add(new XYChart.Data<>(StringUtils.removeTrailingZerosAfterDot(bucketMinValue), count));
							if (bucketMinValue < currentThreshold)
								barChart.lookup(".data%d.chart-bar".formatted(i)).setStyle("-fx-bar-fill: gray;");
							else {
								firstActive = Math.min(firstActive, bucketMinValue);
								countActive += count;
								barChart.lookup(".data%d.chart-bar".formatted(i)).setStyle("-fx-bar-fill: green;");
							}
						}

						controller.getThresholdSlider().setMin(min);
						controller.getThresholdSlider().setMax(max + barWidth);

						if (false) {
							if (firstActive < Double.MAX_VALUE)
								controller.getThresholdSlider().setValue(firstActive);
							else
								controller.getThresholdSlider().setValue(controller.getThresholdSlider().getMax());
						}

						controller.getReportLabel().setText("%,d (of %,d)".formatted(countActive, values.size()));
					}
				} finally {
					inUpdate = false;
				}
		};

		values.addListener(new WeakInvalidationListener(invalidationListener));
		threshold.addListener((v, o, n) -> controller.getThresholdSlider().setValue(n.doubleValue()));
		threshold.addListener(new WeakInvalidationListener(invalidationListener));

		controller.getThresholdSlider().valueProperty().addListener(invalidationListener);

		invalidationListener.invalidated(null);
	}
}
