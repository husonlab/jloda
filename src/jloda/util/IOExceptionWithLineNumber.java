/*
 * IOExceptionWithLineNumber.java Copyright (C) 2022 Daniel H. Huson
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

package jloda.util;

import java.io.IOException;

/**
 * exception with line number
 * Daniel Huson, 1.2018
 */
public class IOExceptionWithLineNumber extends IOException {
    private long lineNumber = -1;

    /**
     * constructor
     *
	 */
    public IOExceptionWithLineNumber(String message, long lineNumber) {
        super("Line " + lineNumber + ": " + message);
        setLineNumber(lineNumber);
    }

    /**
     * constructor
     *
	 */
    public IOExceptionWithLineNumber(long lineNumber, String message) {
        this(message, lineNumber);
    }

    public IOExceptionWithLineNumber(int lineNumber, IOException ioException) {
        super(ioException instanceof IOExceptionWithLineNumber ? ioException.getMessage() : "Line " + lineNumber + ": " + ioException.getMessage());
        setLineNumber(lineNumber);
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }
}
