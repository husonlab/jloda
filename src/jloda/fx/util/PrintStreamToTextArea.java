/*
 * PrintStreamToTextArea.java Copyright (C) 2019. Daniel H. Huson
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

import javafx.scene.control.TextArea;
import jloda.util.Basic;

import java.io.PrintStream;

/**
 * prints to a text area
 * Daniel HUson, 7.2019
 */
public class PrintStreamToTextArea extends PrintStream {
    private final TextArea textArea;

    public PrintStreamToTextArea(TextArea textArea) {
        super(System.out);
        this.textArea = textArea;
    }

    public void println(String x) {
        textArea.appendText(x + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(String x) {
        textArea.appendText(x);
        textArea.positionCaret(textArea.getText().length());
    }

    public void println(Object x) {
        textArea.appendText(x + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(Object x) {
        textArea.appendText("" + x);
        textArea.positionCaret(textArea.getText().length());
    }

    public void println(boolean x) {
        textArea.appendText(x + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(boolean x) {
        textArea.appendText("" + x);
        textArea.positionCaret(textArea.getText().length());
    }

    public void println(int x) {
        textArea.appendText(x + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(int x) {
        textArea.appendText("" + x);
        textArea.positionCaret(textArea.getText().length());
    }

    public void println(float x) {
        textArea.appendText(x + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(float x) {
        textArea.appendText("" + x);
        textArea.positionCaret(textArea.getText().length());
    }

    public void println(char x) {
        textArea.appendText(x + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(char x) {
        textArea.appendText("" + x);
        textArea.positionCaret(textArea.getText().length());
    }

    public void println(double x) {
        textArea.appendText(x + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(double x) {
        textArea.appendText("" + x);
        textArea.positionCaret(textArea.getText().length());
    }

    public void println(char[] x) {
        textArea.appendText(Basic.toString(x) + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(char[] x) {
        textArea.appendText(Basic.toString(x));
        textArea.positionCaret(textArea.getText().length());
    }

    public void println(long x) {
        textArea.appendText(x + "\n");
        textArea.positionCaret(textArea.getText().length());
    }

    public void print(long x) {
        textArea.appendText("" + x);
        textArea.positionCaret(textArea.getText().length());
    }

    public void write(byte[] buf, int off, int len) {
        for (int i = 0; i < len; i++)
            write(buf[off + i]);
    }

    public void write(byte b) {
        print((char) b);
    }

    public void setError() {
    }

    public boolean checkError() {
        return false;
    }

    public void flush() {
    }
}
