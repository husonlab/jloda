/*
 * Print.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.fx.util;

import javafx.application.Platform;
import javafx.print.PageLayout;
import javafx.print.PageRange;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import jloda.fx.window.NotificationManager;
import jloda.util.ProgramProperties;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * print a  node
 * Daniel Huson, 1.2018
 */
public class Print {
	public static PageLayout pageLayoutSelected;

	/**
	 * print the given node
	 */
	public static void print(Stage owner, Node node0) {
		final PrinterJob job = PrinterJob.createPrinterJob();
		if (job != null) {
			if (job.showPrintDialog(owner)) {
				System.err.println(job.getJobSettings());

				final PageLayout pageLayout = (pageLayoutSelected != null ? pageLayoutSelected : job.getJobSettings().getPageLayout());

				final Node node;
				if (node0 instanceof TextArea) {
					final TextArea textArea = (TextArea) node0;
					final Text text = new Text("\n" + textArea.getText());
					text.setWrappingWidth(pageLayout.getPrintableWidth());
					text.setFont(textArea.getFont());
					node = text;
					// todo: need print to multiple pages
				} else
					node = node0;

				final Scale scale;
				if (node == node0 && node.getBoundsInParent().getWidth() > pageLayout.getPrintableWidth() || node.getBoundsInParent().getHeight() > pageLayout.getPrintableHeight()) {
					if (true) {
						System.err.printf("Scene size (%.0f x %.0f) exceeds printable area (%.0f x %.0f), scaled to fit%n", node.getBoundsInParent().getWidth(),
								node.getBoundsInParent().getHeight(), pageLayout.getPrintableWidth(), pageLayout.getPrintableHeight());
						double factor = Math.min(pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth(), pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight());
						scale = new Scale(factor, factor);
						node.getTransforms().add(scale);
					} else {
						javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
						alert.initOwner(owner);
						alert.setResizable(true);

						alert.setTitle("ScalingType Before Printing - " + ProgramProperties.getProgramName());
						alert.setHeaderText(String.format("Scene size (%.0f x %.0f) exceeds printable area (%.0f x %.0f)", node.getBoundsInParent().getWidth(),
								node.getBoundsInParent().getHeight(), pageLayout.getPrintableWidth(), pageLayout.getPrintableHeight()));
						alert.setContentText("ScalingType to fit printable area?");
						ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
						ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
						ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
						alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

						final Optional<ButtonType> result = alert.showAndWait();
						if (result.isPresent()) {
							if (result.get() == buttonTypeYes) {
								final double factor = Math.min(pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth(), pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight());
								scale = new Scale(factor, factor);
								node.getTransforms().add(scale);
							} else if (result.get() == buttonTypeCancel)
								return;
							else
								scale = null;
						} else
							scale = null;
					}
				} else
					scale = null;

				job.jobStatusProperty().addListener((c, o, n) -> {
					//System.err.println("Status: " + o + " -> " + n);
					if (scale != null && n != PrinterJob.JobStatus.NOT_STARTED && n != PrinterJob.JobStatus.PRINTING) {
						Platform.runLater(() -> node.getTransforms().remove(scale));
					}
				});
				job.setPrinter(job.getPrinter());
				if (job.printPage(pageLayout, node))
					job.endJob();
			}
		} else
			NotificationManager.showError("Failed to create Printer Job");
	}

	/**
	 * print a snapshot of the given node
	 */
	public static void printSnapshot(Stage owner, Node node) {
		final PrinterJob job = PrinterJob.createPrinterJob();
		if (job != null) {
			if (job.showPrintDialog(owner)) {
				//System.err.println(job.getJobSettings());

				final PageLayout pageLayout = (pageLayoutSelected != null ? pageLayoutSelected : job.getJobSettings().getPageLayout());

				final Image image = node.snapshot(null, null);
				final ImageView imageView = new ImageView(image);
				imageView.setFitWidth(pageLayout.getPrintableWidth());
				imageView.setPreserveRatio(true);
				if (imageView.getFitHeight() > pageLayout.getPrintableHeight())
					imageView.setFitHeight(pageLayout.getPrintableHeight());

				if (job.printPage(pageLayout, imageView))
					job.endJob();
			}
		} else
			NotificationManager.showError("Failed to create Printer Job");
	}

	/**
	 * show the page layout dialog
	 */
	public static void showPageLayout(Stage owner) {
		final PrinterJob job = PrinterJob.createPrinterJob();
		if (job.showPageSetupDialog(owner)) {
			pageLayoutSelected = (job.getJobSettings().getPageLayout());
		}
	}

