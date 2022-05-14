/*
 *  SliderHistogramController.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.DoubleStringConverter;
import jloda.util.NumberUtils;
import jloda.util.StringUtils;


public class SliderHistogramController {
	@FXML
	private AnchorPane rootPane;


	@FXML
	private Button applyButton;

	@FXML
	private Button cancelButton;

	@FXML
	private BarChart<String, Number> valuesBarChart;

	@FXML
	private Slider thresholdSlider;

	@FXML
	private TextField thresholdTextField;

	@FXML
	private Label reportLabel;

	@FXML
	private void initialize() {
		reportLabel.setText("");

		thresholdSlider.setValue(Double.MIN_VALUE);
		thresholdSlider.valueProperty().addListener((v, o, n) -> thresholdTextField.setText(StringUtils.removeTrailingZerosAfterDot(n.floatValue())));
		thresholdTextField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter()));
		thresholdTextField.textProperty().addListener((v, o, n) -> thresholdSlider.setValue(NumberUtils.isDouble(n) ? Double.parseDouble(n) : 0.0));
	}

	public AnchorPane getRootPane() {
		return rootPane;
	}

	public Button getApplyButton() {
		return applyButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public BarChart<String, Number> getValuesBarChart() {
		return valuesBarChart;
	}


	public Slider getThresholdSlider() {
		return thresholdSlider;
	}

	public TextField getThresholdTextField() {
		return thresholdTextField;
	}

	public Label getReportLabel() {
		return reportLabel;
	}
}
