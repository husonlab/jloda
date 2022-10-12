/*
 *  Copyright (C) 2015 Daniel H. Huson and David J. Bryant
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

import java.io.OutputStream;
import java.io.PrintStream;

public class TextAreaUtils {
	/**
	 * creates a print stream that writes to the provided text area
	 *
	 * @param textArea text area
	 * @param out      original print stream
	 * @return modified print stream (use e.g. System.setErr() to set this and have error messages appear in the text Area
	 */
	private static PrintStream createPrintStream(TextArea textArea, OutputStream out) {
		return new PrintStream(out) {
			public void println(String x) {
				textArea.appendText(x + "\n");
			}

			public void print(String x) {
				textArea.appendText(x);
			}

			public void println(Object x) {
				textArea.appendText(x + "\n");
			}

			public void print(Object x) {
				textArea.appendText(String.valueOf(x));
			}

			public void println(boolean x) {
				textArea.appendText(x + "\n");
			}

			public void print(boolean x) {
				textArea.appendText(String.valueOf(x));
			}

			public void println(int x) {
				textArea.appendText(x + "\n");
			}

			public void print(int x) {
				textArea.appendText(String.valueOf(x));
			}

			public void println(float x) {
				textArea.appendText(x + "\n");
			}

			public void print(float x) {
				textArea.appendText(String.valueOf(x));
			}

			public void println(char x) {
				textArea.appendText(x + "\n");
			}

			public void print(char x) {
				textArea.appendText(String.valueOf(x));
			}

			public void println(double x) {
				textArea.appendText(x + "\n");
			}

			public void print(double x) {
				textArea.appendText(String.valueOf(x));
			}

			public void println(char[] x) {
				textArea.appendText(String.valueOf(x) + "\n");
			}

			public void print(char[] x) {
				textArea.appendText(String.valueOf(x));
			}

			public void println(long x) {
				textArea.appendText(x + "\n");
			}

			public void print(long x) {
				textArea.appendText(String.valueOf(x));
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
		};
	}
}
