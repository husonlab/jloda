/*
 * JavaFX2PDF.java Copyright (C) 2019. Daniel H. Huson
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

package jloda.fx.util;

import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import jloda.util.CanceledException;

import java.io.IOException;

/**
 * print JavaFX to PDF
 * Daniel Huson, 2018
 */
public class JavaFX2PDF {
    private Node printImage;
    private Stage owner;


    /**
     * PDF Export for javafx nodes. Here, it is used to export charts as pdf to user-defined file. Unfortunately, the
     * Page layout cannot be set appropriately. This is reported as JDK-8088509 on the JDK Bug System.
     *
     * @param image JavaFX node
     * @param owner Stage owning the print dialog. Or null, if no stage should own the print dialog.
     */
    public JavaFX2PDF(Node image, Stage owner) {
        this.printImage = image;
        this.owner = owner;
    }

    /**
     * use root as node
     *
     * @param owner
     */
    public JavaFX2PDF(Stage owner) {
        this.printImage = owner.getScene().getRoot();
        this.owner = owner;
    }

    /**
     * Write node image to pdf
     */
    public void print() throws IOException, CanceledException {
        PrinterJob job = PrinterJob.createPrinterJob();
        boolean executePrint = true;
        Printer pdfPrinter = findPrinter();

        // Detect error occurring on MacOS
        if (job == null) {
            throw new IOException("Could not export to PDF file, no PDF printer found.");
        }

        if (pdfPrinter != null) {
            job.setPrinter(pdfPrinter);
        } else {
            executePrint = job.showPrintDialog(owner);
            pdfPrinter = job.getPrinter();
        }
        if (!executePrint) {
            throw new CanceledException();
        }

//        Paper customPaper = PrintHelper.createPaper("Custom", printImage.getLayoutBounds().getWidth(),
//                printImage.getLayoutBounds().getHeight(), Units.POINT);
//        PageLayout layout = pdfPrinter.createPageLayout(customPaper, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
        // TODO: 30.05.2017 improve or wait until bug JDK-8088509 is fixed. Alternatively:
//        job.showPageSetupDialog(new Stage());
//        Paper paper = PrintHelper.createPaper("CustomSize", 600,400, Units.POINT);
        PageLayout layout = pdfPrinter.createPageLayout(Paper.A5, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);
//        job.showPageSetupDialog(stage);
        job.getJobSettings().setPageLayout(layout);

        // Scale image to paper size (A4)
        double scaleX = layout.getPrintableWidth() / printImage.getLayoutBounds().getWidth();
        double scaleY = layout.getPrintableHeight() / printImage.getLayoutBounds().getHeight();
        if (scaleX > scaleY) {
            scaleX = scaleY;
        } else {
            scaleY = scaleX;
        }
        printImage.getTransforms().add(new Scale(scaleX, scaleY));

        boolean printSpooled = job.printPage(layout, printImage);
        if (printSpooled) {
            job.endJob();
            System.err.println("Wrote Image to PDF successfully");
        } else {
            throw new IOException("Error writing PDF.");
        }
        printImage.getTransforms().add(new Scale(1 / scaleX, 1 / scaleY));
    }

    /**
     * Lookup all available printers and return the pdf printer if available.
     */
    private Printer findPrinter() {
        for (Printer printer : Printer.getAllPrinters()) {
            if (printer.getName().endsWith("PDF")) {
                return printer;
            }
        }
        return null;
    }
}