	/**
	 * print a text, over multiple pages, if necessary
	 */
	public static void printText(Stage owner, String text) {
		printText(owner, text, new Font("Courier New", 10));
	}

	/**
	 * print a text, over multiple pages, if necessary
	 */
	public static void printText(Stage owner, String text, Font font) {
		var service = Executors.newSingleThreadExecutor();
		service.submit(() -> {
			var printerJob = PrinterJob.createPrinterJob();
			if (printerJob != null) {
				var jobSettings = printerJob.getJobSettings();
				var pages = createPages(jobSettings.getPageLayout().getPrintableWidth(), jobSettings.getPageLayout().getPrintableHeight(), font, text);
				jobSettings.setPageRanges(new PageRange(1, pages.size()));

				if (printerJob.showPrintDialog(owner)) {
					var pageWidth = jobSettings.getPageLayout().getPrintableWidth();
					var pageHeight = jobSettings.getPageLayout().getPrintableHeight();

					// todo: bug: some pages do not appear if only a subset of pages is selected for printing...
					var printed = false;
					for (var pageRange : jobSettings.getPageRanges()) {
						for (var page = pageRange.getStartPage(); page <= pageRange.getEndPage(); page++) {
							// System.err.println("Printing page " + page);
							var textFlow = new TextFlow(new Text(pages.get(page - 1)));
							textFlow.setStyle(String.format("-fx-font-family: '%s'; -fx-font-size: %fpx;", font.getFamily(), font.getSize()));
							textFlow.setPrefWidth(pageWidth);
							textFlow.setPrefHeight(pageHeight);
							printed = printerJob.printPage(textFlow);
							if (!printed) {
								NotificationManager.showError("Print failed, page=" + page);
								break;
							}
						}
					}
					if (printed)
						printerJob.endJob();
				}
			}
		});
		service.shutdown();
	}

	/**
	 * divide text into pages
	 * See: https://coderanch.com/t/709329/java/JavaFX-approach-dividing-text-blob
	 */
	private static ArrayList<String> createPages(double pageWidth, double pageHeight, Font font, String text) {
		var lineHeight = (new FontMetrics(font)).getLineHeight();
		var linesPerPage = pageHeight / lineHeight;

		var pageBuilder = new StringBuilder();
		var lineBuilder = new StringBuilder();
		var pageNumber = 0;
		var lineNumber = 0;

		var pages = new ArrayList<String>();

		for (var line : text.split("\n")) {
			var internal = new Text();
			internal.setText(line);
			if (internal.getLayoutBounds().getWidth() <= (int) pageWidth) {
				pageBuilder.append(line).append("\n");
				lineNumber++;
			} else {
				int pos = 0;
				var chars = line.toCharArray();
				lineBuilder.setLength(0);
				for (var c : chars) {
					pos++;
					internal.setText(lineBuilder.toString() + c);
					if (internal.getLayoutBounds().getWidth() > pageWidth) {
						pageBuilder.append(lineBuilder);
						lineBuilder.setLength(0);
						lineNumber++;
						if (lineNumber == (int) linesPerPage) {
							var lastWord = lastWord(pageBuilder.toString(), Character.toString(c));
							if (!lastWord.equals(Character.toString(c))) {
								pageBuilder.replace(pageBuilder.lastIndexOf(lastWord), (pageBuilder.length()), "");
							}
							pages.add(pageNumber, pageBuilder.toString());
							pageBuilder.setLength(0);
							lineBuilder.setLength(0);
							lineNumber = 0;
							pageNumber++;
							if (!lastWord.equals(Character.toString(c))) lineBuilder.append(lastWord);
						}
					}
					lineBuilder.append(c);
					if (pos == chars.length) {
						pageBuilder.append(lineBuilder).append("\n");
						lineBuilder.setLength(0);
						lineNumber++;
					}
				}
			}

			if (lineNumber == (int) linesPerPage) {
				pages.add(pageBuilder.toString());
				pageBuilder.setLength(0);
				pageNumber++;
				lineNumber = 0;
			}
		}
		pages.add(pageNumber, pageBuilder.toString());
		return pages;
	}

	private static String lastWord(String line, String chr) {
		var len = line.length();
		if (chr.matches("[a-zA-Z0-9]") && line.substring(len - 1).matches("[a-zA-Z0-9]")) {
			for (var p = (len - 1); p >= 0; p--) {
				var c = line.charAt(p);
				if (Character.toString(c).matches("[$&+,:;=\\\\?@#|/'<>.^* ()%!-]")) {
					return line.substring(p + 1);
				}
			}
		}
		return chr;
	}
}
